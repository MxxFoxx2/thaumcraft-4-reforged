package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;

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

    public static @Nullable TcRecipe get(String key) {
        return RECIPES.get(key);
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
