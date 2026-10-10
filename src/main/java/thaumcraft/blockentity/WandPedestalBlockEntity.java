package thaumcraft.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.node.Node;
import thaumcraft.registry.ModBlockEntities;
import thaumcraft.registry.ModBlocks;
import thaumcraft.wand.Wands;

public class WandPedestalBlockEntity extends TcBlockEntity {
    private ItemStack stack = ItemStack.EMPTY;
    private int counter;
    private boolean somethingChanged;
    private boolean draining;
    private BlockPos drainPos = BlockPos.ZERO;
    private int drainColor;
    private List<BlockPos> nodes;

    public WandPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WAND_PEDESTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WandPedestalBlockEntity entity) {
        entity.tick(level);
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        sync();
    }

    public ItemStack removeStack() {
        ItemStack removed = stack;
        stack = ItemStack.EMPTY;
        sync();
        return removed;
    }

    public boolean isDraining() {
        return draining;
    }

    public BlockPos getDrainPos() {
        return drainPos;
    }

    public int getDrainColor() {
        return drainColor;
    }

    public AspectList getAspects() {
        if (Wands.isWand(stack)) {
            AspectList all = Wands.getAllVis(stack);
            AspectList out = new AspectList();
            for (Aspect aspect : all.getAspectsSorted()) {
                out.add(aspect, all.getAmount(aspect) / 100);
            }
            return out;
        }
        return null;
    }

    public int getComparatorSignal() {
        if (Wands.isWand(stack)) {
            float ratio = Wands.getAllVis(stack).visSize() / (Wands.getMaxVis(stack) * 6.0F);
            return (int) Math.floor(ratio * 14.0F) + 1;
        }
        return 0;
    }

    private void setDraining(boolean value, BlockPos target, int color) {
        if (draining != value || (value && (!drainPos.equals(target) || drainColor != color))) {
            draining = value;
            if (value) {
                drainPos = target;
                drainColor = color;
            }
            sync();
        }
    }

    private void tick(Level tickLevel) {
        if (nodes == null) {
            findNodes(tickLevel);
        }
        counter++;
        boolean recalc = false;
        if (counter % 20 == 0 && somethingChanged && !nodes.isEmpty() && !stack.isEmpty()) {
            tickLevel.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            somethingChanged = false;
        }
        if (counter % 5 == 0 && !nodes.isEmpty() && !stack.isEmpty() && Wands.isWand(stack)) {
            boolean compound = tickLevel.getBlockState(worldPosition.above()).is(ModBlocks.COMPOUND_RECHARGE_FOCUS.get());
            int min = Wands.getCap(stack).tag().equals("iron") || Wands.getRod(stack).tag().equals("wood") ? 0 : 1;
            AspectList room = Wands.getAspectsWithRoom(stack);
            boolean found = false;
            if (room.size() > 0) {
                search:
                for (BlockPos nodePos : nodes) {
                    BlockEntity entity = tickLevel.getBlockEntity(nodePos);
                    if (entity instanceof Node node && !(entity instanceof NodeJarBlockEntity)) {
                        for (Aspect aspect : room.getAspects()) {
                            if (node.getAspects().getAmount(aspect) > min) {
                                Wands.addVis(stack, aspect, 1, true);
                                node.takeFromContainer(aspect, 1);
                                somethingChanged = true;
                                found = true;
                                setDraining(true, nodePos, aspect.getColor());
                                break search;
                            }
                        }
                        if (compound) {
                            for (Aspect aspect : node.getAspects().getAspects()) {
                                if (!aspect.isPrimal()) {
                                    AspectList primals = AspectHelper.reduceToPrimals(new AspectList().add(aspect, 1));
                                    for (Aspect primal : room.getAspects()) {
                                        if (primals.getAmount(primal) > 0 && node.getAspects().getAmount(aspect) > min) {
                                            Wands.addVis(stack, primal, 1, true);
                                            node.takeFromContainer(aspect, 1);
                                            somethingChanged = true;
                                            found = true;
                                            setDraining(true, nodePos, aspect.getColor());
                                            break search;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!found) {
                    recalc = true;
                }
            }
            if (!found) {
                setDraining(false, drainPos, drainColor);
            }
            setChanged();
        }
        if (counter % 100 == 0 && (recalc || nodes.isEmpty())) {
            findNodes(tickLevel);
        }
    }

    private void findNodes(Level tickLevel) {
        nodes = new ArrayList<>();
        for (int x = -8; x <= 8; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -8; z <= 8; z++) {
                    BlockPos target = worldPosition.offset(x, y, z);
                    if (tickLevel.hasChunkAt(target) && tickLevel.getBlockEntity(target) instanceof Node) {
                        nodes.add(target);
                    }
                }
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !stack.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            stack = ItemStack.EMPTY;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        stack = input.read("wand", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        draining = input.getBooleanOr("draining", false);
        drainPos = new BlockPos(input.getIntOr("drainX", 0), input.getIntOr("drainY", 0), input.getIntOr("drainZ", 0));
        drainColor = input.getIntOr("drainColor", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("wand", ItemStack.OPTIONAL_CODEC, stack);
        output.putBoolean("draining", draining);
        output.putInt("drainX", drainPos.getX());
        output.putInt("drainY", drainPos.getY());
        output.putInt("drainZ", drainPos.getZ());
        output.putInt("drainColor", drainColor);
    }
}
