package thaumcraft.crafting;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandKeys;
import thaumcraft.wand.WandParts;

public record TcStack(ResourceLocation item, int count, @Nullable AspectList aspects, Map<String, String> data) {
    public static final TcStack NONE = new TcStack(ResourceLocation.parse("minecraft:air"), 0, null, Map.of());

    public TcStack {
        data = Map.copyOf(data);
    }

    public TcStack(ResourceLocation item, int count, @Nullable AspectList aspects) {
        this(item, count, aspects, Map.of());
    }

    public static TcStack of(String item) {
        return new TcStack(ResourceLocation.parse(item), 1, null);
    }

    public static TcStack of(String item, int count) {
        return new TcStack(ResourceLocation.parse(item), count, null);
    }

    public static TcStack withAspects(String item, int count, AspectList aspects) {
        return new TcStack(ResourceLocation.parse(item), count, aspects);
    }

    public static TcStack withData(String item, int count, Map<String, String> data) {
        return new TcStack(ResourceLocation.parse(item), count, null, data);
    }

    public @Nullable Item resolve() {
        Item resolved = BuiltInRegistries.ITEM.getValue(item);
        return resolved == Items.AIR ? null : resolved;
    }

    public boolean exists() {
        return resolve() != null;
    }

    public ItemStack create() {
        Item resolved = resolve();
        if (resolved == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(resolved, count);
        if (aspects != null) {
            AspectKeys.ASPECTS.set(stack, aspects.copy());
        }
        if (data.containsKey("cap") && data.containsKey("rod")) {
            WandKeys.PARTS.set(stack, new WandParts(data.get("cap"), data.get("rod"), false));
        }
        return stack;
    }

    public boolean matches(ItemStack stack) {
        Item resolved = resolve();
        if (resolved == null || !stack.is(resolved)) {
            return false;
        }
        if (data.containsKey("cap") && data.containsKey("rod")) {
            WandParts parts = WandKeys.PARTS.getOrDefault(stack, WandParts.DEFAULT);
            if (!parts.cap().equals(data.get("cap")) || !parts.rod().equals(data.get("rod"))) {
                return false;
            }
        }
        return aspects == null || aspects.equals(AspectKeys.ASPECTS.get(stack));
    }
}
