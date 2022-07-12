package com.williambl.bigbuckets.client.platform;

import com.williambl.bigbuckets.client.platform.services.IFluidRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public class FabricFluidRenderer implements IFluidRenderer {
    @Override
    public TextureAtlasSprite getSprite(Fluid fluid, CompoundTag data) {
        return FluidVariantRendering.getSprite(FluidVariant.of(fluid, data));
    }

    @Override
    public int getColor(Fluid fluid, CompoundTag data) {
        return FluidVariantRendering.getColor(FluidVariant.of(fluid, data));
    }
}
