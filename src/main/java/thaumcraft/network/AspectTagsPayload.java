package thaumcraft.network;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.EntityAspects;

public record AspectTagsPayload(Map<Item, AspectList> items, Map<Block, AspectList> blocks, Map<EntityType<?>, List<EntityAspects>> entities) implements CustomPacketPayload {
    public static final Type<AspectTagsPayload> TYPE = new Type<>(Thaumcraft.id("aspect_tags"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectTagsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.<RegistryFriendlyByteBuf, Item, AspectList, Map<Item, AspectList>>map(
            LinkedHashMap::new,
            ByteBufCodecs.registry(Registries.ITEM),
            AspectList.STREAM_CODEC.cast()
        ),
        AspectTagsPayload::items,
        ByteBufCodecs.<RegistryFriendlyByteBuf, Block, AspectList, Map<Block, AspectList>>map(
            LinkedHashMap::new,
            ByteBufCodecs.registry(Registries.BLOCK),
            AspectList.STREAM_CODEC.cast()
        ),
        AspectTagsPayload::blocks,
        ByteBufCodecs.<RegistryFriendlyByteBuf, EntityType<?>, List<EntityAspects>, Map<EntityType<?>, List<EntityAspects>>>map(
            LinkedHashMap::new,
            ByteBufCodecs.registry(Registries.ENTITY_TYPE),
            EntityAspects.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().apply(ByteBufCodecs.collection(ArrayList::new))
        ),
        AspectTagsPayload::entities,
        AspectTagsPayload::new
    );

    @Override
    public Type<AspectTagsPayload> type() {
        return TYPE;
    }
}
