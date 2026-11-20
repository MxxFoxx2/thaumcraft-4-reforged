package thaumcraft.api.addon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import thaumcraft.platform.ModNetwork;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.CompoundRecipe;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.ShapelessArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;
import thaumcraft.network.AddonContentPayload;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AddonContentLoader {
    private static final String RECIPES_PATH = "thaumcraft_recipes";
    private static final String RESEARCH_PATH = "thaumcraft_research";
    private static final String PAGE_PREFIX = "tc.research_page.";
    private static final Set<String> RECIPE_KEYS = new HashSet<>();
    private static final Map<String, String> RESEARCH_CATEGORIES = new HashMap<>();
    private static final Map<String, String> RECIPE_TEXTS = new HashMap<>();
    private static final Map<String, String> RESEARCH_TEXTS = new HashMap<>();

    private AddonContentLoader() {
    }

    @SubscribeEvent
    public static void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Thaumcraft.id("addon_content"), (ResourceManagerReloadListener) AddonContentLoader::reload);
    }

    private static void reload(ResourceManager manager) {
        loadRecipes(manager);
        loadResearch(manager);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (RECIPE_TEXTS.isEmpty() && RESEARCH_TEXTS.isEmpty()) {
            return;
        }
        AddonContentPayload payload = new AddonContentPayload(Map.copyOf(RECIPE_TEXTS), Map.copyOf(RESEARCH_TEXTS));
        event.getRelevantPlayers().forEach(player -> ModNetwork.sendToPlayer(player, payload));
    }

    private static String categoryOf(JsonObject json) {
        JsonElement category = json.get("category");
        return category == null ? "" : category.getAsString();
    }

    public static int loadRecipes(ResourceManager manager) {
        return loadRecipes(readTexts(manager, RECIPES_PATH));
    }

    public static int loadResearch(ResourceManager manager) {
        return loadResearch(readTexts(manager, RESEARCH_PATH));
    }

    public static int loadRecipeTexts(Map<String, String> texts) {
        Map<Identifier, String> byId = new java.util.TreeMap<>();
        texts.forEach((key, json) -> byId.put(Identifier.parse(key), json));
        return loadRecipes(byId);
    }

    public static int loadResearchTexts(Map<String, String> texts) {
        Map<Identifier, String> byId = new java.util.TreeMap<>();
        texts.forEach((key, json) -> byId.put(Identifier.parse(key), json));
        return loadResearch(byId);
    }

    public static int loadRecipes(Map<Identifier, String> files) {
        Map<String, JsonObject> parsed = new java.util.TreeMap<>();
        Map<String, Identifier> fileByKey = new java.util.TreeMap<>();
        for (Map.Entry<Identifier, String> entry : new java.util.TreeMap<>(files).entrySet()) {
            Identifier id = entry.getKey();
            JsonObject json = parseJson(id, entry.getValue());
            if (json == null) {
                continue;
            }
            String key = requireKey(id, json);
            if (key == null) {
                continue;
            }
            Identifier previous = fileByKey.putIfAbsent(key, id);
            if (previous != null) {
                Thaumcraft.LOGGER.error("Add-on recipes {} and {} both define key '{}'; the entry from {} is used",
                    previous, id, key, previous);
                continue;
            }
            parsed.put(key, json);
        }
        int removed = 0;
        for (String stale : List.copyOf(RECIPE_KEYS)) {
            if (!parsed.containsKey(stale)) {
                TcRecipes.remove(stale);
                RECIPE_KEYS.remove(stale);
                removed++;
            }
        }
        int loaded = 0;
        for (Map.Entry<String, JsonObject> entry : parsed.entrySet()) {
            if (!registerRecipe(fileByKey.get(entry.getKey()), entry.getKey(), entry.getValue())) {
                continue;
            }
            RECIPE_KEYS.add(entry.getKey());
            RECIPE_TEXTS.put(entry.getKey(), files.get(entry.getKey()));
            loaded++;
        }
        if (loaded + removed > 0) {
            Thaumcraft.LOGGER.info("Loaded {} add-on recipes from JSON ({} removed)", loaded, removed);
        }
        return loaded;
    }

    public static int loadResearch(Map<Identifier, String> files) {
        Map<String, JsonObject> parsed = new java.util.TreeMap<>();
        Map<String, Identifier> fileByKey = new java.util.TreeMap<>();
        for (Map.Entry<Identifier, String> entry : new java.util.TreeMap<>(files).entrySet()) {
            Identifier id = entry.getKey();
            JsonObject json = parseJson(id, entry.getValue());
            if (json == null) {
                continue;
            }
            String key = requireKey(id, json);
            if (key == null) {
                continue;
            }
            Identifier previous = fileByKey.putIfAbsent(key, id);
            if (previous != null) {
                Thaumcraft.LOGGER.error("Add-on research files {} and {} both define key '{}'; the entry from {} is used",
                    previous, id, key, previous);
                continue;
            }
            parsed.put(key, json);
        }
        int removed = 0;
        for (String stale : List.copyOf(RESEARCH_CATEGORIES.keySet())) {
            if (!parsed.containsKey(stale)) {
                ResearchCategories.removeResearch(RESEARCH_CATEGORIES.get(stale), stale);
                RESEARCH_CATEGORIES.remove(stale);
                removed++;
            }
        }
        int loaded = 0;
        for (Map.Entry<String, JsonObject> entry : parsed.entrySet()) {
            if (!registerResearch(fileByKey.get(entry.getKey()), entry.getKey(), entry.getValue())) {
                continue;
            }
            RESEARCH_CATEGORIES.put(entry.getKey(), categoryOf(entry.getValue()));
            RESEARCH_TEXTS.put(entry.getKey(), files.get(entry.getKey()));
            loaded++;
        }
        if (loaded + removed > 0) {
            Thaumcraft.LOGGER.info("Loaded {} add-on research entries from JSON ({} removed)", loaded, removed);
        }
        return loaded;
    }

    private static Map<Identifier, String> readTexts(ResourceManager manager, String path) {
        Map<Identifier, String> texts = new HashMap<>();
        manager.listResources(path, id -> id.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (InputStream stream = resource.open()) {
                texts.put(id, new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception exception) {
                Thaumcraft.LOGGER.error("Add-on content {} could not be read: {}", id, exception.getMessage());
            }
        });
        return texts;
    }

    private static JsonObject parseJson(Identifier id, String text) {
        try {
            return com.google.gson.JsonParser.parseString(text).getAsJsonObject();
        } catch (Exception exception) {
            Thaumcraft.LOGGER.error("Add-on content {} is not valid JSON: {}", id, exception.getMessage());
            return null;
        }
    }

    private static boolean registerRecipe(Identifier id, String key, JsonObject json) {
        try {
            String research = requireResearch(id, json, key);
            if (research == null) {
                return false;
            }
            String type = string(json, "type", id);
            if (type == null) {
                return false;
            }
            TcStack result = stack(json, "result", id);
            if (result == null) {
                return false;
            }
            AspectList aspects = optionalAspects(json, "aspects", id);
            if (aspects == null) {
                return false;
            }
            TcRecipeBuilder builder = new TcRecipeBuilder(id, key, research, result, aspects);
            TcRecipe recipe = switch (type) {
                case "arcane_shaped" -> builder.arcaneShaped(json);
                case "arcane_shapeless" -> builder.arcaneShapeless(json);
                case "crucible" -> builder.crucible(json);
                case "infusion" -> builder.infusion(json);
                case "infusion_enchantment" -> builder.infusionEnchantment(json);
                case "compound" -> builder.compound(json);
                case "vanilla" -> builder.vanilla(json);
                default -> throw new IllegalArgumentException("Unknown recipe type " + type);
            };
            return registerKey(id, key, recipe);
        } catch (Exception exception) {
            Thaumcraft.LOGGER.error("Add-on recipe {} failed to load: {}", id, exception.getMessage());
            return false;
        }
    }

    private static boolean registerKey(Identifier id, String key, TcRecipe recipe) {
        if (RECIPE_KEYS.contains(key)) {
            TcRecipes.replace(recipe);
            return true;
        }
        if (TcRecipes.get(key) != null) {
            Thaumcraft.LOGGER.error("Recipe key '{}' from {} collides with a registered recipe; JSON entry skipped", key, id);
            return false;
        }
        TcRecipes.registerUnique(recipe);
        RECIPE_KEYS.add(key);
        return true;
    }

    private static boolean registerResearch(Identifier id, String key, JsonObject json) {
        try {
            String category = string(json, "category", id);
            if (category == null || ResearchCategories.get(category) == null) {
                Thaumcraft.LOGGER.error("Add-on research {} references unknown category '{}'", id, category);
                return false;
            }
            Integer column = integer(json, "column", id);
            Integer row = integer(json, "row", id);
            if (column == null || row == null) {
                return false;
            }
            JsonObject icon = object(json, "icon", id);
            if (icon == null) {
                return false;
            }
            AspectList tags = aspectTags(json, id);
            if (tags == null) {
                return false;
            }
            ResearchItem item;
            if (icon.has("item")) {
                JsonElement itemElement = icon.get("item");
                if (itemElement == null || !itemElement.isJsonPrimitive()) {
                    Thaumcraft.LOGGER.error("Add-on research {} icon item must be a string", id);
                    return false;
                }
                item = new ResearchItem(key, category, tags, column, row,
                    integerOr(json, "complexity", 1), TcStack.of(itemElement.getAsString()));
            } else if (icon.has("texture")) {
                String texture = string(icon, "texture", id);
                if (texture == null) {
                    return false;
                }
                item = new ResearchItem(key, category, tags, column, row,
                    integerOr(json, "complexity", 1), Identifier.parse(texture));
            } else {
                Thaumcraft.LOGGER.error("Add-on research {} icon needs an item or texture", id);
                return false;
            }
            applyFlags(id, json, item);
            applyList(id, json, "parents", item::setParents);
            applyList(id, json, "parentsHidden", item::setParentsHidden);
            applyList(id, json, "siblings", item::setSiblings);
            Integer warp = integerOr(json, "warp", 0);
            if (warp != null && warp != 0) {
                item.setWarp(warp);
            }
            ResearchPage[] pages = pages(key, json, id);
            if (pages == null) {
                return false;
            }
            item.setPages(pages);
            if (RESEARCH_CATEGORIES.containsKey(key)) {
                ResearchCategories.removeResearch(RESEARCH_CATEGORIES.get(key), key);
            }
            item.registerResearchItem();
            ResearchItem registered = null;
            for (ResearchItem candidate : ResearchCategories.get(category).research()) {
                if (candidate == item) {
                    registered = candidate;
                    break;
                }
            }
            if (registered != null) {
                RESEARCH_CATEGORIES.put(key, category);
                return true;
            }
            Thaumcraft.LOGGER.error("Add-on research {} was not added (key collides with existing research or position overlaps)", id);
            return false;
        } catch (Exception exception) {
            Thaumcraft.LOGGER.error("Add-on research {} failed to load: {}", id, exception.getMessage());
            return false;
        }
    }

    private static void applyFlags(Identifier id, JsonObject json, ResearchItem item) {
        JsonElement flags = json.get("flags");
        if (flags == null || !flags.isJsonArray()) {
            return;
        }
        for (JsonElement flag : flags.getAsJsonArray()) {
            switch (flag.getAsString()) {
                case "SPECIAL" -> item.setSpecial();
                case "SECONDARY" -> item.setSecondary();
                case "ROUND" -> item.setRound();
                case "STUB" -> item.setStub();
                case "VIRTUAL" -> item.setVirtual();
                case "CONCEALED" -> item.setConcealed();
                case "HIDDEN" -> item.setHidden();
                case "LOST" -> item.setLost();
                case "AUTO_UNLOCK" -> item.setAutoUnlock();
                default -> Thaumcraft.LOGGER.error("Add-on research {} has unknown flag {}", id, flag.getAsString());
            }
        }
    }

    private static void applyList(Identifier id, JsonObject json, String field, java.util.function.Consumer<String[]> setter) {
        JsonElement element = json.get(field);
        if (element == null || !element.isJsonArray()) {
            return;
        }
        List<String> values = element.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
        setter.accept(values.toArray(String[]::new));
    }

    private static ResearchPage[] pages(String key, JsonObject json, Identifier id) {
        JsonElement element = json.get("pages");
        if (element == null || !element.isJsonArray()) {
            return new ResearchPage[0];
        }
        List<ResearchPage> pages = new java.util.ArrayList<>();
        for (JsonElement page : element.getAsJsonArray()) {
            if (!page.isJsonObject()) {
                Thaumcraft.LOGGER.error("Add-on research {} has a malformed page", id);
                return null;
            }
            JsonObject object = page.getAsJsonObject();
            if (object.has("text")) {
                pages.add(ResearchPage.text(PAGE_PREFIX + key + "." + object.get("text").getAsString()));
            } else if (object.has("concealed")) {
                JsonObject concealed = object.getAsJsonObject("concealed");
                String research = string(concealed, "research", id);
                String text = concealed.has("text") ? concealed.get("text").getAsString() : null;
                if (research == null || text == null) {
                    return null;
                }
                pages.add(ResearchPage.concealed(research, PAGE_PREFIX + research + "." + text));
            } else if (object.has("recipes")) {
                pages.add(ResearchPage.crafting(strings(object, "recipes")));
            } else if (object.has("arcane")) {
                pages.add(ResearchPage.arcane(strings(object, "arcane")));
            } else if (object.has("crucible")) {
                pages.add(ResearchPage.crucible(strings(object, "crucible")));
            } else if (object.has("infusion")) {
                pages.add(ResearchPage.infusion(strings(object, "infusion")));
            } else if (object.has("infusionEnchantment")) {
                pages.add(ResearchPage.infusionEnchantment(object.get("infusionEnchantment").getAsString()));
            } else if (object.has("compound")) {
                pages.add(ResearchPage.compound(object.get("compound").getAsString()));
            } else if (object.has("smelting")) {
                JsonObject stacks = object.getAsJsonObject("smelting");
                TcStack input = stack(stacks, "input", id);
                TcStack output = stack(stacks, "output", id);
                if (input == null || output == null) {
                    return null;
                }
                pages.add(ResearchPage.smelting(input, output));
            } else if (object.has("creatureInfusion")) {
                pages.add(ResearchPage.creatureInfusion(object.get("creatureInfusion").getAsString()));
            } else if (object.has("image")) {
                pages.add(ResearchPage.image(object.get("image").getAsString(), string(object, "caption", id)));
            } else if (object.has("aspects")) {
                AspectList aspects = optionalAspects(object, "aspects", id);
                if (aspects == null) {
                    return null;
                }
                pages.add(ResearchPage.aspects(aspects));
            } else {
                Thaumcraft.LOGGER.error("Add-on research {} has a page with no known kind", id);
                return null;
            }
        }
        return pages.toArray(new ResearchPage[0]);
    }

    private static String[] strings(JsonObject json, String field) {
        JsonElement element = json.get(field);
        if (element == null || !element.isJsonArray()) {
            return new String[0];
        }
        return element.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
    }

    private static final class TcRecipeBuilder {
        private final Identifier id;
        private final String key;
        private final String research;
        private final TcStack result;
        private final AspectList aspects;

        private TcRecipeBuilder(Identifier id, String key, String research, TcStack result, AspectList aspects) {
            this.id = id;
            this.key = key;
            this.research = research;
            this.result = result;
            this.aspects = aspects;
        }

        private ShapedArcaneRecipe arcaneShaped(JsonObject json) {
            JsonArray pattern = array(json, "pattern");
            JsonElement symbolElement = json.get("symbols");
            if (pattern == null || symbolElement == null || !symbolElement.isJsonObject()) {
                throw new IllegalArgumentException("arcane_shaped needs pattern and symbols");
            }
            Map<String, TcIngredient> symbols = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : symbolElement.getAsJsonObject().entrySet()) {
                TcIngredient ingredient = ingredientOf(entry.getValue());
                if (ingredient == null) {
                    throw new IllegalArgumentException("symbol " + entry.getKey() + " needs an ingredient");
                }
                symbols.put(entry.getKey(), ingredient);
            }
            List<TcIngredient> grid = new java.util.ArrayList<>();
            int width = 0;
            for (JsonElement row : pattern) {
                width = Math.max(width, row.getAsString().length());
            }
            for (JsonElement row : pattern) {
                String line = row.getAsString();
                for (int column = 0; column < width; column++) {
                    grid.add(column < line.length()
                        ? resolveSymbol(line.charAt(column), symbols)
                        : TcIngredient.EMPTY);
                }
            }
            return new ShapedArcaneRecipe(key, research, result, aspects, width, pattern.size(), grid);
        }

        private ShapelessArcaneRecipe arcaneShapeless(JsonObject json) {
            JsonArray ingredients = array(json, "ingredients");
            if (ingredients == null) {
                throw new IllegalArgumentException("arcane_shapeless needs ingredients");
            }
            return new ShapelessArcaneRecipe(key, research, result, aspects, ingredientList(ingredients));
        }

        private CrucibleRecipe crucible(JsonObject json) {
            TcIngredient catalyst = ingredient(json, "catalyst");
            if (catalyst == null) {
                throw new IllegalArgumentException("crucible needs catalyst");
            }
            return new CrucibleRecipe(key, research, result, catalyst, aspects);
        }

        private InfusionRecipe infusion(JsonObject json) {
            TcIngredient central = ingredient(json, "central");
            if (central == null) {
                throw new IllegalArgumentException("infusion needs central");
            }
            Integer instability = integer(json, "instability", id);
            if (instability == null) {
                throw new IllegalArgumentException("infusion needs instability");
            }
            JsonArray components = array(json, "components");
            if (components == null) {
                throw new IllegalArgumentException("infusion needs components");
            }
            return new InfusionRecipe(key, research, result, instability, aspects, central, ingredientList(components));
        }

        private InfusionEnchantmentRecipe infusionEnchantment(JsonObject json) {
            String enchantment = string(json, "enchantment", id);
            if (enchantment == null) {
                throw new IllegalArgumentException("infusion_enchantment needs enchantment");
            }
            Integer instability = integer(json, "instability", id);
            if (instability == null) {
                throw new IllegalArgumentException("infusion_enchantment needs instability");
            }
            JsonArray components = array(json, "components");
            if (components == null) {
                throw new IllegalArgumentException("infusion_enchantment needs components");
            }
            return new InfusionEnchantmentRecipe(key, research,
                Identifier.parse(enchantment),
                instability, aspects, ingredientList(components));
        }

        private CompoundRecipe compound(JsonObject json) {
            Integer width = integer(json, "width", id);
            Integer height = integer(json, "height", id);
            Integer depth = integer(json, "depth", id);
            JsonArray blocks = array(json, "blocks");
            if (width == null || height == null || depth == null || blocks == null) {
                throw new IllegalArgumentException("compound needs width, height, depth and blocks");
            }
            List<TcStack> stacks = new java.util.ArrayList<>();
            for (JsonElement block : blocks) {
                TcStack stack = stackOf(block);
                if (stack == null) {
                    throw new IllegalArgumentException("compound blocks must be stacks");
                }
                stacks.add(stack);
            }
            return new CompoundRecipe(key, research, aspects, width, height, depth, stacks);
        }

        private CraftingRecipeRef vanilla(JsonObject json) {
            String recipe = string(json, "recipe", id);
            if (recipe == null) {
                throw new IllegalArgumentException("vanilla needs recipe");
            }
            return new CraftingRecipeRef(key, research, Identifier.parse(recipe), result);
        }

        private TcIngredient resolveSymbol(char symbol, Map<String, TcIngredient> symbols) {
            if (symbol == ' ') {
                return TcIngredient.EMPTY;
            }
            TcIngredient ingredient = symbols.get(String.valueOf(symbol));
            if (ingredient == null) {
                throw new IllegalArgumentException("No ingredient for pattern symbol " + symbol);
            }
            return ingredient;
        }

        private List<TcIngredient> ingredientList(JsonArray entries) {
            List<TcIngredient> ingredients = new java.util.ArrayList<>();
            for (JsonElement entry : entries) {
                TcIngredient ingredient = ingredientOf(entry);
                if (ingredient == null) {
                    throw new IllegalArgumentException("malformed ingredient entry");
                }
                ingredients.add(ingredient);
            }
            return ingredients;
        }

        private JsonArray array(JsonObject json, String field) {
            JsonElement element = json.get(field);
            return element == null || !element.isJsonArray() ? null : element.getAsJsonArray();
        }
    }

    private static @Nullable TcIngredient ingredient(JsonObject json, String field) {
        JsonElement element = json.get(field);
        return element == null ? null : ingredientOf(element);
    }

    private static @Nullable TcIngredient ingredientOf(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("item")) {
            return TcIngredient.item(object.get("item").getAsString());
        }
        if (object.has("tag")) {
            return TcIngredient.tag(object.get("tag").getAsString());
        }
        if (object.has("stack")) {
            TcStack stack = stackOf(object.get("stack"));
            return stack == null ? null : TcIngredient.of(stack);
        }
        if (object.has("any")) {
            List<TcIngredient> options = new java.util.ArrayList<>();
            for (JsonElement option : object.getAsJsonArray("any")) {
                TcIngredient ingredient = ingredientOf(option);
                if (ingredient == null) {
                    return null;
                }
                options.add(ingredient);
            }
            return TcIngredient.anyOf(options.toArray(TcIngredient[]::new));
        }
        return null;
    }

    private static @Nullable TcStack stack(JsonObject json, String field, Identifier id) {
        JsonElement element = json.get(field);
        if (element == null || !element.isJsonObject()) {
            Thaumcraft.LOGGER.error("Add-on content {} needs a '{}' stack", id, field);
            return null;
        }
        return stackOf(element);
    }

    private static @Nullable TcStack stackOf(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement itemElement = object.get("item");
        String item = itemElement != null && itemElement.isJsonPrimitive() ? itemElement.getAsString() : null;
        if (item == null) {
            return null;
        }
        int count = object.has("count") ? object.get("count").getAsInt() : 1;
        AspectList aspects = object.has("aspects")
            ? AspectList.CODEC.parse(JsonOps.INSTANCE, object.get("aspects")).result().orElse(null)
            : null;
        if (object.has("aspects") && aspects == null) {
            return null;
        }
        Map<String, String> data = new HashMap<>();
        if (object.has("data")) {
            for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("data").entrySet()) {
                data.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return new TcStack(Identifier.parse(item), count, aspects, data);
    }

    private static @Nullable AspectList optionalAspects(JsonObject json, String field, Identifier id) {
        JsonElement element = json.get(field);
        if (element == null) {
            return new AspectList();
        }
        AspectList aspects = AspectList.CODEC.parse(JsonOps.INSTANCE, element).result().orElse(null);
        if (aspects == null) {
            Thaumcraft.LOGGER.error("Add-on content {} has unknown aspect tags in '{}'", id, field);
        }
        return aspects;
    }

    private static @Nullable AspectList aspectTags(JsonObject json, Identifier id) {
        JsonElement element = json.get("tags");
        if (element == null) {
            return new AspectList();
        }
        AspectList aspects = AspectList.CODEC.parse(JsonOps.INSTANCE, element).result().orElse(null);
        if (aspects == null) {
            Thaumcraft.LOGGER.error("Add-on research {} has unknown aspect tags", id);
        }
        return aspects;
    }

    private static @Nullable String string(JsonObject json, String field, Identifier id) {
        JsonElement element = json.get(field);
        if (element == null || !element.isJsonPrimitive()) {
            Thaumcraft.LOGGER.error("Add-on content {} needs a '{}' string", id, field);
            return null;
        }
        return element.getAsString();
    }

    private static @Nullable String requireKey(Identifier id, JsonObject json) {
        String key = string(json, "key", id);
        if (key == null) {
            return null;
        }
        if (key.isBlank() || key.indexOf(':') < 0) {
            Thaumcraft.LOGGER.error("Add-on content {} key '{}' must be namespaced ('addonid:name')", id, key);
            return null;
        }
        return key;
    }

    private static @Nullable String requireResearch(Identifier id, JsonObject json, String key) {
        String research = string(json, "research", id);
        if (research == null || research.isBlank()) {
            Thaumcraft.LOGGER.error("Add-on recipe {} needs a research key", key);
            return null;
        }
        return research;
    }

    private static @Nullable Integer integer(JsonObject json, String field, Identifier id) {
        JsonElement element = json.get(field);
        if (element == null || !element.isJsonPrimitive()) {
            Thaumcraft.LOGGER.error("Add-on content {} needs an '{}' integer", id, field);
            return null;
        }
        return element.getAsInt();
    }

    private static @Nullable Integer integerOr(JsonObject json, String field, int fallback) {
        JsonElement element = json.get(field);
        return element == null ? fallback : element.getAsInt();
    }

    private static JsonObject object(JsonObject json, String field, Identifier id) {
        JsonElement element = json.get(field);
        return element == null || !element.isJsonObject() ? null : element.getAsJsonObject();
    }
}
