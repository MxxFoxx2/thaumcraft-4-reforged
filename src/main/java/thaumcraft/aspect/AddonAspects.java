package thaumcraft.aspect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;

public final class AddonAspects {
    private static final List<Consumer<Addon>> REGISTRARS = new ArrayList<>();
    private static final Set<Consumer<Addon>> APPLIED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Addon API = (tag, color, components, blend, texture) ->
        new Aspect(tag, color, components, texture, blend);
    private static boolean initialized;

    private AddonAspects() {
    }

    public interface Addon {
        Aspect register(String tag, int color, Aspect[] components, int blend, Identifier texture);
    }

    public static synchronized void addRegistrar(Consumer<Addon> registrar) {
        Objects.requireNonNull(registrar, "registrar");
        REGISTRARS.add(registrar);
        if (initialized) {
            apply(registrar);
        }
    }

    public static synchronized void init() {
        for (int index = 0; index < REGISTRARS.size(); index++) {
            apply(REGISTRARS.get(index));
        }
        initialized = true;
    }

    private static void apply(Consumer<Addon> registrar) {
        if (!APPLIED.add(registrar)) {
            return;
        }
        try {
            registrar.accept(API);
        } catch (RuntimeException | Error failure) {
            APPLIED.remove(registrar);
            throw failure;
        }
    }
}
