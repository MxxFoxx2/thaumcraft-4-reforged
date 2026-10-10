package thaumcraft.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public final class BrowserDraw {
    private static final ResourceLocation TOAST_BACKGROUND = ResourceLocation.withDefaultNamespace("toast/advancement");

    private BrowserDraw() {
    }

    public static void full(GuiGraphicsExtractor graphics, ResourceLocation texture, int x, int y, int width, int height, int argb) {
        var gpuTexture = Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture();
        int textureWidth = gpuTexture.getWidth(0);
        int textureHeight = gpuTexture.getHeight(0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, textureWidth, textureHeight, textureWidth, textureHeight, argb);
    }

    public static void reversed(GuiGraphicsExtractor graphics, ResourceLocation texture, int x, int y, int width, int height, int u, int v, int textureWidth, int textureHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, -width, height, textureWidth, textureHeight, 0xFFFFFFFF);
    }

    public static void toastBackground(GuiGraphicsExtractor graphics, int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TOAST_BACKGROUND, 0, 0, width, height);
    }
}
