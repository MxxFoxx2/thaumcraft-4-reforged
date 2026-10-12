package thaumcraft.world.biome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import thaumcraft.taint.TaintBiomes;

public record TaintDecorationFeature() implements Feature {
    public static final MapCodec<TaintDecorationFeature> CODEC = MapCodec.unit(TaintDecorationFeature::new);

    @Override
    public MapCodec<TaintDecorationFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (level.getBiome(origin).is(TaintBiomes.TAINTED_LAND)) {
            TaintDecorationHook.decorate(level, random, origin);
        }
        return true;
    }
}
