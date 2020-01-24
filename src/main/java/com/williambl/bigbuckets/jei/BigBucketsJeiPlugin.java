package com.williambl.bigbuckets.jei;

import com.williambl.bigbuckets.BigBucketRecipe;
import com.williambl.bigbuckets.BigBuckets;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class BigBucketsJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(BigBuckets.MODID, "bigbucketsjeiplugin");
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(BigBucketRecipe.class, bigBucketRecipe -> new BigBucketRecipeCategoryExtension());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<IRecipe<?>> recipes = new ArrayList<>();
        recipes.add(new BigBucketRecipe(new ResourceLocation(BigBuckets.MODID, "bigbucket")));
        registration.addRecipes(recipes, VanillaRecipeCategoryUid.CRAFTING);

        registration.addIngredientInfo(new ItemStack(BigBuckets.BIG_BUCKET_ITEM), VanillaTypes.ITEM,
                "Big Buckets can store more fluid than regular buckets.",
                        "A Big Bucket can be upgraded by placing more buckets with it in a crafting table.");
    }
}
