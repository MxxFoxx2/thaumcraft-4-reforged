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
import thaumcraft.aspect.Aspect;
import thaumcraft.essentia.JarBlockEntity;

public class EssentiaJarRenderer implements BlockEntityRenderer<JarBlockEntity, EssentiaJarRenderer.State> {
    public static class State extends BlockEntityRenderState {
        int posX;
        @Nullable Aspect aspect;
        int amount;
        int maxAmount;
        @Nullable Aspect filter;
        Direction facing = Direction.NORTH;
    }

    public EssentiaJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(JarBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.posX = entity.getBlockPos().getX();
        state.aspect = entity.getAspect();
        state.amount = entity.getAmount();
        state.maxAmount = entity.getMaxAmount();
        state.filter = entity.getAspectFilter();
        state.facing = entity.getFacing();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        EssentiaJarRendering.submit(collector, poseStack, state.lightCoords, state.posX, state.aspect, state.amount, state.maxAmount, state.filter, state.facing);
    }
}
