package thaumcraft.registry;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;

public final class ModVillagers {
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Thaumcraft.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> WIZARD = profession("wizard");
    public static final DeferredHolder<VillagerProfession, VillagerProfession> BANKER = profession("banker");

    private static final int MAX_USES = 7;
    private static final int XP = 5;
    private static final String[] PRIMALS = {"air", "fire", "water", "earth", "order", "entropy"};
    private static final String[] APPRENTICE_RINGS = {"air", "earth", "fire", "water", "order", "entropy"};

    private ModVillagers() {
    }

    private static DeferredHolder<VillagerProfession, VillagerProfession> profession(String name) {
        return PROFESSIONS.register(name, () -> new VillagerProfession(
            name,
            holder -> false,
            holder -> false,
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_LIBRARIAN
        ));
    }

    public static void register(IEventBus bus) {
        PROFESSIONS.register(bus);
    }

    private record Stack(String item, int minimum, int maximum) {
    }

    private static Stack stack(String item, int minimum, int maximum) {
        return new Stack(item, minimum, maximum);
    }

    private static Stack stack(String item) {
        return new Stack(item, 1, 1);
    }

    private static String tc(String name) {
        return Thaumcraft.MODID + ":" + name;
    }

    private static @Nullable Item resolve(String id) {
        ResourceLocation location = ResourceLocation.parse(id);
        return BuiltInRegistries.ITEM.containsKey(location) ? BuiltInRegistries.ITEM.get(location) : null;
    }

    private static int roll(RandomSource random, int minimum, int maximum) {
        return minimum == maximum ? minimum : random.nextIntBetweenInclusive(minimum, maximum);
    }

    private static final class Trade implements ItemListing {
        private final Stack wants;
        private final Stack gives;
        private final Item wantsItem;
        private final Item givesItem;

        private Trade(Stack wants, Stack gives, Item wantsItem, Item givesItem) {
            this.wants = wants;
            this.gives = gives;
            this.wantsItem = wantsItem;
            this.givesItem = givesItem;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return new MerchantOffer(
                new ItemCost(wantsItem, roll(random, wants.minimum(), wants.maximum())),
                new ItemStack(givesItem, roll(random, gives.minimum(), gives.maximum())),
                MAX_USES,
                XP,
                0.05F
            );
        }
    }

    private static void add(List<ItemListing> pool, Stack wants, Stack gives) {
        Item wantsItem = resolve(wants.item());
        Item givesItem = resolve(gives.item());
        if (wantsItem != null && givesItem != null) {
            pool.add(new Trade(wants, gives, wantsItem, givesItem));
        }
    }

    private static List<ItemListing> wizardPool() {
        String emerald = "minecraft:emerald";
        List<ItemListing> pool = new ArrayList<>();
        add(pool, stack(tc("gold_coin"), 20, 22), stack(emerald));
        add(pool, stack(emerald), stack(tc("knowledge_fragment")));
        add(pool, stack(tc("quicksilver"), 4, 6), stack(emerald));
        add(pool, stack(emerald), stack(tc("alumentum")));
        add(pool, stack(tc("amber"), 4, 6), stack(emerald));
        add(pool, stack(emerald), stack(tc("nitor")));
        add(pool, stack(tc("chicken_nugget"), 24, 31), stack(emerald));
        add(pool, stack("minecraft:book", 4, 6), stack(tc("knowledge_fragment")));
        add(pool, stack(tc("beef_nugget"), 24, 31), stack(emerald));
        for (String element : PRIMALS) {
            add(pool, stack(emerald), stack(tc(element + "_shard"), 2, 3));
        }
        add(pool, stack(emerald), stack(tc("mana_bean"), 1, 2));
        add(pool, stack(emerald, 5, 7), stack(tc("bath_salts")));
        add(pool, stack(emerald, 5, 7), stack(tc("protection_ring")));
        add(pool, stack(emerald, 5, 7), stack(tc("vis_stone")));
        for (String element : APPRENTICE_RINGS) {
            add(pool, stack(emerald, 5, 7), stack(tc("apprentice_ring_" + element)));
        }
        return pool;
    }

    private static List<ItemListing> bankerPool() {
        String coin = tc("gold_coin");
        List<ItemListing> pool = new ArrayList<>();
        add(pool, stack(coin, 20, 22), stack("minecraft:emerald"));
        add(pool, stack(coin, 2, 3), stack("minecraft:arrow"));
        add(pool, stack(coin, 6, 8), stack("minecraft:white_wool"));
        add(pool, stack(coin, 3, 4), stack("minecraft:paper"));
        add(pool, stack(coin, 7, 9), stack("minecraft:book"));
        add(pool, stack(coin, 16, 20), stack("minecraft:experience_bottle"));
        add(pool, stack(coin, 9, 12), stack("minecraft:glowstone"));
        add(pool, stack(coin, 2, 3), stack("minecraft:coal"));
        add(pool, stack(coin, 22, 24), stack("minecraft:diamond"));
        add(pool, stack(coin, 6, 8), stack("minecraft:iron_ingot"));
        add(pool, stack(coin, 10, 12), stack(tc("thaumium_ingot")));
        add(pool, stack(coin, 25, 32), stack("minecraft:saddle"));
        return pool;
    }

    @EventBusSubscriber(modid = Thaumcraft.MODID)
    public static final class Trades {
        private Trades() {
        }

        @SubscribeEvent
        static void onVillagerTrades(VillagerTradesEvent event) {
            List<ItemListing> pool;
            if (event.getType() == WIZARD.get()) {
                pool = wizardPool();
            } else if (event.getType() == BANKER.get()) {
                pool = bankerPool();
            } else {
                return;
            }
            for (int level = 1; level <= 5; level++) {
                event.getTrades().computeIfAbsent(level, key -> new ArrayList<>()).addAll(pool);
            }
        }
    }
}
