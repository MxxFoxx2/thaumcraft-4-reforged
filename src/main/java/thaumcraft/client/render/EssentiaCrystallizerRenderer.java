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
import thaumcraft.essentia.EssentiaCrystallizerBlockEntity;

public class EssentiaCrystallizerRenderer implements BlockEntityRenderer<EssentiaCrystallizerBlockEntity, EssentiaCrystallizerRenderer.State> {
    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.DOWN;
        float spin;
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        float ticks;
    }

    public EssentiaCrystallizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EssentiaCrystallizerBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getFacing();
        state.spin = entity.getSpin() + entity.getSpinIncrement() * partialTicks;
        state.red = entity.getCurrentRed();
        state.green = entity.getCurrentGreen();
        state.blue = entity.getCurrentBlue();
        state.ticks = EssentiaRendering.viewerTicks() + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        EssentiaRendering.crystallizer(collector, poseStack, state.facing, state.spin, state.red, state.green, state.blue, state.ticks, state.lightCoords);
    }
}
