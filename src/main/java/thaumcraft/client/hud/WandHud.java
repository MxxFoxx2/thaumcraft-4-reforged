package thaumcraft.client.hud;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.wand.WandVis;
import thaumcraft.wand.Wands;

public final class WandHud {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/hud.png");
    private static final long SAMPLE_INTERVAL = 1000L;
    private static final Map<Integer, WandVis> OLD_VALUES = new HashMap<>();
    private static long nextSample;

    private WandHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.gui.screen() != null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!Wands.isWand(stack)) {
            return;
        }
        long time = Util.getMillis();
        int slot = player.getInventory().selected;
        WandVis current = Wands.getAllVisData(stack);
        WandVis old = OLD_VALUES.get(slot);
        if (old == null) {
            OLD_VALUES.put(slot, current);
            old = current;
        } else if (nextSample <= time) {
            OLD_VALUES.put(slot, current);
            nextSample = time + SAMPLE_INTERVAL;
        }
        int max = Wands.getMaxVis(stack);
        boolean sneaking = player.isShiftKeyDown();
        graphics.pose().pushMatrix();
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5F, 0.5F);
        blit(graphics, 0, 0, 0, 0, 64, 64, 0xFFFFFFFF);
        graphics.pose().popMatrix();
        graphics.pose().translate(16.0F, 16.0F);
        int count = 0;
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            int amount = current.get(aspect);
            graphics.pose().pushMatrix();
            graphics.pose().rotate((float) Math.toRadians(90.0F));
            graphics.pose().rotate((float) Math.toRadians(-15 + count * 24));
            graphics.pose().translate(0.0F, -32.0F);
            graphics.pose().scale(0.5F, 0.5F);
            int level = max <= 0 ? 0 : (int) (30.0F * amount / max);
            blit(graphics, -4, 35 - level, 104, 0, 8, level, 0xCC000000 | aspect.getColor());
            blit(graphics, -8, -3, 72, 0, 16, 42, 0xFFFFFFFF);
            int previous = old.get(aspect);
            if (previous > amount) {
                blit(graphics, -4, -8, 128, 0, 8, 8, 0xFFFFFFFF);
            } else if (previous < amount) {
                blit(graphics, -4, -8, 120, 0, 8, 8, 0xFFFFFFFF);
            }
            if (sneaking) {
                graphics.pose().pushMatrix();
                graphics.pose().rotate((float) Math.toRadians(-90.0F));
                graphics.text(minecraft.font, String.valueOf(amount / 100), -32, -4, 0xFFFFFFFF, true);
                graphics.pose().popMatrix();
            }
            graphics.pose().popMatrix();
            count++;
        }
        graphics.pose().popMatrix();
    }

    private static void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height, int argb) {
        if (width <= 0 || height <= 0) {
            return;
        }
        GuiBlit.blit(graphics, TEXTURE, x, y, width, height, u, v, width, height, 256, 256, argb);
    }
}
