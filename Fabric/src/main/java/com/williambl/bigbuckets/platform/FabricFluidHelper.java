package com.williambl.bigbuckets.platform;

import com.williambl.bigbuckets.mixin.BucketItemAccessor;
import com.williambl.bigbuckets.platform.services.IFluidHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

public class FabricFluidHelper implements IFluidHelper {
    @Override
    public int bucketVolume() {
        return (int) FluidConstants.BUCKET;
    }

    @Override
    public Fluid getFluidFromBucketItem(BucketItem item) {
        return ((BucketItemAccessor)item).getContent();
    }
}
