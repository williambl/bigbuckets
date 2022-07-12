package com.williambl.bigbuckets.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.williambl.bigbuckets.CustomDurabilityItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author BoundaryBreaker
 * MIT Licensed
 * Modified to make CustomDurabilityItem#shouldShowDurability have an effect
 * https://github.com/Boundarybreaker/ShulkerCharm/blob/master/src/main/java/space/bbkr/shulkercharm/mixin/MixinItemRenderer.java
 */
@Mixin(ItemRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class MixinItemRenderer {

    @Shadow protected abstract void fillRect(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

    @Inject(method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void renderCustomDurabilityBar(Font textRenderer, ItemStack stack, int x, int y, String amount, CallbackInfo info) {
        if (stack.getItem() instanceof CustomDurabilityItem durab) {
            if (durab.shouldShowDurability(stack)) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();

                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder builder = tessellator.getBuilder();

                float progress = ((float) durab.getDurability(stack)) / ((float) durab.getMaxDurability(stack));
                int durability = (int) (13 * progress);
                int color = durab.getDurabilityColor(stack);

                this.fillRect(builder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(builder, x + 2, y + 13, durability, 1, color >> 16 & 255, color >> 8 & 255, color & 255, 255);

                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }
}