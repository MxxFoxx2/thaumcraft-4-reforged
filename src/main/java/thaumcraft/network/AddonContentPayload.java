package thaumcraft.network;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import thaumcraft.Thaumcraft;

public record AddonContentPayload(Map<String, String> recipes, Map<String, String> research) implements CustomPacketPayload {
    public static final Type<AddonContentPayload> TYPE = new Type<>(Thaumcraft.id("addon_content"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, AddonContentPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.<net.minecraft.network.RegistryFriendlyByteBuf, String, String, Map<String, String>>map(
                LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
            AddonContentPayload::recipes,
            ByteBufCodecs.<net.minecraft.network.RegistryFriendlyByteBuf, String, String, Map<String, String>>map(
                LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
            AddonContentPayload::research,
            AddonContentPayload::new
        );

    @Override
    public Type<AddonContentPayload> type() {
        return TYPE;
    }
}
