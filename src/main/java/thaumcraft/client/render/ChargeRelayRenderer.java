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
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.arcane.VisChargeRelayBlockEntity;
import thaumcraft.vis.VisNet;
import thaumcraft.vis.VisRelayBlockEntity;

public class ChargeRelayRenderer implements BlockEntityRenderer<VisChargeRelayBlockEntity, ChargeRelayRenderer.State> {
    public static class State extends BlockEntityRenderState {
        int crystalColor = -1;
        boolean connected;
        int ambient;
        float ticks;
    }

    public ChargeRelayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(VisChargeRelayBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        byte color = entity.getColor();
        state.crystalColor = color >= 0 && color < VisRelayBlockEntity.COLORS.length ? VisRelayBlockEntity.COLORS[color] : -1;
        state.connected = VisNet.isNodeValid(entity.getParent());
        state.ambient = WorldRendering.ambientLight(entity.getLevel(), entity.getBlockPos());
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        NodeDeviceRendering.charger(collector, poseStack, state.ambient, state.crystalColor, state.connected, state.ticks);
    }
}
