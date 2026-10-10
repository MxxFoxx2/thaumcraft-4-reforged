package thaumcraft.platform;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;

public final class Registration {
    private Registration() {
    }

    private static ToolMaterial convert(TcToolMaterial material) {
        return new ToolMaterial(
            material.incorrectForDrops(),
            material.durability(),
            material.miningSpeed(),
            material.attackDamageBonus(),
            material.enchantability(),
            material.repairItems()
        );
    }

    private static ArmorType convert(ArmorSlot slot) {
        return switch (slot) {
            case HELMET -> ArmorType.HELMET;
            case CHESTPLATE -> ArmorType.CHESTPLATE;
            case LEGGINGS -> ArmorType.LEGGINGS;
            case BOOTS -> ArmorType.BOOTS;
        };
    }

    private static ArmorMaterial convert(TcArmorMaterial material) {
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Thaumcraft.id(material.assetName()));
        Map<ArmorType, Integer> defense = Map.of(
            ArmorType.HELMET, material.helmetDefense(),
            ArmorType.CHESTPLATE, material.chestplateDefense(),
            ArmorType.LEGGINGS, material.leggingsDefense(),
            ArmorType.BOOTS, material.bootsDefense(),
            ArmorType.BODY, material.chestplateDefense()
        );
        return new ArmorMaterial(
            material.durabilityMultiplier(),
            defense,
            material.enchantability(),
            material.equipSound(),
            material.toughness(),
            material.knockbackResistance(),
            material.repairItems(),
            asset
        );
    }

    public static Item.Properties sword(Item.Properties properties, TcToolMaterial material, float attackDamage, float attackSpeed) {
        return properties.sword(convert(material), attackDamage, attackSpeed);
    }

    public static Item.Properties pickaxe(Item.Properties properties, TcToolMaterial material, float attackDamage, float attackSpeed) {
        return properties.pickaxe(convert(material), attackDamage, attackSpeed);
    }

    public static Item.Properties axe(Item.Properties properties, TcToolMaterial material, float attackDamage, float attackSpeed) {
        return properties.axe(convert(material), attackDamage, attackSpeed);
    }

    public static Item.Properties shovel(Item.Properties properties, TcToolMaterial material, float attackDamage, float attackSpeed) {
        return properties.shovel(convert(material), attackDamage, attackSpeed);
    }

    public static Item.Properties hoe(Item.Properties properties, TcToolMaterial material, float attackDamage, float attackSpeed) {
        return properties.hoe(convert(material), attackDamage, attackSpeed);
    }

    public static Item.Properties armor(Item.Properties properties, TcArmorMaterial material, ArmorSlot slot) {
        return properties.humanoidArmor(convert(material), convert(slot));
    }

    public static Item.Properties fuel(Item.Properties properties, int ticks) {
        return properties.component(
            DataComponents.COOKING_FUEL,
            new CookingFuel(new ResolvableInt.Constant(ticks), ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER))
        );
    }

    public static Item.Properties food(Item.Properties properties, int nutrition, float saturation) {
        return food(properties, nutrition, saturation, false, 1.6F, null, 0.0F);
    }

    public static Item.Properties food(Item.Properties properties, int nutrition, float saturation, boolean alwaysEdible, float seconds) {
        return food(properties, nutrition, saturation, alwaysEdible, seconds, null, 0.0F);
    }

    public static Item.Properties food(
        Item.Properties properties,
        int nutrition,
        float saturation,
        boolean alwaysEdible,
        float seconds,
        @Nullable MobEffectInstance effect,
        float chance
    ) {
        FoodProperties.Builder food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
        if (alwaysEdible) {
            food.alwaysEdible();
        }
        Consumable.Builder consumable = Consumables.defaultFood().consumeSeconds(seconds);
        if (effect != null) {
            consumable.onConsume(new ApplyStatusEffectsConsumeEffect(effect, chance));
        }
        return properties.food(food.build(), consumable.build());
    }
}
