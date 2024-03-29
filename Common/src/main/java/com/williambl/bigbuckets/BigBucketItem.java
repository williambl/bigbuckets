package com.williambl.bigbuckets;

import com.mojang.datafixers.util.Pair;
import com.williambl.bigbuckets.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public abstract class BigBucketItem extends Item implements DispensibleContainerItem {

    public BigBucketItem(Properties builder) {
        super(builder);
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult raytraceresult = getPlayerPOVHitResult(world, player, this.getFullness(stack) == this.getCapacity(stack) ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);

        if (raytraceresult.getType() != BlockHitResult.Type.BLOCK)
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);

        BlockPos blockPos = raytraceresult.getBlockPos().immutable();

        if (
                world.mayInteract(player, blockPos)
                && player.mayUseItemAt(blockPos, raytraceresult.getDirection(), stack)
        ) {
            BlockState blockState = world.getBlockState(blockPos);

            var fillRes = this.tryFill(stack, blockState, world, blockPos, player, raytraceresult);
            if (fillRes.getResult().consumesAction()) {
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }

            var emptyRes = this.tryDrain(player, world, blockPos, raytraceresult, stack);
            if (emptyRes.getResult().consumesAction()) {
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private InteractionResultHolder<ItemStack> tryFill(ItemStack stack, BlockState blockstate, Level level, BlockPos pos, Player player, BlockHitResult raytrace) {
        if (blockstate.getBlock() instanceof BucketPickup bucketPickup) {
            ItemStack filledBucket = bucketPickup.pickupBlock(level, pos, blockstate);
            if (!filledBucket.isEmpty()) {
                player.awardStat(Stats.ITEM_USED.get(this));

                bucketPickup.getPickupSound().ifPresent((sound) -> {
                    player.playSound(sound, 1.0F, 1.0F);
                });

                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

                var fluid = this.getFluidFromOtherItemStack(filledBucket);

                if (!this.canAcceptFluid(stack, fluid, Services.FLUIDS.bucketVolume()) && filledBucket.getItem() instanceof DispensibleContainerItem containerItem) {
                    containerItem.emptyContents(player, level, pos, raytrace);
                    return InteractionResultHolder.fail(stack);
                }

                this.fill(stack, fluid, Services.FLUIDS.bucketVolume());
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    public InteractionResultHolder<ItemStack> tryDrain(Player player, Level level, BlockPos pos, @Nullable BlockHitResult raytrace, ItemStack stack) {
        var content = this.getFluid(stack);
        if (!(content instanceof FlowingFluid)) {
            return InteractionResultHolder.fail(stack);
        } else {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            Material material = state.getMaterial();
            boolean canBeReplaced = state.canBeReplaced(content);
            boolean canPlace = state.isAir() || canBeReplaced || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(level, pos, state, content);
            if (!canPlace) {
                return raytrace != null ? this.tryDrain(player, level, raytrace.getBlockPos().relative(raytrace.getDirection()), null, stack) : InteractionResultHolder.fail(stack);
            } else if (level.dimensionType().ultraWarm() && content.is(FluidTags.WATER)) {
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

                for(int i = 0; i < 8; ++i) {
                    level.addParticle(
                            ParticleTypes.LARGE_SMOKE, (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0, 0.0, 0.0
                    );
                }

                this.drain(stack, Services.FLUIDS.bucketVolume());
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            } else if (block instanceof LiquidBlockContainer && content == Fluids.WATER) {
                ((LiquidBlockContainer)block).placeLiquid(level, pos, state, ((FlowingFluid)content).getSource(false));
                this.playEmptySound(player, level, pos, stack);
                this.drain(stack, Services.FLUIDS.bucketVolume());
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            } else {
                if (!level.isClientSide && canBeReplaced && !material.isLiquid()) {
                    level.destroyBlock(pos, true);
                }

                if (!level.setBlock(pos, content.defaultFluidState().createLegacyBlock(), 11) && !state.getFluidState().isSource()) {
                    return InteractionResultHolder.fail(stack);
                } else {
                    this.playEmptySound(player, level, pos, stack);
                    this.drain(stack, Services.FLUIDS.bucketVolume());
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }
        }
    }

    @Override
    public boolean emptyContents(@Nullable Player var1, Level var2, BlockPos var3, @Nullable BlockHitResult var4) {
        return false; // Can't do much without an itemstack
    }

    protected void playEmptySound(@Nullable Player player, Level worldIn, BlockPos pos, ItemStack stack) {
        SoundEvent soundevent = null; //FIXME
        if (soundevent == null)
            soundevent = this.getFluid(stack).is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        worldIn.playSound(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        var data = this.getBucketStorageData(stack);
        tooltip.add(Component.translatable("item.bigbuckets.big_bucket.desc.fullness_and_capacity", data.fullness() / (float) Services.FLUIDS.bucketVolume(), data.capacity() / (float) Services.FLUIDS.bucketVolume()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    @Nonnull
    public Component getName(ItemStack stack) {
        if (this.getFluid(stack) == Fluids.EMPTY)
            return super.getName(stack);
        return super.getName(stack).copy().append(Component.literal(" (").append(this.getFluid(stack).defaultFluidState().createLegacyBlock().getBlock().getName()).append(Component.literal(")")));
    }

    @Override
    public void fillItemCategory(CreativeModeTab itemGroup, NonNullList<ItemStack> itemStacks) {
        if (this.allowedIn(itemGroup)) {
            ItemStack stack = new ItemStack(this);
            this.setCapacity(stack, 16 * Services.FLUIDS.bucketVolume());
            itemStacks.add(stack);
        }
    }

    private Fluid getFluidFromOtherItemStack(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucket) {
            return Services.FLUIDS.getFluidFromBucketItem(bucket);
        }

        return Fluids.EMPTY;
    }

    public boolean canAcceptFluid(ItemStack stack, Fluid fluid, int amount) {
        return this.getFullness(stack) + amount <= this.getCapacity(stack) && this.canAcceptFluid(stack, fluid);
    }

    public boolean canAcceptFluid(ItemStack stack, Fluid fluid) {
        return (this.getFluid(stack) == fluid || this.getFluid(stack) == Fluids.EMPTY);
    }

    public Fluid getFluid(ItemStack stack) {
        return this.getBucketStorageData(stack).fluid();
    }

    public int getCapacity(ItemStack stack) {
        return this.getBucketStorageData(stack).capacity();
    }

    public int getFullness(ItemStack stack) {
        return this.getBucketStorageData(stack).fullness();
    }

    public BucketStorageData getBucketStorageData(ItemStack stack) {
        return BucketStorageData.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateTagElement("bucketData")).get().left().map(Pair::getFirst).orElseGet(() -> new BucketStorageData(Fluids.EMPTY, Optional.empty(), 0, 0));
    }

    public void setBucketStorageData(ItemStack stack, BucketStorageData data) {
        var tag = stack.getOrCreateTag();
        tag.put("bucketData", BucketStorageData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, Constants.LOG::error));
    }

    public void setCapacity(ItemStack stack, int capacity) {
        var bucketStorageData = this.getBucketStorageData(stack);
        this.setBucketStorageData(stack, bucketStorageData.withCapacity(capacity));
    }

    public int fill(ItemStack stack, Fluid fluid, int amount) {
        var bucketStorageData = this.getBucketStorageData(stack);
        var maxAmount = Math.min(amount, (fluid.isSame(bucketStorageData.fluid()) || bucketStorageData.fluid() == Fluids.EMPTY) ? bucketStorageData.capacity() - bucketStorageData.fullness() : 0);
        this.setBucketStorageData(stack, bucketStorageData.withFluid(fluid, bucketStorageData.data(), bucketStorageData.fullness()+maxAmount));

        return maxAmount;
    }

    public int drain(ItemStack stack, int drainAmount) {
        var bucketStorageData = this.getBucketStorageData(stack);
        var amount = Math.min(drainAmount, bucketStorageData.fullness());
        this.setBucketStorageData(stack, bucketStorageData.withFluid(bucketStorageData.fluid(), bucketStorageData.data(), bucketStorageData.fullness()-amount));

        return amount;
    }

    public boolean shouldShowBar(ItemStack stack) {
        return !this.getFluid(stack).isSame(Fluids.EMPTY);
    }
}
