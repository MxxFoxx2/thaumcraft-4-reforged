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
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.OnewayTubeBlockEntity;

public class TubeOnewayRenderer implements BlockEntityRenderer<OnewayTubeBlockEntity, TubeOnewayRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean connected;
    }

    public TubeOnewayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(OnewayTubeBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getFacing();
        state.connected = entity.getLevel() == null || Essentia.getConnectable(entity.getLevel(), entity.getBlockPos(), state.facing.getOpposite()) != null;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.connected) {
            EssentiaRendering.oneway(collector, poseStack, state.facing, state.lightCoords);
        }
    }
}
