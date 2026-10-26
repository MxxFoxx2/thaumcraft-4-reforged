package thaumcraft.test;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.aspect.EntityAspects;
import thaumcraft.entity.AspectOrb;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class AspectGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, Thaumcraft.MODID);
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
        "addon_aspect_registration", AspectGameTests::addonAspectRegistration
    );
    private static volatile boolean addonAspectsInitialized;

    static {
        AddonAspects.addRegistrar(api -> addonAspectsInitialized = true);
        TESTS.forEach((name, test) -> FUNCTIONS.register(name, () -> test));
    }

    private AspectGameTests() {
    }

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Thaumcraft.id("aspect_game_tests"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS.keySet()) {
            event.registerTest(
                Thaumcraft.id(name),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Thaumcraft.id(name)), new TestData<>(environment, Thaumcraft.id("test/platform"), 200, 0, true))
            );
        }
    }

    private static void addonAspectRegistration(GameTestHelper helper) {
        int initialAspectCount = Aspect.values().length;
        List<Aspect> testAspects = Collections.synchronizedList(new ArrayList<>());
        try {
            assertAddonAspectRegistration(helper, testAspects);
        } finally {
            unregisterTestAspects(testAspects);
        }
        helper.assertValueEqual(Aspect.values().length, initialAspectCount, "test aspect registrations leaked into the global registry");
        for (Aspect aspect : testAspects) {
            helper.assertTrue(Aspect.getAspect(aspect.getTag()) == null, "test aspect remained registered after cleanup");
        }
        helper.succeed();
    }

    private static void assertAddonAspectRegistration(GameTestHelper helper, List<Aspect> testAspects) {
        helper.assertTrue(addonAspectsInitialized, "add-on aspect callbacks were not applied during common setup");
        AtomicReference<Aspect> registeredThroughApi = new AtomicReference<>();
        AddonAspects.addRegistrar(api -> {
            Aspect registered = api.register(
                "api_testaspect",
                0x345678,
                new Aspect[]{Aspect.AIR, Aspect.WATER},
                771,
                Identifier.fromNamespaceAndPath("testaddon", "textures/aspects/api_testaspect.png")
            );
            registeredThroughApi.set(registered);
            testAspects.add(registered);
        });
        Aspect apiAspect = registeredThroughApi.get();
        helper.assertTrue(apiAspect != null && Aspect.getAspect("api_testaspect") == apiAspect,
            "add-on registrar was not applied before tests started");
        if (apiAspect == null) {
            helper.fail("add-on aspect API was not initialized during common setup");
            return;
        }
        helper.assertValueEqual(apiAspect.getBlend(), 771, "add-on API did not preserve blend");
        helper.assertTrue(apiAspect.getImage().equals(Identifier.fromNamespaceAndPath("testaddon", "textures/aspects/api_testaspect.png")),
            "add-on API did not preserve texture identifier");

        Aspect[] components = {Aspect.AIR, Aspect.FIRE};
        Identifier addonImage = Identifier.fromNamespaceAndPath("testaddon", "textures/aspects/testaspect.png");
        Aspect addon = registerTestAspect(testAspects, "testaspect", 0x123456, components, addonImage);
        AspectOrb addonOrb = AspectOrb.create(helper.getLevel(), 0, 0, 0, addon, 1);
        helper.assertTrue(addonOrb != null, "aspect orb could not be created for an add-on aspect");
        Aspect laterRegisteredButEarlierTag = registerTestAspect(
            testAspects,
            "aaa_testaspect",
            0x234567,
            new Aspect[]{Aspect.AIR, Aspect.WATER},
            Identifier.fromNamespaceAndPath("testaddon", "textures/aspects/aaa_testaspect.png")
        );
        if (addonOrb != null) {
            helper.assertTrue(addonOrb.getAspect() == addon,
                "aspect orb changed its aspect when a new tag shifted add-on ordinal ordering");
        }
        registerConcurrentTestAspects(helper, testAspects);

        helper.assertTrue(Aspect.getAspect("testaspect") == addon, "registered aspect was not found by tag");
        helper.assertValueEqual(Aspect.AIR.ordinal(), 0, "built-in aspect ordinal changed");
        helper.assertValueEqual(Aspect.TRAP.ordinal(), 47, "built-in aspect ordinal changed");
        helper.assertTrue(laterRegisteredButEarlierTag.ordinal() > 47, "add-on aspect overlapped built-in ordinal range");
        helper.assertTrue(laterRegisteredButEarlierTag.ordinal() < addon.ordinal(),
            "add-on ordinals depended on registration order instead of tag order");
        helper.assertTrue(Aspect.values()[laterRegisteredButEarlierTag.ordinal()] == laterRegisteredButEarlierTag,
            "deterministic values() ordering disagreed with ordinal()");
        helper.assertTrue(Aspect.values()[addon.ordinal()] == addon,
            "deterministic values() ordering disagreed with ordinal()");
        for (int index = 0; index < 8; index++) {
            String suffix = index < 10 ? "0" + index : Integer.toString(index);
            Aspect parallel = Aspect.getAspect("parallel_testaspect_" + suffix);
            helper.assertTrue(parallel != null, "concurrent aspect registration lost an aspect");
            if (parallel != null) {
                helper.assertTrue(laterRegisteredButEarlierTag.ordinal() < parallel.ordinal(),
                    "concurrent aspect order was not deterministic");
                helper.assertTrue(parallel.ordinal() < addon.ordinal(),
                    "concurrent aspect order was not deterministic");
                if (index > 0) {
                    String previousSuffix = index - 1 < 10 ? "0" + (index - 1) : Integer.toString(index - 1);
                    Aspect previous = Aspect.getAspect("parallel_testaspect_" + previousSuffix);
                    helper.assertTrue(previous != null && previous.ordinal() < parallel.ordinal(),
                        "concurrent aspects were not ordered by tag");
                }
            }
        }
        helper.assertTrue(Aspect.getCompoundAspects().contains(addon), "add-on aspect was not classified as compound");
        helper.assertTrue(!Aspect.getPrimalAspects().contains(addon), "add-on aspect was classified as primal");
        helper.assertTrue(Aspect.getAspect("missing") == null, "unknown aspect tag did not return null");
        helper.assertTrue(addon.getComponents() != null && addon.getComponents().length == 2, "add-on aspect is not compound");
        helper.assertTrue(addon.getComponents()[0] == Aspect.AIR && addon.getComponents()[1] == Aspect.FIRE, "add-on aspect components changed");
        components[0] = Aspect.ENTROPY;
        helper.assertTrue(addon.getComponents()[0] == Aspect.AIR, "add-on aspect retained a mutable component array");
        addon.getComponents()[0] = Aspect.ENTROPY;
        helper.assertTrue(addon.getComponents()[0] == Aspect.AIR, "add-on aspect exposed a mutable component array");
        helper.assertTrue(addon.getImage().equals(addonImage), "add-on aspect image identifier or namespace changed");

        AspectList aspects = new AspectList().add(addon, 7).add(Aspect.AIR, 2);
        var encoded = AspectList.CODEC.encodeStart(JsonOps.INSTANCE, aspects).result().orElseThrow();
        AspectList decoded = AspectList.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        helper.assertTrue(decoded.equals(aspects), "aspect list codec lost the add-on aspect");
        var unknownAspectDecode = Aspect.CODEC.parse(JsonOps.INSTANCE, JsonOps.INSTANCE.createString("missing"));
        helper.assertTrue(unknownAspectDecode.error().isPresent(),
            "unknown aspect CODEC input did not return a DataResult error");
        ByteBuf buffer = Unpooled.buffer();
        try {
            AspectList.STREAM_CODEC.encode(buffer, aspects);
            helper.assertTrue(AspectList.STREAM_CODEC.decode(buffer).equals(aspects), "aspect list stream codec lost the add-on aspect");
        } finally {
            buffer.release();
        }

        AspectRegistry registry = new AspectRegistry();
        registry.registerEntity(EntityTypes.CREEPER, new AspectList().add(Aspect.FIRE, 2),
            "powered", 1, "variant", 1);
        registry.mergeEntity(EntityTypes.CREEPER, new AspectList().add(addon, 3).add(Aspect.FIRE, 1),
            "variant", 1, "powered", 1);
        List<EntityAspects> merged = registry.exportEntities().get(EntityTypes.CREEPER);
        helper.assertValueEqual(merged.size(), 1, "matching entity tags with reordered conditions were not merged");
        helper.assertValueEqual(merged.getFirst().aspects().getAmount(addon), 3, "merged entity tag lost the add-on aspect");
        helper.assertValueEqual(merged.getFirst().aspects().getAmount(Aspect.FIRE), 3, "merged entity tag did not sum aspect amounts");
        helper.assertValueEqual(merged.getFirst().conditions().getFirst().name(), "powered",
            "entity conditions were not stored in canonical name order");
        AspectRegistry aliasingRegistry = new AspectRegistry();
        AspectList mutableAspects = new AspectList();
        aliasingRegistry.registerEntity(EntityTypes.PIG, mutableAspects);
        mutableAspects.add(Aspect.FIRE, 2);
        List<EntityAspects> aliased = aliasingRegistry.exportEntities().get(EntityTypes.PIG);
        helper.assertTrue(aliased != null && aliased.size() == 1, "entity aspect registration was not stored");
        helper.assertValueEqual(aliased.getFirst().aspects().size(), 0,
            "registered entity aspects changed after the caller mutated the original list");
        helper.assertValueEqual(aliased.getFirst().aspects().getAmount(Aspect.FIRE), 0,
            "registered entity aspects kept the aspect added to the caller's list after registration");

        EntityAspects directlyConstructed = new EntityAspects(new AspectList(), List.of(
            new EntityAspects.Condition("variant", 1),
            new EntityAspects.Condition("powered", 1)
        ));
        helper.assertValueEqual(directlyConstructed.conditions().getFirst().name(), "powered",
            "directly constructed entity aspects did not canonicalize conditions");

        registry.mergeEntity(EntityTypes.CREEPER, new AspectList().add(addon, 4), "variant", 0, "powered", 1);
        helper.assertValueEqual(registry.exportEntities().get(EntityTypes.CREEPER).size(), 2,
            "different entity conditions were merged");

        try {
            EntityAspects.of(new AspectList(), "powered", 1, "powered", 0);
            helper.fail("duplicate entity condition names were accepted");
        } catch (IllegalArgumentException expected) {
        }
        try {
            EntityAspects.of(new AspectList(), "powered");
            helper.fail("an incomplete entity condition pair was accepted");
        } catch (IllegalArgumentException expected) {
        }
        try {
            EntityAspects.of(new AspectList(), "powered", null);
            helper.fail("a null entity condition value was accepted");
        } catch (IllegalArgumentException expected) {
        }

        ByteBuf malformedCondition = Unpooled.buffer();
        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.encode(malformedCondition, "powered");
        malformedCondition.writeByte(99);
        try {
            EntityAspects.Condition.STREAM_CODEC.decode(malformedCondition);
            helper.fail("an unknown entity condition value type was accepted");
        } catch (IllegalArgumentException expected) {
        } finally {
            malformedCondition.release();
        }

        try {
            new Aspect(
                "testaspect",
                0x654321,
                new Aspect[]{Aspect.AIR, Aspect.FIRE},
                addonImage,
                1
            );
            helper.fail("duplicate aspect tag was accepted");
        } catch (IllegalArgumentException expected) {
        }
        helper.assertTrue(Aspect.getAspect("testaspect") == addon, "duplicate aspect tag replaced the registered aspect");

        assertRejectedAspect(helper, "testaddon:namespaced_testaspect", new Aspect[]{Aspect.AIR, Aspect.FIRE}, addonImage,
            "namespaced add-on aspect tag was accepted");
        assertRejectedAspect(helper, "nullcomponents", null, Thaumcraft.id("textures/aspects/nullcomponents.png"), "add-on aspect with null components was accepted");
        assertRejectedAspect(helper, "invalidaspect", new Aspect[]{Aspect.AIR}, Thaumcraft.id("textures/aspects/invalidaspect.png"), "add-on aspect with invalid components was accepted");
        assertRejectedAspect(helper, "nullcomponent", new Aspect[]{Aspect.AIR, null}, Thaumcraft.id("textures/aspects/nullcomponent.png"), "add-on aspect with a null component was accepted");
        assertRejectedAspect(helper, "nullimage", new Aspect[]{Aspect.AIR, Aspect.FIRE}, null, "add-on aspect with a null image was accepted");

    }

    private static Aspect registerTestAspect(List<Aspect> testAspects, String tag, int color, Aspect[] components, Identifier image) {
        Aspect aspect = new Aspect(tag, color, components, image, 1);
        testAspects.add(aspect);
        return aspect;
    }

    @SuppressWarnings("unchecked")
    private static void unregisterTestAspects(List<Aspect> testAspects) {
        try {
            java.lang.reflect.Field byTagField = Aspect.class.getDeclaredField("BY_TAG");
            java.lang.reflect.Field valuesField = Aspect.class.getDeclaredField("VALUES");
            byTagField.setAccessible(true);
            valuesField.setAccessible(true);
            synchronized (Aspect.class) {
                Map<String, Aspect> byTag = (Map<String, Aspect>) byTagField.get(null);
                List<Aspect> values = (List<Aspect>) valuesField.get(null);
                for (Aspect aspect : testAspects) {
                    if (!byTag.remove(aspect.getTag(), aspect) || !values.remove(aspect)) {
                        throw new IllegalStateException("Test aspect was missing from the registry: " + aspect.getTag());
                    }
                }
            }
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Failed to clean up GameTest aspects", failure);
        }
    }

    private static void registerConcurrentTestAspects(GameTestHelper helper, List<Aspect> testAspects) {
        CountDownLatch start = new CountDownLatch(1);
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            final int number = index;
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    String suffix = number < 10 ? "0" + number : Integer.toString(number);
                    String tag = "parallel_testaspect_" + suffix;
                    registerTestAspect(
                        testAspects,
                        tag,
                        0x345678,
                        new Aspect[]{Aspect.AIR, Aspect.FIRE},
                        Identifier.fromNamespaceAndPath("testaddon", "textures/aspects/" + tag + ".png")
                    );
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    failures.add(interrupted);
                } catch (Throwable failure) {
                    failures.add(failure);
                }
            }, "aspect-registration-test-" + index);
            thread.start();
            threads.add(thread);
        }

        start.countDown();
        boolean interrupted = false;
        for (Thread thread : threads) {
            while (thread.isAlive()) {
                try {
                    thread.join();
                } catch (InterruptedException exception) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
            helper.fail("interrupted while testing concurrent aspect registration");
        }
        helper.assertTrue(failures.isEmpty(), "concurrent aspect registration failed: " + failures);
    }

    private static void assertRejectedAspect(GameTestHelper helper, String tag, Aspect[] components, Identifier image, String message) {
        try {
            new Aspect(tag, 0x654321, components, image, 1);
            helper.fail(message);
        } catch (IllegalArgumentException expected) {
        }
        helper.assertTrue(Aspect.getAspect(tag) == null, "rejected aspect was registered");
    }
}
