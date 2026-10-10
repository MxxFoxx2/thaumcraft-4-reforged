package thaumcraft.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.AdvancedAlchemicalFurnaceBlock;
import thaumcraft.alchemy.AlchemicalFurnaceBlock;
import thaumcraft.alchemy.AlembicBlock;
import thaumcraft.alchemy.BellowsBlock;
import thaumcraft.alchemy.CrucibleBlock;
import thaumcraft.alchemy.FluxBlock;
import thaumcraft.alchemy.FluxGasBlock;
import thaumcraft.alchemy.FluxGooBlock;
import thaumcraft.alchemy.FluxScrubberBlock;
import thaumcraft.alchemy.MnemonicMatrixBlock;
import thaumcraft.alchemy.ThaumatoriumBlock;
import thaumcraft.arcane.ArcaneWorkbenchBlock;
import thaumcraft.arcane.DeconstructionTableBlock;
import thaumcraft.arcane.VisChargeRelayBlock;
import thaumcraft.block.AmberBlock;
import thaumcraft.block.CandleBlock;
import thaumcraft.block.ChargedObsidianTotemBlock;
import thaumcraft.block.CustomPlantBlock;
import thaumcraft.block.FlammableBlock;
import thaumcraft.block.GolemFetterBlock;
import thaumcraft.block.InfusedStoneBlock;
import thaumcraft.block.LootBlock;
import thaumcraft.block.MagicalLeavesBlock;
import thaumcraft.block.MagicalLogBlock;
import thaumcraft.block.MagicalSaplingBlock;
import thaumcraft.block.ManaPodBlock;
import thaumcraft.block.NodeJarBlock;
import thaumcraft.block.ObsidianTotemBlock;
import thaumcraft.block.RechargeFocusBlock;
import thaumcraft.block.ResearchTableBlock;
import thaumcraft.block.ResearchTablePartBlock;
import thaumcraft.block.SilverwoodKnotBlock;
import thaumcraft.block.TableBlock;
import thaumcraft.block.TravelPavingStoneBlock;
import thaumcraft.block.WandPedestalBlock;
import thaumcraft.block.WardingPavingStoneBlock;
import thaumcraft.essentia.CentrifugeBlock;
import thaumcraft.essentia.EssentiaBufferBlock;
import thaumcraft.essentia.EssentiaCrystallizerBlock;
import thaumcraft.essentia.EssentiaReservoirBlock;
import thaumcraft.essentia.FilledJarItem;
import thaumcraft.essentia.JarBlock;
import thaumcraft.essentia.TubeBlock;
import thaumcraft.infusion.InfusionMatrixBlock;
import thaumcraft.infusion.InfusionPillarBlock;
import thaumcraft.infusion.PedestalBlock;
import thaumcraft.item.ManaBeanItem;
import thaumcraft.item.NodeJarItem;
import thaumcraft.node.AuraNodeBlock;
import thaumcraft.node.EnergizedNodeBlock;
import thaumcraft.node.NodeStabilizerBlock;
import thaumcraft.node.NodeTransducerBlock;
import thaumcraft.platform.Registration;
import thaumcraft.vis.VisRelayBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Thaumcraft.MODID);

    public static final String[] CANDLE_COLORS = {
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
    };
    public static final int[] CANDLE_TINTS = {
        15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131,
        10526880, 2651799, 8073150, 2437522, 5320730, 3887386, 11743532, 1973019,
    };

    public static final DeferredBlock<DropExperienceBlock> CINNABAR_ORE = withItem(BLOCKS.registerBlock(
        "cinnabar_ore",
        properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
        properties -> rock(properties, 1.5F, 5.0F)
    ));
    public static final DeferredBlock<InfusedStoneBlock> AIR_INFUSED_STONE = infusedStone("air_infused_stone", 0xFFFF7E);
    public static final DeferredBlock<InfusedStoneBlock> FIRE_INFUSED_STONE = infusedStone("fire_infused_stone", 0xFF3C01);
    public static final DeferredBlock<InfusedStoneBlock> WATER_INFUSED_STONE = infusedStone("water_infused_stone", 0x0090FF);
    public static final DeferredBlock<InfusedStoneBlock> EARTH_INFUSED_STONE = infusedStone("earth_infused_stone", 0x00A000);
    public static final DeferredBlock<InfusedStoneBlock> ORDER_INFUSED_STONE = infusedStone("order_infused_stone", 0xEECCFF);
    public static final DeferredBlock<InfusedStoneBlock> ENTROPY_INFUSED_STONE = infusedStone("entropy_infused_stone", 0x555577);
    public static final DeferredBlock<DropExperienceBlock> AMBER_ORE = withItem(BLOCKS.registerBlock(
        "amber_ore",
        properties -> new DropExperienceBlock(UniformInt.of(1, 4), properties),
        properties -> rock(properties, 1.5F, 5.0F)
    ));

    public static final DeferredBlock<MagicalLogBlock> GREATWOOD_LOG = withItem(
        BLOCKS.registerBlock("greatwood_log", MagicalLogBlock::new, ModBlocks::log),
        properties -> Registration.fuel(properties, 400)
    );
    public static final DeferredBlock<MagicalLogBlock> SILVERWOOD_LOG = withItem(
        BLOCKS.registerBlock("silverwood_log", MagicalLogBlock::new, ModBlocks::log),
        properties -> Registration.fuel(properties, 400)
    );
    public static final DeferredBlock<MagicalLeavesBlock> GREATWOOD_LEAVES = withItem(BLOCKS.registerBlock(
        "greatwood_leaves",
        properties -> new MagicalLeavesBlock(() -> ModBlocks.GREATWOOD_SAPLING.get(), 200, false, properties),
        ModBlocks::leaves
    ));
    public static final DeferredBlock<MagicalLeavesBlock> SILVERWOOD_LEAVES = withItem(BLOCKS.registerBlock(
        "silverwood_leaves",
        properties -> new MagicalLeavesBlock(() -> ModBlocks.SILVERWOOD_SAPLING.get(), 250, true, properties),
        properties -> leaves(properties).lightLevel(state -> 7)
    ));
    public static final DeferredBlock<MagicalSaplingBlock> GREATWOOD_SAPLING = withItem(BLOCKS.registerBlock(
        "greatwood_sapling",
        properties -> new MagicalSaplingBlock(false, properties),
        properties -> plant(properties).randomTicks()
    ));
    public static final DeferredBlock<MagicalSaplingBlock> SILVERWOOD_SAPLING = withItem(BLOCKS.registerBlock(
        "silverwood_sapling",
        properties -> new MagicalSaplingBlock(true, properties),
        properties -> plant(properties).randomTicks().lightLevel(state -> 8)
    ));
    public static final DeferredBlock<CustomPlantBlock> SHIMMERLEAF = withItem(BLOCKS.registerBlock(
        "shimmerleaf",
        properties -> new CustomPlantBlock(CustomPlantBlock.Kind.SHIMMERLEAF, properties),
        properties -> plant(properties).lightLevel(state -> 8)
    ));
    public static final DeferredBlock<CustomPlantBlock> CINDERPEARL = withItem(BLOCKS.registerBlock(
        "cinderpearl",
        properties -> new CustomPlantBlock(CustomPlantBlock.Kind.CINDERPEARL, properties),
        properties -> plant(properties).lightLevel(state -> 8)
    ));
    public static final DeferredBlock<CustomPlantBlock> ETHEREAL_BLOOM = withItem(BLOCKS.registerBlock(
        "ethereal_bloom",
        properties -> new CustomPlantBlock(CustomPlantBlock.Kind.ETHEREAL_BLOOM, properties),
        properties -> plant(properties).lightLevel(state -> 15)
    ));
    public static final DeferredBlock<CustomPlantBlock> VISHROOM = withItem(BLOCKS.registerBlock(
        "vishroom",
        properties -> new CustomPlantBlock(CustomPlantBlock.Kind.VISHROOM, properties),
        properties -> plant(properties).lightLevel(state -> 8)
    ));

    public static final DeferredBlock<ManaPodBlock> MANA_POD = BLOCKS.registerBlock(
        "mana_pod",
        ManaPodBlock::new,
        properties -> properties
            .mapColor(MapColor.PLANT)
            .strength(0.5F)
            .sound(SoundType.GRASS)
            .randomTicks()
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY)
            .lightLevel(state -> state.getValue(ManaPodBlock.AGE))
    );
    public static final DeferredItem<ManaBeanItem> MANA_BEAN = ModItems.ITEMS.registerItem("mana_bean", ManaBeanItem::new, ManaBeanItem::properties);

    public static final DeferredBlock<AmberBlock> AMBER_BLOCK = withItem(BLOCKS.registerBlock("amber_block", AmberBlock::new, ModBlocks::amber));
    public static final DeferredBlock<AmberBlock> AMBER_BRICKS = withItem(BLOCKS.registerBlock("amber_bricks", AmberBlock::new, ModBlocks::amber));

    public static final DeferredBlock<ObsidianTotemBlock> OBSIDIAN_TOTEM = withItem(BLOCKS.registerBlock(
        "obsidian_totem",
        ObsidianTotemBlock::new,
        properties -> obsidian(properties)
    ));
    public static final DeferredBlock<Block> OBSIDIAN_TILE = withItem(BLOCKS.registerSimpleBlock("obsidian_tile", ModBlocks::obsidian));
    public static final DeferredBlock<TravelPavingStoneBlock> TRAVEL_PAVING_STONE = withItem(BLOCKS.registerBlock(
        "travel_paving_stone",
        TravelPavingStoneBlock::new,
        properties -> noSpawn(rock(properties, 2.0F, 10.0F)).lightLevel(state -> 9)
    ));
    public static final DeferredBlock<WardingPavingStoneBlock> WARDING_PAVING_STONE = withItem(BLOCKS.registerBlock(
        "warding_paving_stone",
        WardingPavingStoneBlock::new,
        properties -> noSpawn(rock(properties, 2.0F, 10.0F))
    ));
    public static final DeferredBlock<Block> THAUMIUM_BLOCK = withItem(BLOCKS.registerSimpleBlock(
        "thaumium_block",
        properties -> rock(properties, 4.0F, 20.0F).mapColor(MapColor.COLOR_PURPLE).sound(SoundType.METAL)
    ));
    public static final DeferredBlock<Block> TALLOW_BLOCK = withItem(BLOCKS.registerSimpleBlock(
        "tallow_block",
        properties -> rock(properties, 2.0F, 10.0F).mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.WOOL)
    ));
    public static final DeferredBlock<Block> ARCANE_STONE = withItem(BLOCKS.registerSimpleBlock("arcane_stone", properties -> rock(properties, 4.0F, 20.0F)));
    public static final DeferredBlock<Block> ARCANE_STONE_BRICKS = withItem(BLOCKS.registerSimpleBlock("arcane_stone_bricks", properties -> rock(properties, 4.0F, 20.0F)));
    public static final DeferredBlock<GolemFetterBlock> GOLEM_FETTER = withItem(BLOCKS.registerBlock(
        "golem_fetter",
        GolemFetterBlock::new,
        properties -> rock(properties, 2.0F, 10.0F)
    ));
    public static final DeferredBlock<Block> ANCIENT_STONE = withItem(BLOCKS.registerSimpleBlock("ancient_stone", properties -> rock(properties, 2.0F, 10.0F)));
    public static final DeferredBlock<Block> ANCIENT_ROCK = withItem(BLOCKS.registerSimpleBlock("ancient_rock", properties -> rock(properties, 2.0F, 10.0F)));
    public static final DeferredBlock<Block> ANCIENT_STONE_NO_SPAWN = withItem(BLOCKS.registerSimpleBlock(
        "ancient_stone_no_spawn",
        properties -> noSpawn(rock(properties, 2.0F, 10.0F))
    ));
    public static final DeferredBlock<Block> CRUSTED_STONE = withItem(BLOCKS.registerSimpleBlock(
        "crusted_stone",
        properties -> rock(properties, 2.0F, 10.0F).lightLevel(state -> 4)
    ));
    public static final DeferredBlock<Block> ANCIENT_STONE_PEDESTAL = withItem(BLOCKS.registerSimpleBlock(
        "ancient_stone_pedestal",
        properties -> rock(properties, 2.0F, 10.0F)
    ));

    public static final DeferredBlock<FlammableBlock> GREATWOOD_PLANKS = withItem(BLOCKS.registerBlock(
        "greatwood_planks",
        properties -> new FlammableBlock(20, 5, properties),
        ModBlocks::planks
    ));
    public static final DeferredBlock<FlammableBlock> SILVERWOOD_PLANKS = withItem(BLOCKS.registerBlock(
        "silverwood_planks",
        properties -> new FlammableBlock(20, 5, properties),
        ModBlocks::planks
    ));
    public static final DeferredBlock<SlabBlock> GREATWOOD_SLAB = withItem(BLOCKS.registerBlock(
        "greatwood_slab",
        SlabBlock::new,
        properties -> wood(properties, 2.0F, 5.0F)
    ));
    public static final DeferredBlock<SlabBlock> SILVERWOOD_SLAB = withItem(BLOCKS.registerBlock(
        "silverwood_slab",
        SlabBlock::new,
        properties -> wood(properties, 2.0F, 5.0F)
    ));
    public static final DeferredBlock<SlabBlock> ARCANE_STONE_SLAB = withItem(BLOCKS.registerBlock(
        "arcane_stone_slab",
        SlabBlock::new,
        properties -> rock(properties, 2.0F, 10.0F)
    ));
    public static final DeferredBlock<SlabBlock> ANCIENT_STONE_SLAB = withItem(BLOCKS.registerBlock(
        "ancient_stone_slab",
        SlabBlock::new,
        properties -> rock(properties, 2.0F, 10.0F)
    ));
    public static final DeferredBlock<StairBlock> GREATWOOD_STAIRS = withItem(BLOCKS.registerBlock(
        "greatwood_stairs",
        properties -> new StairBlock(ModBlocks.GREATWOOD_PLANKS.get().defaultBlockState(), properties),
        ModBlocks::planks
    ));
    public static final DeferredBlock<StairBlock> SILVERWOOD_STAIRS = withItem(BLOCKS.registerBlock(
        "silverwood_stairs",
        properties -> new StairBlock(ModBlocks.SILVERWOOD_PLANKS.get().defaultBlockState(), properties),
        ModBlocks::planks
    ));
    public static final DeferredBlock<StairBlock> ARCANE_STONE_STAIRS = withItem(BLOCKS.registerBlock(
        "arcane_stone_stairs",
        properties -> new StairBlock(ModBlocks.ARCANE_STONE_BRICKS.get().defaultBlockState(), properties),
        properties -> rock(properties, 2.0F, 10.0F)
    ));
    public static final DeferredBlock<StairBlock> ANCIENT_STONE_STAIRS = withItem(BLOCKS.registerBlock(
        "ancient_stone_stairs",
        properties -> new StairBlock(ModBlocks.ANCIENT_STONE.get().defaultBlockState(), properties),
        properties -> rock(properties, 2.0F, 10.0F)
    ));

    public static final DeferredBlock<LootBlock> LOOT_URN = withItem(BLOCKS.registerBlock(
        "loot_urn",
        properties -> new LootBlock(LootBlock.Kind.URN, properties),
        properties -> properties.mapColor(MapColor.CLAY).strength(0.15F, 0.0F).noOcclusion()
    ));
    public static final DeferredBlock<LootBlock> LOOT_CRATE = withItem(BLOCKS.registerBlock(
        "loot_crate",
        properties -> new LootBlock(LootBlock.Kind.CRATE, properties),
        properties -> wood(properties, 0.15F, 0.0F).noOcclusion()
    ));

    public static final DeferredBlock<AuraNodeBlock> AURA_NODE = withItem(BLOCKS.registerBlock(
        "aura_node",
        AuraNodeBlock::new,
        properties -> airy(properties)
    ));
    public static final DeferredBlock<EnergizedNodeBlock> ENERGIZED_AURA_NODE = BLOCKS.registerBlock(
        "energized_aura_node",
        EnergizedNodeBlock::new,
        properties -> airy(properties)
    );
    public static final DeferredBlock<ChargedObsidianTotemBlock> CHARGED_OBSIDIAN_TOTEM = withItem(BLOCKS.registerBlock(
        "charged_obsidian_totem",
        ChargedObsidianTotemBlock::new,
        properties -> obsidian(properties)
    ));
    public static final DeferredBlock<SilverwoodKnotBlock> SILVERWOOD_KNOT = BLOCKS.registerBlock(
        "silverwood_knot",
        SilverwoodKnotBlock::new,
        ModBlocks::log
    );
    public static final DeferredBlock<NodeStabilizerBlock> NODE_STABILIZER = withItem(BLOCKS.registerBlock(
        "node_stabilizer",
        properties -> new NodeStabilizerBlock(false, properties),
        properties -> stoneDevice(properties)
    ));
    public static final DeferredBlock<NodeStabilizerBlock> ADVANCED_NODE_STABILIZER = withItem(BLOCKS.registerBlock(
        "advanced_node_stabilizer",
        properties -> new NodeStabilizerBlock(true, properties),
        properties -> stoneDevice(properties)
    ));
    public static final DeferredBlock<NodeTransducerBlock> NODE_TRANSDUCER = withItem(BLOCKS.registerBlock(
        "node_transducer",
        NodeTransducerBlock::new,
        properties -> stoneDevice(properties)
    ));
    public static final DeferredBlock<WandPedestalBlock> WAND_RECHARGE_PEDESTAL = withItem(BLOCKS.registerBlock(
        "wand_recharge_pedestal",
        WandPedestalBlock::new,
        properties -> stoneDevice(properties)
    ));
    public static final DeferredBlock<RechargeFocusBlock> COMPOUND_RECHARGE_FOCUS = withItem(BLOCKS.registerBlock(
        "compound_recharge_focus",
        RechargeFocusBlock::new,
        properties -> stoneDevice(properties)
    ));
    public static final DeferredBlock<VisRelayBlock> VIS_RELAY = withItem(BLOCKS.registerBlock(
        "vis_relay",
        VisRelayBlock::new,
        properties -> properties
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(3.0F, 17.0F)
            .sound(SoundType.METAL)
            .noOcclusion()
            .lightLevel(state -> state.getValue(VisRelayBlock.CONNECTED) ? 10 : 2)
    ));
    public static final DeferredBlock<NodeJarBlock> NODE_JAR = BLOCKS.registerBlock(
        "node_jar",
        NodeJarBlock::new,
        properties -> properties
            .mapColor(MapColor.NONE)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .lightLevel(state -> 11)
    );
    public static final DeferredItem<NodeJarItem> NODE_JAR_ITEM = ModItems.ITEMS.registerItem(
        "node_jar",
        properties -> new NodeJarItem(NODE_JAR.get(), properties),
        NodeJarItem::properties
    );

    public static final DeferredBlock<TableBlock> TABLE = withItem(BLOCKS.registerBlock("table", TableBlock::new, ModBlocks::tableProperties));
    public static final DeferredBlock<ResearchTableBlock> RESEARCH_TABLE = BLOCKS.registerBlock("research_table", ResearchTableBlock::new, ModBlocks::tableProperties);
    public static final DeferredBlock<ResearchTablePartBlock> RESEARCH_TABLE_PART = BLOCKS.registerBlock("research_table_part", ResearchTablePartBlock::new, ModBlocks::tableProperties);

    public static final DeferredBlock<JarBlock> WARDED_JAR = withItem(BLOCKS.registerBlock("warded_jar", properties -> new JarBlock(false, properties), ModBlocks::jar));
    public static final DeferredBlock<JarBlock> VOID_JAR = withItem(BLOCKS.registerBlock("void_jar", properties -> new JarBlock(true, properties), ModBlocks::jar));
    public static final DeferredItem<FilledJarItem> FILLED_JAR = ModItems.ITEMS.registerItem(
        "filled_jar",
        properties -> new FilledJarItem(WARDED_JAR.get(), properties),
        properties -> properties.stacksTo(1)
    );
    public static final DeferredItem<FilledJarItem> FILLED_VOID_JAR = ModItems.ITEMS.registerItem(
        "filled_void_jar",
        properties -> new FilledJarItem(VOID_JAR.get(), properties),
        properties -> properties.stacksTo(1)
    );
    public static final DeferredBlock<TubeBlock> ESSENTIA_TUBE = tube("essentia_tube", TubeBlock.Kind.TUBE);
    public static final DeferredBlock<TubeBlock> ESSENTIA_VALVE = tube("essentia_valve", TubeBlock.Kind.VALVE);
    public static final DeferredBlock<TubeBlock> FILTERED_ESSENTIA_TUBE = tube("filtered_essentia_tube", TubeBlock.Kind.FILTER);
    public static final DeferredBlock<TubeBlock> RESTRICTED_ESSENTIA_TUBE = tube("restricted_essentia_tube", TubeBlock.Kind.RESTRICT);
    public static final DeferredBlock<TubeBlock> DIRECTIONAL_ESSENTIA_TUBE = tube("directional_essentia_tube", TubeBlock.Kind.ONEWAY);
    public static final DeferredBlock<EssentiaBufferBlock> ESSENTIA_BUFFER = withItem(BLOCKS.registerBlock("essentia_buffer", EssentiaBufferBlock::new, ModBlocks::tubeProperties));
    public static final DeferredBlock<CentrifugeBlock> ALCHEMICAL_CENTRIFUGE = withItem(BLOCKS.registerBlock("alchemical_centrifuge", CentrifugeBlock::new, ModBlocks::tubeProperties));
    public static final DeferredBlock<EssentiaCrystallizerBlock> ESSENTIA_CRYSTALLIZER = withItem(BLOCKS.registerBlock(
        "essentia_crystallizer",
        EssentiaCrystallizerBlock::new,
        ModBlocks::tubeProperties
    ));
    public static final DeferredBlock<EssentiaReservoirBlock> ESSENTIA_RESERVOIR = withItem(BLOCKS.registerBlock(
        "essentia_reservoir",
        EssentiaReservoirBlock::new,
        properties -> metal(properties, 2.0F, 17.0F).noOcclusion()
    ));

    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = withItem(BLOCKS.registerBlock("crucible", CrucibleBlock::new, ModBlocks::metalDevice));
    public static final DeferredBlock<AlembicBlock> ALEMBIC = withItem(BLOCKS.registerBlock("alembic", AlembicBlock::new, ModBlocks::metalDevice));
    public static final DeferredBlock<Block> ALCHEMICAL_CONSTRUCT = withItem(BLOCKS.registerSimpleBlock(
        "alchemical_construct",
        properties -> metal(properties, 3.0F, 17.0F)
    ));
    public static final DeferredBlock<Block> ADVANCED_ALCHEMICAL_CONSTRUCT = withItem(BLOCKS.registerSimpleBlock(
        "advanced_alchemical_construct",
        properties -> metal(properties, 3.0F, 17.0F).lightLevel(state -> 11)
    ));
    public static final DeferredBlock<ThaumatoriumBlock> THAUMATORIUM = BLOCKS.registerBlock(
        "thaumatorium",
        properties -> new ThaumatoriumBlock(false, properties),
        ModBlocks::metalDevice
    );
    public static final DeferredBlock<ThaumatoriumBlock> THAUMATORIUM_TOP = BLOCKS.registerBlock(
        "thaumatorium_top",
        properties -> new ThaumatoriumBlock(true, properties),
        ModBlocks::metalDevice
    );
    public static final DeferredBlock<MnemonicMatrixBlock> MNEMONIC_MATRIX = withItem(BLOCKS.registerBlock(
        "mnemonic_matrix",
        MnemonicMatrixBlock::new,
        ModBlocks::metalDevice
    ));
    public static final DeferredBlock<AlchemicalFurnaceBlock> ALCHEMICAL_FURNACE = withItem(BLOCKS.registerBlock(
        "alchemical_furnace",
        AlchemicalFurnaceBlock::new,
        properties -> rock(properties, 3.0F, 25.0F).lightLevel(AlchemicalFurnaceBlock::lightLevel)
    ));
    public static final DeferredBlock<AdvancedAlchemicalFurnaceBlock> ADVANCED_ALCHEMICAL_FURNACE = BLOCKS.registerBlock(
        "advanced_alchemical_furnace",
        AdvancedAlchemicalFurnaceBlock::new,
        properties -> metalDevice(properties).lightLevel(AdvancedAlchemicalFurnaceBlock::lightLevel)
    );
    public static final DeferredBlock<FluxGasBlock> FLUX_GAS = withItem(BLOCKS.registerBlock(
        "flux_gas",
        FluxGasBlock::new,
        properties -> properties
            .mapColor(MapColor.COLOR_PURPLE)
            .replaceable()
            .noCollision()
            .noOcclusion()
            .strength(100.0F)
            .noLootTable()
            .randomTicks()
            .lightLevel(state -> state.getValue(FluxBlock.LEVEL) * 7 / 8)
            .pushReaction(PushReaction.DESTROY)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, box) -> false)
    ));
    public static final DeferredBlock<FluxGooBlock> FLUX_GOO = withItem(BLOCKS.registerBlock(
        "flux_goo",
        FluxGooBlock::new,
        properties -> properties
            .mapColor(MapColor.COLOR_PURPLE)
            .replaceable()
            .noCollision()
            .noOcclusion()
            .strength(100.0F)
            .noLootTable()
            .randomTicks()
            .lightLevel(state -> state.getValue(FluxBlock.LEVEL) * 7 / 8)
            .sound(SoundType.SLIME_BLOCK)
            .pushReaction(PushReaction.DESTROY)
    ));
    public static final DeferredBlock<FluxScrubberBlock> FLUX_SCRUBBER = withItem(BLOCKS.registerBlock(
        "flux_scrubber",
        FluxScrubberBlock::new,
        properties -> stoneDevice(properties)
    ));
    public static final DeferredBlock<BellowsBlock> ARCANE_BELLOWS = withItem(BLOCKS.registerBlock(
        "arcane_bellows",
        BellowsBlock::new,
        properties -> wood(properties, 2.5F, 10.0F).noOcclusion()
    ));

    public static final DeferredBlock<ArcaneWorkbenchBlock> ARCANE_WORKTABLE = withItem(BLOCKS.registerBlock(
        "arcane_worktable",
        ArcaneWorkbenchBlock::new,
        ModBlocks::tableProperties
    ));
    public static final DeferredBlock<DeconstructionTableBlock> DECONSTRUCTION_TABLE = withItem(BLOCKS.registerBlock(
        "deconstruction_table",
        DeconstructionTableBlock::new,
        ModBlocks::tableProperties
    ));
    public static final DeferredBlock<VisChargeRelayBlock> VIS_CHARGE_RELAY = withItem(BLOCKS.registerBlock(
        "vis_charge_relay",
        VisChargeRelayBlock::new,
        ModBlocks::metalDevice
    ));

    public static final DeferredBlock<InfusionMatrixBlock> RUNIC_MATRIX = withItem(BLOCKS.registerBlock(
        "runic_matrix",
        InfusionMatrixBlock::new,
        properties -> stoneDevice(properties).lightLevel(state -> 10)
    ));
    public static final DeferredBlock<PedestalBlock> ARCANE_PEDESTAL = withItem(BLOCKS.registerBlock(
        "arcane_pedestal",
        PedestalBlock::new,
        properties -> stoneDevice(properties)
    ));
    public static final DeferredBlock<InfusionPillarBlock> INFUSION_PILLAR = BLOCKS.registerBlock(
        "infusion_pillar",
        properties -> new InfusionPillarBlock(false, properties),
        properties -> stoneDevice(properties)
    );
    public static final DeferredBlock<InfusionPillarBlock> INFUSION_PILLAR_TOP = BLOCKS.registerBlock(
        "infusion_pillar_top",
        properties -> new InfusionPillarBlock(true, properties),
        properties -> stoneDevice(properties)
    );

    public static final Map<String, DeferredBlock<CandleBlock>> CANDLES = candles();

    private ModBlocks() {
    }

    private static Map<String, DeferredBlock<CandleBlock>> candles() {
        Map<String, DeferredBlock<CandleBlock>> candles = new LinkedHashMap<>();
        for (String color : CANDLE_COLORS) {
            candles.put(color, withItem(BLOCKS.registerBlock(
                color + "_tallow_candle",
                CandleBlock::new,
                properties -> properties
                    .mapColor(MapColor.WOOL)
                    .noCollision()
                    .noOcclusion()
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .lightLevel(state -> 14)
                    .pushReaction(PushReaction.DESTROY)
            )));
        }
        return candles;
    }

    private static DeferredBlock<TubeBlock> tube(String name, TubeBlock.Kind kind) {
        return withItem(BLOCKS.registerBlock(name, properties -> new TubeBlock(kind, properties), ModBlocks::tubeProperties));
    }

    private static BlockBehaviour.Properties metal(BlockBehaviour.Properties properties, float hardness, float resistance) {
        return properties.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(hardness, resistance).sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties metalDevice(BlockBehaviour.Properties properties) {
        return metal(properties, 3.0F, 17.0F).noOcclusion();
    }

    private static BlockBehaviour.Properties tubeProperties(BlockBehaviour.Properties properties) {
        return metal(properties, 0.5F, 5.0F).noOcclusion();
    }

    private static BlockBehaviour.Properties jar(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.NONE).strength(0.3F).sound(SoundType.GLASS).noOcclusion().lightLevel(state -> 9);
    }

    private static BlockBehaviour.Properties tableProperties(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.WOOD)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .ignitedByLava();
    }

    private static <B extends Block> DeferredBlock<B> withItem(DeferredBlock<B> block) {
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    private static <B extends Block> DeferredBlock<B> withItem(
        DeferredBlock<B> block,
        UnaryOperator<Item.Properties> itemProperties
    ) {
        ModItems.ITEMS.registerSimpleBlockItem(block, itemProperties);
        return block;
    }

    private static DeferredBlock<InfusedStoneBlock> infusedStone(String name, int color) {
        return withItem(BLOCKS.registerBlock(name, properties -> new InfusedStoneBlock(color, properties), properties -> rock(properties, 1.5F, 5.0F)));
    }

    private static BlockBehaviour.Properties rock(BlockBehaviour.Properties properties, float hardness, float resistance) {
        return properties
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(hardness, resistance)
            .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties noSpawn(BlockBehaviour.Properties properties) {
        return properties.isValidSpawn((state, level, pos, type) -> false);
    }

    private static BlockBehaviour.Properties obsidian(BlockBehaviour.Properties properties) {
        return rock(properties, 30.0F, 999.0F).mapColor(MapColor.COLOR_BLACK);
    }

    private static BlockBehaviour.Properties stoneDevice(BlockBehaviour.Properties properties) {
        return rock(properties, 3.0F, 25.0F).noOcclusion();
    }

    private static BlockBehaviour.Properties airy(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.NONE)
            .noCollision()
            .noOcclusion()
            .strength(2.0F, 200.0F)
            .sound(SoundType.WOOL)
            .lightLevel(state -> 8)
            .randomTicks()
            .pushReaction(PushReaction.BLOCK)
            .isValidSpawn((state, level, pos, type) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, box) -> false);
    }

    private static BlockBehaviour.Properties amber(BlockBehaviour.Properties properties) {
        return rock(properties, 1.5F, 5.0F).mapColor(MapColor.COLOR_ORANGE).noOcclusion();
    }

    private static BlockBehaviour.Properties wood(BlockBehaviour.Properties properties, float hardness, float resistance) {
        return properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(hardness, resistance).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties planks(BlockBehaviour.Properties properties) {
        return wood(properties, 2.5F, 10.0F);
    }

    private static BlockBehaviour.Properties log(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties leaves(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.PLANT)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos, box) -> false)
            .pushReaction(PushReaction.DESTROY)
            .isRedstoneConductor((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties plant(BlockBehaviour.Properties properties) {
        return properties
            .mapColor(MapColor.PLANT)
            .noCollision()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.NONE)
            .pushReaction(PushReaction.DESTROY);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
