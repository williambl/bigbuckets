package com.williambl.bigbuckets.recipe;

import com.williambl.bigbuckets.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.security.Provider;

public class BigBucketIncreaseCapacityRecipe extends CustomRecipe {
    public BigBucketIncreaseCapacityRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        int i = 0;
        ItemStack bigBucketStack = ItemStack.EMPTY;

        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack stackInSlot = inv.getItem(j);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() == Services.REGISTRY.bigBucketItem().get()) {
                    if (bigBucketStack.isEmpty())
                        bigBucketStack = stackInSlot;
                    else
                        return false;
                } else {
                    if (stackInSlot.getItem() == Items.BUCKET)
                        i++;
                    else {
                        return false;
                    }
                }
            }
        }

        return !bigBucketStack.isEmpty() && i > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        int i = 0;
        ItemStack bigBucketStack = ItemStack.EMPTY;

        for (int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack stackInSlot = inv.getItem(j);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() == Services.REGISTRY.bigBucketItem().get()) {
                    bigBucketStack = stackInSlot.copy();
                } else {
                    if (stackInSlot.getItem() == Items.BUCKET)
                        i++;
                }
            }
        }

        Services.REGISTRY.bigBucketItem().get().setCapacity(bigBucketStack, Services.REGISTRY.bigBucketItem().get().getCapacity(bigBucketStack) + i * Services.FLUIDS.bucketVolume());
        return bigBucketStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Services.REGISTRY.bigBucketIncreaseCapacityRecipeSerializer().get();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}