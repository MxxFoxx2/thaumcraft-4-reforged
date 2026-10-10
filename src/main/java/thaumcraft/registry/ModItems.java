package thaumcraft.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.essentia.PhialItem;
import thaumcraft.item.AspectContainerItem;
import thaumcraft.item.CrystalEssenceItem;
import thaumcraft.item.GogglesItem;
import thaumcraft.item.KnowledgeFragmentItem;
import thaumcraft.item.LootBagItem;
import thaumcraft.item.PrimalCharmItem;
import thaumcraft.item.ResearchNotesItem;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.item.ThaumometerItem;
import thaumcraft.item.ThaumonomiconItem;
import thaumcraft.item.ZombieBrainItem;
import thaumcraft.platform.Registration;
import thaumcraft.wand.WandItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Thaumcraft.MODID);

    static {
        Registration.armorMaterial(GogglesItem.MATERIAL);
    }

    public static final DeferredItem<Item> ALUMENTUM = RegistryHelper.registerSimpleItem(ITEMS, "alumentum", properties -> Registration.fuel(properties, 6400));
    public static final DeferredItem<Item> NITOR = RegistryHelper.registerSimpleItem(ITEMS, "nitor");
    public static final DeferredItem<Item> THAUMIUM_INGOT = RegistryHelper.registerSimpleItem(ITEMS, "thaumium_ingot");
    public static final DeferredItem<Item> QUICKSILVER = RegistryHelper.registerSimpleItem(ITEMS, "quicksilver");
    public static final DeferredItem<Item> MAGIC_TALLOW = RegistryHelper.registerSimpleItem(ITEMS, "magic_tallow");
    public static final DeferredItem<Item> AMBER = RegistryHelper.registerSimpleItem(ITEMS, "amber");
    public static final DeferredItem<Item> ENCHANTED_FABRIC = RegistryHelper.registerSimpleItem(ITEMS, "enchanted_fabric");
    public static final DeferredItem<Item> VIS_FILTER = RegistryHelper.registerSimpleItem(ITEMS, "vis_filter");
    public static final DeferredItem<Item> KNOWLEDGE_FRAGMENT = RegistryHelper.registerItem(ITEMS, "knowledge_fragment", KnowledgeFragmentItem::new);
    public static final DeferredItem<Item> MIRRORED_GLASS = RegistryHelper.registerSimpleItem(ITEMS, "mirrored_glass");
    public static final DeferredItem<Item> TAINTED_GOO = RegistryHelper.registerSimpleItem(ITEMS, "tainted_goo");
    public static final DeferredItem<Item> TAINT_TENDRIL = RegistryHelper.registerSimpleItem(ITEMS, "taint_tendril");
    public static final DeferredItem<Item> JAR_LABEL = RegistryHelper.registerItem(ITEMS, "jar_label", properties -> new AspectContainerItem(properties, "", false));
    public static final DeferredItem<Item> SALIS_MUNDUS = RegistryHelper.registerSimpleItem(ITEMS, "salis_mundus");
    public static final DeferredItem<Item> PRIMAL_CHARM = RegistryHelper.registerItem(ITEMS, "primal_charm", PrimalCharmItem::new);
    public static final DeferredItem<Item> VOID_METAL_INGOT = RegistryHelper.registerSimpleItem(ITEMS, "void_metal_ingot");
    public static final DeferredItem<Item> VOID_SEED = RegistryHelper.registerSimpleItem(ITEMS, "void_seed");
    public static final DeferredItem<Item> GOLD_COIN = RegistryHelper.registerSimpleItem(ITEMS, "gold_coin");

    public static final DeferredItem<Item> AIR_SHARD = RegistryHelper.registerSimpleItem(ITEMS, "air_shard");
    public static final DeferredItem<Item> FIRE_SHARD = RegistryHelper.registerSimpleItem(ITEMS, "fire_shard");
    public static final DeferredItem<Item> WATER_SHARD = RegistryHelper.registerSimpleItem(ITEMS, "water_shard");
    public static final DeferredItem<Item> EARTH_SHARD = RegistryHelper.registerSimpleItem(ITEMS, "earth_shard");
    public static final DeferredItem<Item> ORDER_SHARD = RegistryHelper.registerSimpleItem(ITEMS, "order_shard");
    public static final DeferredItem<Item> ENTROPY_SHARD = RegistryHelper.registerSimpleItem(ITEMS, "entropy_shard");
    public static final DeferredItem<Item> BALANCED_SHARD = RegistryHelper.registerSimpleItem(ITEMS, "balanced_shard");

    public static final DeferredItem<Item> COPPER_NUGGET = RegistryHelper.registerSimpleItem(ITEMS, "copper_nugget");
    public static final DeferredItem<Item> TIN_NUGGET = RegistryHelper.registerSimpleItem(ITEMS, "tin_nugget");
    public static final DeferredItem<Item> SILVER_NUGGET = RegistryHelper.registerSimpleItem(ITEMS, "silver_nugget");
    public static final DeferredItem<Item> LEAD_NUGGET = RegistryHelper.registerSimpleItem(ITEMS, "lead_nugget");
    public static final DeferredItem<Item> QUICKSILVER_DROP = RegistryHelper.registerSimpleItem(ITEMS, "quicksilver_drop");
    public static final DeferredItem<Item> THAUMIUM_NUGGET = RegistryHelper.registerSimpleItem(ITEMS, "thaumium_nugget");
    public static final DeferredItem<Item> VOID_METAL_NUGGET = RegistryHelper.registerSimpleItem(ITEMS, "void_metal_nugget");
    public static final DeferredItem<Item> NATIVE_IRON_CLUSTER = RegistryHelper.registerSimpleItem(ITEMS, "native_iron_cluster");
    public static final DeferredItem<Item> NATIVE_COPPER_CLUSTER = RegistryHelper.registerSimpleItem(ITEMS, "native_copper_cluster");
    public static final DeferredItem<Item> NATIVE_TIN_CLUSTER = RegistryHelper.registerSimpleItem(ITEMS, "native_tin_cluster");
    public static final DeferredItem<Item> NATIVE_SILVER_CLUSTER = RegistryHelper.registerSimpleItem(ITEMS, "native_silver_cluster");
    public static final DeferredItem<Item> NATIVE_LEAD_CLUSTER = RegistryHelper.registerSimpleItem(ITEMS, "native_lead_cluster");
    public static final DeferredItem<Item> NATIVE_CINNABAR_CLUSTER = RegistryHelper.registerSimpleItem(ITEMS, "native_cinnabar_cluster");
    public static final DeferredItem<Item> NATIVE_GOLD_CLUSTER = RegistryHelper.registerSimpleItem(ITEMS, "native_gold_cluster");

    public static final DeferredItem<Item> CHICKEN_NUGGET = nugget("chicken_nugget");
    public static final DeferredItem<Item> BEEF_NUGGET = nugget("beef_nugget");
    public static final DeferredItem<Item> PORK_NUGGET = nugget("pork_nugget");
    public static final DeferredItem<Item> FISH_NUGGET = nugget("fish_nugget");
    public static final DeferredItem<Item> TRIPLE_MEAT_TREAT = RegistryHelper.registerSimpleItem(ITEMS, 
        "triple_meat_treat",
        properties -> Registration.food(properties, 6, 0.8F, true, 1.6F, new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66F)
    );
    public static final DeferredItem<Item> ZOMBIE_BRAIN = RegistryHelper.registerItem(ITEMS, 
        "zombie_brain",
        ZombieBrainItem::new,
        properties -> Registration.food(properties, 4, 0.2F, false, 1.6F, new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.8F)
    );

    public static final DeferredItem<Item> GLASS_PHIAL = RegistryHelper.registerItem(ITEMS, "glass_phial", properties -> new PhialItem(false, properties));
    public static final DeferredItem<Item> ESSENTIA_PHIAL = RegistryHelper.registerItem(ITEMS, "essentia_phial", properties -> new PhialItem(true, properties));
    public static final DeferredItem<Item> WISP_ESSENCE = RegistryHelper.registerItem(ITEMS, "wisp_essence", properties -> new AspectContainerItem(properties, " x", true));
    public static final DeferredItem<Item> CRYSTALLIZED_ESSENCE = RegistryHelper.registerItem(ITEMS, "crystallized_essence", CrystalEssenceItem::new);

    public static final DeferredItem<Item> COMMON_LOOT_BAG = RegistryHelper.registerItem(ITEMS, "common_loot_bag", LootBagItem::new);
    public static final DeferredItem<Item> UNCOMMON_LOOT_BAG = RegistryHelper.registerItem(ITEMS, "uncommon_loot_bag", LootBagItem::new, properties -> properties.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RARE_LOOT_BAG = RegistryHelper.registerItem(ITEMS, "rare_loot_bag", LootBagItem::new, properties -> properties.rarity(Rarity.RARE));

    public static final DeferredItem<WandItem> WAND = RegistryHelper.registerItem(ITEMS, "wand", WandItem::new, WandItem::properties);
    public static final DeferredItem<Item> IRON_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "iron_wand_cap");
    public static final DeferredItem<Item> GOLD_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "gold_wand_cap");
    public static final DeferredItem<Item> THAUMIUM_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "thaumium_wand_cap");
    public static final DeferredItem<Item> COPPER_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "copper_wand_cap");
    public static final DeferredItem<Item> SILVER_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "silver_wand_cap");
    public static final DeferredItem<Item> INERT_SILVER_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "inert_silver_wand_cap");
    public static final DeferredItem<Item> INERT_THAUMIUM_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "inert_thaumium_wand_cap");
    public static final DeferredItem<Item> VOID_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "void_wand_cap");
    public static final DeferredItem<Item> INERT_VOID_WAND_CAP = RegistryHelper.registerSimpleItem(ITEMS, "inert_void_wand_cap");
    public static final DeferredItem<Item> GREATWOOD_ROD = RegistryHelper.registerSimpleItem(ITEMS, "greatwood_rod");
    public static final DeferredItem<Item> OBSIDIAN_ROD = RegistryHelper.registerSimpleItem(ITEMS, "obsidian_rod");
    public static final DeferredItem<Item> SILVERWOOD_ROD = RegistryHelper.registerSimpleItem(ITEMS, "silverwood_rod");
    public static final DeferredItem<Item> ICY_ROD = RegistryHelper.registerSimpleItem(ITEMS, "icy_rod");
    public static final DeferredItem<Item> QUARTZ_ROD = RegistryHelper.registerSimpleItem(ITEMS, "quartz_rod");
    public static final DeferredItem<Item> REED_ROD = RegistryHelper.registerSimpleItem(ITEMS, "reed_rod");
    public static final DeferredItem<Item> BLAZING_ROD = RegistryHelper.registerSimpleItem(ITEMS, "blazing_rod");
    public static final DeferredItem<Item> BONE_ROD = RegistryHelper.registerSimpleItem(ITEMS, "bone_rod");
    public static final DeferredItem<Item> GREATWOOD_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "greatwood_staff_core");
    public static final DeferredItem<Item> OBSIDIAN_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "obsidian_staff_core");
    public static final DeferredItem<Item> SILVERWOOD_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "silverwood_staff_core");
    public static final DeferredItem<Item> ICY_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "icy_staff_core");
    public static final DeferredItem<Item> QUARTZ_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "quartz_staff_core");
    public static final DeferredItem<Item> REED_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "reed_staff_core");
    public static final DeferredItem<Item> BLAZING_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "blazing_staff_core");
    public static final DeferredItem<Item> BONE_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "bone_staff_core");
    public static final DeferredItem<Item> PRIMAL_STAFF_CORE = RegistryHelper.registerSimpleItem(ITEMS, "primal_staff_core");
    public static final DeferredItem<ThaumometerItem> THAUMOMETER = RegistryHelper.registerItem(ITEMS, "thaumometer", ThaumometerItem::new, ThaumometerItem::properties);
    public static final DeferredItem<GogglesItem> GOGGLES_OF_REVEALING = RegistryHelper.registerItem(ITEMS, "goggles_of_revealing", GogglesItem::new, GogglesItem::properties);
    public static final DeferredItem<ScribingToolsItem> SCRIBING_TOOLS = RegistryHelper.registerItem(ITEMS, "scribing_tools", ScribingToolsItem::new, ScribingToolsItem::properties);
    public static final DeferredItem<ResearchNotesItem> RESEARCH_NOTES = RegistryHelper.registerItem(ITEMS, "research_notes", properties -> new ResearchNotesItem(properties, false), properties -> properties.rarity(Rarity.RARE));
    public static final DeferredItem<ResearchNotesItem> DISCOVERY = RegistryHelper.registerItem(ITEMS, "discovery", properties -> new ResearchNotesItem(properties, true), properties -> properties.rarity(Rarity.EPIC).stacksTo(64));
    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON = RegistryHelper.registerItem(ITEMS, "thaumonomicon", properties -> new ThaumonomiconItem(properties, false), ThaumonomiconItem::properties);
    public static final DeferredItem<ThaumonomiconItem> THAUMONOMICON_CHEAT = RegistryHelper.registerItem(ITEMS, "thaumonomicon_cheat", properties -> new ThaumonomiconItem(properties, true), ThaumonomiconItem::properties);

    private ModItems() {
    }

    private static DeferredItem<Item> nugget(String name) {
        return RegistryHelper.registerSimpleItem(ITEMS, name, properties -> Registration.food(properties, 1, 0.3F, false, 0.5F));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
