package com.williambl.bigbuckets.jei;

import com.williambl.bigbuckets.BigBuckets;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Size2i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BigBucketUpgradeCategoryExtension implements ICraftingCategoryExtension {

    @Override
    public void setIngredients(IIngredients ingredients) {
        List<Ingredient> inputs = new ArrayList<>();
        inputs.add(Ingredient.fromItems(Items.BUCKET));
        ItemStack inputBB = new ItemStack(BigBuckets.BIG_BUCKET_ITEM);
        inputBB.getOrCreateChildTag("BigBuckets").putInt("Capacity", 2);
        inputs.add(Ingredient.fromStacks(inputBB));

        ItemStack output = new ItemStack(BigBuckets.BIG_BUCKET_ITEM);
        output.getOrCreateChildTag("BigBuckets").putInt("Capacity", 3);

        ingredients.setInputIngredients(inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(BigBuckets.MODID, "bigbucket_upgrade");
    }

    @Nullable
    @Override
    public Size2i getSize() {
        return null;
    }
}
