package thaumcraft.block;

import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class InfusedStoneBlock extends DropExperienceBlock {
    private final int color;

    public InfusedStoneBlock(int color, Properties properties) {
        super(UniformInt.of(0, 3), properties);
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            RandomSource random = level.getRandom();
            for (int index = 0; index < 3; index++) {
                level.addParticle(
                    new DustParticleOptions(Vec3.fromRGB24(color).toVector3f(), 1.0F),
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0.0,
                    0.0,
                    0.0
                );
            }
        }
        super.attack(state, level, pos, player);
    }
}
