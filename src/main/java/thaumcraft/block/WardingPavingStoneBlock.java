package thaumcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.blockentity.WardingStoneBlockEntity;
import thaumcraft.registry.ModBlockEntities;

public class WardingPavingStoneBlock extends Block implements EntityBlock {
    public WardingPavingStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardingStoneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.WARDING_STONE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> WardingStoneBlockEntity.serverTick(tickLevel, pos, tickState, (WardingStoneBlockEntity) entity);
    }
}
