package thaumcraft.research;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.WarpingGear;
import thaumcraft.network.WarpMessagePayload;
import thaumcraft.platform.Baubles;
import thaumcraft.platform.ModNetwork;

public final class Warp {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private Warp() {
    }

    public static int get(Player player, WarpType type) {
        return Knowledge.get(player).getWarp(type);
    }

    public static int total(Player player) {
        return Knowledge.get(player).getTotalWarp();
    }

    public static void add(ServerPlayer player, int amount, WarpType type) {
        if (amount == 0) {
            return;
        }
        PlayerKnowledge knowledge = Knowledge.get(player);
        if (amount < 0 && (type == WarpType.PERMANENT || knowledge.getWarp(type) <= 0)) {
            return;
        }
        knowledge.setWarp(type, knowledge.getWarp(type) + amount);
        knowledge.setWarpCounter(knowledge.getTotalWarp());
        Knowledge.sync(player);
        ModNetwork.sendToPlayer(player, new WarpMessagePayload(type.ordinal(), amount));
    }

    public static void set(ServerPlayer player, int amount, WarpType type) {
        Knowledge.get(player).setWarp(type, amount);
        Knowledge.sync(player);
    }

    public static int getFinalWarp(ItemStack stack, Player player) {
        return !stack.isEmpty() && stack.getItem() instanceof WarpingGear gear ? gear.getWarp(stack, player) : 0;
    }

    public static int getWarpFromGear(Player player) {
        int warp = getFinalWarp(player.getMainHandItem(), player);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            warp += getFinalWarp(player.getItemBySlot(slot), player);
        }
        for (ItemStack stack : Baubles.stacks(player)) {
            warp += getFinalWarp(stack, player);
        }
        return warp;
    }
}
