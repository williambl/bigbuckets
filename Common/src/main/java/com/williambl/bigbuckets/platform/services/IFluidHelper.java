package com.williambl.bigbuckets.platform.services;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface IFluidHelper {
    int bucketVolume();

    Fluid getFluidFromBucketItem(BucketItem item);
}
