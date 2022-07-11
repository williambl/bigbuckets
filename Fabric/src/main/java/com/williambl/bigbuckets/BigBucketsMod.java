package com.williambl.bigbuckets;

import com.williambl.bigbuckets.recipe.BigBucketIncreaseCapacityRecipe;
import com.williambl.bigbuckets.recipe.BigBucketRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

import static com.williambl.bigbuckets.BigBucketsCommon.id;

public class BigBucketsMod implements ModInitializer {

    //public static BigBucketItem BIG_BUCKET_ITEM = Registry.register(Registry.ITEM, new BigBucketItem())
    public static SimpleRecipeSerializer<BigBucketRecipe> BIG_BUCKET_RECIPE_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, id("crafting_special_big_bucket"), new SimpleRecipeSerializer<>(BigBucketRecipe::new));
    public static SimpleRecipeSerializer<BigBucketIncreaseCapacityRecipe> BIG_BUCKET_INCREASE_CAPACITY_RECIPE_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, id("crafting_special_big_bucket_increase_capacity"), new SimpleRecipeSerializer<>(BigBucketIncreaseCapacityRecipe::new));

    @Override
    public void onInitialize() {
        BigBucketsCommon.init();
    }
}
