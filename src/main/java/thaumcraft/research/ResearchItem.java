package thaumcraft.research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;

public class ResearchItem {
    private static final String PAGE_PREFIX = "tc.research_page.";
    private static final String INFUSION = "INFUSION";

    private final String key;
    private final String category;
    private final AspectList tags;
    private final int displayColumn;
    private final int displayRow;
    private final @Nullable TcStack iconItem;
    private final @Nullable Identifier iconTexture;
    private List<String> parents = List.of();
    private List<String> parentsHidden = List.of();
    private List<String> siblings = List.of();
    private int complexity;
    private boolean special;
    private boolean secondary;
    private boolean round;
    private boolean stub;
    private boolean virtual;
    private boolean concealed;
    private boolean hidden;
    private boolean lost;
    private boolean autoUnlock;
    private int warp;
    private List<Identifier> itemTriggers = List.of();
    private List<Identifier> entityTriggers = List.of();
    private List<Aspect> aspectTriggers = List.of();
    private List<ResearchPage> pages = List.of();
    private List<String> requiredTags = List.of();
    private BooleanSupplier availability = () -> true;
    private boolean infusionParent;

    public ResearchItem setRequiredTags(String... tags) {
        this.requiredTags = List.of(tags);
        return this;
    }

    public boolean isAvailable() {
        for (String tag : this.requiredTags) {
            var key = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(tag));
            if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.get(key).map(set -> set.size() > 0).orElse(false)) return false;
        }
        return availability.getAsBoolean();
    }

    public ResearchItem(String key, String category) {
        this.key = key;
        this.category = category;
        this.tags = new AspectList();
        this.displayColumn = 0;
        this.displayRow = 0;
        this.iconItem = null;
        this.iconTexture = null;
        this.virtual = true;
    }

    public ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, TcStack icon) {
        this(key, category, tags, column, row, complexity, icon, null);
    }

    public ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, Identifier icon) {
        this(key, category, tags, column, row, complexity, null, icon);
    }

    private ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, @Nullable TcStack iconItem, @Nullable Identifier iconTexture) {
        this.key = key;
        this.category = category;
        this.tags = tags;
        this.displayColumn = column;
        this.displayRow = row;
        this.iconItem = iconItem;
        this.iconTexture = iconTexture;
        this.complexity = Math.max(1, Math.min(3, complexity));
    }

    public ResearchItem setSpecial() {
        special = true;
        return this;
    }

    public ResearchItem setSecondary() {
        secondary = true;
        return this;
    }

    public ResearchItem setRound() {
        round = true;
        return this;
    }

    public ResearchItem setStub() {
        stub = true;
        return this;
    }

    public ResearchItem setVirtual() {
        virtual = true;
        return this;
    }

    public ResearchItem setConcealed() {
        concealed = true;
        return this;
    }

    public ResearchItem setHidden() {
        hidden = true;
        return this;
    }

    public ResearchItem setLost() {
        lost = true;
        return this;
    }

    public ResearchItem setAutoUnlock() {
        autoUnlock = true;
        return this;
    }

    public ResearchItem setComplexity(int complexity) {
        this.complexity = complexity;
        return this;
    }

    public ResearchItem setWarp(int warp) {
        this.warp = warp;
        return this;
    }

    public ResearchItem setParents(String... parents) {
        this.parents = List.of(parents);
        return this;
    }

    public ResearchItem setParentsHidden(String... parentsHidden) {
        this.parentsHidden = List.of(parentsHidden);
        return this;
    }

    public ResearchItem setSiblings(String... siblings) {
        this.siblings = List.of(siblings);
        return this;
    }

    public ResearchItem setItemTriggers(String... items) {
        this.itemTriggers = ids(items);
        return this;
    }

    public ResearchItem setEntityTriggers(String... entities) {
        this.entityTriggers = ids(entities);
        return this;
    }

    public ResearchItem setAspectTriggers(Aspect... aspects) {
        this.aspectTriggers = List.of(aspects);
        return this;
    }

    public ResearchItem setPages(ResearchPage... pages) {
        this.pages = List.of(pages);
        return this;
    }

    public ResearchItem when(BooleanSupplier condition) {
        this.availability = Objects.requireNonNull(condition, "condition");
        return this;
    }

    public ResearchItem setTextPages(String... pageKeys) {
        ResearchPage[] converted = new ResearchPage[pageKeys.length];
        for (int index = 0; index < pageKeys.length; index++) {
            converted[index] = ResearchPage.text(PAGE_PREFIX + key + "." + pageKeys[index]);
        }
        return setPages(converted);
    }

    public ResearchItem requireInfusionResearch() {
        infusionParent = true;
        return this;
    }

    public ResearchItem registerResearchItem() {
        if (infusionParent && !parentsHidden.contains(INFUSION)
            && pages.stream().anyMatch(page -> page instanceof ResearchPage.Infusion)) {
            List<String> hidden = new ArrayList<>(parentsHidden);
            hidden.add(INFUSION);
            parentsHidden = List.copyOf(hidden);
        }
        ResearchCategories.addResearch(this);
        return this;
    }

    public String key() {
        return key;
    }

    public String category() {
        return category;
    }

    public AspectList tags() {
        return tags;
    }

    public int column() {
        return displayColumn;
    }

    public int row() {
        return displayRow;
    }

    public @Nullable TcStack iconItem() {
        return iconItem;
    }

    public @Nullable Identifier iconTexture() {
        return iconTexture;
    }

    public List<String> parents() {
        return parents;
    }

    public List<String> parentsHidden() {
        return parentsHidden;
    }

    public List<String> siblings() {
        return siblings;
    }

    public int complexity() {
        return complexity;
    }

    public int warp() {
        return warp;
    }

    public boolean isSpecial() {
        return special;
    }

    public boolean isSecondary() {
        return secondary;
    }

    public boolean isRound() {
        return round;
    }

    public boolean isStub() {
        return stub;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public boolean isConcealed() {
        return concealed;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isLost() {
        return lost;
    }

    public boolean isAutoUnlock() {
        return autoUnlock;
    }

    public List<Identifier> itemTriggers() {
        return itemTriggers;
    }

    public List<Identifier> entityTriggers() {
        return entityTriggers;
    }

    public List<Aspect> aspectTriggers() {
        return aspectTriggers;
    }

    public List<ResearchPage> pages() {
        return pages;
    }

    public String nameKey() {
        return "tc.research_name." + key;
    }

    public String textKey() {
        return "tc.research_text." + key;
    }

    public @Nullable Aspect primaryTag() {
        Aspect primary = null;
        int highest = 0;
        for (Aspect aspect : tags.getAspects()) {
            if (tags.getAmount(aspect) > highest) {
                primary = aspect;
                highest = tags.getAmount(aspect);
            }
        }
        return primary;
    }

    private static List<Identifier> ids(String[] values) {
        return Arrays.stream(values).map(Identifier::parse).toList();
    }
}
