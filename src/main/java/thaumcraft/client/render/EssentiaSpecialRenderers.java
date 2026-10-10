package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.essentia.EssentiaKeys;

public final class EssentiaSpecialRenderers {
    private static final int JAR_MAX_AMOUNT = 64;

    private EssentiaSpecialRenderers() {
    }

    private static void cubeExtents(Consumer<Vector3fc> output) {
        for (float x : new float[] {0.0F, 1.0F}) {
            for (float y : new float[] {0.0F, 1.0F}) {
                for (float z : new float[] {0.0F, 1.0F}) {
                    output.accept(new Vector3f(x, y, z));
                }
            }
        }
    }

    public record JarContents(@Nullable Aspect aspect, int amount, @Nullable Aspect filter) {
    }

    public record FilledJar(boolean voidJar) implements SpecialModelRenderer<JarContents> {
        @Override
        public void submit(
            @Nullable JarContents contents,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
        ) {
            if (contents != null) {
                EssentiaJarRendering.submit(collector, poseStack, lightCoords, 0, contents.aspect(), contents.amount(), JAR_MAX_AMOUNT, contents.filter(), Direction.EAST);
            }
            EssentiaJarRendering.glass(collector, poseStack, lightCoords, voidJar);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (float x : new float[] {0.1875F, 0.8125F}) {
                for (float y : new float[] {0.0F, 0.875F}) {
                    for (float z : new float[] {0.1875F, 0.8125F}) {
                        output.accept(new Vector3f(x, y, z));
                    }
                }
            }
        }

        @Override
        public @Nullable JarContents extractArgument(ItemStack stack) {
            AspectList aspects = AspectKeys.ASPECTS.get(stack);
            Aspect aspect = null;
            int amount = 0;
            if (aspects != null && aspects.size() == 1) {
                aspect = aspects.getAspects().get(0);
                amount = aspects.getAmount(aspect);
            }
            return new JarContents(aspect, amount, EssentiaKeys.FILTER.get(stack));
        }

        public record Unbaked(boolean voidJar) implements SpecialModelRenderer.Unbaked<JarContents> {
            public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("void", false).forGetter(Unbaked::voidJar)
            ).apply(instance, Unbaked::new));

            @Override
            public SpecialModelRenderer<JarContents> bake(SpecialModelRenderer.BakingContext context) {
                return new FilledJar(voidJar);
            }

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }
        }
    }

    public record Centrifuge() implements NoDataSpecialModelRenderer {
        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            EssentiaRendering.centrifuge(collector, poseStack, 0.0F, lightCoords);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            cubeExtents(output);
        }

        public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public Centrifuge bake(BakingContext context) {
                return new Centrifuge();
            }

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }
        }
    }

    public record Crystallizer() implements NoDataSpecialModelRenderer {
        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            EssentiaRendering.crystallizer(collector, poseStack, Direction.DOWN, 0.0F, 1.0F, 1.0F, 1.0F, EssentiaRendering.viewerTicks(), lightCoords);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            cubeExtents(output);
        }

        public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public Crystallizer bake(BakingContext context) {
                return new Crystallizer();
            }

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }
        }
    }

    public record Reservoir() implements NoDataSpecialModelRenderer {
        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            EssentiaRendering.reservoir(collector, poseStack, Direction.DOWN, lightCoords);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            cubeExtents(output);
        }

        public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public Reservoir bake(BakingContext context) {
                return new Reservoir();
            }

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }
        }
    }

    public record TubeValve() implements NoDataSpecialModelRenderer {
        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            EssentiaRendering.valve(collector, poseStack, Direction.EAST, 0.0F, lightCoords);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (float x : new float[] {0.25F, 0.75F}) {
                for (float y : new float[] {0.25F, 0.75F}) {
                    for (float z : new float[] {0.25F, 0.75F}) {
                        output.accept(new Vector3f(x, y, z));
                    }
                }
            }
        }

        public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public TubeValve bake(BakingContext context) {
                return new TubeValve();
            }

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }
        }
    }
}
