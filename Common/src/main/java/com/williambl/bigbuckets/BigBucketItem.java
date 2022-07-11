package com.williambl.bigbuckets;

import com.williambl.bigbuckets.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class BigBucketItem extends Item {

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

            if (this.tryFill(stack, blockState, world, blockPos, player, raytraceresult))
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);

            if (this.tryEmpty(player, world, blockPos, raytraceresult, stack))
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private boolean tryFill(ItemStack stack, BlockState blockstate, Level world, BlockPos pos, Player player, BlockHitResult raytrace) {
        //TODO
        return false;
    }

    public boolean tryEmpty(Player player, Level world, BlockPos pos, @Nullable BlockHitResult raytrace, ItemStack stack) {
        //TODO
        return false;
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
        tooltip.add(Component.translatable("item.bigbuckets.bigbucket.desc.fluid", this.getFluid(stack).defaultFluidState().createLegacyBlock().getBlock().getName()));
        tooltip.add(Component.translatable("item.bigbuckets.bigbucket.desc.capacity", this.getCapacity(stack) / 1000f));
        tooltip.add(Component.translatable("item.bigbuckets.bigbucket.desc.fullness", this.getFullness(stack)/1000f));
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

    public boolean canAcceptFluid(ItemStack stack, Fluid fluid, int amount) {
        return this.getFullness(stack) + amount <= this.getCapacity(stack) && (this.getFluid(stack) == fluid || this.getFluid(stack) == Fluids.EMPTY);
    }

    /*
     * PLATFORM DEPENDENT CODE
     */

    public abstract Fluid getFluid(ItemStack stack);

    public abstract int getCapacity(ItemStack stack);

    public abstract int getFullness(ItemStack stack);

    public abstract void setCapacity(ItemStack stack, int capacity);

    public abstract int fill(ItemStack stack, Fluid fluid, int amount); //TODO abstract fluidstack some way?

    public abstract int drain(ItemStack stack, int drainAmount);
}
