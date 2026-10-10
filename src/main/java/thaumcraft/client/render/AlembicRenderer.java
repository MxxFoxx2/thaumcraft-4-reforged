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
import thaumcraft.alchemy.AlembicBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.essentia.Essentia;
import thaumcraft.essentia.EssentiaBufferBlockEntity;
import thaumcraft.essentia.TubeBlockEntity;

public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity, AlembicRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean aboveFurnace;
        boolean aboveAlembic;
        @Nullable Aspect filter;
        final boolean[] nozzles = new boolean[6];
    }

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AlembicBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getFacing();
        state.aboveFurnace = entity.isAboveFurnace();
        state.aboveAlembic = entity.isAboveAlembic();
        state.filter = entity.getAspectFilter();
        for (Direction direction : Direction.values()) {
            boolean shown = false;
            if (entity.getLevel() != null && entity.canOutputTo(direction)) {
                Object target = Essentia.getConnectable(entity.getLevel(), entity.getBlockPos(), direction);
                shown = target != null && !(target instanceof TubeBlockEntity) && !(target instanceof EssentiaBufferBlockEntity);
            }
            state.nozzles[direction.ordinal()] = shown;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        AlchemyRendering.alembic(collector, poseStack, state.lightCoords, true, state.facing, state.aboveFurnace, state.aboveAlembic, state.filter, state.nozzles);
    }
}
