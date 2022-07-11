package com.williambl.bigbuckets.platform;

import com.williambl.bigbuckets.BigBucketItem;
import com.williambl.bigbuckets.BigBucketsMod;
import com.williambl.bigbuckets.platform.services.IBigBucketsRegistry;
import com.williambl.bigbuckets.recipe.BigBucketIncreaseCapacityRecipe;
import com.williambl.bigbuckets.recipe.BigBucketRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

import java.util.function.Supplier;

public class ForgeBigBucketsRegistry implements IBigBucketsRegistry {
    @Override
    public Supplier<BigBucketItem> bigBucketItem() {
        return null;
    }

    @Override
    public Supplier<SimpleRecipeSerializer<BigBucketRecipe>> bigBucketRecipeSerializer() {
        return BigBucketsMod.BIG_BUCKET_RECIPE_SERIALIZER;
    }

    @Override
    public Supplier<SimpleRecipeSerializer<BigBucketIncreaseCapacityRecipe>> bigBucketIncreaseCapacityRecipeSerializer() {
        return BigBucketsMod.BIG_BUCKET_INCREASE_CAPACITY_RECIPE_SERIALIZER;
    }
}
