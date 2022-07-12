package com.williambl.bigbuckets.client.platform;

import com.williambl.bigbuckets.client.platform.services.IFluidRenderer;
import com.williambl.bigbuckets.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class ForgeFluidRenderer implements IFluidRenderer {
    @Override
    public TextureAtlasSprite getSprite(Fluid fluid, CompoundTag data) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(IClientFluidTypeExtensions.of(fluid).getStillTexture(new FluidStack(fluid, Services.FLUIDS.bucketVolume(), data)));
    }

    @Override
    public int getColor(Fluid fluid, CompoundTag data) {
        return IClientFluidTypeExtensions.of(fluid).getTintColor(new FluidStack(fluid, Services.FLUIDS.bucketVolume(), data));
    }
}
