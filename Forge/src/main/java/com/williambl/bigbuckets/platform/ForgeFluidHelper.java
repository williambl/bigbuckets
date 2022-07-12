package com.williambl.bigbuckets.platform;

import com.williambl.bigbuckets.platform.services.IFluidHelper;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;

public class ForgeFluidHelper implements IFluidHelper {
    @Override
    public int bucketVolume() {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public Fluid getFluidFromBucketItem(BucketItem item) {
        return item.getFluid();
    }
}
