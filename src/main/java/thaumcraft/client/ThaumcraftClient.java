package thaumcraft.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.fx.ClientFx;
import thaumcraft.client.research.ResearchBrowserScreen;
import thaumcraft.client.research.ThaumometerClient;
import thaumcraft.client.screen.AlchemicalFurnaceScreen;
import thaumcraft.client.screen.ArcaneWorkbenchScreen;
import thaumcraft.client.screen.DeconstructionTableScreen;
import thaumcraft.client.screen.ResearchTableScreen;
import thaumcraft.client.screen.ThaumatoriumScreen;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModMenus;
import thaumcraft.research.ResearchClientHooks;

@Mod(value = Thaumcraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class ThaumcraftClient {
    public ThaumcraftClient(IEventBus modEventBus, ModContainer container) {
        ClientPayloadHandlers.register();
        Fx.set(new ClientFx());
        ResearchClientHooks.setBookOpener(ResearchBrowserScreen::open);
        ThaumometerClient.register();
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
        event.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(ModMenus.DECONSTRUCTION_TABLE.get(), DeconstructionTableScreen::new);
        event.register(ModMenus.ALCHEMICAL_FURNACE.get(), AlchemicalFurnaceScreen::new);
        event.register(ModMenus.THAUMATORIUM.get(), ThaumatoriumScreen::new);
    }
}
