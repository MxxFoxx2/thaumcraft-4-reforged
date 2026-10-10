package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.block.ResearchTableBlock;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.item.ResearchNotesItem;
import thaumcraft.item.ScribingToolsItem;

public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity, ResearchTableRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState scribe = new ItemStackRenderState();
        final ItemStackRenderState note = new ItemStackRenderState();
        float rotation;
    }

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ResearchTableBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.scribe.clear();
        state.note.clear();
        if (entity.getLevel() == null) {
            return;
        }
        Direction facing = entity.getBlockState().getValue(ResearchTableBlock.FACING);
        state.rotation = 270.0F - facing.toYRot();
        ItemStack scribe = entity.getItem(ResearchTableBlockEntity.SCRIBE_SLOT);
        if (scribe.getItem() instanceof ScribingToolsItem) {
            itemModelResolver.updateForTopItem(state.scribe, scribe.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
        ItemStack note = entity.getItem(ResearchTableBlockEntity.NOTE_SLOT);
        if (note.getItem() instanceof ResearchNotesItem) {
            itemModelResolver.updateForTopItem(state.note, note.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong() + 1);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.scribe.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.01F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            poseStack.translate(-0.28F, 0.0F, -0.28F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(0.3F, 0.3F, 0.3F);
            state.scribe.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (!state.note.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.02F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            poseStack.translate(0.1F, 0.0F, -0.35F);
            poseStack.mulPose(Axis.YP.rotationDegrees(15.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
            state.note.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(ResearchTableBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(1.0, 0.0, 1.0).expandTowards(0.0, 1.0, 0.0);
    }
}
