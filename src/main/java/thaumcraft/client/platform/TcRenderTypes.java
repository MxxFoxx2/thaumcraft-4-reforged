package thaumcraft.client.platform;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class TcRenderTypes extends RenderType {
    private static final ShaderStateShard PARTICLE_SHADER = new ShaderStateShard(GameRenderer::getParticleShader);
    private static final Map<ResourceLocation, RenderType> ADDITIVE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ADDITIVE_NO_DEPTH = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_NO_DEPTH = new HashMap<>();

    private TcRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                          Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public enum Kind {
        ADDITIVE,
        TRANSLUCENT,
        ADDITIVE_NO_DEPTH,
        TRANSLUCENT_NO_DEPTH,
        ENTITY_CUTOUT,
        ENTITY_TRANSLUCENT
    }

    public static RenderType of(Kind kind, ResourceLocation texture) {
        return switch (kind) {
            case ADDITIVE -> additive(texture);
            case TRANSLUCENT -> translucent(texture);
            case ADDITIVE_NO_DEPTH -> additiveNoDepth(texture);
            case TRANSLUCENT_NO_DEPTH -> translucentNoDepth(texture);
            case ENTITY_CUTOUT -> entityCutout(texture);
            case ENTITY_TRANSLUCENT -> entityTranslucent(texture);
        };
    }

    private static RenderType particleLike(String name, ResourceLocation texture, TransparencyStateShard transparency, DepthTestStateShard depthTest) {
        return RenderType.create(
            name,
            DefaultVertexFormat.PARTICLE,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(PARTICLE_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(transparency)
                .setLightmapState(LIGHTMAP)
                .setCullState(NO_CULL)
                .setDepthTestState(depthTest)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)
        );
    }

    public static RenderType additive(ResourceLocation texture) {
        return ADDITIVE.computeIfAbsent(texture, key -> particleLike("thaumcraft_additive", key, LIGHTNING_TRANSPARENCY, LEQUAL_DEPTH_TEST));
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.computeIfAbsent(texture, key -> particleLike("thaumcraft_translucent", key, TRANSLUCENT_TRANSPARENCY, LEQUAL_DEPTH_TEST));
    }

    public static RenderType additiveNoDepth(ResourceLocation texture) {
        return ADDITIVE_NO_DEPTH.computeIfAbsent(texture, key -> particleLike("thaumcraft_additive_no_depth", key, LIGHTNING_TRANSPARENCY, NO_DEPTH_TEST));
    }

    public static RenderType translucentNoDepth(ResourceLocation texture) {
        return TRANSLUCENT_NO_DEPTH.computeIfAbsent(texture, key -> particleLike("thaumcraft_translucent_no_depth", key, TRANSLUCENT_TRANSPARENCY, NO_DEPTH_TEST));
    }

    public static RenderType byBlend(ResourceLocation texture, int blendMode, boolean depth) {
        if (depth) {
            return byBlend(texture, blendMode);
        }
        return blendMode == 771 ? translucentNoDepth(texture) : additiveNoDepth(texture);
    }

    public static RenderType byBlend(ResourceLocation texture, int blendMode) {
        return blendMode == 771 ? translucent(texture) : additive(texture);
    }

    public static RenderType outline(ResourceLocation texture) {
        return RenderType.outline(texture);
    }
}
