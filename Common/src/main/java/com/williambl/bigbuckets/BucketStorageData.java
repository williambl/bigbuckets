package com.williambl.bigbuckets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public record BucketStorageData(Fluid fluid, Optional<CompoundTag> data, int fullness, int capacity) {
    public static final Codec<BucketStorageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registry.FLUID.byNameCodec().optionalFieldOf("fluid", Fluids.EMPTY).forGetter(BucketStorageData::fluid),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(BucketStorageData::data),
            Codec.INT.optionalFieldOf("fullness", 0).forGetter(BucketStorageData::fullness),
            Codec.INT.optionalFieldOf("capacity", 0).forGetter(BucketStorageData::capacity)
    ).apply(instance, BucketStorageData::new));

    public BucketStorageData withFluid(Fluid fluid, Optional<CompoundTag> data, int fullness) {
        return fullness == 0 ? new BucketStorageData(Fluids.EMPTY, Optional.empty(), 0, this.capacity()) : new BucketStorageData(fluid, data, fullness, this.capacity());
    }

    public BucketStorageData withCapacity(int capacity) {
        return new BucketStorageData(this.fluid(), this.data(), this.fullness(), capacity);
    }
}
