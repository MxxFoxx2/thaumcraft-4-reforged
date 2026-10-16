package thaumcraft.client.render.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import thaumcraft.client.platform.ImmediateGeometry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import thaumcraft.client.platform.CameraRenderState;
import net.minecraft.util.Mth;
import thaumcraft.client.fx.ParticleFx;
import thaumcraft.client.platform.Billboards;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.WorldRendering;
import thaumcraft.entity.golem.GolemBobber;

import net.minecraft.resources.ResourceLocation;

public final class GolemBobberRenderer extends EntityRenderer<GolemBobber> {
    public GolemBobberRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    public GolemBobberRenderState createRenderState() {
        return new GolemBobberRenderState();
    }

    public void extractRenderState(GolemBobber entity, GolemBobberRenderState state, float partialTicks) {
        state.ageInTicks = entity.tickCount + partialTicks;
        state.bobberX = Mth.lerp(partialTicks, entity.xo, entity.getX());
        state.bobberY = Mth.lerp(partialTicks, entity.yo, entity.getY());
        state.bobberZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
        var fisher = entity.getFisher();
        state.hasFisher = fisher != null;
        if (fisher != null) {
            state.fisherX = Mth.lerp(partialTicks, fisher.xo, fisher.getX());
            state.fisherY = Mth.lerp(partialTicks, fisher.yo, fisher.getY()) + fisher.getEyeHeight() - 0.4;
            state.fisherZ = Mth.lerp(partialTicks, fisher.zo, fisher.getZ());
        }
    }

    @Override
    public void render(GolemBobber entity, float yaw, float partialTicks, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        var state = createRenderState();
        extractRenderState(entity, state, partialTicks);
        var collector = new ImmediateGeometry(buffers);
        var camera = new CameraRenderState();
        Billboards.submit(
            poseStack,
            collector,
            camera,
            TcRenderTypes.entityCutoutNoCull(ParticleFx.PARTICLES),
            1.0F / 16.0F,
            2.0F / 16.0F,
            2.0F / 16.0F,
            3.0F / 16.0F,
            0.25F,
            0xFFFFFFFF,
            0xF000F0
        );
        if (state.hasFisher) {
            WorldRendering.submitFloatyLine(
                buffers,
                poseStack,
                net.minecraft.core.BlockPos.containing(state.bobberX, state.bobberY, state.bobberZ),
                state.fisherX,
                state.fisherY,
                state.fisherZ,
                state.bobberX,
                state.bobberY,
                state.bobberZ,
                0xFF202020,
                32.0F,
                System.nanoTime()
            );
        }
        super.render(entity, yaw, partialTicks, poseStack, buffers, light);
    }
    @Override
    public ResourceLocation getTextureLocation(GolemBobber entity) {
        return thaumcraft.Thaumcraft.id("textures/misc/particles.png");
    }
}
