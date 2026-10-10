package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.Thaumcraft;

public class TcItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelResourceLocation VALVE_BASE = ModelResourceLocation.standalone(Thaumcraft.id("item/essentia_valve_base"));

    public TcItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet entityModelSet) {
        super(dispatcher, entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        switch (BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()) {
            case "wand" -> WandSpecialRenderer.render(stack, LegacyItemView.of(displayContext), poseStack, buffers, light);
            case "thaumometer" -> ThaumometerItemRenderer.render(LegacyItemView.of(displayContext), poseStack, buffers, light);
            case "aura_node" -> NodeItemRenderer.render(poseStack, buffers);
            case "node_jar" -> NodeJarItemRenderer.render(stack, poseStack, buffers, light);
            case "node_stabilizer" -> NodeDeviceSpecialRenderers.stabilizer(poseStack, buffers, light, false);
            case "advanced_node_stabilizer" -> NodeDeviceSpecialRenderers.stabilizer(poseStack, buffers, light, true);
            case "node_transducer" -> NodeDeviceSpecialRenderers.transducer(poseStack, buffers, light);
            case "vis_relay" -> NodeDeviceSpecialRenderers.relay(poseStack, buffers, light);
            case "vis_charge_relay" -> ArcaneSpecialRenderers.chargeRelay(poseStack, buffers, light);
            case "runic_matrix" -> ArcaneSpecialRenderers.runicMatrix(poseStack, buffers, light);
            case "alchemical_centrifuge" -> EssentiaSpecialRenderers.centrifuge(poseStack, buffers, light);
            case "essentia_crystallizer" -> EssentiaSpecialRenderers.crystallizer(poseStack, buffers, light);
            case "essentia_reservoir" -> EssentiaSpecialRenderers.reservoir(poseStack, buffers, light);
            case "filled_jar" -> EssentiaSpecialRenderers.filledJar(stack, poseStack, buffers, light, false);
            case "filled_void_jar" -> EssentiaSpecialRenderers.filledJar(stack, poseStack, buffers, light, true);
            case "alembic" -> AlchemySpecialRenderers.alembic(poseStack, buffers, light);
            case "flux_scrubber" -> AlchemySpecialRenderers.fluxScrubber(poseStack, buffers, light);
            case "arcane_bellows" -> AlchemySpecialRenderers.bellows(poseStack, buffers, light);
            case "essentia_valve" -> {
                BakedModel model = Minecraft.getInstance().getModelManager().getModel(VALVE_BASE);
                Minecraft.getInstance().getItemRenderer().renderModelLists(
                    model,
                    stack,
                    light,
                    overlay,
                    poseStack,
                    buffers.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true))
                );
                EssentiaSpecialRenderers.tubeValve(poseStack, buffers, light);
            }
            default -> {
            }
        }
    }
}
