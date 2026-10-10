package thaumcraft.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.essentia.EssentiaKeys;

public final class AlchemyHooks {
    private AlchemyHooks() {
    }

    public static boolean isNitor(BlockGetter level, BlockPos pos, BlockState state) {
        return false;
    }

    public static boolean spawnThaumicSlime(ServerLevel level, BlockPos pos, int size) {
        return false;
    }

    public static boolean taintFromFlux(ServerLevel level, BlockPos pos) {
        return false;
    }

    public static boolean isThaumicSlime(Entity entity) {
        return false;
    }

    public static int getSlimeSize(Entity entity) {
        return 0;
    }

    public static void setSlimeSize(Entity entity, int size) {
    }

    public static @Nullable Aspect getJarFilter(ItemStack jar) {
        return EssentiaKeys.FILTER.get(jar);
    }
}
