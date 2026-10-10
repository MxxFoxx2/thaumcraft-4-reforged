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
import net.minecraft.core.Direction;
import thaumcraft.essentia.EssentiaReservoirBlockEntity;

public class EssentiaReservoirRenderer implements BlockEntityRenderer<EssentiaReservoirBlockEntity, EssentiaReservoirRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.DOWN;
        float level;
        float red;
        float green;
        float blue;
    }

    public EssentiaReservoirRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EssentiaReservoirBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getFacing();
        int total = entity.getAspects().visSize();
        state.level = entity.getDisplayAspect() != null && total != 0 ? (float) total / entity.getMaxAmount() : 0.0F;
        state.red = entity.getCurrentRed();
        state.green = entity.getCurrentGreen();
        state.blue = entity.getCurrentBlue();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        EssentiaRendering.reservoir(collector, poseStack, state.facing, state.lightCoords);
        if (state.level > 0.0F) {
            EssentiaRendering.reservoirLiquid(collector, poseStack, state.level, state.red, state.green, state.blue);
        }
    }
}
