package com.williambl.bigbuckets.recipe;

import com.williambl.bigbuckets.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BigBucketRecipe extends CustomRecipe {
   public BigBucketRecipe(ResourceLocation idIn) {
      super(idIn);
   }

   @Override
   public boolean matches(CraftingContainer inv, Level worldIn) {
      int i = 0;

      for (int j = 0; j < inv.getContainerSize(); ++j) {
         ItemStack stackInSlot = inv.getItem(j);
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
   public ItemStack assemble(CraftingContainer inv) {
      ItemStack stack = Services.REGISTRY.bigBucketItem().get().getDefaultInstance();
      Services.REGISTRY.bigBucketItem().get().setCapacity(stack, 2 * Services.FLUIDS.bucketVolume());
      return stack;
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return Services.REGISTRY.bigBucketRecipeSerializer().get();
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 2;
   }
}