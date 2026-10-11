# Backport to Minecraft 1.21.1

## Targets

- Minecraft 1.21.1, NeoForge `21.1.256`, Java 21 (Gradle runs on Java 25: `export JAVA_HOME=/opt/homebrew/opt/openjdk@25`, toolchain 21), ModDevGradle `2.0.148`
- Parchment `1.21.1` / `2024.11.17`
- JEI `19.57.0.451`, TerraBlender `1.21.1-4.1.0.1`, Curios `9.5.1+1.21.1`
- Branch: `mc/1.21.1`, worktree `~/projects/thaumcraft-4-reforged-1.21.1`

## Last synced main commit

`163651a` (phase 8 complete)

## Sync procedure

1. `git log --reverse 163651a..main` in the worktree of this branch.
2. A commit touching only resources, lang or `tools/`: `git cherry-pick`, then regenerate with `python3 tools/gen_resources.py --mc 1.21.1`.
3. A commit with Java: `git show <sha> | python3 tools/backport/rewrite.py --target 1.21.1 | git apply -3`, resolve conflicts by hand, run `compileJava`.
4. Commit with the same message, then update the sha above.
5. Check ids: `python3 tools/validate_ids.py --mc 1.21.1`.

## Reference sources

In the main checkout (gitignored): `reference/1.21.1/mc` (net/minecraft, com/mojang) and `reference/1.21.1/neo` (net/neoforged). The Thaumcraft 3 port of the same classes lives in `~/projects/thaumcraft-reforged-1.21.1` (branch `mc/1.21.1` of TC3 Reforged): a class that exists there is taken from there.

## Branch-only differences (TC4)

- Baubles: `platform/Baubles` is backed by Curios (`amulet`, `ring` x2, `belt` slots). The own slot implementation of `main` (`platform/baubles/*`, `client/platform/baubles/*`) does not exist on this branch
- GameTests: `@GameTestHolder` + `@PrefixGameTestTemplate` instead of the `test_function` registry and `TestEnvironmentDefinition`
- Item models: `items/*.json` client item definitions do not exist; the generator writes `models/item` with `overrides`, special renderers go through `BlockEntityWithoutLevelRenderer` + `IClientItemExtensions`, tints through `RegisterColorHandlersEvent.Item`
- Data attachments: no `.sync()`, `DataAttachments` sends its own payload

## rewrite.py rules (1.21.1)

- Imports: `Identifier` to `ResourceLocation`, `EntitySpawnReason` to `MobSpawnType`, `EntityTypes` to `EntityType`, `ItemUseAnimation` to `UseAnim`, `VegetationBlock` to `BushBlock`, `ARGB` to `FastColor` (`FastColor.ARGB32.*`), `org.jspecify.annotations.Nullable` to `javax.annotation.Nullable`, `ValueInput`/`ValueOutput` to `thaumcraft.compat.*`
- Package moves: golems (`animal.golem.*` to `animal`), `animal.sheep`, `monster.zombie`, `monster.spider`, `npc.villager`, `projectile.arrow`, `projectile.throwableitemprojectile`, `ParticleStatus`, `BlockAndTintGetter`, `PlayerModel`
- Constants: `EntitySpawnReason.SPAWN_ITEM_USE` to `SPAWN_EGG`, `MobEffects` (SPEED, SLOWNESS, HASTE, MINING_FATIGUE, STRENGTH, INSTANT_HEALTH, INSTANT_DAMAGE, JUMP_BOOST, NAUSEA, RESISTANCE), `PushReaction.POPPED/IMMOVEABLE` to `DESTROY/BLOCK`
- Calls: `x.hurtServer(level, source, amount)` to `x.hurt(source, amount)` (and declarations), `poseStack.rotateDegrees(Axis.X, a)` to `poseStack.mulPose(Axis.X.rotationDegrees(a))`, `.identifier()` to `.location()`, `getMinY()` to `getMinBuildHeight()`, `getSelectedSlot()` to `.selected`, `getNonEquipmentItems()` to `.items`

## API differences (26.3 vs 1.21.1)

| Area | 26.3 | 1.21.1 |
|---|---|---|
| Ids | `Identifier` | `ResourceLocation` |
| Save data | `ValueInput`/`ValueOutput`, `loadAdditional(ValueInput)` | `CompoundTag` + `HolderLookup.Provider` (compat adapters) |
| Saved data | `SavedDataType` | `SavedData.Factory` |
| Item component stack template | `ItemStackTemplate` | `ItemStack.OPTIONAL_CODEC` |
| Item data components getter | `DataComponentGetter` | `DataComponentInput` in `applyImplicitComponents` |
| Registration | `registerBlock(name, fn)` | `register(name, () -> new ...)` with `Properties` |
| Materials | `ToolMaterial`, `ArmorMaterial` + `ArmorType` + `EquipmentAsset` | `Tier`, `ArmorMaterial` holder + `ArmorItem.Type` |
| Item use | `ItemUseAnimation`, `Consumable` | `UseAnim`, `FoodProperties` and `use` overrides |
| Interaction | `InteractionResult.SUCCESS_SERVER`, `useItemOn`/`useWithoutItem` return `InteractionResult` | `ItemInteractionResult`, `InteractionResultHolder` for `use` |
| Block updates | `neighborChanged(..., Orientation)`, `updateShape(..., ScheduledTickAccess, ...)`, `affectNeighborsAfterRemoval` | `neighborChanged(..., Block, BlockPos, boolean)`, `updateShape(state, dir, neighborState, LevelAccessor, pos, neighborPos)`, `onRemove` |
| Entities | `hurtServer(ServerLevel, ...)`, `InsideBlockEffectApplier` | `hurt(...)`, `entityInside(state, level, pos, entity)` |
| Level | `getMinY()/getMaxY()` | `getMinBuildHeight()/getMaxBuildHeight()` (max is exclusive) |
| Inventory | `getSelectedSlot()`, `getNonEquipmentItems()` | `selected`, `items` |
| Potions | `MobEffects.SPEED` etc. | `MOVEMENT_SPEED` etc. |
| Transfer | `ResourceHandler`, `Transaction`, `ItemResource`, `FluidResource` | `IItemHandler`, `IFluidHandler` capabilities |
| Network | client handlers registered separately, `ClientPacketDistributor` | handlers in `playToClient`, `PacketDistributor` |
| Attachments | `.sync()` | custom payload |
| Villagers | `TradeSet`, `villager_trade` data | `VillagerTradesEvent` |
| Recipes | `recipeAccess()`, `RecipeDisplay`, `RecipesReceivedEvent` | `getRecipeManager()`, client `RecipeManager` |
| Fuel | `context_int_provider` item component | `furnace_fuels` data map |
| Furnace AT | `cookingTimer` | `cookingProgress` |
| Render | extract/submit, `SubmitNodeCollector`, render states, `RenderPipeline`, `GuiGraphicsExtractor` | `render(...)` with `MultiBufferSource`, `RenderType`, `GuiGraphics` |
| Entity render | `EntityRenderState`, `AvatarRenderer` | `LivingEntityRenderer`, `PlayerRenderer` |
| Item models | `items/*.json` client item definitions, `SpecialModelRenderer`, tint sources | `models/item` overrides, `BlockEntityWithoutLevelRenderer`, `RegisterColorHandlersEvent` |
| Input events | `KeyEvent`, `MouseButtonEvent` | plain `int` parameters |
| Particles | `SingleQuadParticle.Layer` | `TextureSheetParticle`, `ParticleRenderType` |
| Events | `ExtractLevelRenderStateEvent`, `SubmitCustomGeometryEvent`, `AddClientReloadListenersEvent` | `RenderLevelStageEvent`, `RegisterClientReloadListenersEvent` |
| Annotations | `org.jspecify.annotations.Nullable` | `javax.annotation.Nullable` |

## Compat shims (`thaumcraft/compat`)

- `ValueInput`: `of(CompoundTag, Provider)`, `read(name, codec)`, `read(MapCodec)`, `child`, `childOrEmpty`, `childrenList`, `childrenListOrEmpty`, `list`, `listOrEmpty`, `get*Or`, `getInt`, `getLong`, `getString`, `getIntArray`, `keySet`, `lookup`, `tag`
- `ValueOutput`: `of(CompoundTag, Provider)`, `store(name, codec, value)`, `storeNullable`, `store(MapCodec, value)`, `store(CompoundTag)`, `put*`, `child`, `childrenList`, `list`, `discard`, `isEmpty`, `tag`
- Codec IO goes through `provider.createSerializationContext(NbtOps.INSTANCE)`
- `TcBlockEntity` bridges `loadAdditional(CompoundTag, Provider)` and `saveAdditional(CompoundTag, Provider)` to `loadAdditional(ValueInput)` and `saveAdditional(ValueOutput)`. Block entities extending vanilla classes directly need the same bridge: `ArcaneBoreBlockEntity`, `HungryChestBlockEntity` (both `BaseContainerBlockEntity`), `InfernalFurnaceBlockEntity` (`BlockEntity`)

## Deviations on this branch

- Curios: slot ids `necklace`, `ring` (size raised to 2 by `data/curios/curios/slots/ring.json` with `ADD`), `belt`; `Baubles` slot order 0 necklace, 1-2 ring, 3 belt; `platform/BaublesCurios` registers every `Bauble` item as a curio in common setup
- Data attachments: `DataAttachments.SyncPayload` instead of `.sync()`, player attachments are resent on login, respawn and dimension change
- Village trades: `world/village/VillageTrades` (`VillagerTradesEvent`), the whole pool is added to every level 1-5 and vanilla picks 2, same tables as the `villager_trade` data on `main`; `tools/backport/trades.json` is the manifest written by the generator
- GameTests: names are `thaumcraft.<class>.<method>`; relative Y is one higher than on `main` (1.21.1 puts the structure block layer at relative Y 0); `runGameTestServer` leaves a flat `run/world`, delete it before `runServer`
- Tools from `Registration` use the `TOOL` component on a plain `Item` (no right-click block actions); fast food eats in 0.8 s instead of 0.5 s; goggles extend `ArmorItem` with the armor layer `textures/models/armor/goggles_layer_1.png`
- Blocks: `PlantSoil.PLAINS` uses `BlockTags.DIRT`; no `getEntityInsideCollisionShape`; workbench result slot without `safeClone`; bellows speed up furnaces through `CookTime`
- Rendering: `WorldFx` draws in `RenderLevelStageEvent` (`AFTER_PARTICLES`) instead of vanilla particles; translucent layer order follows call order instead of `order(n)`; REPEAT sampler is the default; GUI `nextStratum` became `flush`, tooltips draw immediately; the first person sleeve is always drawn
- Item models: special items render through `client/render/TcItemRenderer` (`BlockEntityWithoutLevelRenderer`) by `tools/backport/item_models_1.21.1.json`, tints through `ItemColor`
- Worldgen: patches (`count` + `offset` with `trapezoid` on 26.3) are written as `minecraft:random_patch` with `xz_spread` 7 and `y_spread` 3
- Aspects: smelting recipes (`AbstractCookingRecipe`) are read separately because they are not `SingleItemRecipe` on 1.21.1; `minecraft:boat` and `minecraft:chest_boat` entities have entries in `tools/modern_aspects/entities.json`

## Phase 8 validation (2026-10-11)

- compileJava, check_seams.py, validate_ids.py --mc 1.21.1 pass; all 32 GameTests pass (29 ported, 3 backport regressions).
- Dedicated server loads a normal TerraBlender world; focal manipulator and warded block placement, /reload, focus inventory and save-all pass without ERROR entries.
- Armor materials are registered before registry events; robe/void robe materials have dyeable base and undyed overlay layers. Dye recipes are generated and the six robes support water cauldron washing.
- compat/ItemStackTemplate preserves immutable item-stack components with value equality; raw ItemStack component values fail development validation.
- GearMovementPayload relays forward input for server traveller boots/haste because vanilla 1.21.1 lacks getLastClientInput.
- platform/CamouflagedBlockEntity owns ModelData/ModelProperty; only its two server-safe NeoForge model-data imports are exempted from the client-package seam rule.
- Visual validation and two-client animation/synchronization checks remain pending.
