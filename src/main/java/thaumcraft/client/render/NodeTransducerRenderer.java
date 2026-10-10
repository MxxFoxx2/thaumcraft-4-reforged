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
import thaumcraft.node.NodeTransducerBlockEntity;

public class NodeTransducerRenderer implements BlockEntityRenderer<NodeTransducerBlockEntity, NodeTransducerRenderer.State> {
    public static class State extends BlockEntityRenderState {
        int count;
        int status;
        int ambient;
        int viewerTicks;
    }

    public NodeTransducerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NodeTransducerBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        state.count = entity.getCount();
        state.status = entity.getStatus();
        state.ambient = WorldRendering.ambientLight(entity.getLevel(), entity.getBlockPos());
        state.viewerTicks = viewer == null ? 0 : viewer.tickCount;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        NodeDeviceRendering.transducer(collector, poseStack, state.ambient, state.status, state.count, state.viewerTicks, true);
    }

    @Override
    public AABB getRenderBoundingBox(NodeTransducerBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(1.0);
    }
}
