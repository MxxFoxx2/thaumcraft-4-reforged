package thaumcraft.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import thaumcraft.crafting.TcIngredient;

final class InfusionLayout {
    static final int WIDTH = 130;
    static final int HEIGHT = 102 + 18 * 3 + 12;
    static final int CENTER_X = 49;
    static final int CENTER_Y = 49;
    static final int PER_ROW = 5;
    static final int ASPECTS_Y = 102;

    private InfusionLayout() {
    }

    static void components(IRecipeLayoutBuilder builder, List<TcIngredient> components) {
        float slice = components.isEmpty() ? 0.0F : 360 / components.size();
        float rotation = -90.0F;
        for (TcIngredient component : components) {
            int offsetX = (int) (Mth.cos(rotation / 180.0F * Mth.PI) * 40.0F);
            int offsetY = (int) (Mth.sin(rotation / 180.0F * Mth.PI) * 40.0F);
            rotation += slice;
            TcJei.accept(builder.addInputSlot(CENTER_X + offsetX - 8, CENTER_Y + offsetY - 8).setStandardSlotBackground(), component);
        }
    }

    static void instability(GuiGraphics graphics, int instability) {
        Language language = Language.getInstance();
        int level = Math.min(5, instability / 2);
        Component text = Component.literal(language.getOrDefault("tc.inst") + " " + language.getOrDefault("tc.inst." + level));
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, text, (WIDTH - font.width(text)) / 2, HEIGHT - 10, 0xFF404040, false);
    }
}
