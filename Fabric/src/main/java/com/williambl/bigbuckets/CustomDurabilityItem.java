package com.williambl.bigbuckets;

import net.minecraft.world.item.ItemStack;

/**
 * @author BoundaryBreaker
 * MIT Licensed
 * https://github.com/Boundarybreaker/ShulkerCharm/blob/master/src/main/java/space/bbkr/shulkercharm/mixin/MixinItemRenderer.java
 */
public interface CustomDurabilityItem {

    boolean shouldShowDurability(ItemStack stack);

    int getMaxDurability(ItemStack stack);

    int getDurability(ItemStack stack);

    int getDurabilityColor(ItemStack stack);
}