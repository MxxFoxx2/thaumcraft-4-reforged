package thaumcraft.client.color;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;

public record AspectTint() implements ItemTintSource {
    public static final MapCodec<AspectTint> MAP_CODEC = MapCodec.unit(new AspectTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        AspectList aspects = AspectKeys.ASPECTS.getOrDefault(stack, AspectList.EMPTY);
        if (aspects.isEmpty()) {
            return 0xFFFFFFFF;
        }
        return FastColor.ARGB32.opaque(aspects.getAspects().get(0).getColor());
    }

    @Override
    public MapCodec<AspectTint> type() {
        return MAP_CODEC;
    }
}
