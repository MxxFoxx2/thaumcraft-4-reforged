package thaumcraft;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.crafting.ConfigRecipes;
import thaumcraft.essentia.EssentiaKeys;
import thaumcraft.infusion.InfusionKeys;
import thaumcraft.item.NodeJarKeys;
import thaumcraft.network.ModPayloads;
import thaumcraft.platform.Platform;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModCapabilities;
import thaumcraft.registry.ModCreativeTabs;
import thaumcraft.registry.ModFeatures;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModMenus;
import thaumcraft.registry.ModSounds;
import thaumcraft.registry.ModVillagers;
import thaumcraft.research.ConfigResearch;
import thaumcraft.research.Knowledge;
import thaumcraft.research.ResearchNoteKeys;
import thaumcraft.test.AlchemyGameTests;
import thaumcraft.test.ArcaneGameTests;
import thaumcraft.test.EssentiaGameTests;
import thaumcraft.test.HexGridGameTests;
import thaumcraft.test.InfusionGameTests;
import thaumcraft.test.ThaumcraftGameTests;
import thaumcraft.wand.WandKeys;

@Mod(Thaumcraft.MODID)
public class Thaumcraft {
    public static final String MODID = "thaumcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Thaumcraft(IEventBus modEventBus, ModContainer modContainer) {
        AspectKeys.init();
        WandKeys.init();
        NodeJarKeys.init();
        EssentiaKeys.init();
        InfusionKeys.init();
        ResearchNoteKeys.init();
        Knowledge.init();
        Platform.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModVillagers.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCapabilities.register();
        ModPayloads.register();
        ConfigRecipes.init();
        ConfigResearch.init();
        ThaumcraftGameTests.register(modEventBus);
        HexGridGameTests.register(modEventBus);
        EssentiaGameTests.register(modEventBus);
        AlchemyGameTests.register(modEventBus);
        ArcaneGameTests.register(modEventBus);
        InfusionGameTests.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
