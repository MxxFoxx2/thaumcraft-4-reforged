package thaumcraft.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.CrucibleBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import thaumcraft.research.Knowledge;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.Wands;

@GameTestHolder(Thaumcraft.MODID)
@PrefixGameTestTemplate(false)
public final class AlchemyGameTests {
    private AlchemyGameTests() {
    }

    public static void register(IEventBus bus) {
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void crucibleRecipeOutput(GameTestHelper helper) {
        BlockPos crucibleOffset = new BlockPos(2, 1, 2);
        helper.setBlock(crucibleOffset.below(), Blocks.LAVA);
        helper.setBlock(crucibleOffset, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible = helper.<CrucibleBlockEntity>getBlockEntity(crucibleOffset);
        crucible.fillWater(1000);
        crucible.setHeat(180);
        crucible.getAspects().add(Aspect.FIRE, 2).add(Aspect.WATER, 2).add(Aspect.EARTH, 2).add(Aspect.ORDER, 2).add(Aspect.ENTROPY, 2);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Knowledge.get(player).addResearch("CRUCIBLE");
        ItemEntity shard = helper.spawnItem(ModItems.AIR_SHARD.get(), crucibleOffset.above());
        shard.setThrower(player);
        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(ModItems.BALANCED_SHARD.get(), crucibleOffset, 2.0);
            helper.assertTrue(crucible.getAspects().isEmpty(), "aspects left in crucible");
        });
    }

    @GameTest(template = "test/platform", timeoutTicks = 200)
    public static void cauldronToCrucible(GameTestHelper helper) {
        BlockPos offset = new BlockPos(2, 1, 2);
        helper.setBlock(offset, Blocks.CAULDRON);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        Wands.fill(wand);
        boolean done = WandTriggers.perform(helper.getLevel(), wand, player, helper.absolutePos(offset), Direction.UP);
        helper.assertTrue(done, "trigger did not fire");
        helper.assertBlockPresent(ModBlocks.CRUCIBLE.get(), offset);
        helper.succeed();
    }
}
