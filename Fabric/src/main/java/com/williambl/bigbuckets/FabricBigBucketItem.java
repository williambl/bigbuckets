package com.williambl.bigbuckets;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public class FabricBigBucketItem extends BigBucketItem implements CustomDurabilityItem {
    public FabricBigBucketItem(Properties builder) {
        super(builder);
    }

    @Override
    public BucketStorageData getBucketStorageData(ItemStack stack) {
        return BucketStorageData.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateTagElement("bucketData")).get().left().map(Pair::getFirst).orElseGet(() -> new BucketStorageData(Fluids.EMPTY, Optional.empty(), 0, 0));
    }

    @Override
    public void setCapacity(ItemStack stack, int capacity) {
        var bucketStorageData = this.getBucketStorageData(stack);
        var tag = stack.getOrCreateTag();
        tag.put("bucketData", BucketStorageData.CODEC.encodeStart(NbtOps.INSTANCE, bucketStorageData.withCapacity(capacity)).getOrThrow(false, Constants.LOG::error));
    }

    @Override
    public int fill(ItemStack stack, Fluid fluid, int amount) {
        var bucketStorageData = this.getBucketStorageData(stack);
        var maxAmount = Math.min(amount, (fluid.isSame(bucketStorageData.fluid()) || bucketStorageData.fluid() == Fluids.EMPTY) ? bucketStorageData.capacity() - bucketStorageData.fullness() : 0);

        stack.getOrCreateTag().put("bucketData", BucketStorageData.CODEC.encodeStart(NbtOps.INSTANCE, bucketStorageData.withFluid(fluid, bucketStorageData.data(), bucketStorageData.fullness()+maxAmount)).getOrThrow(false, Constants.LOG::error));

        return maxAmount;
    }

    @Override
    public int drain(ItemStack stack, int drainAmount) {
        var bucketStorageData = this.getBucketStorageData(stack);
        var amount = Math.min(drainAmount, bucketStorageData.fullness());

        stack.getOrCreateTag().put("bucketData", BucketStorageData.CODEC.encodeStart(NbtOps.INSTANCE, bucketStorageData.withFluid(bucketStorageData.fluid(), bucketStorageData.data(), bucketStorageData.fullness()-amount)).getOrThrow(false, Constants.LOG::error));

        return amount;
    }

    @Override
    public boolean shouldShowDurability(ItemStack stack) {
        return getFluid(stack) != Fluids.EMPTY;
    }

    @Override
    public int getMaxDurability(ItemStack stack) {
        return getCapacity(stack);
    }

    @Override
    public int getDurability(ItemStack stack) {
        return getFullness(stack);
    }

    @Override
    public int getDurabilityColor(ItemStack stack) {
        float f = getDurability(stack);
        float g = getMaxDurability(stack);
        float h = Math.max(0.0F, f / g);
        return Mth.hsvToRgb(h / 3.0F, 1.0F, 1.0F);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class BigBucketStorage implements SingleSlotStorage<FluidVariant> {
        private final ContainerItemContext context;

        public BigBucketStorage(ContainerItemContext context) {
            this.context = context;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            if (!context.getItemVariant().isOf(BigBucketsMod.BIG_BUCKET_ITEM)) return 0;

            var stack = context.getItemVariant().toStack();
            if (BigBucketsMod.BIG_BUCKET_ITEM.canAcceptFluid(stack, resource.getFluid())) {
                var amount = BigBucketsMod.BIG_BUCKET_ITEM.fill(stack, resource.getFluid(), (int) maxAmount);
                ItemVariant newVariant = ItemVariant.of(stack);
                if (context.exchange(newVariant, 1, transaction) == 1) {
                    return amount;
                }
            }

            return 0;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            if (!context.getItemVariant().isOf(BigBucketsMod.BIG_BUCKET_ITEM)) return 0;

            var stack = context.getItemVariant().toStack();
            if (BigBucketsMod.BIG_BUCKET_ITEM.getFluid(stack).isSame(resource.getFluid())) {
                var amount = BigBucketsMod.BIG_BUCKET_ITEM.drain(stack, (int) maxAmount);
                ItemVariant newVariant = ItemVariant.of(stack);
                if (context.exchange(newVariant, 1, transaction) == 1) {
                    return amount;
                }
            }

            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return context.getItemVariant().isOf(BigBucketsMod.BIG_BUCKET_ITEM) && BigBucketsMod.BIG_BUCKET_ITEM.getFluid(context.getItemVariant().toStack()) == Fluids.EMPTY;
        }

        @Override
        public FluidVariant getResource() {
            return FluidVariant.of(BigBucketsMod.BIG_BUCKET_ITEM.getFluid(context.getItemVariant().toStack()));
        }

        @Override
        public long getAmount() {
            return BigBucketsMod.BIG_BUCKET_ITEM.getFullness(context.getItemVariant().toStack());
        }

        @Override
        public long getCapacity() {
            return BigBucketsMod.BIG_BUCKET_ITEM.getCapacity(context.getItemVariant().toStack());
        }
    }
}
