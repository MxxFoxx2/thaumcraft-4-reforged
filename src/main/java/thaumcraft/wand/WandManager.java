package thaumcraft.wand;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.platform.Baubles;

public final class WandManager {
    private static final Map<Integer, Long> COOLDOWN_SERVER = new HashMap<>();
    private static final Map<Integer, Long> COOLDOWN_CLIENT = new HashMap<>();
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private WandManager() {
    }

    public static float getTotalVisDiscount(@Nullable Player player, @Nullable Aspect aspect) {
        if (player == null) {
            return 0.0F;
        }
        int total = 0;
        for (ItemStack stack : Baubles.stacks(player)) {
            if (stack.getItem() instanceof VisDiscountGear gear) {
                total += gear.getVisDiscount(stack, player, aspect);
            }
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof VisDiscountGear gear) {
                total += gear.getVisDiscount(stack, player, aspect);
            }
        }
        total -= getVisExhaustPenalty(player);
        return total / 100.0F;
    }

    private static int getVisExhaustPenalty(Player player) {
        return 0;
    }

    public static boolean consumeVisFromInventory(Player player, AspectList cost) {
        Inventory inventory = player.getInventory();
        for (int slot = Inventory.INVENTORY_SIZE - 1; slot >= 0; slot--) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof WandItem wand && wand.consumeAllVis(stack, player, cost, true, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        Map<Integer, Long> cooldowns = entity.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER;
        Long until = cooldowns.get(entity.getId());
        return until != null && until > System.currentTimeMillis();
    }

    public static float getCooldown(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            return 0.0F;
        }
        Long until = COOLDOWN_CLIENT.get(entity.getId());
        return until == null ? 0.0F : (until - System.currentTimeMillis()) / 1000.0F;
    }

    public static void setCooldown(LivingEntity entity, int milliseconds) {
        if (milliseconds == 0) {
            COOLDOWN_CLIENT.remove(entity.getId());
            COOLDOWN_SERVER.remove(entity.getId());
        } else if (entity.level().isClientSide()) {
            COOLDOWN_CLIENT.put(entity.getId(), System.currentTimeMillis() + milliseconds);
        } else {
            COOLDOWN_SERVER.put(entity.getId(), System.currentTimeMillis() + milliseconds);
        }
    }
}
