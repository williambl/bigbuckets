package com.williambl.bigbuckets;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class BigBucketItem extends Item {

    public BigBucketItem(Properties builder) {
        super(builder);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        BlockRayTraceResult raytraceresult = rayTrace(world, player, getFullness(stack) == getCapacity(stack) ? RayTraceContext.FluidMode.NONE : RayTraceContext.FluidMode.SOURCE_ONLY);

        if (raytraceresult.getType() != RayTraceResult.Type.BLOCK)
            return new ActionResult<>(ActionResultType.PASS, stack);

        BlockPos blockPos = raytraceresult.getPos();

        if (
                world.isBlockModifiable(player, blockPos)
                        && player.canPlayerEdit(blockPos, raytraceresult.getFace(), stack)
        ) {
            BlockState blockState = world.getBlockState(blockPos);

            if (tryFill(stack, blockState, world, blockPos, player))
                return new ActionResult<>(ActionResultType.SUCCESS, stack);

            BlockPos fluidPos =
                    blockState.getBlock() instanceof ILiquidContainer
                            && ((ILiquidContainer) blockState.getBlock()).canContainFluid(world, blockPos, blockState, getFluid(stack)) ?
                            blockPos
                            : raytraceresult.getPos().offset(raytraceresult.getFace());

            if (tryEmpty(player, world, fluidPos, raytraceresult, stack))
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.FAIL, stack);
    }

    private boolean tryFill(ItemStack stack, BlockState blockState, World world, BlockPos pos, PlayerEntity player) {
        if (blockState.getBlock() instanceof IBucketPickupHandler && getFullness(stack) < getCapacity(stack)) {
            Fluid fluid = ((IBucketPickupHandler) blockState.getBlock()).pickupFluid(world, pos, blockState);
            if (fluid != Fluids.EMPTY && canAcceptFluid(stack, fluid, FluidAttributes.BUCKET_VOLUME)) {
                SoundEvent soundevent = getFluid(stack).getAttributes().getFillSound();
                if (soundevent == null)
                    soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                player.playSound(soundevent, 1.0F, 1.0F);

                player.addStat(Stats.ITEM_USED.get(this));
                fill(stack, new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME));
                return true;
            }
        }
        return false;
    }

    public boolean tryEmpty(PlayerEntity player, World world, BlockPos pos, @Nullable BlockRayTraceResult raytrace, ItemStack stack) {
        if (!(getFluid(stack) instanceof FlowingFluid))
            return false;

        Fluid fluid = getFluid(stack);
        BlockState blockstate = world.getBlockState(pos);
        boolean isBlockReplaceable = blockstate.canBucketPlace(fluid) || raytrace == null;
        if (
                world.isAirBlock(pos)
                        || isBlockReplaceable
                        || (
                        blockstate.getBlock() instanceof ILiquidContainer
                                && ((ILiquidContainer) blockstate.getBlock()).canContainFluid(world, pos, blockstate, getFluid(stack))
                )
        ) {
            if (world.getDimension().isUltrawarm() && fluid.isIn(FluidTags.WATER)) {
                world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                for (int i = 0; i < 8; ++i)
                    world.addParticle(ParticleTypes.LARGE_SMOKE, (double) pos.getX() + world.rand.nextDouble(), (double) pos.getY() + world.rand.nextDouble(), (double) pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);

            } else if (blockstate.getBlock() instanceof ILiquidContainer && fluid.isIn(FluidTags.WATER)) {
                if (((ILiquidContainer) blockstate.getBlock()).receiveFluid(world, pos, blockstate, ((FlowingFluid) fluid).getStillFluidState(false)))
                    playEmptySound(player, world, pos, stack);
            } else {
                if (!world.isRemote && isBlockReplaceable && !blockstate.getMaterial().isLiquid())
                    world.destroyBlock(pos, true);

                playEmptySound(player, world, pos, stack);
                world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 1 | 2 | 8);
            }

            if (player instanceof ServerPlayerEntity) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
            }
            player.addStat(Stats.ITEM_USED.get(this));
            drain(stack, FluidAttributes.BUCKET_VOLUME);
            return true;
        }

        return tryEmpty(player, world, raytrace.getPos().offset(raytrace.getFace()), null, stack);
    }

    protected void playEmptySound(@Nullable PlayerEntity player, IWorld worldIn, BlockPos pos, ItemStack stack) {
        SoundEvent soundevent = getFluid(stack).getAttributes().getEmptySound();
        if (soundevent == null)
            soundevent = getFluid(stack).isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        worldIn.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TranslationTextComponent("item.bigbuckets.bigbucket.desc.fluid", getFluid(stack).getDefaultState().getBlockState().getBlock().getName()));
        tooltip.add(new TranslationTextComponent("item.bigbuckets.bigbucket.desc.capacity", getCapacity(stack) / 1000f));
        tooltip.add(new TranslationTextComponent("item.bigbuckets.bigbucket.desc.fullness", getFullness(stack)/1000f));
    }

    @Override
    @Nonnull
    public ITextComponent getDisplayName(ItemStack stack) {
        if (getFluid(stack) == Fluids.EMPTY)
            return super.getDisplayName(stack);
        return super.getDisplayName(stack).shallowCopy().append(new StringTextComponent(" (").append(getFluid(stack).getDefaultState().getBlockState().getBlock().getName()).append(new StringTextComponent(")")));
    }

    @Override
    public void fillItemGroup(ItemGroup itemGroup, NonNullList<ItemStack> itemStacks) {
        if (isInGroup(itemGroup)) {
            ItemStack stack = new ItemStack(this);
            setCapacity(stack, 16 * FluidAttributes.BUCKET_VOLUME);
            itemStacks.add(stack);
        }
    }

    public boolean canAcceptFluid(ItemStack stack, Fluid fluid, int amount) {
        return getFullness(stack) + amount <= getCapacity(stack) && (getFluid(stack) == fluid || getFluid(stack) == Fluids.EMPTY);
    }

    /*
     * PLATFORM DEPENDENT CODE
     */

    @SuppressWarnings("ConstantConditions")
    @PlatformDependent
    public Fluid getFluid(ItemStack stack) {
        final LazyOptional<IFluidHandlerItem> cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (cap.isPresent()) {
            final BigBucketFluidHandler fluidHandler = (BigBucketFluidHandler) cap.orElseThrow(NullPointerException::new);
            if (stack.hasTag() && stack.getTag().contains("BigBuckets")) {
                // Handling for old-style NBT
                final CompoundNBT tag = stack.getChildTag("BigBuckets");
                fixNBT(tag, fluidHandler, stack);
            }
            return fluidHandler.getFluid().getFluid();
        }
        return Fluids.EMPTY;
    }

    @SuppressWarnings("ConstantConditions")
    @PlatformDependent
    public int getCapacity(ItemStack stack) {
        final LazyOptional<IFluidHandlerItem> cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (cap.isPresent()) {
            final BigBucketFluidHandler fluidHandler = (BigBucketFluidHandler) cap.orElseThrow(NullPointerException::new);
            if (stack.hasTag() && stack.getTag().contains("BigBuckets")) {
                // Handling for old-style NBT
                final CompoundNBT tag = stack.getChildTag("BigBuckets");
                fixNBT(tag, fluidHandler, stack);
            }
            return fluidHandler.getTankCapacity(0);
        }
        return 0;
    }

    @SuppressWarnings("ConstantConditions")
    @PlatformDependent
    public int getFullness(ItemStack stack) {
        final LazyOptional<IFluidHandlerItem> cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (cap.isPresent()) {
            final BigBucketFluidHandler fluidHandler = (BigBucketFluidHandler) cap.orElseThrow(NullPointerException::new);
            if (stack.hasTag() && stack.getTag().contains("BigBuckets")) {
                // Handling for old-style NBT
                final CompoundNBT tag = stack.getChildTag("BigBuckets");
                fixNBT(tag, fluidHandler, stack);
            }
            return fluidHandler.getFluid().getAmount();
        }
        return 0;
    }

    @PlatformDependent
    public void setCapacity(ItemStack stack, int capacity) {
        final LazyOptional<IFluidHandlerItem> cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (cap.isPresent()) {
            final BigBucketFluidHandler fluidHandler = (BigBucketFluidHandler) cap.orElseThrow(NullPointerException::new);
            fluidHandler.setTankCapacity(capacity);
        }
    }

    @PlatformDependent
    public void fill(ItemStack stack, FluidStack fluidStack) {
        final LazyOptional<IFluidHandlerItem> cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (cap.isPresent()) {
            final BigBucketFluidHandler fluidHandler = (BigBucketFluidHandler) cap.orElseThrow(NullPointerException::new);
            fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @PlatformDependent
    public void drain(ItemStack stack, int drainAmount) {
        final LazyOptional<IFluidHandlerItem> cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

        if (cap.isPresent()) {
            final BigBucketFluidHandler fluidHandler = (BigBucketFluidHandler) cap.orElseThrow(NullPointerException::new);
            fluidHandler.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
        }
    }


    /*
     * FORGE SPECIFIC START
     */

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    @PlatformDependent
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

                return cap == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY ?
                        (LazyOptional<T>) LazyOptional.of(() -> new BigBucketFluidHandler(stack))
                        : LazyOptional.empty();
            }
        };
    }

    @Override
    @PlatformDependent
    public boolean showDurabilityBar(ItemStack stack) {
        return getCapacity(stack) > 0;
    }

    @Override
    @PlatformDependent
    public double getDurabilityForDisplay(ItemStack stack) {
        double fullness = getFullness(stack);
        double capacity = getCapacity(stack);
        return fullness == capacity ? 0.0 : (capacity-fullness)/capacity;
    }

    //Because I'm too cool for datafixers
    @PlatformDependent
    private void fixNBT(CompoundNBT tag, BigBucketFluidHandler fluidHandler, ItemStack stack) {
        final Fluid oldFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("Fluid")));
        final int oldCapacity = tag.getInt("Capacity");
        final int oldFullness = tag.getInt("Fullness");

        fluidHandler.setTankCapacity(oldCapacity * FluidAttributes.BUCKET_VOLUME);

        if (oldFluid == null) {
            stack.removeChildTag("BigBuckets");
            return;
        }

        final FluidStack fluidStack = new FluidStack(oldFluid, oldFullness);

        fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        stack.removeChildTag("BigBuckets");
    }
}
