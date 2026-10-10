package thaumcraft.client.color;

import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.Thaumcraft;
import thaumcraft.block.InfusedStoneBlock;
import thaumcraft.registry.ModBlocks;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class BlockTints {
    private BlockTints() {
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        List<DeferredBlock<InfusedStoneBlock>> infusedStones = List.of(
            ModBlocks.AIR_INFUSED_STONE,
            ModBlocks.FIRE_INFUSED_STONE,
            ModBlocks.WATER_INFUSED_STONE,
            ModBlocks.EARTH_INFUSED_STONE,
            ModBlocks.ORDER_INFUSED_STONE,
            ModBlocks.ENTROPY_INFUSED_STONE
        );
        for (DeferredBlock<InfusedStoneBlock> stone : infusedStones) {
            event.register(List.of(constant(stone.get().getColor())), stone.get());
        }
        event.register(List.of(BlockTintSources.foliage()), ModBlocks.GREATWOOD_LEAVES.get());
        event.register(List.of(constant(0x8899AA)), ModBlocks.SILVERWOOD_LEAVES.get());
        for (int index = 0; index < ModBlocks.CANDLE_COLORS.length; index++) {
            Block candle = ModBlocks.CANDLES.get(ModBlocks.CANDLE_COLORS[index]).get();
            event.register(List.of(constant(ModBlocks.CANDLE_TINTS[index])), candle);
        }
    }

    private static BlockTintSource constant(int color) {
        return BlockTintSources.constant(FastColor.ARGB32.opaque(color));
    }
}
