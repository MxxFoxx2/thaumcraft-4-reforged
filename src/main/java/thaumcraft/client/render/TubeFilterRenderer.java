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
import net.minecraft.util.FastColor;
import thaumcraft.essentia.FilterTubeBlockEntity;

public class TubeFilterRenderer implements BlockEntityRenderer<FilterTubeBlockEntity, TubeFilterRenderer.State> {
    public static class State extends BlockEntityRenderState {
        int color = 0xFFFFFFFF;
    }

    public TubeFilterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FilterTubeBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.color = entity.getAspectFilter() == null ? 0xFFFFFFFF : FastColor.ARGB32.opaque(entity.getAspectFilter().getColor());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        EssentiaRendering.filterCore(collector, poseStack, state.color, state.lightCoords);
    }
}
