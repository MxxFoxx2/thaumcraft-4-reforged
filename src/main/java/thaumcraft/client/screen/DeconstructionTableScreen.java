package thaumcraft.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.client.platform.GuiBlit;
import thaumcraft.menu.DeconstructionTableMenu;
import thaumcraft.registry.ModSounds;

public class DeconstructionTableScreen extends AbstractContainerScreen<DeconstructionTableMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/gui_decontable.png");
    private static final int ASPECT_X = 64;
    private static final int ASPECT_Y = 48;

    public DeconstructionTableScreen(DeconstructionTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    private boolean overAspect(double mouseX, double mouseY) {
        double offsetX = mouseX - (leftPos + ASPECT_X);
        double offsetY = mouseY - (topPos + ASPECT_Y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < 16 && offsetY < 16;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        GuiBlit.blit(graphics, TEXTURE, leftPos, topPos, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight, 256, 256, 0xFFFFFFFF);
        if (menu.getBreakTime() > 0) {
            int scaled = menu.getBreakTimeScaled(46);
            GuiBlit.blit(graphics, TEXTURE, leftPos + 93, topPos + 15 + 46 - scaled, 9, scaled, 176, 46 - scaled, 9, scaled, 256, 256, 0xFFFFFFFF);
        }
        Aspect aspect = menu.getAspect();
        if (aspect != null) {
            AspectRenderer.drawTag(graphics, leftPos + ASPECT_X, topPos + ASPECT_Y, aspect, 0.0F);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Aspect aspect = menu.getAspect();
        if (aspect != null && overAspect(mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(font, List.of(Component.literal(aspect.getName()), aspect.getDescription()), Optional.empty(), mouseX, mouseY - 8);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && menu.getAspect() != null && overAspect(event.x(), event.y())) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, DeconstructionTableMenu.BUTTON_COLLECT);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.HHOFF.get(), 1.0F + minecraft.level.getRandom().nextFloat() * 0.1F, 0.2F));
        }
        return super.mouseClicked(event, doubleClick);
    }
}
