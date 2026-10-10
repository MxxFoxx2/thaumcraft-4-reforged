package thaumcraft.platform;

import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class DataAttachments {
    public record SyncPayload(String key, int entityId, byte[] data) implements CustomPacketPayload {
        public static final Type<SyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Thaumcraft.MODID, "attachment_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncPayload::key,
            ByteBufCodecs.VAR_INT, SyncPayload::entityId,
            ByteBufCodecs.BYTE_ARRAY, SyncPayload::data,
            SyncPayload::new
        );

        @Override
        public Type<SyncPayload> type() {
            return TYPE;
        }
    }

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Thaumcraft.MODID);

    private DataAttachments() {
    }

    static <T> Supplier<AttachmentType<T>> register(String name, Supplier<AttachmentType<T>> factory) {
        return ATTACHMENTS.register(name, factory);
    }

    static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
        ModNetwork.toClient(SyncPayload.TYPE, SyncPayload.STREAM_CODEC);
        ModNetwork.setClientHandler(SyncPayload.TYPE, DataAttachments::receive);
    }

    static <T> void sendSync(DataKey<T> key, Entity holder, boolean toTracking) {
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = key.syncCodec();
        if (codec == null) {
            return;
        }
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), holder.registryAccess());
        codec.encode(buffer, key.current(holder));
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        SyncPayload payload = new SyncPayload(key.name(), holder.getId(), data);
        if (toTracking) {
            ModNetwork.sendToTrackingAndSelf(holder, payload);
        } else if (holder instanceof ServerPlayer player) {
            ModNetwork.sendToPlayer(player, payload);
        }
    }

    private static <T> void receive(SyncPayload payload, Player player) {
        @SuppressWarnings("unchecked")
        DataKey<T> key = (DataKey<T>) DataKey.KEYS.get(payload.key());
        Entity holder = player.level().getEntity(payload.entityId());
        if (key == null || holder == null) {
            return;
        }
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = key.syncCodec();
        if (codec == null) {
            return;
        }
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()), player.registryAccess());
        key.receive(holder, codec.decode(buffer));
    }

    private static void syncAll(Entity holder) {
        for (DataKey<?> key : DataKey.KEYS.values()) {
            key.sync(holder);
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncAll(event.getEntity());
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncAll(event.getEntity());
    }

    @SubscribeEvent
    static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncAll(event.getEntity());
    }
}
