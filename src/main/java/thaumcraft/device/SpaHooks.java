package thaumcraft.device;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class SpaHooks {
    private SpaHooks() {
    }

    public static boolean isBathSalts(ItemStack stack) {
        return false;
    }

    public static Block mixedFluidBlock() {
        return Blocks.WATER;
    }
}
