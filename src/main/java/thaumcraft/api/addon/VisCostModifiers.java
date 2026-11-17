package thaumcraft.api.addon;

import java.util.Arrays;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;

public final class VisCostModifiers {
    private static volatile VisCostModifier[] modifiers = new VisCostModifier[0];

    private VisCostModifiers() {
    }

    @FunctionalInterface
    public interface VisCostModifier {
        float modify(@Nullable Player player, Aspect aspect, float modifier);
    }

    public static synchronized void register(VisCostModifier modifier) {
        VisCostModifier[] updated = Arrays.copyOf(modifiers, modifiers.length + 1);
        updated[modifiers.length] = modifier;
        modifiers = updated;
    }

    public static float apply(@Nullable Player player, Aspect aspect, float modifier) {
        float result = modifier;
        for (VisCostModifier candidate : modifiers) {
            result = candidate.modify(player, aspect, result);
        }
        return result;
    }
}
