package thaumcraft.client.platform;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public final class GuiBlit {
    private GuiBlit() {
    }

    public static void blit(
        GuiGraphicsExtractor graphics,
        ResourceLocation texture,
        int x,
        int y,
        int width,
        int height,
        int u,
        int v,
        int regionWidth,
        int regionHeight,
        int textureWidth,
        int textureHeight,
        int argb
    ) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) u, (float) v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, argb);
    }
}
