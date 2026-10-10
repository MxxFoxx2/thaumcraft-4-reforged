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
import thaumcraft.essentia.EssentiaBufferBlockEntity;

public class TubeBufferRenderer implements BlockEntityRenderer<EssentiaBufferBlockEntity, TubeBufferRenderer.State> {
    public static class State extends BlockEntityRenderState {
        final int[] choked = new int[6];
    }

    public TubeBufferRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EssentiaBufferBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        for (Direction direction : Direction.values()) {
            boolean shown = entity.getLevel() != null
                && entity.getChokedSide(direction) != 0
                && entity.isSideOpen(direction)
                && Essentia.getConnectable(entity.getLevel(), entity.getBlockPos(), direction) != null;
            state.choked[direction.ordinal()] = shown ? entity.getChokedSide(direction) : 0;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Direction direction : Direction.values()) {
            int choke = state.choked[direction.ordinal()];
            if (choke != 0) {
                EssentiaRendering.buffer(collector, poseStack, direction, choke == 2, state.lightCoords);
            }
        }
    }
}
