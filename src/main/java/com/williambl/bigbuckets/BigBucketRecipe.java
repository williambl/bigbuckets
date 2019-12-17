package com.williambl.bigbuckets;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BigBucketRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
   public BigBucketRecipe() {
      super();
   }

   @Override
   public boolean matches(InventoryCrafting inv, World worldIn) {
      int i = 0;

      for (int j = 0; j < inv.getSizeInventory(); ++j) {
         ItemStack stackInSlot = inv.getStackInSlot(j);
         if (!stackInSlot.isEmpty()) {
            if (stackInSlot.getItem() == Items.BUCKET)
               i++;
            else
               return false;
         }
      }

      return i == 2;
   }

   @Override
   public ItemStack getCraftingResult(InventoryCrafting inv) {
      ItemStack stack = new ItemStack(BigBuckets.BIG_BUCKET_ITEM);
      stack.getOrCreateSubCompound("BigBuckets").setInteger("Capacity", 2);
      return stack;
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
   public NonNullList<Ingredient> getIngredients() {
      return NonNullList.withSize(2, Ingredient.fromItem(Items.BUCKET));
   }

   @Override
   public boolean isDynamic() {
      return true;
   }

}