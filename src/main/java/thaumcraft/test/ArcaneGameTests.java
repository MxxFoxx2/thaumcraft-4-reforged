package thaumcraft.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.arcane.ArcaneWorkbenchBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.ArcaneCrafting;
import thaumcraft.menu.ArcaneWorkbenchMenu;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.research.Knowledge;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.Wands;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class ArcaneGameTests {
    private ArcaneGameTests() {
    }

    public static void register(IEventBus bus) {
    }

    private static ServerPlayer mockServerPlayer(GameTestHelper helper) {
        return TestSupport.mockServerPlayer(helper);
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void workbenchConsumesVis(GameTestHelper helper) {
        BlockPos tablePos = new BlockPos(1, 1, 1);
        helper.setBlock(tablePos, ModBlocks.ARCANE_WORKTABLE.get());
        ArcaneWorkbenchBlockEntity workbench = helper.<ArcaneWorkbenchBlockEntity>getBlockEntity(tablePos);
        ServerPlayer player = mockServerPlayer(helper);
        Knowledge.get(player).addResearch("VISPOWER");
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        Wands.fill(wand);
        int fireBefore = Wands.getVis(wand, Aspect.FIRE);
        workbench.setItem(ArcaneWorkbenchBlockEntity.WAND_SLOT, wand);
        for (int slot : new int[]{1, 3, 5, 7}) {
            workbench.setItem(slot, new ItemStack(Items.IRON_INGOT));
        }
        workbench.setItem(4, new ItemStack(ModItems.BALANCED_SHARD.get()));
        ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(1, player.getInventory(), helper.absolutePos(tablePos));
        ItemStack result = menu.getSlot(ArcaneWorkbenchMenu.RESULT_SLOT).getItem();
        helper.assertTrue(result.is(ModBlocks.VIS_RELAY.get().asItem()) && result.getCount() == 2, "arcane result missing");
        helper.assertValueEqual(menu.getMatchedAspects().getAmount(Aspect.FIRE), 8, "matched fire cost");
        menu.clicked(ArcaneWorkbenchMenu.RESULT_SLOT, 0, ClickType.PICKUP, player);
        helper.assertTrue(menu.getCarried().is(ModBlocks.VIS_RELAY.get().asItem()), "result not taken");
        helper.assertValueEqual(Wands.getVis(workbench.getWand(), Aspect.FIRE), fireBefore - 800, "fire vis left");
        helper.assertValueEqual(Wands.getVis(workbench.getWand(), Aspect.AIR), fireBefore, "air vis untouched");
        for (int slot = 0; slot < ArcaneWorkbenchBlockEntity.GRID_SLOTS; slot++) {
            helper.assertTrue(workbench.getItem(slot).isEmpty(), "grid slot " + slot + " not consumed");
        }
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void wandRecipeBuildsWand(GameTestHelper helper) {
        ServerPlayer player = mockServerPlayer(helper);
        List<ItemStack> grid = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            grid.add(ItemStack.EMPTY);
        }
        grid.set(6, new ItemStack(ModItems.GOLD_WAND_CAP.get()));
        grid.set(2, new ItemStack(ModItems.GOLD_WAND_CAP.get()));
        grid.set(4, new ItemStack(ModItems.GREATWOOD_ROD.get()));
        helper.assertTrue(ArcaneCrafting.findResult(grid, player).isEmpty(), "wand crafted without research");
        Knowledge.get(player).addResearch("CAP_gold");
        Knowledge.get(player).addResearch("ROD_greatwood");
        ItemStack result = ArcaneCrafting.findResult(grid, player);
        helper.assertTrue(Wands.isWand(result), "no wand built");
        helper.assertValueEqual(Wands.getCap(result).tag(), "gold", "cap");
        helper.assertValueEqual(Wands.getRod(result).tag(), "greatwood", "rod");
        helper.assertValueEqual(ArcaneCrafting.findAspects(grid, player).getAmount(Aspect.ORDER), 9, "order cost");
        helper.succeed();
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void tableWandBecomesWorktable(GameTestHelper helper) {
        BlockPos tablePos = new BlockPos(1, 1, 1);
        helper.setBlock(tablePos, ModBlocks.TABLE.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = Wands.create(WandRegistry.CAP_IRON, WandRegistry.ROD_GREATWOOD, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        boolean done = WandTriggers.perform(helper.getLevel(), wand, player, helper.absolutePos(tablePos), Direction.UP);
        helper.assertTrue(done, "trigger did not fire");
        helper.assertBlockPresent(ModBlocks.ARCANE_WORKTABLE.get(), tablePos);
        ArcaneWorkbenchBlockEntity workbench = helper.<ArcaneWorkbenchBlockEntity>getBlockEntity(tablePos);
        helper.assertTrue(Wands.isWand(workbench.getWand()), "wand not placed in the workbench");
        helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(), "wand still in hand");
        helper.succeed();
    }
}
