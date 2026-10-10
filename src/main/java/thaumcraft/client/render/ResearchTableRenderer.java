package thaumcraft.client.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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

public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {
    public static class State {
        int lightCoords;
        BlockPos blockPos = BlockPos.ZERO;
        final ItemSlot scribe = new ItemSlot();
        final ItemSlot note = new ItemSlot();
        float rotation;
    }

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ResearchTableBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        State state = new State();
        state.lightCoords = packedLight;
        state.blockPos = entity.getBlockPos();
        extract(entity, state, partialTick, camera.getPosition());
        submit(state, poseStack, buffers, camera);
    }

    private void extract(ResearchTableBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition) {
        state.scribe.clear();
        state.note.clear();
        if (entity.getLevel() == null) {
            return;
        }
        Direction facing = entity.getBlockState().getValue(ResearchTableBlock.FACING);
        state.rotation = 270.0F - facing.toYRot();
        ItemStack scribe = entity.getItem(ResearchTableBlockEntity.SCRIBE_SLOT);
        if (scribe.getItem() instanceof ScribingToolsItem) {
            state.scribe.set(scribe.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), (int) entity.getBlockPos().asLong());
        }
        ItemStack note = entity.getItem(ResearchTableBlockEntity.NOTE_SLOT);
        if (note.getItem() instanceof ResearchNotesItem) {
            state.note.set(note.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), (int) entity.getBlockPos().asLong() + 1);
        }
    }

    private void submit(State state, PoseStack poseStack, MultiBufferSource buffers, Camera camera) {
        if (!state.scribe.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.01F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            poseStack.translate(-0.28F, 0.0F, -0.28F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(0.3F, 0.3F, 0.3F);
            state.scribe.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
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
            state.note.submit(poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(ResearchTableBlockEntity entity) {
        return new AABB(entity.getBlockPos()).inflate(1.0, 0.0, 1.0).expandTowards(0.0, 1.0, 0.0);
    }
}
