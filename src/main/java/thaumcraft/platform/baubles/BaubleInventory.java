package thaumcraft.platform.baubles;

import java.util.Collection;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Baubles;

public final class BaubleInventory implements Container, ValueIOSerializable {
    private final Player player;
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(Baubles.slotCount(), ItemStack.EMPTY);
    private boolean blockEvents;

    public BaubleInventory(Player player) {
        this.player = player;
    }

    public Player player() {
        return player;
    }

    public void setBlockEvents(boolean blockEvents) {
        this.blockEvents = blockEvents;
    }

    public static boolean isValidFor(BaubleType type, ItemStack stack, Player player) {
        return !stack.isEmpty() && stack.getItem() instanceof Bauble bauble && bauble.getBaubleType(stack) == type && bauble.canEquip(stack, player);
    }

    private void unequipped(ItemStack stack) {
        if (!blockEvents && !player.level().isClientSide() && stack.getItem() instanceof Bauble bauble) {
            bauble.onUnequipped(stack, player);
        }
    }

    private void equipped(ItemStack stack) {
        if (!blockEvents && !player.level().isClientSide() && stack.getItem() instanceof Bauble bauble) {
            bauble.onEquipped(stack, player);
        }
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return stacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < stacks.size() ? stacks.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = getItem(slot);
        if (stack.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.split(count);
        unequipped(removed);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        stacks.set(slot, ItemStack.EMPTY);
        setChanged();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= stacks.size()) {
            return;
        }
        ItemStack previous = stacks.get(slot);
        if (!previous.isEmpty()) {
            unequipped(previous);
        }
        stacks.set(slot, stack);
        if (!stack.isEmpty()) {
            equipped(stack);
        }
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isValidFor(Baubles.slotType(slot), stack, player);
    }

    @Override
    public void setChanged() {
        if (!player.level().isClientSide()) {
            player.syncData(BaubleAttachments.BAUBLES);
        }
    }

    @Override
    public boolean stillValid(Player user) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    public void dropAll(Collection<ItemEntity> drops) {
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), stack.copy());
            entity.setPickUpDelay(40);
            float speed = player.getRandom().nextFloat() * 0.5F;
            float angle = player.getRandom().nextFloat() * (float) Math.PI * 2.0F;
            entity.setDeltaMovement(new Vec3(-Mth.sin(angle) * speed, 0.2F, Mth.cos(angle) * speed));
            drops.add(entity);
            stacks.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        for (ItemStack stack : stacks) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        }
    }

    public void read(RegistryFriendlyByteBuf buffer) {
        for (int slot = 0; slot < stacks.size(); slot++) {
            stacks.set(slot, ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        ContainerHelper.saveAllItems(output, stacks);
    }

    @Override
    public void deserialize(ValueInput input) {
        ContainerHelper.loadAllItems(input, stacks);
    }
}
