package com.williambl.bigbuckets;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BigBucketFluidHandler implements IFluidHandlerItem, ICapabilityProvider {
    private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);

    @NotNull
    protected ItemStack container;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     */
    public BigBucketFluidHandler(@NotNull ItemStack container) {
        this.container = container;
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return container;
    }

    @NotNull
    public FluidStack getFluid() {
        var data = BigBucketsMod.BIG_BUCKET_ITEM.get().getBucketStorageData(this.container);

        return new FluidStack(data.fluid(), data.fullness(), data.data().orElse(null));
    }

    protected void setFluid(FluidStack fluid) {
        var data = BigBucketsMod.BIG_BUCKET_ITEM.get().getBucketStorageData(this.container);
        BigBucketsMod.BIG_BUCKET_ITEM.get().setBucketStorageData(this.container, data.withFluid(fluid.getFluid(), Optional.ofNullable(fluid.getTag()), fluid.getAmount()));
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return BigBucketsMod.BIG_BUCKET_ITEM.get().getBucketStorageData(this.container).capacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {

        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (container.getCount() != 1 || resource.isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }

        FluidStack contained = getFluid();
        if (contained.isEmpty()) {
            int fillAmount = Math.min(this.getTankCapacity(0), resource.getAmount());

            if (doFill.execute()) {
                FluidStack filled = resource.copy();
                filled.setAmount(fillAmount);
                setFluid(filled);
            }

            return fillAmount;
        } else {
            if (contained.isFluidEqual(resource)) {
                int fillAmount = Math.min(this.getTankCapacity(0) - contained.getAmount(), resource.getAmount());

                if (doFill.execute() && fillAmount > 0) {
                    contained.grow(fillAmount);
                    setFluid(contained);
                }

                return fillAmount;
            }

            return 0;
        }
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty() || !resource.isFluidEqual(getFluid())) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack contained = getFluid();
        if (contained.isEmpty() || !canDrainFluidType(contained)) {
            return FluidStack.EMPTY;
        }

        final int drainAmount = Math.min(contained.getAmount(), maxDrain);

        FluidStack drained = contained.copy();
        drained.setAmount(drainAmount);

        if (action.execute()) {
            contained.shrink(drainAmount);
            if (contained.isEmpty()) {
                setContainerToEmpty();
            } else {
                setFluid(contained);
            }
        }

        return drained;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    public boolean canDrainFluidType(FluidStack fluid) {
        return true;
    }

    /**
     * Override this method for special handling.
     * Can be used to swap out or destroy the container.
     */
    protected void setContainerToEmpty() {
        this.setFluid(FluidStack.EMPTY);
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.orEmpty(capability, holder);
    }
}