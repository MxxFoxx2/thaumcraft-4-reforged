package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectList;

public final class TcRecipes {
    private static final Map<String, TcRecipe> RECIPES = new LinkedHashMap<>();

    private TcRecipes() {
    }

    public static <T extends TcRecipe> T register(T recipe) {
        validate(recipe);
        TcRecipe previous = RECIPES.put(recipe.key(), recipe);
        if (previous != null) {
            Thaumcraft.LOGGER.warn("Recipe key '{}' was silently replaced: {} -> {}."
                + " Use TcRecipes.replace for deliberate overwrites or registerUnique to reject duplicates.",
                recipe.key(), previous.getClass().getName(), recipe.getClass().getName());
        }
        return recipe;
    }

    public static <T extends TcRecipe> T registerUnique(T recipe) {
        validate(recipe);
        TcRecipe previous = RECIPES.putIfAbsent(recipe.key(), recipe);
        if (previous != null) {
            throw new IllegalStateException("Recipe key '" + recipe.key() + "' is already registered by "
                + previous.getClass().getName() + "; refusing " + recipe.getClass().getName()
                + " (use TcRecipes.replace to overwrite deliberately)");
        }
        return recipe;
    }

    public static <T extends TcRecipe> T replace(T recipe) {
        validate(recipe);
        RECIPES.put(recipe.key(), recipe);
        return recipe;
    }

    private static void validate(TcRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(recipe.key(), "recipe key (recipe class " + recipe.getClass().getName() + ")");
        if (recipe.key().isBlank()) {
            throw new IllegalArgumentException("Recipe key must not be blank (recipe class "
                + recipe.getClass().getName() + ")");
        }
    }

    public static ShapedArcaneRecipe arcane(String key, String research, TcStack result, AspectList aspects,
        String[] pattern, Object... symbols) {
        List<TcIngredient> grid = new ArrayList<>();
        int width = 0;
        for (String row : pattern) {
            width = Math.max(width, row.length());
        }
        for (String row : pattern) {
            for (int column = 0; column < width; column++) {
                grid.add(column < row.length() ? symbol(row.charAt(column), symbols) : TcIngredient.EMPTY);
            }
        }
        return registerUnique(new ShapedArcaneRecipe(key, research, result, aspects, width, pattern.length, grid));
    }

    public static ShapelessArcaneRecipe arcaneShapeless(String key, String research, TcStack result, AspectList aspects,
        TcIngredient... ingredients) {
        return registerUnique(new ShapelessArcaneRecipe(key, research, result, aspects, List.of(ingredients)));
    }

    public static InfusionRecipe infusion(String key, String research, TcStack result, int instability,
        AspectList aspects, TcIngredient central, TcIngredient... components) {
        return registerUnique(new InfusionRecipe(key, research, result, instability, aspects, central,
            List.of(components)));
    }

    public static InfusionRecipe infusion(String key, TcStack result, int instability, AspectList aspects,
        TcIngredient central, TcIngredient... components) {
        return infusion(key, key, result, instability, aspects, central, components);
    }

    public static InfusionEnchantmentRecipe enchantment(String key, String research, Identifier enchantment,
        int instability, AspectList aspects, TcIngredient... components) {
        return registerUnique(new InfusionEnchantmentRecipe(key, research, enchantment, instability, aspects,
            List.of(components)));
    }

    public static CompoundRecipe compound(String key, String research, AspectList aspects, int width, int height,
        int depth, TcStack... blocks) {
        return registerUnique(new CompoundRecipe(key, research, aspects, width, height, depth, List.of(blocks)));
    }

    public static CrucibleRecipe crucible(String key, TcStack result, TcIngredient catalyst, AspectList aspects) {
        return crucible(key, key, result, catalyst, aspects);
    }

    public static CrucibleRecipe crucible(String key, String research, TcStack result, TcIngredient catalyst,
        AspectList aspects) {
        return registerUnique(new CrucibleRecipe(key, research, result, catalyst, aspects));
    }

    public static CraftingRecipeRef vanilla(String key, String research, Identifier recipe, TcStack result) {
        return registerUnique(new CraftingRecipeRef(key, research, recipe, result));
    }

    private static TcIngredient symbol(char symbol, Object[] symbols) {
        if (symbol == ' ') {
            return TcIngredient.EMPTY;
        }
        for (int index = 0; index + 1 < symbols.length; index += 2) {
            if (symbols[index] instanceof Character key && key == symbol) {
                return (TcIngredient) symbols[index + 1];
            }
        }
        throw new IllegalArgumentException("No ingredient for pattern symbol " + symbol);
    }

    public static @Nullable TcRecipe get(String key) {
        return RECIPES.get(key);
    }

    public static @Nullable TcRecipe remove(String key) {
        return RECIPES.remove(key);
    }

    public static Collection<TcRecipe> all() {
        return Collections.unmodifiableCollection(RECIPES.values());
    }

    public static <T extends TcRecipe> List<T> all(Class<T> type) {
        List<T> matching = new ArrayList<>();
        for (TcRecipe recipe : RECIPES.values()) {
            if (type.isInstance(recipe)) {
                matching.add(type.cast(recipe));
            }
        }
        return matching;
    }
}
