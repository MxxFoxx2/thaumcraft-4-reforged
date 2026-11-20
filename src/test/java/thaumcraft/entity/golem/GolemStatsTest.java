package thaumcraft.entity.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GolemStatsTest {
    @Test
    void golemTypeHasOnlyBuiltInMaterials() {
        assertEquals(8, GolemType.values().length);
        assertEquals("STRAW", GolemType.values()[0].name());
        assertEquals("THAUMIUM", GolemType.values()[7].name());
    }

    @Test
    void statsMatchGolemTypeCharacteristics() {
        GolemStats wood = GolemStats.of(GolemType.WOOD);
        assertEquals(20, wood.health());
        assertEquals(6, wood.armor());
        assertEquals(0.35F, wood.speed());
        assertFalse(wood.fireResist());
        assertEquals(1, wood.upgrades());
        assertEquals(4, wood.carry());
        assertEquals(75, wood.regenDelay());
        assertEquals(1, wood.strength());
        assertEquals(0, wood.visCost());
        assertFalse(wood.heavy());

        GolemStats iron = GolemStats.of(GolemType.IRON);
        assertEquals(35, iron.health());
        assertEquals(15, iron.armor());
        assertTrue(iron.heavy());
        assertEquals(32, iron.carry());

        GolemStats thaumium = GolemStats.of(GolemType.THAUMIUM);
        assertTrue(thaumium.fireResist());
        assertEquals(40, thaumium.health());
    }

    @Test
    void heavyMarksWaterCapableGolems() {
        for (GolemType type : GolemType.values()) {
            assertTrue(GolemStats.of(type).heavy() == (type.ordinal() >= GolemType.STONE.ordinal()),
                type.name() + " heavy mismatch");
        }
    }

    @Test
    void allEightTypesMatchTheirCharacteristics() {
        Object[][] expected = {
            {"STRAW", 10, 0, 0.38F, false, 1, 1, 75, 0},
            {"WOOD", 20, 6, 0.35F, false, 1, 4, 75, 1},
            {"TALLOW", 20, 9, 0.33F, false, 2, 8, 75, 2},
            {"CLAY", 25, 9, 0.33F, true, 1, 8, 100, 2},
            {"FLESH", 15, 6, 0.35F, false, 2, 4, 40, 1},
            {"STONE", 30, 12, 0.32F, true, 1, 16, 100, 3},
            {"IRON", 35, 15, 0.31F, true, 1, 32, 125, 4},
            {"THAUMIUM", 40, 15, 0.32F, true, 2, 32, 100, 4},
        };
        for (Object[] row : expected) {
            GolemStats stats = GolemStats.of(GolemType.valueOf((String) row[0]));
            assertEquals(row[1], stats.health(), row[0] + " health");
            assertEquals(row[2], stats.armor(), row[0] + " armor");
            assertEquals((float) row[3], stats.speed(), row[0] + " speed");
            assertEquals(row[4], stats.fireResist(), row[0] + " fireResist");
            assertEquals(row[5], stats.upgrades(), row[0] + " upgrades");
            assertEquals(row[6], stats.carry(), row[0] + " carry");
            assertEquals(row[7], stats.regenDelay(), row[0] + " regenDelay");
            assertEquals(row[8], stats.strength(), row[0] + " strength");
            assertEquals(0, stats.visCost(), row[0] + " visCost");
        }
    }

    @Test
    void golemStatsDefaultsFollowThePassedType() {
        assertEquals(GolemStats.of(GolemType.STRAW), GolemStats.of(GolemType.STRAW));
        assertFalse(GolemStats.of(GolemType.STRAW).equals(GolemStats.of(GolemType.WOOD)));
        assertEquals(GolemType.STRAW.health, GolemStats.of(GolemType.STRAW).health());
        assertEquals(GolemType.THAUMIUM.upgrades, GolemStats.of(GolemType.THAUMIUM).upgrades());
    }
}
