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
import thaumcraft.alchemy.FluxScrubberBlockEntity;

public class FluxScrubberRenderer implements BlockEntityRenderer<FluxScrubberBlockEntity, FluxScrubberRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.DOWN;
        float ticks;
    }

    public FluxScrubberRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FluxScrubberBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getFacing();
        state.ticks = EssentiaRendering.viewerTicks() + partialTicks + (entity.getBlockPos().hashCode() & 31);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        AlchemyRendering.fluxScrubber(collector, poseStack, state.lightCoords, state.facing, state.ticks);
    }
}
