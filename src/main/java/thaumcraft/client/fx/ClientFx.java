package thaumcraft.client.fx;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.client.ParticleStatus;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.geom.LightningBolt;
import thaumcraft.client.platform.ParticleSprites;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.fx.FxProxy;

public final class ClientFx implements FxProxy {
    @Override
    public void beamPulse(Level level, Entity source, Entity target, int color) {
        BeamFx beam = new BeamFx(
            level,
            source.getX(),
            source.getY() + source.getEyeHeight(),
            source.getZ(),
            target,
            (color >> 16 & 255) / 255.0F,
            (color >> 8 & 255) / 255.0F,
            (color & 255) / 255.0F,
            20
        );
        beam.setBlendMode(771);
        beam.setWidth(2.5F);
        beam.setType(1);
        beam.setReverse(true);
        beam.setPulse(true);
        WorldFxRenderer.add(beam);
    }

    @Override
    public void nodeBolt(Level level, float x, float y, float z, float targetX, float targetY, float targetZ) {
        BoltRenderer.add(new LightningBolt(x, y, z, targetX, targetY, targetZ, level.getRandom().nextLong(), 10, 4.0F, 5).setType(0).defaultFractal());
    }

    @Override
    public void visDrain(Level level, BlockPos drainer, BlockPos source, int primal) {
        List<Aspect> primals = Aspect.getPrimalAspects();
        if (primal < 0 || primal >= primals.size()) {
            return;
        }
        RandomSource random = level.getRandom();
        WorldFxRenderer.add(new VisSparkleFx(
            random,
            source.getX() + 0.4 + random.nextFloat() * 0.2F,
            source.getY() + 0.4 + random.nextFloat() * 0.2F,
            source.getZ() + 0.4 + random.nextFloat() * 0.2F,
            drainer.getX() + random.nextFloat(),
            drainer.getY() + random.nextFloat(),
            drainer.getZ() + random.nextFloat(),
            primals.get(primal).getColor()
        ));
    }

    public static int particleCount(int base) {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        if (status == ParticleStatus.MINIMAL) {
            return 0;
        }
        return status == ParticleStatus.DECREASED ? base : base * 2;
    }

    @Override
    public void blockSparkle(Level level, BlockPos pos, int color, int count) {
        RandomSource random = level.getRandom();
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        for (int index = 0; index < particleCount(count); index++) {
            if (color == -9999) {
                red = 0.33F + random.nextFloat() * 0.67F;
                green = 0.33F + random.nextFloat() * 0.67F;
                blue = 0.33F + random.nextFloat() * 0.67F;
            }
            GenericFx fx = new GenericFx(
                random,
                pos.getX() - 0.1F + random.nextFloat() * 1.2F,
                pos.getY() - 0.1F + random.nextFloat() * 1.2F,
                pos.getZ() - 0.1F + random.nextFloat() * 1.2F,
                0.0,
                random.nextFloat() * 0.02,
                0.0,
                false,
                112,
                9,
                1,
                5 + random.nextInt(8),
                random.nextInt(10),
                0.7F + random.nextFloat() * 0.4F
            );
            fx.color(red - 0.2F + random.nextFloat() * 0.4F, green - 0.2F + random.nextFloat() * 0.4F, blue - 0.2F + random.nextFloat() * 0.4F);
            fx.alpha(0.9F);
            WorldFxRenderer.add(fx);
        }
    }

    @Override
    public void crucibleBoil(Level level, BlockPos pos, float fluidHeight, int aspectAmount, int count) {
        RandomSource random = level.getRandom();
        List<Aspect> aspects = level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible ? crucible.getAspects().getAspects() : List.of();
        for (int index = 0; index < particleCount(1); index++) {
            BubbleFx fx = new BubbleFx(
                random,
                pos.getX() + 0.2F + random.nextFloat() * 0.6F,
                pos.getY() + 0.1F + fluidHeight,
                pos.getZ() + 0.2F + random.nextFloat() * 0.6F,
                3
            );
            if (aspects.isEmpty()) {
                fx.color(1.0F, 1.0F, 1.0F);
            } else {
                int color = aspects.get(random.nextInt(aspects.size())).getColor();
                fx.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
            }
            fx.bubbleSpeed(0.003 * count);
            WorldFxRenderer.add(fx);
        }
    }

    @Override
    public void crucibleBubble(Level level, float x, float y, float z, float red, float green, float blue) {
        BubbleFx fx = new BubbleFx(level.getRandom(), x, y, z, 1);
        fx.color(red, green, blue);
        WorldFxRenderer.add(fx);
    }

    @Override
    public void crucibleFroth(Level level, float x, float y, float z) {
        BubbleFx fx = new BubbleFx(level.getRandom(), x, y, z, -4);
        fx.color(0.5F, 0.5F, 0.7F);
        fx.froth();
        WorldFxRenderer.add(fx);
    }

    @Override
    public void crucibleFrothDown(Level level, float x, float y, float z) {
        BubbleFx fx = new BubbleFx(level.getRandom(), x, y, z, -4);
        fx.color(0.5F, 0.5F, 0.7F);
        fx.froth2();
        WorldFxRenderer.add(fx);
    }

    @Override
    public void ventParticles(Level level, double x, double y, double z, double motionX, double motionY, double motionZ, int color, float scale) {
        WorldFxRenderer.add(new VentFx(level.getRandom(), x, y, z, motionX, motionY, motionZ, color, scale));
    }

    @Override
    public void essentiaSource(Level level, BlockPos pos, int dx, int dy, int dz, int color) {
        EssentiaSourceTracker.add(pos, pos.offset(-dx, -dy, -dz), color);
    }

    @Override
    public void essentiaTrail(Level level, BlockPos from, BlockPos to, int count, int color, float scale) {
        WorldFxRenderer.add(new EssentiaTrailFx(
            level.getRandom(),
            from.getX() + 0.5,
            from.getY() + 0.5,
            from.getZ() + 0.5,
            to.getX() + 0.5,
            to.getY() + 0.5,
            to.getZ() + 0.5,
            count,
            color,
            scale
        ));
    }

    @Override
    public void infusionItemParticles(Level level, double x, double y, double z, BlockPos target, ItemStack stack) {
        RandomSource random = level.getRandom();
        TextureAtlasSprite sprite = ParticleSprites.item(level, stack, random);
        if (sprite == null) {
            return;
        }
        BoreParticleFx fx = new BoreParticleFx(random, x, y, z, target.getX() + 0.5, target.getY() - 0.5, target.getZ() + 0.5, sprite);
        fx.motion((float) random.nextGaussian() * 0.03F, (float) random.nextGaussian() * 0.03F, (float) random.nextGaussian() * 0.03F);
        WorldFxRenderer.add(fx);
    }

    @Override
    public void infusionBlockParticles(Level level, double x, double y, double z, BlockPos target, BlockState state) {
        TextureAtlasSprite sprite = ParticleSprites.block(state);
        if (sprite == null) {
            return;
        }
        WorldFxRenderer.add(new BoreParticleFx(level.getRandom(), x, y, z, target.getX() + 0.5, target.getY() - 0.5, target.getZ() + 0.5, sprite));
    }

    @Override
    public void infusionSparkle(Level level, double x, double y, double z, BlockPos target, boolean alternate) {
        RandomSource random = level.getRandom();
        BoreSparkleFx fx = new BoreSparkleFx(random, x, y, z, target.getX() + 0.5, target.getY() - 0.5, target.getZ() + 0.5);
        if (alternate) {
            fx.color(0.2F, 0.6F + random.nextFloat() * 0.3F, 0.3F);
        } else {
            fx.color(0.4F + random.nextFloat() * 0.2F, 0.2F, 0.6F + random.nextFloat() * 0.3F);
        }
        WorldFxRenderer.add(fx);
    }

    @Override
    public void blockRunes(Level level, double x, double y, double z, float red, float green, float blue, int duration, float gravity) {
        WorldFxRenderer.add(new BlockRunesFx(level.getRandom(), x + 0.5, y + 0.5, z + 0.5, red, green, blue, duration, gravity));
    }
}
