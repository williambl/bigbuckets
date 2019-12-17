package com.williambl.bigbuckets;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class BigBucketIncreaseCapacityRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    public BigBucketIncreaseCapacityRecipe() {
        super();
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        int i = 0;
        ItemStack bigBucketStack = ItemStack.EMPTY;

        for (int j = 0; j < inv.getSizeInventory(); ++j) {
            ItemStack stackInSlot = inv.getStackInSlot(j);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() == BigBuckets.BIG_BUCKET_ITEM) {
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
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int i = 0;
        ItemStack bigBucketStack = ItemStack.EMPTY;

        for (int j = 0; j < inv.getSizeInventory(); ++j) {
            ItemStack stackInSlot = inv.getStackInSlot(j);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() == BigBuckets.BIG_BUCKET_ITEM) {
                    bigBucketStack = stackInSlot.copy();
                } else {
                    if (stackInSlot.getItem() == Items.BUCKET)
                        i++;
                }
            }
        }

        BigBuckets.BIG_BUCKET_ITEM.setCapacity(bigBucketStack, BigBuckets.BIG_BUCKET_ITEM.getCapacity(bigBucketStack) + i);
        return bigBucketStack;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.create();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.from(Ingredient.fromItem(Items.BUCKET), Ingredient.fromItem(BigBuckets.BIG_BUCKET_ITEM));
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}