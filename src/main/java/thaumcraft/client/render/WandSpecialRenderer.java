package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.WandGeometry;
import thaumcraft.client.platform.ItemGeometry;
import thaumcraft.wand.WandParts;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

public class WandSpecialRenderer implements SpecialModelRenderer<WandParts> {
    private final LegacyItemView view;

    public WandSpecialRenderer(LegacyItemView view) {
        this.view = view;
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

    @Override
    public void submit(
        @Nullable WandParts parts,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int lightCoords,
        int overlayCoords,
        boolean hasFoil,
        int outlineColor
    ) {
        poseStack.pushPose();
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
        ItemGeometry.solid(collector.order(0), poseStack, rod.texture(), false, sink -> WandGeometry.rod(sink, staff, light, 0xFFFFFFFF));
        ItemGeometry.solid(collector.order(0), poseStack, resolved.getCap().texture(), false, sink -> WandGeometry.caps(sink, staff, sceptre, lightCoords));
        if (sceptre || rod.runes()) {
            int runeLight = Math.max(lightCoords & 0xFFFF, 200) | (lightCoords & 0xFFFF0000);
            ItemGeometry.blended(
                collector.order(1),
                poseStack,
                SCRIPT,
                BLEND_ADDITIVE,
                sink -> WandGeometry.runes(sink, staff, sceptre, rod.runes(), ticks, runeLight)
            );
        }
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (boolean staff : new boolean[] {false, true}) {
            PoseStack poseStack = new PoseStack();
            transform(poseStack, view, staff);
            float top = staff ? 2.4F : 1.4F;
            for (float x : new float[] {-0.15F, 0.15F}) {
                for (float y : new float[] {-0.1F, top}) {
                    for (float z : new float[] {-0.15F, 0.15F}) {
                        output.accept(poseStack.last().pose().transformPosition(x, y, z, new Vector3f()));
                    }
                }
            }
        }
    }

    @Override
    public @Nullable WandParts extractArgument(ItemStack stack) {
        return Wands.getParts(stack);
    }

    public record Unbaked(LegacyItemView view) implements SpecialModelRenderer.Unbaked<WandParts> {
        public static final MapCodec<Unbaked> MAP_CODEC = LegacyItemView.FIELD.xmap(Unbaked::new, Unbaked::view);

        @Override
        public SpecialModelRenderer<WandParts> bake(SpecialModelRenderer.BakingContext context) {
            return new WandSpecialRenderer(view);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
