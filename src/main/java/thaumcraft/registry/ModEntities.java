package thaumcraft.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.entity.monster.FireBat;
import thaumcraft.entity.projectile.Ember;
import thaumcraft.entity.projectile.ExplosiveOrb;
import thaumcraft.entity.projectile.FrostShard;
import thaumcraft.entity.projectile.PechBlast;
import thaumcraft.entity.projectile.PrimalArrow;
import thaumcraft.entity.projectile.PrimalOrb;
import thaumcraft.entity.projectile.ShockOrb;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Thaumcraft.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<FireBat>> FIREBAT = ENTITIES.register("firebat", () -> EntityType.Builder.of(FireBat::new, MobCategory.MONSTER).sized(0.5F, 0.9F).eyeHeight(0.45F).fireImmune().clientTrackingRange(4).updateInterval(3).build("thaumcraft:firebat"));
    public static final DeferredHolder<EntityType<?>, EntityType<PrimalOrb>> PRIMAL_ORB = projectile("primal_orb", PrimalOrb::new, 0.25F);
    public static final DeferredHolder<EntityType<?>, EntityType<FrostShard>> FROST_SHARD = projectile("frost_shard", FrostShard::new, 0.25F);
    public static final DeferredHolder<EntityType<?>, EntityType<PrimalArrow>> PRIMAL_ARROW = projectile("primal_arrow", PrimalArrow::new, 0.5F);
    public static final DeferredHolder<EntityType<?>, EntityType<PechBlast>> PECH_BLAST = projectile("pech_blast", PechBlast::new, 0.25F);
    public static final DeferredHolder<EntityType<?>, EntityType<ShockOrb>> SHOCK_ORB = projectile("shock_orb", ShockOrb::new, 0.25F);
    public static final DeferredHolder<EntityType<?>, EntityType<Ember>> EMBER = projectile("ember", Ember::new, 0.25F);
    public static final DeferredHolder<EntityType<?>, EntityType<ExplosiveOrb>> EXPLOSIVE_ORB = projectile("explosive_orb", ExplosiveOrb::new, 0.25F);

    private ModEntities() {
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> projectile(String name, EntityType.EntityFactory<T> factory, float size) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.MISC).sized(size, size).clientTrackingRange(4).updateInterval(20).build("thaumcraft:" + name));
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ModEntities::createAttributes);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(FIREBAT.get(), FireBat.createAttributes().build());
    }
}
