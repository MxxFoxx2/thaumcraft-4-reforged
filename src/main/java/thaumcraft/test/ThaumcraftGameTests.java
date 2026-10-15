package thaumcraft.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.WandPedestalBlockEntity;
import thaumcraft.node.NodeBlockEntity;
import thaumcraft.node.NodeGenerator;
import thaumcraft.node.NodeType;
import thaumcraft.registry.ModBlocks;
import thaumcraft.network.PlayerCompletePayload;
import thaumcraft.network.ScanPayload;
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

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ThaumcraftGameTests {
    private ThaumcraftGameTests() {
    }

    public static void register(IEventBus bus) {
    }

    private static ServerPlayer mockServerPlayer(GameTestHelper helper) {
        return TestSupport.mockServerPlayer(helper);
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

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void researchWarpSplit(GameTestHelper helper) {
        testResearch("TESTWARP", 5);
        ServerPlayer player = mockServerPlayer(helper);
        helper.assertTrue(ResearchManager.complete(player, "TESTWARP"), "research not completed");
        helper.assertTrue(!ResearchManager.complete(player, "TESTWARP"), "research completed twice");
        helper.assertValueEqual(Warp.get(player, WarpType.PERMANENT), 3, "permanent warp");
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), 2, "sticky warp");
        helper.assertValueEqual(Warp.total(player), Knowledge.get(player).getWarpCounter(), "warp counter");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void researchPurchase(GameTestHelper helper) {
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

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void modernScanTargets(GameTestHelper helper) {
        for (String id : List.of("deepslate", "tuff_brick_wall", "waxed_oxidized_cut_copper_stairs", "lime_concrete", "echo_shard")) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(id));
            ScanManager.Target target = ScanManager.resolveStack(new ItemStack(item));
            helper.assertTrue(target != null && target.aspects() != null && target.aspects().size() > 0, "no aspects for " + id);
        }
        for (String id : List.of("axolotl", "warden", "husk")) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.withDefaultNamespace(id));
            Entity entity = helper.spawn(type, new BlockPos(1, 3, 1));
            ScanManager.Target target = ScanManager.resolveEntity(entity);
            helper.assertTrue(target != null && target.aspects() != null && target.aspects().size() > 0, "no aspects for " + id);
            entity.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void scanAwardCaps(GameTestHelper helper) {
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

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void scanFluidTargets(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        helper.getLevel().setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        ScanManager.Target water = ScanManager.resolveBlock(helper.getLevel(), pos);
        helper.assertTrue(water != null && water.aspects() != null, "water must be scannable without an item");
        helper.assertValueEqual(water.aspects().size(), 1, "water aspect count");
        helper.assertValueEqual(water.aspects().getAmount(Aspect.WATER), 4, "water aspects");
        helper.assertTrue(!water.key().equals(ScanManager.itemKey(new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET))), "water is not a bucket");
        int before = Knowledge.get(player).getAspectPool(Aspect.WATER);
        ScanPayload payload = new ScanPayload(ScanManager.KIND_BLOCK, pos, 0);
        ScanManager.handleScan(payload, player);
        helper.assertTrue(ScanManager.hasBeenScanned(player, water), "water scan must be saved");
        helper.assertValueEqual(Knowledge.get(player).getAspectPool(Aspect.WATER), before + 4, "water scan award");
        ScanManager.handleScan(payload, player);
        helper.assertValueEqual(Knowledge.get(player).getAspectPool(Aspect.WATER), before + 4, "water must not award twice");
        helper.getLevel().setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 3));
        helper.assertTrue(ScanManager.hasBeenScanned(player, ScanManager.resolveBlock(helper.getLevel(), pos)), "flowing water must share the scan");
        helper.getLevel().setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        ScanManager.Target lava = ScanManager.resolveBlock(helper.getLevel(), pos);
        helper.assertTrue(lava != null && lava.aspects() != null, "lava must be scannable without an item");
        helper.assertValueEqual(lava.aspects().size(), 2, "lava aspect count");
        helper.assertValueEqual(lava.aspects().getAmount(Aspect.FIRE), 3, "lava fire aspects");
        helper.assertValueEqual(lava.aspects().getAmount(Aspect.EARTH), 1, "lava earth aspects");
        helper.assertTrue(!ScanManager.hasBeenScanned(player, lava), "water must not mark lava scanned");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void scanBlocksThroughWater(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        BlockPos waterPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos floorPos = waterPos.below();
        helper.getLevel().setBlockAndUpdate(floorPos, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(waterPos, Blocks.WATER.defaultBlockState());
        player.moveTo(waterPos.getX() + 0.5, waterPos.getY() + 0.5, waterPos.getZ() + 0.5, 0.0F, 90.0F);
        var hit = ScanManager.pickBlock(helper.getLevel(), player);
        helper.assertTrue(hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(waterPos), "unscanned water surface must be targetable");
        ScanManager.Target water = ScanManager.resolveBlock(helper.getLevel(), waterPos);
        helper.assertTrue(ScanManager.completeScan(player, water), "water scan must complete");
        hit = ScanManager.pickBlock(helper.getLevel(), player);
        helper.assertTrue(hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(floorPos), "scanned water must not hide the bottom");
        player.setPos(waterPos.getX() + 0.5, waterPos.getY() - player.getEyeHeight() + 0.5, waterPos.getZ() + 0.5);
        hit = ScanManager.pickBlock(helper.getLevel(), player);
        helper.assertTrue(hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(floorPos), "submerged scanner must target the bottom");
        ScanManager.Target floor = ScanManager.resolveChecked(player, new ScanPayload(ScanManager.KIND_BLOCK, floorPos, 0));
        helper.assertTrue(floor != null && floor.aspects() != null, "server must accept the underwater block");
        helper.getLevel().setBlockAndUpdate(waterPos, Blocks.OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true));
        helper.assertTrue(!ScanManager.resolveBlock(helper.getLevel(), waterPos).key().equals(water.key()), "waterlogged blocks must keep their own scan");
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

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void wandTapsNode(GameTestHelper helper) {
        BlockPos nodePos = helper.absolutePos(new BlockPos(3, 3, 3));
        NodeGenerator.createNodeAt(helper.getLevel(), nodePos, NodeType.NORMAL, null, primals(20));
        NodeBlockEntity node = helper.<NodeBlockEntity>getBlockEntity(new BlockPos(3, 3, 3));
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

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void pedestalChargesWand(GameTestHelper helper) {
        NodeGenerator.createNodeAt(helper.getLevel(), helper.absolutePos(new BlockPos(3, 5, 3)), NodeType.NORMAL, null, primals(20));
        helper.setBlock(new BlockPos(1, 2, 1), ModBlocks.WAND_RECHARGE_PEDESTAL.get());
        WandPedestalBlockEntity pedestal = helper.<WandPedestalBlockEntity>getBlockEntity(new BlockPos(1, 2, 1));
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        pedestal.setStack(wand);
        helper.succeedWhen(() -> helper.assertTrue(totalVis(pedestal.getStack()) >= 600, "pedestal did not charge the wand"));
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void nodeJarTrigger(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 3, 3);
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
