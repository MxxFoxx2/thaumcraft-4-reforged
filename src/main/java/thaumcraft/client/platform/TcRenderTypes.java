package thaumcraft.client.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.ResourceLocation;

public final class TcRenderTypes {
    private static final Map<ResourceLocation, RenderType> ADDITIVE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ADDITIVE_NO_DEPTH = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_NO_DEPTH = new HashMap<>();

    private TcRenderTypes() {
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

    public static RenderType additive(ResourceLocation texture) {
        return ADDITIVE.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_additive",
            RenderSetup.builder(ModRenderPipelines.ADDITIVE_PARTICLE)
                .setOitPipelines(RenderPipelines.OIT_PARTICLE)
                .withTexture("Sampler0", key, () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR))
                .useLightmap()
                .createRenderSetup()
        ));
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_translucent",
            RenderSetup.builder(ModRenderPipelines.TRANSLUCENT_PARTICLE)
                .setOitPipelines(RenderPipelines.OIT_PARTICLE)
                .withTexture("Sampler0", key, () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR))
                .useLightmap()
                .createRenderSetup()
        ));
    }

    public static RenderType additiveNoDepth(ResourceLocation texture) {
        return ADDITIVE_NO_DEPTH.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_additive_no_depth",
            RenderSetup.builder(ModRenderPipelines.ADDITIVE_NO_DEPTH)
                .setOitPipelines(RenderPipelines.OIT_PARTICLE)
                .withTexture("Sampler0", key, () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR))
                .useLightmap()
                .createRenderSetup()
        ));
    }

    public static RenderType translucentNoDepth(ResourceLocation texture) {
        return TRANSLUCENT_NO_DEPTH.computeIfAbsent(texture, key -> RenderType.create(
            "thaumcraft_translucent_no_depth",
            RenderSetup.builder(ModRenderPipelines.TRANSLUCENT_NO_DEPTH)
                .setOitPipelines(RenderPipelines.OIT_PARTICLE)
                .withTexture("Sampler0", key, () -> RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR))
                .useLightmap()
                .createRenderSetup()
        ));
    }

    public static RenderType byBlend(ResourceLocation texture, int blendMode, boolean depth) {
        if (depth) {
            return byBlend(texture, blendMode);
        }
        return blendMode == 771 ? translucentNoDepth(texture) : additiveNoDepth(texture);
    }

    public static RenderType entityCutout(ResourceLocation texture) {
        return RenderTypes.entityCutout(texture);
    }

    public static RenderType entityTranslucent(ResourceLocation texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    public static RenderType byBlend(ResourceLocation texture, int blendMode) {
        return blendMode == 771 ? translucent(texture) : additive(texture);
    }
}
