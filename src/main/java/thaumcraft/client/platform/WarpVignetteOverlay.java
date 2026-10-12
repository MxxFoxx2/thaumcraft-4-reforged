package thaumcraft.client.platform;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import thaumcraft.Thaumcraft;

public final class WarpVignetteOverlay {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/misc/vignette.png");

    private WarpVignetteOverlay() {
    }

    public static void draw(RenderGuiLayerEvent.Post event, int alpha) {
        var graphics = event.getGuiGraphics();
        GuiBlit.blit(
            graphics, TEXTURE, 0, 0, graphics.guiWidth(), graphics.guiHeight(),
            0, 0, 256, 256, 256, 256, alpha << 24 | 0xFFFFFF
        );
    }
}
