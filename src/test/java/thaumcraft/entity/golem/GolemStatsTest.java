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
}
