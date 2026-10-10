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
import net.minecraft.util.Mth;
import thaumcraft.alchemy.BellowsBlock;
import thaumcraft.alchemy.BellowsBlockEntity;

public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity, BellowsRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float inflation;
    }

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BellowsBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getBlockState().getValue(BellowsBlock.FACING);
        state.inflation = Mth.lerp(partialTicks, entity.getPrevInflation(), entity.getInflation());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        AlchemyRendering.bellows(collector, poseStack, state.lightCoords, state.facing, state.inflation);
    }
}
