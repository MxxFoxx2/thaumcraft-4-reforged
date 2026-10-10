package thaumcraft.client.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.platform.ModNetwork;

public final class ClientNetwork {
    private ClientNetwork() {
    }

    public static <T extends CustomPacketPayload> void handle(CustomPacketPayload.Type<T> type, ModNetwork.Handler<T> handler) {
        ModNetwork.setClientHandler(type, handler);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
