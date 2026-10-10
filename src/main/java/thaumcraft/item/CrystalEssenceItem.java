package thaumcraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;

public class CrystalEssenceItem extends AspectContainerItem {
    public CrystalEssenceItem(Properties properties) {
        super(properties, " x", true);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!AspectKeys.ASPECTS.has(stack)) {
            Aspect[] aspects = Aspect.values();
            AspectTooltips.setSingle(stack, aspects[level.getRandom().nextInt(aspects.length)], 1);
        }
        super.inventoryTick(stack, level, owner, slot);
    }
}
