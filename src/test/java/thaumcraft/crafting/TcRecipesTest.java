package thaumcraft.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import thaumcraft.aspect.AspectList;

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

    @Test
    void arcaneExpandsPatternIntoPaddedGrid() {
        TcIngredient stone = TcIngredient.item("minecraft:stone");
        TcIngredient dirt = TcIngredient.item("minecraft:dirt");
        ShapedArcaneRecipe recipe = TcRecipes.arcane(BASE + "arcaneGrid", "testResearch",
            TcStack.of("minecraft:stone"), new AspectList(), new String[] {"AB", " C"},
            'A', stone, 'B', dirt, 'C', dirt);
        assertEquals(2, recipe.width());
        assertEquals(2, recipe.height());
        assertEquals(4, recipe.grid().size());
        assertEquals(stone.getClass(), recipe.grid().get(0).getClass());
        assertSame(TcIngredient.EMPTY, recipe.grid().get(2));
    }

    @Test
    void arcaneThrowsOnUnmappedSymbol() {
        assertThrows(IllegalArgumentException.class, () -> TcRecipes.arcane(BASE + "arcaneMissing", "testResearch",
            TcStack.of("minecraft:stone"), new AspectList(), new String[] {"AX"},
            'A', TcIngredient.item("minecraft:stone")));
    }

    @Test
    void constructionHelpersRegisterUnderTheirKey() {
        AspectList aspects = new AspectList();
        TcStack result = TcStack.of("minecraft:stone");
        TcIngredient ingredient = TcIngredient.item("minecraft:stone");
        ShapedArcaneRecipe arcane = TcRecipes.arcane(BASE + "helperArcane", "testResearch", result, aspects,
            new String[] {"A"}, 'A', ingredient);
        assertSame(arcane, TcRecipes.get(BASE + "helperArcane"));
        ShapelessArcaneRecipe shapeless = TcRecipes.arcaneShapeless(BASE + "helperShapeless", "testResearch",
            result, aspects, ingredient);
        assertSame(shapeless, TcRecipes.get(BASE + "helperShapeless"));
        InfusionRecipe infusion = TcRecipes.infusion(BASE + "helperInfusion", result, 2, aspects, ingredient, ingredient);
        assertEquals(BASE + "helperInfusion", infusion.research());
        CrucibleRecipe crucible = TcRecipes.crucible(BASE + "helperCrucible", result, ingredient, aspects);
        assertEquals(BASE + "helperCrucible", crucible.research());
        InfusionEnchantmentRecipe enchantment = TcRecipes.enchantment(BASE + "helperEnchantment", "testResearch",
            Identifier.parse("minecraft:sharpness"), 1, aspects, ingredient);
        assertSame(enchantment, TcRecipes.get(BASE + "helperEnchantment"));
        CompoundRecipe compound = TcRecipes.compound(BASE + "helperCompound", "testResearch", aspects, 1, 1, 1, result);
        assertSame(compound, TcRecipes.get(BASE + "helperCompound"));
        CraftingRecipeRef vanilla = TcRecipes.vanilla(BASE + "helperVanilla", "testResearch",
            Identifier.parse("minecraft:test_recipe"), result);
        assertSame(vanilla, TcRecipes.get(BASE + "helperVanilla"));
    }

    @Test
    void constructionHelpersRejectDuplicateKeys() {
        TcIngredient ingredient = TcIngredient.item("minecraft:stone");
        TcStack result = TcStack.of("minecraft:stone");
        AspectList aspects = new AspectList();
        TcRecipes.infusion(BASE + "helperDuplicate", result, 1, aspects, ingredient);
        assertThrows(IllegalStateException.class, () -> TcRecipes.infusion(BASE + "helperDuplicate", result, 1,
            aspects, ingredient));
    }
}
