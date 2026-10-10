package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
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
import thaumcraft.alchemy.ThaumatoriumBlockEntity;
import thaumcraft.crafting.CrucibleRecipe;

public class ThaumatoriumRenderer implements BlockEntityRenderer<ThaumatoriumBlockEntity, ThaumatoriumRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        Direction facing = Direction.NORTH;
    }

    public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ThaumatoriumBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getFacing();
        state.item.clear();
        int count = entity.getRecipeKeys().size();
        if (entity.getLevel() == null || count == 0) {
            return;
        }
        int viewerTicks = EssentiaRendering.viewerTicks();
        CrucibleRecipe recipe = entity.recipeAt(viewerTicks / 40 % count);
        if (recipe != null) {
            ItemStack stack = recipe.result().create();
            stack.setCount(1);
            itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        AlchemyRendering.thaumatorium(collector, poseStack, state.lightCoords, state.facing);
        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            AlchemyRendering.thaumatoriumItemTransform(poseStack, state.facing);
            state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(ThaumatoriumBlockEntity entity) {
        return new AABB(entity.getBlockPos()).expandTowards(0.0, 1.0, 0.0).inflate(1.0);
    }
}
