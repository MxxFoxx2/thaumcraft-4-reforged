package thaumcraft.test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import com.mojang.serialization.JsonOps;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.platform.OuterLandsData;
import thaumcraft.world.dim.OuterLands;
import thaumcraft.world.dim.Cell;
import thaumcraft.world.dim.CellLoc;
import thaumcraft.world.dim.MazeGenerator;

@net.neoforged.neoforge.gametest.GameTestHolder(Thaumcraft.MODID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class MazeGameTests {



    private MazeGameTests() {
    }





    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "mazegametests_cellpackroundtrip")
    public static void cellPackRoundTrip(GameTestHelper helper) {
        Cell source = new Cell();
        source.north = true;
        source.east = true;
        source.below = true;
        source.feature = 14;
        Cell result = new Cell(source.pack());
        helper.assertTrue(result.north && !result.south && result.east && !result.west, "horizontal bits");
        helper.assertTrue(!result.above && result.below, "vertical bits");
        helper.assertValueEqual((int) result.feature, 14, "feature bits");
        helper.assertTrue(new CellLoc(3, -4).equals(new CellLoc(3, -4)), "cell coordinate value equality");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "mazegametests_savedstateroundtrip")
    public static void savedStateRoundTrip(GameTestHelper helper) {
        CellLoc location = new CellLoc(4, -7);
        OuterLandsData data = new OuterLandsData();
        OuterLandsData.MazeState maze = new OuterLandsData.MazeState(991L, 4, -7, 15, 17,
            java.util.List.of(new OuterLandsData.CellData(location, (short) 257)), java.util.List.of());
        data.putMaze("4,-7", maze);
        data.putAlias("8,8", "4,-7");
        data.updateMaze("4,-7", maze.withPlaced(location));
        helper.assertValueEqual(maze.withPlaced(location).withPlaced(location).placed().size(), 1, "placed-cell progress deduplicates");
        int boss = data.nextBoss(RandomSource.create(1L));
        helper.assertTrue(boss >= 0 && boss < 4, "boss cycle index is bounded");
        var encoded = OuterLandsData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        OuterLandsData restored = OuterLandsData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        helper.assertTrue(restored.maze("8,8").placed().contains(location), "maze alias and placement survive codec round trip");
        helper.assertTrue(restored.maze("4,-7").seed() == 991L, "maze seed survives codec round trip");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "mazegametests_dimensionkey")
    public static void dimensionKey(GameTestHelper helper) {
        helper.assertTrue(OuterLands.OUTER_LANDS.location().equals(Thaumcraft.id("outer_lands")), "outer dimension registry key");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "mazegametests_sameseedtopology")
    public static void sameSeedTopology(GameTestHelper helper) {
        MazeGenerator first = generator(17, 15, 0x74A11L);
        MazeGenerator second = generator(17, 15, 0x74A11L);
        helper.assertTrue(java.util.Arrays.deepEquals(first.grid, second.grid), "same seed produced different topology");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "mazegametests_connectionsreciprocalandreachable")
    public static void connectionsReciprocalAndReachable(GameTestHelper helper) {
        MazeGenerator maze = generator(17, 15, 0x13A2L);
        Set<CellLoc> connected = new HashSet<>();
        ArrayDeque<CellLoc> pending = new ArrayDeque<>();
        int startX = -1, startZ = -1;
        for (int z = 0; z < maze.height && startX < 0; z++) for (int x = 0; x < maze.width; x++) {
            if (maze.grid[z][x] > 0) { startX = x; startZ = z; break; }
        }
        helper.assertTrue(startX >= 0, "maze has no cells");
        pending.add(new CellLoc(startX, startZ));
        while (!pending.isEmpty()) {
            CellLoc loc = pending.removeFirst();
            if (!connected.add(loc)) continue;
            Cell cell = new Cell((short) maze.grid[loc.z()][loc.x()]);
            if (cell.feature >= 2 && cell.feature <= 5) {
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                    int x = loc.x() + dx, z = loc.z() + dz;
                    if (x >= 0 && z >= 0 && x < maze.width && z < maze.height) {
                        Cell roomPart = new Cell((short) maze.grid[z][x]);
                        if (roomPart.feature >= 2 && roomPart.feature <= 5) pending.add(new CellLoc(x, z));
                    }
                }
            }
            if (cell.north) checkConnection(helper, maze, connected, pending, loc, 0, -1, true);
            if (cell.south) checkConnection(helper, maze, connected, pending, loc, 0, 1, false);
            if (cell.east) checkConnection(helper, maze, connected, pending, loc, 1, 0, false);
            if (cell.west) checkConnection(helper, maze, connected, pending, loc, -1, 0, true);
        }
        int cells = 0;
        boolean portalReachable = false;
        boolean bossRoomReachable = false;
        boolean keyReachable = false;
        for (CellLoc loc : connected) {
            Cell cell = new Cell((short) maze.grid[loc.z()][loc.x()]);
            if (cell.feature == 1) portalReachable = true;
            if (cell.feature >= 2 && cell.feature <= 5) bossRoomReachable = true;
            if (cell.feature == 6) keyReachable = true;
        }
        for (int[] row : maze.grid) for (int value : row) if (value > 0) cells++;
        helper.assertValueEqual(connected.size(), cells, "all maze cells are reachable through reciprocal passages");
        helper.assertTrue(portalReachable, "portal is reachable");
        helper.assertTrue(bossRoomReachable, "boss room is reachable");
        helper.assertTrue(keyReachable, "key room is reachable");
        helper.succeed();
    }

    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "mazegametests_chunkboundaryconnections")
    public static void chunkBoundaryConnections(GameTestHelper helper) {
        MazeGenerator maze = generator(17, 15, 0x13A2L);
        int originX = -(maze.width / 2);
        int originZ = -(maze.height / 2);
        boolean crossedChunkZero = false;
        for (int z = 0; z < maze.height; z++) {
            for (int x = 0; x < maze.width; x++) {
                Cell cell = new Cell((short) maze.grid[z][x]);
                int chunkX = originX + x;
                int chunkZ = originZ + z;
                if (cell.east) {
                    if (chunkX == -1) {
                        helper.assertTrue(originX + x + 1 == 0, "east passage was not placed between chunks -1 and 0");
                        helper.assertTrue(new Cell((short) maze.grid[z][x + 1]).west, "east boundary passage lacks reciprocal west link");
                        crossedChunkZero = true;
                    }
                }
                if (cell.south) {
                    if (chunkZ == -1) {
                        helper.assertTrue(originZ + z + 1 == 0, "south passage was not placed between chunks -1 and 0");
                        helper.assertTrue(new Cell((short) maze.grid[z + 1][x]).north, "south boundary passage lacks reciprocal north link");
                        crossedChunkZero = true;
                    }
                }
            }
        }
        helper.assertTrue(crossedChunkZero, "connected maze never crossed the chunk-coordinate zero boundary");
        helper.succeed();
    }

    private static MazeGenerator generator(int width, int height, long seed) {
        MazeGenerator maze = new MazeGenerator(width, height, seed);
        long retry = seed;
        while (!maze.generate()) maze = new MazeGenerator(width, height, ++retry);
        return maze;
    }

    private static void checkConnection(GameTestHelper helper, MazeGenerator maze, Set<CellLoc> connected,
        ArrayDeque<CellLoc> pending, CellLoc from, int dx, int dz, boolean northOrWest) {
        int x = from.x() + dx;
        int z = from.z() + dz;
        helper.assertTrue(x >= 0 && z >= 0 && x < maze.width && z < maze.height, "passage escaped maze boundary");
        Cell target = new Cell((short) maze.grid[z][x]);
        boolean reciprocal = northOrWest ? (dz == -1 ? target.south : target.east) : (dz == 1 ? target.north : target.west);
        helper.assertTrue(reciprocal, "passage is not reciprocal");
        pending.add(new CellLoc(x, z));
    }
}
