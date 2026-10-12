package thaumcraft.entity.monster;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.network.ShieldFxPayload;
import thaumcraft.platform.ModNetwork;
import thaumcraft.registry.ModEntities;
import thaumcraft.registry.ModSounds;
import thaumcraft.entity.monster.eldritch.EldritchGuardian;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class ChampionModifier {
    public static final int NORMAL = -1;
    public static final int UNPROCESSED = -2;
    private static final String[] NAMES = {"bold", "spine", "armor", "mighty", "grim", "warded", "warp", "undying", "fiery", "sickly", "venomous", "vampiric", "infested"};
    private static final int[] TYPES = {-1, 2, 2, -1, 1, 0, 1, 0, 1, 1, 1, 1, 2};
    private static final AttributeModifier CHAMPION_HEALTH = modifier("a62bef38-48cc-42a6-ac5e-ef913841c4fd", 30.0, AttributeModifier.Operation.ADD_VALUE);
    private static final AttributeModifier CHAMPION_DAMAGE = modifier("a340d2db-d881-4c25-ac62-f0ad14cd63b0", 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier BOLD_SPEED = modifier("4b1edd33-caa9-47ae-a702-d86c05701037", 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier MIGHTY_DAMAGE = modifier("7163897f-07f5-49b3-9ce4-b74beb83d2d3", 3.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private ChampionModifier() {
    }

    private static AttributeModifier modifier(String id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(Thaumcraft.id("champion/" + id), amount, operation);
    }

    public static int get(Mob mob) {
        return ChampionKeys.MODIFIER.get(mob);
    }

    public static void assign(Mob mob, int id, boolean persist) {
        if (id < 0 || id >= NAMES.length || mob.level().isClientSide() || get(mob) != UNPROCESSED) {
            return;
        }
        ChampionKeys.MODIFIER.set(mob, id);
        mob.getAttribute(Attributes.MAX_HEALTH).addOrReplacePermanentModifier(CHAMPION_HEALTH);
        mob.getAttribute(Attributes.ATTACK_DAMAGE).addOrReplacePermanentModifier(CHAMPION_DAMAGE);
        if (id == 0) {
            mob.getAttribute(Attributes.MOVEMENT_SPEED).addOrReplacePermanentModifier(BOLD_SPEED);
        } else if (id == 3) {
            mob.getAttribute(Attributes.ATTACK_DAMAGE).addOrReplacePermanentModifier(MIGHTY_DAMAGE);
        } else if (id == 5) {
            mob.setAbsorptionAmount(mob.getAbsorptionAmount() + (float) (mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2.0));
        }
        mob.heal(25.0F);
        if (!isVanilla(mob, "ender_dragon")) {
            mob.setCustomName(Component.translatable("champion.mod." + NAMES[id]).append(" ").append(mob.getName()));
        }
        if (persist) {
            mob.setPersistenceRequired();
        }
        ChampionKeys.MODIFIER.sync(mob);
    }

    @SubscribeEvent
    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        int state = get(mob);
        if (state != UNPROCESSED) {
            return;
        }
        int whitelist = whitelistLevel(mob);
        if (whitelist < 0) {
            ChampionKeys.MODIFIER.set(mob, NORMAL);
            return;
        }
        int roll = level.getRandom().nextInt(100);
        if (level.getDifficulty() == net.minecraft.world.Difficulty.EASY) {
            roll += 2;
        }
        if (level.getDifficulty() == net.minecraft.world.Difficulty.HARD) {
            roll -= 2;
        }
        if (level.dimension() == net.minecraft.world.level.Level.END) {
            roll -= 2;
        }
        roll -= whitelist - 1;
        if (roll <= 0 && mob.getAttributeBaseValue(Attributes.MAX_HEALTH) >= 10.0) {
            int id = isVanilla(mob, "creeper") ? 0 : level.getRandom().nextInt(NAMES.length);
            assign(mob, id, false);
        } else {
            ChampionKeys.MODIFIER.set(mob, NORMAL);
        }
    }

    private static int whitelistLevel(Mob mob) {
        EntityType<?> type = mob.getType();
        if (isVanilla(mob, "zombie") || isVanilla(mob, "spider") || isVanilla(mob, "blaze") || isVanilla(mob, "enderman") || isVanilla(mob, "skeleton")) {
            return 0;
        }
        if (isVanilla(mob, "witch") || type == ModEntities.WISP.get() || type == ModEntities.PECH.get()
            || type == ModEntities.CULTIST_KNIGHT.get() || type == ModEntities.CULTIST_CLERIC.get()) {
            return 1;
        }
        if (type == ModEntities.ELDRITCH_CRAB.get()) {
            return 0;
        }
        if (type == ModEntities.TAINTACLE.get()) {
            return 2;
        }
        if (type == ModEntities.INHABITED_ZOMBIE.get()) {
            return 3;
        }
        return -1;
    }

    private static boolean isVanilla(Mob mob, String path) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).equals(ResourceLocation.withDefaultNamespace(path));
    }

    @SubscribeEvent
    static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide() || mob.isDeadOrDying()) {
            return;
        }
        int id = get(mob);
        if (id == 5 && mob.getInvulnerableTime() <= 0 && mob.tickCount % 25 == 0) {
            float limit = (float) (mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2.0);
            if (mob.getAbsorptionAmount() < limit) {
                mob.setAbsorptionAmount(mob.getAbsorptionAmount() + 1.0F);
            }
        }
        if (id == 7 && mob.tickCount % 20 == 0) {
            mob.heal(1.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (event.getAmount() <= 0.0F || !(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return;
        }
        if (event.getEntity() instanceof Mob defender) {
            int id = get(defender);
            boolean absorptionBranch = (id == 5 || defender instanceof EldritchGuardian) && defender.getAbsorptionAmount() > 0.0F;
            if (absorptionBranch && defender.level() instanceof ServerLevel serverLevel) {
                int targetId = source.getEntity() == null ? -1 : source.getEntity().getId();
                ModNetwork.sendToNear(serverLevel, null, defender.getX(), defender.getY(), defender.getZ(), 32.0, new ShieldFxPayload(defender.getId(), targetId));
                serverLevel.playSound(null, defender.blockPosition(), ModSounds.RUNIC_SHIELD_EFFECT.get(), SoundSource.HOSTILE, 0.66F, 1.1F + serverLevel.getRandom().nextFloat() * 0.1F);
            } else if (id == 1 && !source.is(DamageTypes.THORNS) && defender.level() instanceof ServerLevel serverLevel) {
                attacker.hurt(defender.damageSources().thorns(defender), 1 + defender.getRandom().nextInt(3));
            } else if (id == 2 && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                event.setAmount(event.getAmount() * 19.0F / 25.0F);
            } else if (id == 12 && defender.level() instanceof ServerLevel serverLevel && serverLevel.getRandom().nextFloat() < 0.4F) {
                var spider = ModEntities.TAINT_SPIDER.get().create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (spider != null) {
                    spider.snapTo(defender.getX(), defender.getY() + defender.getBbHeight() / 2.0, defender.getZ(), serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);
                    serverLevel.addFreshEntity(spider);
                    serverLevel.playSound(null, defender.blockPosition(), ModSounds.GORE.get(), SoundSource.HOSTILE, 0.5F, 1.0F);
                }
            }
        }
        if (event.getAmount() <= 0.0F || !(attacker instanceof Mob mob)) {
            return;
        }
        int id = get(mob);
        if (id < 0 || TYPES[id] != 1 || !(event.getEntity() instanceof LivingEntity target) || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        switch (id) {
            case 4 -> {
                if (level.getRandom().nextFloat() < 0.4F) target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
            }
            case 6 -> {
                if (target instanceof ServerPlayer player && level.getRandom().nextFloat() < 0.33F) {
                    Warp.add(player, 1 + level.getRandom().nextInt(3), WarpType.PERMANENT);
                }
            }
            case 8 -> {
                if (level.getRandom().nextFloat() < 0.4F) target.igniteForSeconds(4.0F);
            }
            case 9 -> {
                if (level.getRandom().nextFloat() < 0.4F) target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 500));
            }
            case 10 -> {
                if (level.getRandom().nextFloat() < 0.4F) target.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
            }
            case 11 -> mob.heal(Math.max(2.0F, event.getAmount() / 2.0F));
            default -> { }
        }
    }

    public static String name(int id) {
        return id >= 0 && id < NAMES.length ? NAMES[id] : "";
    }

    public static int count() {
        return NAMES.length;
    }

    public static List<String> names() {
        return List.of(NAMES);
    }
}
