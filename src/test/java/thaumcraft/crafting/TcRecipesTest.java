package thaumcraft.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class TcRecipesTest {
    private static final String BASE = "test:";

    private static CraftingRecipeRef recipe(String key) {
        return new CraftingRecipeRef(BASE + key, "testResearch", Identifier.parse("thaumcraft:test_recipe"),
            TcStack.of("minecraft:stone"));
    }

    @Test
    void registerStoresRecipeUnderItsKey() {
        CraftingRecipeRef recipe = recipe("registerStores");
        assertSame(recipe, TcRecipes.register(recipe));
        assertSame(recipe, TcRecipes.get(BASE + "registerStores"));
    }

    @Test
    void registerSilentlyReplacesAndKeepsLatestRecipe() {
        CraftingRecipeRef first = recipe("silentReplace");
        CraftingRecipeRef second = recipe("silentReplace");
        TcRecipes.register(first);
        TcRecipes.register(second);
        assertSame(second, TcRecipes.get(BASE + "silentReplace"));
        assertNotSame(first, TcRecipes.get(BASE + "silentReplace"));
    }

    @Test
    void registerUniqueAcceptsTheFirstRecipe() {
        CraftingRecipeRef recipe = recipe("uniqueFirst");
        assertSame(recipe, TcRecipes.registerUnique(recipe));
        assertSame(recipe, TcRecipes.get(BASE + "uniqueFirst"));
    }

    @Test
    void registerUniqueThrowsOnDuplicateKeyAndKeepsTheOriginal() {
        CraftingRecipeRef first = recipe("uniqueDuplicate");
        CraftingRecipeRef second = recipe("uniqueDuplicate");
        TcRecipes.registerUnique(first);
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> TcRecipes.registerUnique(second));
        assertSame(first, TcRecipes.get(BASE + "uniqueDuplicate"));
        String message = thrown.getMessage();
        assertNotNull(message);
        assertEquals(true, message.contains(BASE + "uniqueDuplicate"));
        assertEquals(true, message.contains(first.getClass().getName()));
        assertEquals(true, message.contains(second.getClass().getName()));
    }

    @Test
    void replaceOverwritesDeliberately() {
        CraftingRecipeRef first = recipe("deliberateReplace");
        CraftingRecipeRef second = recipe("deliberateReplace");
        TcRecipes.register(first);
        assertSame(second, TcRecipes.replace(second));
        assertSame(second, TcRecipes.get(BASE + "deliberateReplace"));
    }

    @Test
    void registerRejectsMissingOrBlankKeys() {
        CraftingRecipeRef blankKey = new CraftingRecipeRef(" ", "testResearch",
            Identifier.parse("thaumcraft:test_recipe"), TcStack.of("minecraft:stone"));
        assertThrows(IllegalArgumentException.class, () -> TcRecipes.register(blankKey));
        assertThrows(IllegalArgumentException.class, () -> TcRecipes.registerUnique(blankKey));
        assertThrows(IllegalArgumentException.class, () -> TcRecipes.replace(blankKey));
        CraftingRecipeRef nullKey = new CraftingRecipeRef(null, "testResearch",
            Identifier.parse("thaumcraft:test_recipe"), TcStack.of("minecraft:stone"));
        assertThrows(NullPointerException.class, () -> TcRecipes.register(nullKey));
        assertThrows(NullPointerException.class, () -> TcRecipes.registerUnique(nullKey));
        assertThrows(NullPointerException.class, () -> TcRecipes.replace(nullKey));
        assertThrows(NullPointerException.class, () -> TcRecipes.register(null));
    }

    @Test
    void rejectedRegistrationsLeaveTheRegistryUntouched() {
        CraftingRecipeRef existing = recipe("rejected");
        TcRecipes.register(existing);
        CraftingRecipeRef blankKey = new CraftingRecipeRef(" ", "testResearch",
            Identifier.parse("thaumcraft:test_recipe"), TcStack.of("minecraft:stone"));
        assertThrows(IllegalArgumentException.class, () -> TcRecipes.register(blankKey));
        assertSame(existing, TcRecipes.get(BASE + "rejected"));
        assertNull(TcRecipes.get(" "));
    }
}
