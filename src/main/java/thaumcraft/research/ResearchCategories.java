package thaumcraft.research;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;

public final class ResearchCategories {
    private static final Map<String, ResearchCategory> CATEGORIES = new LinkedHashMap<>();

    private ResearchCategories() {
    }

    public static ResearchCategory registerCategory(String key, Identifier icon, Identifier background) {
        return CATEGORIES.computeIfAbsent(key, ignored -> new ResearchCategory(key, icon, background));
    }

    public static @Nullable ResearchCategory get(String key) {
        return CATEGORIES.get(key);
    }

    public static Collection<ResearchCategory> all() {
        return Collections.unmodifiableCollection(CATEGORIES.values());
    }

    public static @Nullable ResearchItem getResearch(String key) {
        for (ResearchCategory category : CATEGORIES.values()) {
            ResearchItem item = category.get(key);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public static List<ResearchItem> allResearch() {
        List<ResearchItem> items = new ArrayList<>();
        for (ResearchCategory category : CATEGORIES.values()) {
            items.addAll(category.research());
        }
        return items;
    }

    public static void addResearch(ResearchItem item) {
        ResearchCategory category = CATEGORIES.get(item.category());
        if (category == null) {
            Thaumcraft.LOGGER.error("Research {} has unknown category {}", item.key(), item.category());
            return;
        }
        for (ResearchCategory existing : CATEGORIES.values()) {
            if (existing.hasKey(item.key())) {
                Thaumcraft.LOGGER.error("Research {} not added as its key is already used in category {}",
                    item.key(), existing.key());
                return;
            }
        }
        if (!category.add(item)) {
            Thaumcraft.LOGGER.error("Research {} not added as it overlaps with existing research", item.key());
        }
    }

    public static boolean removeResearch(String category, String key) {
        ResearchCategory existing = CATEGORIES.get(category);
        return existing != null && existing.remove(key);
    }
}
