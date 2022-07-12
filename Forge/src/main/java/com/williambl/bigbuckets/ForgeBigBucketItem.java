package com.williambl.bigbuckets;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class ForgeBigBucketItem extends BigBucketItem {
    public ForgeBigBucketItem(Properties builder) {
        super(builder);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
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
}
