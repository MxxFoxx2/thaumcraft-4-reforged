package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.util.FastColor;
import thaumcraft.alchemy.CrucibleBlockEntity;

public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderer.State> {
    public static class State extends BlockEntityRenderState {
        boolean liquid;
        float height;
        int color;
    }

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrucibleBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.liquid = entity.getWaterAmount() > 0;
        state.height = entity.getFluidHeight();
        float recolor = entity.tagAmount() / 100.0F;
        if (recolor > 0.0F) {
            recolor = 0.5F + recolor / 2.0F;
        }
        int water = entity.getLevel() instanceof BlockAndTintGetter tintGetter ? BiomeColors.getAverageWaterColor(tintGetter, entity.getBlockPos()) : 0x3F76E4;
        float red = Math.max(0.0F, 1.0F - recolor / 3.0F) * FastColor.ARGB32.red(water) / 255.0F;
        float green = Math.max(0.0F, 1.0F - recolor) * FastColor.ARGB32.green(water) / 255.0F;
        float blue = Math.max(0.0F, 1.0F - recolor / 2.0F) * FastColor.ARGB32.blue(water) / 255.0F;
        state.color = FastColor.ARGB32.colorFromFloat(1.0F, red, green, blue);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.liquid) {
            AlchemyRendering.crucibleWater(collector, poseStack, state.height, state.color, state.lightCoords);
        }
    }
}
