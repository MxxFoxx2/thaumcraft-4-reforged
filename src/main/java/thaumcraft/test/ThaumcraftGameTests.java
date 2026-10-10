package thaumcraft.test;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.WandPedestalBlockEntity;
import thaumcraft.node.NodeBlockEntity;
import thaumcraft.node.NodeGenerator;
import thaumcraft.node.NodeType;
import thaumcraft.registry.ModBlocks;
import thaumcraft.network.PlayerCompletePayload;
import thaumcraft.research.Knowledge;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ScanManager;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.Wands;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ThaumcraftGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, Thaumcraft.MODID);
    private static final String[] TESTS = {"wand_taps_node", "pedestal_charges_wand", "node_jar_trigger", "research_warp_split", "research_purchase", "scan_award_caps", "modern_scan_targets"};

    static {
        FUNCTIONS.register(TESTS[0], () -> ThaumcraftGameTests::wandTapsNode);
        FUNCTIONS.register(TESTS[1], () -> ThaumcraftGameTests::pedestalChargesWand);
        FUNCTIONS.register(TESTS[2], () -> ThaumcraftGameTests::nodeJarTrigger);
        FUNCTIONS.register(TESTS[3], () -> ThaumcraftGameTests::researchWarpSplit);
        FUNCTIONS.register(TESTS[4], () -> ThaumcraftGameTests::researchPurchase);
        FUNCTIONS.register(TESTS[5], () -> ThaumcraftGameTests::scanAwardCaps);
        FUNCTIONS.register(TESTS[6], () -> ThaumcraftGameTests::modernScanTargets);
    }

    private ThaumcraftGameTests() {
    }

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Thaumcraft.id("default"),
            new TestEnvironmentDefinition.AllOf()
        );
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, Thaumcraft.id(name));
            event.registerTest(
                Thaumcraft.id(name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Thaumcraft.id("test/platform"), 200, 0, true))
            );
        }
    }

    private static ServerPlayer mockServerPlayer(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        return player;
    }

    private static ResearchItem testResearch(String key, int warp) {
        if (ResearchCategories.get("TESTCAT") == null) {
            ResearchCategories.registerCategory("TESTCAT", Thaumcraft.id("textures/misc/blank.png"), Thaumcraft.id("textures/gui/gui_researchback.png"));
        }
        ResearchItem existing = ResearchCategories.getResearch(key);
        if (existing != null) {
            return existing;
        }
        int column = ResearchCategories.allResearch().size() * 2;
        ResearchItem item = new ResearchItem(key, "TESTCAT", new AspectList().add(Aspect.AIR, 5), column, 0, 1, Thaumcraft.id("textures/misc/blank.png"))
            .setSecondary()
            .setWarp(warp);
        item.registerResearchItem();
        return item;
    }

    private static void researchWarpSplit(GameTestHelper helper) {
        testResearch("TESTWARP", 5);
        ServerPlayer player = mockServerPlayer(helper);
        helper.assertTrue(ResearchManager.complete(player, "TESTWARP"), "research not completed");
        helper.assertTrue(!ResearchManager.complete(player, "TESTWARP"), "research completed twice");
        helper.assertValueEqual(Warp.get(player, WarpType.PERMANENT), 3, "permanent warp");
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), 2, "sticky warp");
        helper.assertValueEqual(Warp.total(player), Knowledge.get(player).getWarpCounter(), "warp counter");
        helper.succeed();
    }

    private static void researchPurchase(GameTestHelper helper) {
        testResearch("TESTBUY", 0);
        ServerPlayer player = mockServerPlayer(helper);
        Knowledge.get(player).setAspectPool(Aspect.AIR, 4);
        ResearchManager.handlePlayerComplete(new PlayerCompletePayload("TESTBUY", ResearchManager.ACTION_PURCHASE), player);
        helper.assertTrue(!ResearchManager.isComplete(player, "TESTBUY"), "bought without enough pool");
        Knowledge.get(player).setAspectPool(Aspect.AIR, 12);
        ResearchManager.handlePlayerComplete(new PlayerCompletePayload("TESTBUY", ResearchManager.ACTION_PURCHASE), player);
        helper.assertTrue(ResearchManager.isComplete(player, "TESTBUY"), "purchase failed");
        helper.assertValueEqual(Knowledge.get(player).getAspectPool(Aspect.AIR), 7, "pool after purchase");
        helper.succeed();
    }

    private static void modernScanTargets(GameTestHelper helper) {
        for (String id : List.of("deepslate", "tuff_brick_wall", "waxed_oxidized_cut_copper_stairs", "lime_concrete", "echo_shard")) {
            Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(id));
            ScanManager.Target target = ScanManager.resolveStack(new ItemStack(item));
            helper.assertTrue(target != null && target.aspects() != null && target.aspects().size() > 0, "no aspects for " + id);
        }
        for (String id : List.of("axolotl", "warden", "husk")) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.withDefaultNamespace(id));
            Entity entity = helper.spawn(type, new BlockPos(1, 2, 1));
            ScanManager.Target target = ScanManager.resolveEntity(entity);
            helper.assertTrue(target != null && target.aspects() != null && target.aspects().size() > 0, "no aspects for " + id);
            entity.discard();
        }
        helper.succeed();
    }

    private static void scanAwardCaps(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        PlayerKnowledge knowledge = Knowledge.get(player);
        knowledge.setAspectPool(Aspect.FIRE, 10);
        helper.assertValueEqual(ResearchManager.awardAspect(player, Aspect.FIRE, 4), 4, "normal award");
        knowledge.setAspectPool(Aspect.FIRE, 100);
        helper.assertValueEqual(ResearchManager.awardAspect(player, Aspect.FIRE, 16), 4, "award above cap");
        knowledge.setAspectPool(Aspect.FIRE, 125);
        helper.assertValueEqual(ResearchManager.awardAspect(player, Aspect.FIRE, 16), 1, "award above 125");
        helper.assertValueEqual(knowledge.getAspectPool(Aspect.FIRE), 126, "pool after awards");
        helper.succeed();
    }

    private static AspectList primals(int amount) {
        AspectList aspects = new AspectList();
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            aspects.add(aspect, amount);
        }
        return aspects;
    }

    private static int totalVis(ItemStack wand) {
        return Wands.getAllVis(wand).visSize();
    }

    private static void wandTapsNode(GameTestHelper helper) {
        BlockPos nodePos = helper.absolutePos(new BlockPos(3, 2, 3));
        NodeGenerator.createNodeAt(helper.getLevel(), nodePos, NodeType.NORMAL, null, primals(20));
        NodeBlockEntity node = helper.getBlockEntity(new BlockPos(3, 2, 3), NodeBlockEntity.class);
        int before = node.getAspects().visSize();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = Wands.create(WandRegistry.CAP_IRON, WandRegistry.ROD_WOOD, false);
        for (int tick = 5; tick <= 50; tick += 5) {
            node.onUsingWandTick(wand, player, tick);
        }
        helper.assertTrue(totalVis(wand) > 0, "wand received no vis");
        helper.assertTrue(node.getAspects().visSize() < before, "node lost no vis");
        helper.assertValueEqual(before - node.getAspects().visSize(), totalVis(wand) / 100, "vis moved");
        helper.succeed();
    }

    private static void pedestalChargesWand(GameTestHelper helper) {
        NodeGenerator.createNodeAt(helper.getLevel(), helper.absolutePos(new BlockPos(3, 4, 3)), NodeType.NORMAL, null, primals(20));
        helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.WAND_RECHARGE_PEDESTAL.get());
        WandPedestalBlockEntity pedestal = helper.getBlockEntity(new BlockPos(1, 1, 1), WandPedestalBlockEntity.class);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        pedestal.setStack(wand);
        helper.succeedWhen(() -> helper.assertTrue(totalVis(pedestal.getStack()) >= 600, "pedestal did not charge the wand"));
    }

    private static void nodeJarTrigger(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 2, 3);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (x != 0 || y != 0 || z != 0) {
                        helper.setBlock(center.offset(x, y, z), Blocks.GLASS);
                    }
                }
                helper.setBlock(center.offset(x, 2, z), Blocks.OAK_SLAB);
            }
        }
        NodeGenerator.createNodeAt(helper.getLevel(), helper.absolutePos(center), NodeType.HUNGRY, null, primals(15));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Knowledge.get(player).addResearch("NODEJAR");
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_SILVERWOOD, false);
        Wands.fill(wand);
        boolean done = WandTriggers.perform(helper.getLevel(), wand, player, helper.absolutePos(center.offset(1, 1, 1)), Direction.UP);
        helper.assertTrue(done, "trigger did not fire");
        helper.assertBlockPresent(ModBlocks.NODE_JAR.get(), center);
        helper.assertBlockPresent(Blocks.AIR, center.offset(1, 1, 1));
        helper.assertValueEqual(Wands.getVis(wand, Aspect.AIR), Wands.getMaxVis(wand) - 7000, "air vis left");
        helper.succeed();
    }
}
