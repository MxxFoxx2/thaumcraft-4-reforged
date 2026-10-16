package thaumcraft.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.eldritch.EldritchBlockEntity;
import thaumcraft.item.tool.FollowingItems;
import thaumcraft.registry.EldritchContent;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModItems;

@net.neoforged.neoforge.gametest.GameTestHolder(Thaumcraft.MODID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class PortBugGameTests {







    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        player.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, CommonListenerCookie.createInitial(player.getGameProfile(), false)) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        helper.getLevel().addNewPlayer(player);

        return player;
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "portbuggametests_naturalspawnsupport")
    public static void naturalSpawnSupport(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        for (var type : List.of(ModEntities.ELDRITCH_GUARDIAN.get(), ModEntities.INHABITED_ZOMBIE.get())) {
            for (var floor : List.of(Blocks.AIR, ModBlocks.ANCIENT_STONE_NO_SPAWN.get(), EldritchContent.ELDRITCH_NOTHING.get())) {
                helper.getLevel().setBlockAndUpdate(pos.below(), floor.defaultBlockState());
                helper.assertTrue(!SpawnPlacements.isSpawnPositionOk(type, helper.getLevel(), pos), "natural spawn allowed without a valid floor: " + floor);
            }
            helper.getLevel().setBlockAndUpdate(pos.below(), ModBlocks.ANCIENT_STONE.get().defaultBlockState());
            helper.assertTrue(SpawnPlacements.isSpawnPositionOk(type, helper.getLevel(), pos), "natural spawn rejected a normal maze floor");
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 100, batch = "portbuggametests_zombiespawnsupport")
    public static void zombieSpawnSupport(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        for (var type : List.of(ModEntities.BRAINY_ZOMBIE.get(), ModEntities.GIANT_BRAINY_ZOMBIE.get())) {
            for (var floor : List.of(Blocks.AIR, Blocks.WATER, Blocks.LAVA)) {
                level.setBlockAndUpdate(pos.below(), floor.defaultBlockState());
                helper.assertTrue(!SpawnPlacements.isSpawnPositionOk(type, level, pos), "zombie spawn allowed above " + floor);
                helper.assertTrue(!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, net.minecraft.util.RandomSource.create(0)), "zombie spawn predicate allowed an invalid floor");
            }
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            helper.assertTrue(SpawnPlacements.isSpawnPositionOk(type, level, pos), "zombie spawn rejected dry solid ground");
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            helper.assertTrue(!SpawnPlacements.isSpawnPositionOk(type, level, pos), "zombie spawn allowed in water above solid ground");
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.above(), Blocks.WATER.defaultBlockState());
            helper.assertTrue(!SpawnPlacements.isSpawnPositionOk(type, level, pos), "zombie spawn allowed with its head underwater");
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 1200, batch = "portbuggametests_altarpopulation")
    public static void altarPopulation(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(16, 2, 16));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, EldritchContent.ELDRITCH_ALTAR.get().defaultBlockState());
        var altar = (EldritchBlockEntity) level.getBlockEntity(pos);
        altar.setSpawner(true);
        altar.setSpawnType((byte) 1);
        var guard = ModEntities.ELDRITCH_GUARDIAN.get().create(level);
        guard.setPos(Vec3.atCenterOf(pos.above()));
        guard.setNoAi(true);
        level.addFreshEntity(guard);
        for (int x = -13; x <= 13; x++) {
            for (int z = -13; z <= 13; z++) {
                level.setBlockAndUpdate(pos.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(pos.offset(x, 5, z), Blocks.STONE.defaultBlockState());
            }
        }
        helper.runAfterDelay(20, () -> {
            for (int i = 0; i < 1000; i++) {
                EldritchBlockEntity.tick(level, pos, level.getBlockState(pos), altar);
            }
            var bounds = new net.minecraft.world.phys.AABB(pos).inflate(20);
            helper.assertValueEqual(level.getEntitiesOfClass(thaumcraft.entity.monster.eldritch.EldritchGuardian.class, bounds).size(), 1, "altar accumulated guardians despite its existing guardian");
            guard.discard();
            for (int i = 0; i < 4000; i++) {
                EldritchBlockEntity.tick(level, pos, level.getBlockState(pos), altar);
            }
            helper.assertValueEqual(level.getEntitiesOfClass(thaumcraft.entity.monster.eldritch.EldritchGuardian.class, bounds).size(), 1, "altar must replace its lost guardian without accumulating more");
            level.getEntitiesOfClass(thaumcraft.entity.monster.eldritch.EldritchGuardian.class, bounds).forEach(net.minecraft.world.entity.Entity::discard);
            helper.succeed();
        });
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/eldritch_regression", timeoutTicks = 1200, batch = "portbuggametests_crusherdrops")
    public static void crusherDrops(GameTestHelper helper) {
        var player = player(helper);
        BlockPos center = helper.absolutePos(new BlockPos(16, 3, 16));
        player.setPos(center.getX() + 0.5, center.getY() - 1, center.getZ() + 5);
        player.setNoGravity(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.PRIMAL_CRUSHER.get()));
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                helper.getLevel().setBlockAndUpdate(center.offset(x, y, 0), Blocks.STONE.defaultBlockState());
            }
        }
        helper.onEachTick(player::aiStep);
        player.gameMode.destroyBlock(center);
        var area = new net.minecraft.world.phys.AABB(center).inflate(3);
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
        helper.assertValueEqual(drops.stream().mapToInt(item -> item.getItem().getCount()).sum(), 9, "crusher must drop exactly nine blocks including the center");
        helper.assertTrue(drops.stream().allMatch(FollowingItems::isFollowing), "every crusher drop must follow the player");
        double initial = drops.stream().mapToDouble(item -> item.distanceToSqr(player)).sum();
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(drops.stream().filter(item -> !item.isRemoved()).mapToDouble(item -> item.distanceToSqr(player)).sum() < initial, "crusher drops must approach the player");
            player.setPos(player.position().add(2, 0, 0));
        });
        helper.runAfterDelay(100, () -> {
            int count = player.getInventory().countItem(Items.COBBLESTONE);
            helper.assertValueEqual(count, 9, "following drops must reach the moving player and be picked up without loss: " + drops.stream().filter(item -> !item.isRemoved()).map(item -> item.position() + " following=" + FollowingItems.isFollowing(item) + " age=" + item.tickCount).toList());
            player.discard();
            helper.succeed();
        });
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "portbuggametests_releasestarget")
    public static void releasesTarget(GameTestHelper helper) {
        var player = player(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        player.setPos(Vec3.atCenterOf(pos.offset(4, 0, 0)));
        var item = new ItemEntity(helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.DIAMOND, 7));
        helper.getLevel().addFreshEntity(item);
        FollowingItems.follow(item, player);
        player.discard();
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!FollowingItems.isFollowing(item) && !item.isNoGravity() && !item.noPhysics, "lost target must restore normal item physics");
            helper.assertValueEqual(item.getItem().getCount(), 7, "releasing target must preserve the stack");
            item.discard();
            helper.succeed();
        });
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "portbuggametests_releasesdimension")
    public static void releasesDimension(GameTestHelper helper) {
        var player = player(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        player.setPos(Vec3.atCenterOf(pos));
        var item = new ItemEntity(helper.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.DIAMOND, 7));
        helper.getLevel().addFreshEntity(item);
        FollowingItems.follow(item, player);
        var outer = helper.getLevel().getServer().getLevel(thaumcraft.world.dim.OuterLands.OUTER_LANDS);
        helper.assertTrue(outer != null, "Outer Lands missing from test dimensions");
        var moved = thaumcraft.platform.Teleport.to(player, outer, new Vec3(8, 80, 8), 0, 0);
        helper.assertTrue(moved != null && moved.level() == outer, "test target did not change dimensions");
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!FollowingItems.isFollowing(item) && !item.isNoGravity() && !item.noPhysics, "dimension change must release the item");
            helper.assertValueEqual(item.getItem().getCount(), 7, "dimension change must preserve the stack");
            item.discard();
            moved.discard();
            helper.succeed();
        });
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "portbuggametests_trunkdimensions")
    public static void trunkDimensions(GameTestHelper helper) {
        var trunk = ModEntities.TRAVELING_TRUNK.get().create(helper.getLevel());
        helper.assertTrue(Math.abs(trunk.getBbWidth() - 0.8F) < 0.0001F && Math.abs(trunk.getBbHeight() - 0.8F) < 0.0001F, "trunk dimensions must match TC4");
        helper.succeed();
    }
}
