package thaumcraft.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public final class Menus {
    private Menus() {
    }

    @FunctionalInterface
    public interface PosFactory<M extends AbstractContainerMenu> {
        M create(int containerId, Inventory inventory, BlockPos pos);
    }

    public static <M extends AbstractContainerMenu> MenuType<M> withPos(PosFactory<M> factory) {
        return IMenuTypeExtension.create((containerId, inventory, buffer) -> factory.create(containerId, inventory, buffer.readBlockPos()));
    }

    public static <M extends AbstractContainerMenu> MenuType<M> simple(MenuType.MenuSupplier<M> factory) {
        return new MenuType<>(factory, FeatureFlags.VANILLA_SET);
    }

    public static void open(ServerPlayer player, MenuProvider provider) {
        player.openMenu(provider);
    }

    public static void open(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(provider, pos);
    }
}
