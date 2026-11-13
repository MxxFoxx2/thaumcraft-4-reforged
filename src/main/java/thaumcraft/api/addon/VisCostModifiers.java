package thaumcraft.api.addon;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;

public final class VisCostModifiers {
    private static final List<VisCostModifier> MODIFIERS = new CopyOnWriteArrayList<>();

    private VisCostModifiers() {
    }

    @FunctionalInterface
    public interface VisCostModifier {
        float modify(@Nullable Player player, Aspect aspect, float modifier);
    }

    public static void register(VisCostModifier modifier) {
        MODIFIERS.add(modifier);
    }

    public static float apply(@Nullable Player player, Aspect aspect, float modifier) {
        float result = modifier;
        for (VisCostModifier candidate : MODIFIERS) {
            result = candidate.modify(player, aspect, result);
        }
        return result;
    }
}
