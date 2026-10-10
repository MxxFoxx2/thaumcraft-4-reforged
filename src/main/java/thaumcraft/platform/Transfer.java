package thaumcraft.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import javax.annotation.Nullable;

public final class Transfer {
    private static final List<Exposure<?>> ITEMS = new ArrayList<>();
    private static final List<Exposure<?>> FLUIDS = new ArrayList<>();

    private Transfer() {
    }

    private record Exposure<B extends BlockEntity>(Supplier<? extends BlockEntityType<B>> type, BiFunction<B, @Nullable Direction, @Nullable Object> provider) {
    }

    public static final class FluidTank {
        private final Handler handler;

        public FluidTank(int capacity, Predicate<FluidStack> filter, boolean extractable, Runnable onChanged) {
            this.handler = new Handler(capacity, filter, extractable, onChanged);
        }

        public FluidStack getFluid() {
            return handler.getResource(0).toStack(handler.getAmountAsInt(0));
        }

        public void setFluid(FluidStack stack) {
            handler.set(0, FluidResource.of(stack), stack.getAmount());
        }

        public int getAmount() {
            return handler.getAmountAsInt(0);
        }

        public int getCapacity() {
            return handler.capacity();
        }

        private static final class Handler extends FluidStacksResourceHandler {
            private final Predicate<FluidStack> filter;
            private final boolean extractable;
            private final Runnable onChanged;

            private Handler(int capacity, Predicate<FluidStack> filter, boolean extractable, Runnable onChanged) {
                super(1, capacity);
                this.filter = filter;
                this.extractable = extractable;
                this.onChanged = onChanged;
            }

            private int capacity() {
                return capacity;
            }

            @Override
            public boolean isValid(int index, FluidResource resource) {
                return filter.test(resource.toStack(1));
            }

            @Override
            public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
                return extractable ? super.extract(index, resource, amount, transaction) : 0;
            }

            @Override
            protected void onContentsChanged(int index, FluidStack previousContents) {
                onChanged.run();
            }
        }
    }

    public static <B extends BlockEntity> void exposeItems(Supplier<? extends BlockEntityType<B>> type, BiFunction<B, @Nullable Direction, @Nullable Container> provider) {
        ITEMS.add(new Exposure<>(type, provider::apply));
    }

    public static <B extends BlockEntity> void exposeFluids(Supplier<? extends BlockEntityType<B>> type, BiFunction<B, @Nullable Direction, @Nullable FluidTank> provider) {
        FLUIDS.add(new Exposure<>(type, provider::apply));
    }

    public static void register(IEventBus bus) {
        bus.addListener(Transfer::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Exposure<?> exposure : ITEMS) {
            registerItems(event, exposure);
        }
        for (Exposure<?> exposure : FLUIDS) {
            registerFluids(event, exposure);
        }
    }

    private static <B extends BlockEntity> void registerItems(RegisterCapabilitiesEvent event, Exposure<B> exposure) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, exposure.type().get(), (entity, side) -> {
            Object container = exposure.provider().apply(entity, side);
            if (container instanceof WorldlyContainer worldly && side != null) {
                return new WorldlyContainerWrapper(worldly, side);
            }
            return container instanceof Container plain ? VanillaContainerWrapper.of(plain) : null;
        });
    }

    private static <B extends BlockEntity> void registerFluids(RegisterCapabilitiesEvent event, Exposure<B> exposure) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, exposure.type().get(), (entity, side) ->
            exposure.provider().apply(entity, side) instanceof FluidTank tank ? tank.handler : null
        );
    }

    private static @Nullable ResourceHandler<ItemResource> items(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side);
    }

    private static @Nullable ResourceHandler<FluidResource> fluids(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
    }

    public static boolean hasItems(Level level, BlockPos pos, @Nullable Direction side) {
        return items(level, pos, side) != null;
    }

    public static boolean hasFluids(Level level, BlockPos pos, @Nullable Direction side) {
        return fluids(level, pos, side) != null;
    }

    public static int count(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter) {
        ResourceHandler<ItemResource> handler = items(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.size(); index++) {
            ItemResource resource = handler.getResource(index);
            if (!resource.isEmpty() && filter.test(resource.toStack(1))) {
                total += handler.getAmountAsInt(index);
            }
        }
        return total;
    }

    public static ItemStack insert(Level level, BlockPos pos, @Nullable Direction side, ItemStack stack, boolean simulate) {
        ResourceHandler<ItemResource> handler = items(level, pos, side);
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    public static ItemStack extract(Level level, BlockPos pos, @Nullable Direction side, Predicate<ItemStack> filter, int maxAmount, boolean simulate) {
        ResourceHandler<ItemResource> handler = items(level, pos, side);
        if (handler == null || maxAmount <= 0) {
            return ItemStack.EMPTY;
        }
        for (int index = 0; index < handler.size(); index++) {
            ItemResource resource = handler.getResource(index);
            if (resource.isEmpty() || !filter.test(resource.toStack(1))) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = handler.extract(resource, maxAmount, transaction);
                if (extracted <= 0) {
                    continue;
                }
                if (!simulate) {
                    transaction.commit();
                }
                return resource.toStack(extracted);
            }
        }
        return ItemStack.EMPTY;
    }

    public static int countFluid(Level level, BlockPos pos, @Nullable Direction side, Predicate<FluidStack> filter) {
        ResourceHandler<FluidResource> handler = fluids(level, pos, side);
        if (handler == null) {
            return 0;
        }
        int total = 0;
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            if (!resource.isEmpty() && filter.test(resource.toStack(1))) {
                total += handler.getAmountAsInt(index);
            }
        }
        return total;
    }

    public static FluidStack insertFluid(Level level, BlockPos pos, @Nullable Direction side, FluidStack stack, boolean simulate) {
        ResourceHandler<FluidResource> handler = fluids(level, pos, side);
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(FluidResource.of(stack), stack.getAmount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return stack.copyWithAmount(stack.getAmount() - inserted);
        }
    }

    public static FluidStack extractFluid(Level level, BlockPos pos, @Nullable Direction side, Predicate<FluidStack> filter, int maxAmount, boolean simulate) {
        ResourceHandler<FluidResource> handler = fluids(level, pos, side);
        if (handler == null || maxAmount <= 0) {
            return FluidStack.EMPTY;
        }
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            if (resource.isEmpty() || !filter.test(resource.toStack(1))) {
                continue;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = handler.extract(resource, maxAmount, transaction);
                if (extracted <= 0) {
                    continue;
                }
                if (!simulate) {
                    transaction.commit();
                }
                return resource.toStack(extracted);
            }
        }
        return FluidStack.EMPTY;
    }
}
