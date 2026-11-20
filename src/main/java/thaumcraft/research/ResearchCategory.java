package thaumcraft.research;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import thaumcraft.Config;

public final class ResearchCategory {
    private static final String MIRROR = "MIRROR";

    private final String key;
    private final Identifier icon;
    private final Identifier background;
    private final Map<String, ResearchItem> research = new LinkedHashMap<>();
    private int minColumn;
    private int minRow;
    private int maxColumn;
    private int maxRow;

    ResearchCategory(String key, Identifier icon, Identifier background) {
        this.key = key;
        this.icon = icon;
        this.background = background;
    }

    public String key() {
        return key;
    }

    public Identifier icon() {
        return icon;
    }

    public Identifier background() {
        return background;
    }

    public String nameKey() {
        return "tc.research_category." + key;
    }

    public Collection<ResearchItem> research() {
        return research.values().stream().filter(item -> item.isAvailable() && (Config.allowMirrors() || !MIRROR.equals(item.key()))).toList();
    }

    public @Nullable ResearchItem get(String researchKey) {
        if (MIRROR.equals(researchKey) && !Config.allowMirrors()) {
            return null;
        }
        ResearchItem item = research.get(researchKey);
        return item != null && item.isAvailable() ? item : null;
    }

    public int minColumn() {
        return minColumn;
    }

    public int minRow() {
        return minRow;
    }

    public int maxColumn() {
        return maxColumn;
    }

    public int maxRow() {
        return maxRow;
    }

    boolean add(ResearchItem item) {
        if (research.containsKey(item.key())) {
            return false;
        }
        if (!item.isVirtual()) {
            for (ResearchItem other : research.values()) {
                if (other.column() == item.column() && other.row() == item.row()) {
                    return false;
                }
            }
        }
        research.put(item.key(), item);
        minColumn = Math.min(minColumn, item.column());
        minRow = Math.min(minRow, item.row());
        maxColumn = Math.max(maxColumn, item.column());
        maxRow = Math.max(maxRow, item.row());
        return true;
    }

    public boolean hasKey(String researchKey) {
        return research.containsKey(researchKey);
    }

    boolean remove(String researchKey) {
        ResearchItem removed = research.remove(researchKey);
        if (removed == null) {
            return false;
        }
        recomputeBounds();
        return true;
    }

    private void recomputeBounds() {
        minColumn = 0;
        minRow = 0;
        maxColumn = 0;
        maxRow = 0;
        for (ResearchItem item : research.values()) {
            minColumn = Math.min(minColumn, item.column());
            minRow = Math.min(minRow, item.row());
            maxColumn = Math.max(maxColumn, item.column());
            maxRow = Math.max(maxRow, item.row());
        }
    }
}
