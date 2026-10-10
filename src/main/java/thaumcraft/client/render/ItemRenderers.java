package thaumcraft.client.render;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import thaumcraft.Thaumcraft;
import thaumcraft.client.hud.WandHud;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ItemRenderers {
    private static final List<String> CUSTOM_RENDERED = List.of(
        "wand",
        "thaumometer",
        "aura_node",
        "node_jar",
        "node_stabilizer",
        "advanced_node_stabilizer",
        "node_transducer",
        "vis_relay",
        "vis_charge_relay",
        "runic_matrix",
        "alchemical_centrifuge",
        "essentia_crystallizer",
        "essentia_reservoir",
        "filled_jar",
        "filled_void_jar",
        "alembic",
        "flux_scrubber",
        "arcane_bellows",
        "essentia_valve"
    );
    private static TcItemRenderer renderer;

    private ItemRenderers() {
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    renderer = new TcItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }
        };
        for (String path : CUSTOM_RENDERED) {
            Item item = BuiltInRegistries.ITEM.get(Thaumcraft.id(path));
            if (item != Items.AIR) {
                event.registerItem(extensions, item);
            }
        }
    }

    @SubscribeEvent
    static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(TcItemRenderer.VALVE_BASE);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, Thaumcraft.id("wand_hud"), WandHud::render);
    }
}
