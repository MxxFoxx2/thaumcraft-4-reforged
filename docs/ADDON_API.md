# Add-on API (experimental)

All hooks are plain static calls on Thaumcraft classes. Call them from your mod constructor unless noted otherwise. The client and the server must load the same add-ons.

## Aspects

- `AddonAspects.addRegistrar(api -> api.register(tag, color, components, blend, texture))` registers a new aspect.
  - Registrars run during `FMLCommonSetupEvent` on both sides. A registrar added after setup runs immediately.
  - Tags are global and must not contain `:`. Each aspect needs exactly two components. A duplicate tag throws.
  - The add-on provides the `tc.aspect.<tag>` translation and the texture.
- `Aspect` is a registry-backed class. Built-in ordinals 0-47 are stable, add-on aspects follow in tag order. Saves and packets store the tag; an ordinal is only valid within one session.
- `AspectSync.addRegistrar(Consumer<AspectRegistry>)` adds item and entity aspects. It runs on every rebuild, including `/reload`, after the built-in tables and before derived aspects are computed. Direct edits to `AspectRegistry.active()` are lost on `/reload`.
- `AspectRegistry.mergeEntity(type, aspects, nbt...)` and `AspectRegistrar.entityCopy(...)` add amounts to the entity entry with the same NBT conditions, or create a new entry.

## Wands

- `WandRegistry.registerCap(tag, discount, special, specialDiscount, item, craftCost, metal, texture)` and `WandRegistry.registerRod(base, capacity, item, craftCost, update, glowing, staff, runes, texture)`.
  - `item` and `texture` are full `Identifier`s in any namespace. A duplicate tag throws.
  - The add-on provides the research keys `CAP_<tag>` / `ROD_<tag>` and the translations `item.Wand.<tag>.cap` / `item.Wand.<tag>.rod`.
- `Wands.isWand(stack)` accepts any `WandItem` subclass, so an add-on may register its own wand item (for example a disposable wand with custom vis rules) and it is recognized by the workbench, focus use, charging and HUD like the base wand.
  - An add-on wand item sets its default parts and vis with `WandKeys.PARTS.apply(properties, new WandParts(...))` and `WandKeys.VIS.apply(properties, new WandVis(...))` in its `Item.Properties` operator. Build these records directly; do not create an `ItemStack` during registration (components are not bound yet).
- `WandTriggers.register(Predicate<BlockState>, WandTrigger)` adds a wand right-click action on a block.
- Focus pouches: subclass `FocusPouchItem` and override `pouchSize(stack)` and `canStore(stack)`. The static `getInventory` / `setInventory` and the wand focus switcher use the item's size and filter.

## Crafting

- `TcRecipes.register(recipe)` registers a TC4-keyed recipe and logs a warning when it silently replaces an existing one.
  - `TcRecipes.registerUnique(recipe)` throws instead: a key collision names the previously registered recipe and leaves it in place.
  - `TcRecipes.replace(recipe)` overwrites the key deliberately, without the warning.
  - Keys must be non-blank; add-ons should namespace them (`addonid:key`) so collisions stay impossible.
- `TcRecipes.arcane(key, research, result, aspects, pattern, symbols...)`, `arcaneShapeless(...)`, `infusion(...)`, `crucible(...)`, `enchantment(...)`, `compound(...)` and `vanilla(...)` construct and register a recipe in one call with `registerUnique` semantics.
  - Pattern rows expand into a grid padded with `TcIngredient.EMPTY`; every non-space symbol must map to an ingredient or the registration throws.
  - `crucible` and `infusion` have key-only overloads that reuse the key as the research key.
  - `vanilla(key, research, recipe, result)` takes the full `Identifier` of the vanilla crafting recipe.
- `ArcaneCrafting.registerDynamic(DynamicArcaneRecipe)` adds an arcane recipe computed at match time. Dynamic recipes are checked after the wand and sceptre recipes.
- `TcStack.registerDataHandler(key, BiConsumer<ItemStack, String>)`: `TcStack.create()` calls the handler for a recipe stack with this `data` key. An infusion result with such a key is created through `create()` instead of copying the central item.
- `TcStack.registerDataMatcher(key, BiPredicate<ItemStack, String>)`: `TcStack.matches()` requires the matcher to accept every `data` value with this key, like the metadata check in TC4 (for example a potion type).

## Nodes and vis

- `NodeGenerator.createNodeAt(LevelAccessor, BlockPos, NodeType, NodeModifier, AspectList)` places an aura node with exact aspects and type. `NodeType` includes `SYNTHETIC` and `ETHEREAL` for add-on machines; add-on node types must be appended after the built-ins so ordinal-based sync stays stable.
- `thaumcraft.node.Node` (implemented by `NodeBlockEntity`, `NodeJarBlockEntity`) exposes aspects, base capacity, type and modifier; `thaumcraft.vis.VisNet.drainVis(Level, BlockPos, Aspect, int)` drains vis from the nearest source network.

## Wands

- `thaumcraft.api.addon.VisCostModifiers.register((player, aspect, modifier) -> ...)` scales the final vis consumption multiplier for a player; the result is clamped by the wand as usual.

## Golems

- `GolemType` includes add-on materials (`GRASS`, `DIRT`, `PLANT`, `ROCK`, `METAL`, `CLOTH`, `SAND`, `REDSTONE`, `TNT`, `ICE`, `CACTUS`, `CAKE`, `WEB`, `VOID`) with a `visCost` field; append further materials after the built-ins. `GolemPlacerItem(Properties, GolemType)` and `GolemBellItem` work with any `GolemBase` subclass; golem deaths surface through the standard `LivingDeathEvent`.

## Research pages

- `ResearchItem.when(BooleanSupplier)` gates `isAvailable()`: the entry stays unavailable while the supplier fails. Keep the supplier config-driven and identical on the client and the dedicated server.
- `ResearchItem.setTextPages(String... pages)` builds text pages with `tc.research_page.<key>.<page>` lang keys instead of manual `ResearchPage.text(...)` calls.
- `ResearchItem.requireInfusionResearch()` adds a hidden `INFUSION` parent at registration when the entry has an infusion page; opt-in, not every infusion-bearing entry needs it.
- `ResearchPage.creatureInfusion(String payload)` adds an add-on page type; the stock screen renders a localized placeholder line (`tc.research_page.creature_infusion`) and add-ons may render the payload themselves.
- Custom research entries and categories use the same public statics as the built-in ones (`ResearchCategories.registerCategory`, `ResearchItem.registerResearchItem`).

## FX

- `Fx.get()` returns the side-safe particle proxy. All methods are optional no-ops on the dedicated server; `blocksplosion`, `containmentCapture` and `deadCreature` are reserved for add-on machines.
- `Fx.get().machineBeam(level, key, from, to, color, width, type)` creates or updates a persistent client-side beam per dimension and block position. The color, width and texture type are fixed when the beam is first created; later calls only move its endpoints.

## Other hooks

- `Warp.registerItem(Identifier, int)` makes an item give warp when crafted.
- `CropUtils.addClickableCrop(Predicate<BlockState>)` marks a grown crop that golems harvest by right-click.
- `GolemFluids.registerDrainable(Predicate<BlockState>, Drainable)` lets liquid golems drain a custom fluid block.
- `TcArmorMaterial` takes a full `Identifier` for the armor asset, so add-on armor can use its own namespace.
