package thaumcraft.api.addon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

class AddonContentLoaderTest {
    private static final Identifier RECIPE_FILE = Identifier.parse("testaddon:recipes/one.json");
    private static final Identifier RESEARCH_FILE = Identifier.parse("testaddon:research/one.json");
    private static final String CATEGORY = "test_json_category";

    private static String recipeJson(String key) {
        return """
            {
              "key": "%s",
              "type": "arcane_shaped",
              "research": "TESTRESEARCH",
              "result": {"item": "minecraft:stone", "count": 2},
              "aspects": {"ignis": 5, "ordo": 5},
              "pattern": ["AB", " C"],
              "symbols": {
                "A": {"item": "minecraft:iron_ingot"},
                "B": {"tag": "c:ingots"},
                "C": {"item": "minecraft:lever"}
              }
            }
            """.formatted(key);
    }

    private static String researchJson(String key, int column) {
        return """
            {
              "key": "%s",
              "category": "%s",
              "column": %d,
              "row": 7,
              "complexity": 2,
              "icon": {"item": "minecraft:stone"},
              "tags": {"ignis": 3},
              "flags": ["CONCEALED"],
              "pages": [
                {"text": "1"},
                {"recipes": ["testaddon:one"]}
              ]
            }
            """.formatted(key, CATEGORY, column);
    }

    @Test
    void recipeJsonRegistersWithExpandedGrid() {
        Map<Identifier, String> files = Map.of(RECIPE_FILE, recipeJson("testaddon:one"));
        assertEquals(1, AddonContentLoader.loadRecipes(files));
        ShapedArcaneRecipe recipe = (ShapedArcaneRecipe) TcRecipes.get("testaddon:one");
        assertNotNull(recipe);
        assertEquals(2, recipe.width());
        assertEquals(2, recipe.height());
        assertEquals(4, recipe.grid().size());
        assertSame(TcIngredient.EMPTY, recipe.grid().get(2));
        assertEquals(2, recipe.result().count());
    }

    @Test
    void recipeReloadReplacesWithoutThrowing() {
        Map<Identifier, String> files = Map.of(RECIPE_FILE, recipeJson("testaddon:reload"));
        assertEquals(1, AddonContentLoader.loadRecipes(files));
        assertEquals(1, AddonContentLoader.loadRecipes(files));
        assertNotNull(TcRecipes.get("testaddon:reload"));
    }

    @Test
    void recipeCollidingWithCodeRegistrationIsSkipped() {
        TcRecipes.registerUnique(new CrucibleRecipe("testaddon:collision", "TESTRESEARCH",
            TcStack.of("minecraft:stone"), TcIngredient.item("minecraft:stone"), new AspectList()));
        Map<Identifier, String> files = Map.of(RECIPE_FILE, recipeJson("testaddon:collision"));
        assertEquals(0, AddonContentLoader.loadRecipes(files));
        assertSame(CrucibleRecipe.class, TcRecipes.get("testaddon:collision").getClass());
    }

    @Test
    void malformedJsonIsSkippedWithoutThrowing() {
        Map<Identifier, String> files = Map.of(RECIPE_FILE, "{ not valid");
        assertEquals(0, AddonContentLoader.loadRecipes(files));
        assertNull(TcRecipes.get("testaddon:one.json"));
    }

    @Test
    void unknownAspectIsSkipped() {
        String json = recipeJson("testaddon:badaspect").replace("\"ignis\": 5", "\"nosuchaspect\": 5");
        Map<Identifier, String> files = Map.of(RECIPE_FILE, json);
        assertEquals(0, AddonContentLoader.loadRecipes(files));
        assertNull(TcRecipes.get("testaddon:badaspect"));
    }

    @Test
    void researchJsonRegistersWithFlagsAndPages() {
        ResearchCategories.registerCategory(CATEGORY,
            Identifier.parse("thaumcraft:test_icon"), Identifier.parse("thaumcraft:test_background"));
        Map<Identifier, String> files = Map.of(RESEARCH_FILE, researchJson("testaddon:test_research_a", 3));
        assertEquals(1, AddonContentLoader.loadResearch(files));
        ResearchItem entry = ResearchCategories.get(CATEGORY).get("testaddon:test_research_a");
        assertNotNull(entry);
        assertTrue(entry.isConcealed());
        assertEquals(2, entry.pages().size());
        assertSame(ResearchPage.Text.class, entry.pages().get(0).getClass());
        assertEquals("tc.research_page.testaddon:test_research_a.1",
            ((ResearchPage.Text) entry.pages().get(0)).text());
        assertFalse(entry.parentsHidden().contains("INFUSION"));
    }

    @Test
    void researchReloadIsSkipped() {
        ResearchCategories.registerCategory(CATEGORY,
            Identifier.parse("thaumcraft:test_icon"), Identifier.parse("thaumcraft:test_background"));
        Map<Identifier, String> files = Map.of(RESEARCH_FILE, researchJson("testaddon:test_research_b", 4));
        assertEquals(1, AddonContentLoader.loadResearch(files));
        assertEquals(0, AddonContentLoader.loadResearch(files));
        assertNotNull(ResearchCategories.get(CATEGORY).get("testaddon:test_research_b"));
    }
}
