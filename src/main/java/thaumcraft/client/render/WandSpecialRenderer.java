package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.WandGeometry;
import thaumcraft.client.platform.ItemGeometry;
import thaumcraft.wand.WandParts;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

public final class WandSpecialRenderer {
    private WandSpecialRenderer() {
    }

    private static void transform(PoseStack poseStack, LegacyItemView view, boolean staff) {
        view.apply(poseStack);
        if (staff) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
        }
        switch (view) {
            case INVENTORY -> {
                if (staff) {
                    poseStack.scale(0.8F, 0.8F, 0.8F);
                }
                poseStack.mulPose(Axis.ZP.rotationDegrees(66.0F));
                poseStack.translate(0.0F, 0.6F, 0.0F);
                if (staff) {
                    poseStack.translate(-0.7F, 0.6F, 0.0F);
                }
            }
            case ENTITY -> {
                if (staff) {
                    poseStack.translate(0.0F, 1.5F, 0.0F);
                    poseStack.scale(0.9F, 0.9F, 0.9F);
                } else {
                    poseStack.translate(0.0F, 1.0F, 0.0F);
                }
            }
            case EQUIPPED, FIRST_PERSON -> {
                poseStack.translate(0.5F, 1.5F, 0.5F);
                if (view == LegacyItemView.FIRST_PERSON) {
                    poseStack.scale(1.0F, 1.1F, 1.0F);
                }
            }
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
    }

    private static final ResourceLocation SCRIPT = Thaumcraft.id("textures/misc/script.png");
    private static final int BLEND_ADDITIVE = 1;

    public static void render(ItemStack stack, LegacyItemView view, PoseStack poseStack, MultiBufferSource buffers, int lightCoords) {
        poseStack.pushPose();
        WandParts parts = Wands.getParts(stack);
        WandParts resolved = parts == null ? WandParts.DEFAULT : parts;
        WandRod rod = resolved.getRod();
        boolean staff = rod.staff();
        boolean sceptre = resolved.sceptre();
        transform(poseStack, view, staff);
        float ticks = Util.getMillis() / 50.0F;
        int rodLight = lightCoords;
        if (rod.glowing()) {
            int glow = (int) (200.0F + (float) Math.sin(ticks) * 5.0F + 5.0F);
            rodLight = Math.max(lightCoords & 0xFFFF, glow) | (lightCoords & 0xFFFF0000);
        }
        int light = rodLight;
        ItemGeometry.solid(buffers, poseStack, rod.texture(), false, sink -> WandGeometry.rod(sink, staff, light, 0xFFFFFFFF));
        ItemGeometry.solid(buffers, poseStack, resolved.getCap().texture(), false, sink -> WandGeometry.caps(sink, staff, sceptre, lightCoords));
        if (sceptre || rod.runes()) {
            int runeLight = Math.max(lightCoords & 0xFFFF, 200) | (lightCoords & 0xFFFF0000);
            ItemGeometry.blended(
                buffers,
                poseStack,
                SCRIPT,
                BLEND_ADDITIVE,
                sink -> WandGeometry.runes(sink, staff, sceptre, rod.runes(), ticks, runeLight)
            );
        }
        poseStack.popPose();
    }
}
