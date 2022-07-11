package com.williambl.bigbuckets;

import com.williambl.bigbuckets.recipe.BigBucketIncreaseCapacityRecipe;
import com.williambl.bigbuckets.recipe.BigBucketRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Constants.MOD_ID)
public class BigBucketsMod {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);

    public static final RegistryObject<SimpleRecipeSerializer<BigBucketRecipe>> BIG_BUCKET_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "crafting_special_big_bucket",
            () -> new SimpleRecipeSerializer<>(BigBucketRecipe::new)
    );

    public static final RegistryObject<SimpleRecipeSerializer<BigBucketIncreaseCapacityRecipe>> BIG_BUCKET_INCREASE_CAPACITY_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(
            "crafting_special_big_bucket_increase_capacity",
            () -> new SimpleRecipeSerializer<>(BigBucketIncreaseCapacityRecipe::new)
    );

    public BigBucketsMod() {
        BigBucketsCommon.init();

        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}