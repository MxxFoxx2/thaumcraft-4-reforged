package thaumcraft.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import thaumcraft.aspect.AspectList;

class ResearchItemTest {
    private static final String CATEGORY = "test_category";

    private static ResearchItem research(String key) {
        return research(key, 0, 0);
    }

    private static ResearchItem research(String key, int column, int row) {
        return new ResearchItem(key, CATEGORY, new AspectList(), column, row, 1,
            Identifier.parse("thaumcraft:test_icon"));
    }


    private static void registerTestCategory() {
        ResearchCategories.registerCategory(CATEGORY,
            Identifier.parse("thaumcraft:test_category_icon"),
            Identifier.parse("thaumcraft:test_category_background"));
    }

    @Test
    void availabilityDefaultsToTrue() {
        assertTrue(research("availabilityDefault").isAvailable());
    }

    @Test
    void whenGatesAvailability() {
        AtomicInteger calls = new AtomicInteger();
        ResearchItem item = research("whenGates").when(() -> calls.incrementAndGet() < 3);
        assertTrue(item.isAvailable());
        assertTrue(item.isAvailable());
        assertFalse(item.isAvailable());
        assertEquals(3, calls.get());
    }

    @Test
    void whenRejectsNullSupplier() {
        assertThrows(NullPointerException.class, () -> research("whenNull").when(null));
    }

    @Test
    void setTextPagesBuildsQualifiedLangKeys() {
        ResearchItem item = research("pages").setTextPages("0", "1");
        assertEquals(2, item.pages().size());
        assertEquals("tc.research_page.pages.0", ((ResearchPage.Text) item.pages().get(0)).text());
        assertEquals("tc.research_page.pages.1", ((ResearchPage.Text) item.pages().get(1)).text());
    }

    @Test
    void requireInfusionResearchAddsHiddenInfusionParentForInfusionPages() {
        registerTestCategory();
        ResearchItem item = research("infusionParent", 0, 0)
            .setPages(ResearchPage.text("tc.research_page.infusionParent.0"),
                new ResearchPage.Infusion(List.of("test_infusion_recipe")))
            .requireInfusionResearch();
        assertSame(item, item.registerResearchItem());
        assertTrue(item.parentsHidden().contains("INFUSION"));
        assertNotNull(ResearchCategories.get(CATEGORY).get("infusionParent"));
    }

    @Test
    void requireInfusionResearchWithoutInfusionPagesAddsNoParent() {
        registerTestCategory();
        ResearchItem item = research("plainParent", 1, 0).setTextPages("0").requireInfusionResearch();
        item.registerResearchItem();
        assertFalse(item.parentsHidden().contains("INFUSION"));
        assertNotNull(ResearchCategories.get(CATEGORY).get("plainParent"));
    }

    @Test
    void researchKeysAreUniqueAcrossCategories() {
        String other = "test_category_b";
        ResearchCategories.registerCategory(other,
            Identifier.parse("thaumcraft:test_icon"), Identifier.parse("thaumcraft:test_background"));
        research("crossCategoryDup", 6, 6).registerResearchItem();
        ResearchItem otherItem = new ResearchItem("crossCategoryDup", other, new AspectList(), 5, 5, 1,
            Identifier.parse("thaumcraft:test_icon"));
        otherItem.registerResearchItem();
        assertFalse(ResearchCategories.get(other).hasKey("crossCategoryDup"));
        assertNotNull(ResearchCategories.get(CATEGORY).get("crossCategoryDup"));
    }

    @Test
    void unregisteredResearchIsNotInCategory() {
        registerTestCategory();
        ResearchItem item = research("neverRegistered");
        assertNull(ResearchCategories.get(CATEGORY).get("neverRegistered"));
    }
}
