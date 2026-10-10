package thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.geom.BoxGeometry;
import thaumcraft.client.platform.ItemGeometry;
import thaumcraft.item.NodeJarData;
import thaumcraft.item.NodeJarItem;

public class NodeJarItemRenderer implements SpecialModelRenderer<NodeJarItemRenderer.Contents> {
    public record Contents(AspectList aspects, NodeJarData data) {
    }

    @Override
    public void submit(
        @Nullable Contents contents,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int lightCoords,
        int overlayCoords,
        boolean hasFoil,
        int outlineColor
    ) {
        if (contents != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.4F, 0.5F);
            NodeRendering.submitCross(collector.order(1), poseStack, contents.aspects(), contents.data().type(), contents.data().modifier());
            poseStack.popPose();
        }
        TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        BoxGeometry.UvRect bottom = rect(atlas.getSprite(Thaumcraft.id("block/jar_bottom")));
        BoxGeometry.UvRect top = rect(atlas.getSprite(Thaumcraft.id("block/jar_top")));
        BoxGeometry.UvRect side = rect(atlas.getSprite(Thaumcraft.id("block/jar_side")));
        ItemGeometry.solid(collector.order(2), poseStack, TextureAtlas.LOCATION_BLOCKS, true, sink -> {
            BoxGeometry.box(sink, 0.1875F, 0.0F, 0.1875F, 0.8125F, 0.75F, 0.8125F, bottom, top, side, 0xFFFFFFFF, lightCoords);
            BoxGeometry.box(sink, 0.3125F, 0.75F, 0.3125F, 0.6875F, 0.875F, 0.6875F, bottom, top, side, 0xFFFFFFFF, lightCoords);
        });
    }

    private static BoxGeometry.UvRect rect(TextureAtlasSprite sprite) {
        return new BoxGeometry.UvRect(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
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
    public @Nullable Contents extractArgument(ItemStack stack) {
        AspectList aspects = NodeJarItem.getAspects(stack);
        return aspects == null ? null : new Contents(aspects, NodeJarItem.getData(stack));
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Contents> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<Contents> bake(SpecialModelRenderer.BakingContext context) {
            return new NodeJarItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
