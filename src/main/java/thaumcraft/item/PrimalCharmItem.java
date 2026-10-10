package thaumcraft.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import javax.annotation.Nullable;
import thaumcraft.research.ResearchManager;

public class PrimalCharmItem extends Item {
    public PrimalCharmItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if (owner instanceof ServerPlayer player
            && level.getRandom().nextInt(20000) == 42
            && !ResearchManager.isComplete(player, "FOCUSPRIMAL")
            && !ResearchManager.isComplete(player, "@FOCUSPRIMAL")) {
            player.sendSystemMessage(Component.translatable("tc.primalcharm.trigger").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            ResearchManager.complete(player, "@FOCUSPRIMAL");
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        RandomSource random = RandomSource.create(stack.hashCode() + System.currentTimeMillis() / 6000L);
        if (random.nextInt(200) < 25) {
            builder.accept(Component.translatable("tc.primalcharm." + random.nextInt(5)).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, display, builder, flag);
    }
}
