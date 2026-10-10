package thaumcraft.platform.baubles;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;

public final class BaubleAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Thaumcraft.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BaubleInventory>> BAUBLES = ATTACHMENTS.register("baubles", () -> AttachmentType
        .serializable(holder -> new BaubleInventory((Player) holder))
        .copyOnDeath()
        .sync(new AttachmentSyncHandler<BaubleInventory>() {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, BaubleInventory attachment, boolean initialSync) {
                attachment.write(buffer);
            }

            @Override
            public @Nullable BaubleInventory read(IAttachmentHolder holder, RegistryFriendlyByteBuf buffer, @Nullable BaubleInventory previousValue) {
                BaubleInventory inventory = previousValue != null ? previousValue : new BaubleInventory((Player) holder);
                inventory.read(buffer);
                return inventory;
            }
        })
        .build());

    private BaubleAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static BaubleInventory inventory(Player player) {
        return player.getData(BAUBLES);
    }
}
