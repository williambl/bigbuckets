package com.williambl.bigbuckets.client.platform.services;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

public interface IFluidRenderer {
    TextureAtlasSprite getSprite(Fluid fluid, CompoundTag data);
    int getColor(Fluid fluid, CompoundTag data);
}
