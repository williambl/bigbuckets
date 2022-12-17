package com.williambl.bigbuckets.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.williambl.bigbuckets.BigBucketItem;
import com.williambl.bigbuckets.client.platform.ClientServices;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modified heavily from LemmaEOF's custom durability bar
 * <a href="https://github.com/Boundarybreaker/ShulkerCharm/blob/master/src/main/java/space/bbkr/shulkercharm/mixin/MixinItemRenderer.java">source</a>
 */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow protected abstract void fillRect(BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha);

    private void fillRectWithTexture(BufferBuilder bufferBuilder, int i, int j, int k, int l, float u0, float v0, float u1, float v1, int color) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(i + 0, j + l, 0.0).uv(u0, v1).color(color).endVertex();
        bufferBuilder.vertex(i + k, j + l, 0.0).uv(u1, v1).color(color).endVertex();
        bufferBuilder.vertex(i + k, j + 0, 0.0).uv(u1, v0).color(color).endVertex();
        bufferBuilder.vertex(i + 0, j + 0, 0.0).uv(u0, v0).color(color).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }

    @Inject(method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
    private void bigBuckets$renderBigBucketBar(Font textRenderer, ItemStack stack, int x, int y, String amount, CallbackInfo info) {
        if (stack.getItem() instanceof BigBucketItem item) {
            if (item.shouldShowBar(stack)) {
                var data = item.getBucketStorageData(stack);

                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();

                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder builder = tessellator.getBuilder();

                float progress = ((float) data.fullness()) / ((float) data.capacity());
                int durability = (int) (13 * progress);

                this.fillRect(builder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);

                RenderSystem.enableTexture();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                var fluidRenderer = ClientServices.FLUIDS;

                TextureAtlasSprite sprite = fluidRenderer.getSprite(data.fluid(), data.data().orElse(null));
                RenderSystem.setShaderTexture(0, sprite.atlas().location());

                int color = fluidRenderer.getColor(data.fluid(), data.data().orElse(null));

                this.fillRectWithTexture(builder, x + 2, y + 13, durability, 1, sprite.getU0(), sprite.getV0(), sprite.getU(durability), sprite.getV(1.0), color);

                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
            }
        }
    }
}