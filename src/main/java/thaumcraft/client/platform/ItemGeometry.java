package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.geom.QuadSink;

public final class ItemGeometry {
    private ItemGeometry() {
    }

    public static void solid(OrderedSubmitNodeCollector collector, PoseStack poseStack, ResourceLocation texture, boolean translucent, Consumer<QuadSink> geometry) {
        RenderType renderType = translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> geometry.accept(new EntityQuadSink(pose, buffer)));
    }

    public static void blended(OrderedSubmitNodeCollector collector, PoseStack poseStack, ResourceLocation texture, int blendMode, Consumer<QuadSink> geometry) {
        collector.submitCustomGeometry(
            poseStack,
            TcRenderTypes.byBlend(texture, blendMode),
            (pose, buffer) -> geometry.accept(new VertexQuadSink(pose, buffer))
        );
    }
}
