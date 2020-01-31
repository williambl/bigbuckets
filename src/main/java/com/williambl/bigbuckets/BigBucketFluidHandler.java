package com.williambl.bigbuckets;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;

public class BigBucketFluidHandler extends FluidHandlerItemStack {

    public static final String CAPACITY_NBT_KEY = "Capacity";

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     */
    public BigBucketFluidHandler(@Nonnull ItemStack container) {
        super(container, container.hasTag() ? container.getTag().getInt(CAPACITY_NBT_KEY) : 0);
    }

    public void setTankCapacity(int newCapacity) {
        this.capacity = newCapacity;

        if (!container.hasTag())
        {
            container.setTag(new CompoundNBT());
        }

        container.getTag().putInt(CAPACITY_NBT_KEY, newCapacity);
    }
}
