package thaumcraft.client.color;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.research.ResearchNote;
import thaumcraft.research.ResearchNoteKeys;

public record ResearchNoteTint() implements ItemTintSource {
    public static final MapCodec<ResearchNoteTint> MAP_CODEC = MapCodec.unit(new ResearchNoteTint());
    private static final int DEFAULT_COLOR = 10066329;

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        ResearchNote note = ResearchNoteKeys.NOTE.get(stack);
        return FastColor.ARGB32.opaque(note == null ? DEFAULT_COLOR : note.color());
    }

    @Override
    public MapCodec<ResearchNoteTint> type() {
        return MAP_CODEC;
    }
}
