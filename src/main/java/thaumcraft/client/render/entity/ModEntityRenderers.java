package thaumcraft.client.render.entity;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.registry.ModEntities;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ModEntityRenderers {
    private ModEntityRenderers() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FIREBAT.get(), FireBatRenderer::new);
        event.registerEntityRenderer(ModEntities.EMBER.get(), EmberRenderer::new);
        event.registerEntityRenderer(ModEntities.SHOCK_ORB.get(), ShockOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.EXPLOSIVE_ORB.get(), ExplosiveOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.PRIMAL_ORB.get(), PrimalOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.FROST_SHARD.get(), FrostShardRenderer::new);
        event.registerEntityRenderer(ModEntities.PECH_BLAST.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.PRIMAL_ARROW.get(), PrimalArrowRenderer::new);
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FireBatModel.LAYER, FireBatModel::createBodyLayer);
    }
}
