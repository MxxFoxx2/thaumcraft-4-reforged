package thaumcraft.registry;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;

public final class ModVillagers {
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Thaumcraft.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> WIZARD = profession("wizard");
    public static final DeferredHolder<VillagerProfession, VillagerProfession> BANKER = profession("banker");

    private ModVillagers() {
    }

    private static DeferredHolder<VillagerProfession, VillagerProfession> profession(String name) {
        return PROFESSIONS.register(name, () -> {
            Int2ObjectMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectOpenHashMap<>();
            for (int level = 1; level <= 5; level++) {
                tradeSets.put(level, ResourceKey.create(Registries.TRADE_SET, Thaumcraft.id(name + "/level_" + level)));
            }
            return new VillagerProfession(
                Component.translatable("entity.thaumcraft.villager." + name),
                holder -> false,
                holder -> false,
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_LIBRARIAN,
                tradeSets
            );
        });
    }

    public static void register(IEventBus bus) {
        PROFESSIONS.register(bus);
    }
}
