package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.infusion.InfusionMatrixBlockEntity;

public class RunicMatrixRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity, RunicMatrixRenderer.State> {
    public static class State extends BlockEntityRenderState {
        boolean active;
        boolean crafting;
        float startUp;
        int instability;
        int craftCount;
        float ticks;
    }

    public RunicMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(InfusionMatrixBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partialTicks;
        state.active = entity.isActive();
        state.crafting = entity.isCrafting();
        state.startUp = entity.getStartUp();
        state.instability = entity.getInstability();
        state.craftCount = entity.getCraftCount();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RunicMatrixRendering.submit(collector, poseStack, state.lightCoords, true, state.active, state.crafting, state.startUp, state.instability, state.craftCount, state.ticks);
    }

    @Override
    public AABB getRenderBoundingBox(InfusionMatrixBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(2.0);
    }
}
