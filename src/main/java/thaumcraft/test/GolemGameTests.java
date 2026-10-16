package thaumcraft.test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionCrafting;
import thaumcraft.device.HungryChestBlockEntity;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemKeys;
import thaumcraft.entity.golem.GolemSnapshot;
import thaumcraft.entity.golem.GolemType;
import thaumcraft.entity.golem.LogisticsGoals;
import thaumcraft.entity.golem.Marker;
import thaumcraft.entity.golem.TravelingTrunk;
import thaumcraft.essentia.JarBlockEntity;
import thaumcraft.menu.GolemMenu;
import thaumcraft.platform.GolemFluids;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModEntities;

@net.neoforged.neoforge.gametest.GameTestHolder(Thaumcraft.MODID)
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public final class GolemGameTests {

    private GolemGameTests() { }


    private static GolemSnapshot state(int core) { return new GolemSnapshot(core, false, List.of(), "", List.of(), ItemContainerContents.EMPTY, List.of(), 0); }
    private static GolemBase golem(GameTestHelper helper, GolemType type, int core) {
        GolemBase golem = new GolemBase(ModEntities.GOLEM.get(), helper.getLevel());
        BlockPos home = helper.absolutePos(new BlockPos(3, 2, 2));
        golem.setPos(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        golem.setHome(home, Direction.EAST);
        golem.configure(type, state(core));
        golem.setHealth(golem.getMaxHealth());
        return golem;
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_materials")
    public static void materials(GameTestHelper helper) {
        for (GolemType type : GolemType.values()) {
            GolemBase golem = golem(helper, type, 2);
            helper.assertValueEqual((int) golem.getMaxHealth(), type.health, "material health");
            helper.assertValueEqual(golem.getCarryLimit(), type.carry, "material carry");
            helper.assertTrue(golem.fireImmune() == type.fireResist, "material fire immunity");
        }
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_snapshot")
    public static void snapshot(GameTestHelper helper) {
        GolemBase original = golem(helper, GolemType.THAUMIUM, 0);
        Marker marker = new Marker(GlobalPos.of(helper.getLevel().dimension(), original.getRestrictCenter().east(2)), Direction.WEST, 3);
        original.configure(GolemType.THAUMIUM, new GolemSnapshot(0, true, List.of(2, 3, 4), "HGR", List.of(marker), ItemContainerContents.fromItems(List.of(new ItemStack(Items.WHEAT, 12))), List.of(3), 160));
        ItemStack packed = original.toItem(false);
        GolemBase restored = golem(helper, GolemType.THAUMIUM, -1);
        restored.configure(GolemType.THAUMIUM, GolemKeys.snapshot(packed));
        helper.assertTrue(original.snapshot().equals(restored.snapshot()), "packed golem loses settings");
        helper.assertValueEqual(restored.inventory().getItem(0).getCount(), 12, "filter count");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_bellcycle")
    public static void bellCycle(GameTestHelper helper) {
        GolemBase original = golem(helper, GolemType.WOOD, 2);
        original.inventory().setItem(0, new ItemStack(Items.WHEAT));
        original.setCarried(new ItemStack(Items.WHEAT, 3));
        var expected = original.snapshot();
        helper.getLevel().addFreshEntity(original);
        var player = TestSupport.mockServerPlayer(helper);
        player.setPos(original.position().add(3, 0, 0));
        ItemStack bell = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id("golem_bell")));
        ((thaumcraft.item.golem.GolemBellItem) bell.getItem()).onLeftClickEntity(bell, player, original);
        helper.assertTrue(original.isRemoved(), "bell leaves original entity alive");
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, original.getBoundingBox().inflate(2));
        var packed = drops.stream().filter(item -> item.getItem().getItem() instanceof thaumcraft.item.golem.GolemPlacerItem).findFirst().orElseThrow();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, packed.getItem().copy());
        packed.discard();
        BlockPos ground = original.getRestrictCenter().below();
        helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
        var context = new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(ground), Direction.UP, ground, false));
        helper.assertTrue(player.getMainHandItem().useOn(context).consumesAction(), "packed golem cannot be placed");
        var placed = helper.getLevel().getEntitiesOfClass(GolemBase.class, original.getBoundingBox().inflate(2));
        helper.assertValueEqual(placed.size(), 1, "bell cycle duplicates golem");
        helper.assertTrue(expected.equals(placed.getFirst().snapshot()), "bell cycle loses configuration");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "placement does not consume item");
        helper.assertValueEqual(drops.stream().filter(item -> item.getItem().is(Items.WHEAT)).mapToInt(item -> item.getItem().getCount()).sum(), 3, "bell loses carried items");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_tagfilter")
    public static void tagFilter(GameTestHelper helper) {
        GolemBase golem = golem(helper, GolemType.THAUMIUM, 0);
        golem.configure(GolemType.THAUMIUM, new GolemSnapshot(0, false, List.of(5), "", List.of(), ItemContainerContents.EMPTY, List.of(), 1 << 5));
        ItemStack filter = new ItemStack(Items.OAK_PLANKS);
        ItemStack alternative = new ItemStack(Items.BIRCH_PLANKS);
        helper.assertTrue(golem.matchesFilter(filter, alternative), "tag equivalent rejected");
        alternative.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("named"));
        helper.assertFalse(golem.matchesFilter(filter, alternative), "component-sensitive filter accepts named item");
        golem.setToggle(7, true);
        helper.assertTrue(golem.matchesFilter(filter, alternative), "ignore-data filter rejects equivalent");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_saveresources")
    public static void saveResources(GameTestHelper helper) {
        GolemBase original = golem(helper, GolemType.IRON, 5);
        UUID owner = UUID.randomUUID();
        original.setOwnerUUID(owner);
        original.setFluidCarried(new FluidStack(Fluids.WATER, 3000));
        original.setCarried(new ItemStack(Items.WHEAT, 7));
        net.minecraft.nbt.CompoundTag output = new net.minecraft.nbt.CompoundTag();
        original.saveWithoutId(output);
        GolemBase loaded = new GolemBase(ModEntities.GOLEM.get(), helper.getLevel());
        loaded.load(output);
        helper.assertValueEqual(loaded.getFluidCarried().getAmount(), 3000, "saved fluid");
        helper.assertValueEqual(loaded.getCarried().getCount(), 7, "saved carried items");
        helper.assertTrue(owner.equals(loaded.getOwnerUUID()) && original.getRestrictCenter().equals(loaded.getRestrictCenter()), "saved owner/home");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_advancedinfusion")
    public static void advancedInfusion(GameTestHelper helper) {
        ItemStack central = new ItemStack(BuiltInRegistries.ITEM.get(Thaumcraft.id("wood_golem")));
        GolemKeys.STATE.set(central, new GolemSnapshot(2, false, List.of(3), "H", List.of(), ItemContainerContents.fromItems(List.of(new ItemStack(Items.WHEAT))), List.of(5), 0));
        ItemStack output = InfusionCrafting.result(helper.getLevel(), InfusionCrafting.TYPE_RECIPE, "AdvancedGolem", central);
        helper.assertTrue(output.is(central.getItem()), "advanced infusion changes material");
        helper.assertTrue(GolemKeys.snapshot(output).advanced(), "advanced infusion flag");
        helper.assertValueEqual(GolemKeys.snapshot(output).core(), 2, "advanced infusion removes core");
        helper.assertTrue(GolemKeys.snapshot(output).filters().equals(GolemKeys.snapshot(central).filters()), "advanced infusion removes filters");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_ghostfilter")
    public static void ghostFilter(GameTestHelper helper) {
        GolemBase golem = golem(helper, GolemType.WOOD, 0);
        helper.getLevel().addFreshEntity(golem);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(golem.position());
        GolemMenu menu = new GolemMenu(1, player.getInventory(), golem.getId());
        menu.setCarried(new ItemStack(Items.WHEAT, 12));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        helper.assertValueEqual(menu.getCarried().getCount(), 12, "ghost filter consumes cursor");
        helper.assertValueEqual(golem.inventory().getItem(0).getCount(), 12, "ghost count");
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        helper.assertValueEqual(menu.getCarried().getCount(), 12, "shift click duplicates cursor");
        menu.removed(player);
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_filtercolors")
    public static void filterColors(GameTestHelper helper) {
        GolemBase golem = golem(helper, GolemType.WOOD, 2);
        golem.inventory().setItem(0, new ItemStack(Items.WHEAT));
        golem.setColors(0, 4);
        helper.assertTrue(golem.getColorsMatching(new ItemStack(Items.WHEAT)).equals(List.of(4)), "colored filter");
        helper.assertTrue(golem.getColorsMatching(new ItemStack(Items.CARROT)).isEmpty(), "unmatched filter accepted");
        golem.inventory().clearContent();
        helper.assertValueEqual(golem.getColorsMatching(new ItemStack(Items.CARROT)).size(), 6, "empty filters should accept all");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_partialitems")
    public static void partialItems(GameTestHelper helper) {
        GolemBase golem = golem(helper, GolemType.IRON, 2);
        BlockPos pos = golem.getRestrictCenter().west();
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.HUNGRY_CHEST.get().defaultBlockState());
        HungryChestBlockEntity chest = (HungryChestBlockEntity) helper.getLevel().getBlockEntity(pos);
        for (int slot = 0; slot < chest.getContainerSize(); slot++) chest.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        chest.setItem(0, new ItemStack(Items.WHEAT, 60));
        golem.setCarried(new ItemStack(Items.WHEAT, 10));
        GoalSelector goals = new GoalSelector(helper.getLevel()::getProfiler);
        LogisticsGoals.install(golem, goals);
        goals.tick();
        helper.assertValueEqual(chest.getItem(0).getCount(), 64, "partial target amount");
        helper.assertValueEqual(golem.getCarried().getCount(), 6, "partial carry remainder");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_worldfluid")
    public static void worldFluid(GameTestHelper helper) {
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
        helper.assertValueEqual(GolemFluids.pickup(helper.getLevel(), source, true).getAmount(), 1000, "source preview");
        helper.assertTrue(helper.getLevel().getFluidState(source).isSource(), "preview drains source");
        helper.assertValueEqual(GolemFluids.pickup(helper.getLevel(), source, false).getAmount(), 1000, "source pickup");
        helper.assertTrue(helper.getLevel().getFluidState(source).isEmpty(), "source not drained");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_partialessentia")
    public static void partialEssentia(GameTestHelper helper) {
        GolemBase golem = golem(helper, GolemType.IRON, 6);
        BlockPos pos = golem.getRestrictCenter().east();
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.WARDED_JAR.get().defaultBlockState());
        JarBlockEntity jar = (JarBlockEntity) helper.getLevel().getBlockEntity(pos);
        jar.addToContainer(Aspect.AIR, 60);
        golem.setMarkers(List.of(new Marker(GlobalPos.of(helper.getLevel().dimension(), pos), Direction.UP, -1)));
        golem.setEssentia(Aspect.AIR, 8);
        GoalSelector goals = new GoalSelector(helper.getLevel()::getProfiler);
        LogisticsGoals.install(golem, goals);
        goals.tick();
        helper.assertValueEqual(jar.getAmount(), 64, "partial jar amount");
        helper.assertValueEqual(golem.getEssentiaAmount(), 4, "partial essentia remainder");
        helper.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_harvest")
    public static void harvest(GameTestHelper helper) {
        GolemBase golem = golem(helper, GolemType.STRAW, 3);
        BlockPos crop = golem.getRestrictCenter().east();
        helper.getLevel().setBlockAndUpdate(crop.below(), Blocks.FARMLAND.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(crop, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        helper.getLevel().addFreshEntity(golem);
        helper.succeedWhen(() -> helper.assertFalse(helper.getLevel().getBlockState(crop).is(Blocks.WHEAT), "straw golem has not harvested mature wheat"));
    }
    @net.minecraft.gametest.framework.GameTest(template = "test/platform", timeoutTicks = 1200, batch = "golemgametests_trunksave")
    public static void trunkSave(GameTestHelper helper) {
        TravelingTrunk trunk = new TravelingTrunk(ModEntities.TRAVELING_TRUNK.get(), helper.getLevel());
        trunk.setUpgrade(1);
        trunk.inventory().setItem(35, new ItemStack(Items.DIAMOND, 17));
        trunk.setStay(true);
        net.minecraft.nbt.CompoundTag output = new net.minecraft.nbt.CompoundTag();
        trunk.saveWithoutId(output);
        TravelingTrunk loaded = new TravelingTrunk(ModEntities.TRAVELING_TRUNK.get(), helper.getLevel());
        loaded.load(output);
        helper.assertValueEqual(loaded.inventory().getContainerSize(), 36, "upgraded trunk size");
        helper.assertValueEqual(loaded.inventory().getItem(35).getCount(), 17, "trunk inventory save");
        helper.assertTrue(loaded.getStay(), "trunk stay save");
        helper.succeed();
    }
}
