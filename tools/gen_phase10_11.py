import json

NS = "thaumcraft"


def write_damage_types(ctx):
    definitions = {
        "taint": ["minecraft:bypasses_armor", "neoforge:is_magic"],
        "dissolve": ["minecraft:bypasses_armor"],
        "tentacle": [],
        "swarm": [],
    }
    for name, tags in definitions.items():
        ctx.write(
            ctx.DATA / NS / "damage_type" / f"{name}.json",
            {"exhaustion": 0.3, "message_id": f"{NS}.{name}", "scaling": "when_caused_by_living_non_player"},
        )
        for tag in tags:
            namespace, path = tag.split(":", 1)
            tag_path = ctx.DATA / namespace / "tags" / "damage_type" / f"{path}.json"
            current = json.loads(tag_path.read_text(encoding="utf-8")) if tag_path.exists() else {"replace": False, "values": []}
            values = current["values"]
            if f"{NS}:{name}" not in values:
                values.append(f"{NS}:{name}")
            current["values"] = sorted(values, key=lambda entry: entry if isinstance(entry, str) else entry["id"])
            ctx.write(tag_path, current)

    entity_tags = {
        "arthropod": ["eldritch_crab", "mind_spider", "taint_spider"],
        "undead": ["brainy_zombie", "giant_brainy_zombie", "inhabited_zombie", "eldritch_guardian"],
    }
    for name, entities in entity_tags.items():
        path = ctx.DATA / "minecraft" / "tags" / "entity_type" / f"{name}.json"
        current = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {"replace": False, "values": []}
        values = current["values"]
        for entity in entities:
            identifier = f"{NS}:{entity}"
            if identifier not in values:
                values.append(identifier)
        current["values"] = sorted(values, key=lambda entry: entry if isinstance(entry, str) else entry["id"])
        ctx.write(path, current)

    ctx.write(
        ctx.DATA / NS / ctx.profile.loot_folder / "entities" / "inhabited_zombie.json",
        ctx.profile.loot_table({
            "type": "minecraft:entity",
            "pools": [],
            "random_sequence": f"{NS}:entities/inhabited_zombie",
        }),
    )


def write_spawn_modifiers(ctx):
    profile = ctx.profile
    brainy_biomes = [
        "minecraft:forest",
        "minecraft:flower_forest",
        "minecraft:birch_forest",
        "minecraft:old_growth_birch_forest",
        "minecraft:dark_forest",
        "minecraft:plains",
        "minecraft:sunflower_plains",
        "minecraft:taiga",
        "minecraft:snowy_taiga",
        "minecraft:old_growth_pine_taiga",
        "minecraft:old_growth_spruce_taiga",
        "minecraft:jungle",
        "minecraft:sparse_jungle",
        "minecraft:bamboo_jungle",
    ]
    ctx.write(
        ctx.DATA / NS / "tags" / "worldgen" / "biome" / "brainy_zombie_biomes.json",
        {"replace": False, "values": brainy_biomes},
    )
    modifiers = {
        "brainy_zombie": (f"#{NS}:brainy_zombie_biomes", 10),
        "pech": ("#c:is_magical", 10),
        "firebat": ("#minecraft:is_nether", 10, 1, 2),
        "wisp": ("#minecraft:is_nether", 5),
        "taintacle": (f"{NS}:tainted_land", 1),
    }
    for name, settings in modifiers.items():
        biomes, weight, *counts = settings
        ctx.write(
            ctx.DATA / profile.biome_modifier_namespace / "biome_modifier" / f"{name}_spawns.json",
            profile.spawn_modifier(biomes, f"{NS}:{name}", weight, *(counts or [1, 1])),
        )
    ctx.write(
        ctx.DATA / profile.biome_modifier_namespace / "biome_modifier" / "halloween_firebat_spawns.json",
        {"type": f"{NS}:halloween_firebat_spawns", "biomes": f"#{NS}:brainy_zombie_biomes"},
    )


def block(ctx, name, texture):
    ctx.cube_all(name, texture)
    ctx.simple_state(name)
    ctx.block_item(name)


def silk_touch_drop(ctx, block, item=None, count=1):
    entries = [{"type": "minecraft:item", "condition": "minecraft:tool/can_silk_touch", "name": f"{NS}:{block}"}]
    if item is not None:
        drop = {"type": "minecraft:item", "name": item}
        if count > 1:
            drop["functions"] = [{"function": "minecraft:set_count", "count": count}]
        entries.append(drop)
    ctx.write_loot(block, [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": entries}]}])


def no_drop(ctx, name):
    ctx.write_loot(name, [])


def cross(ctx, name, texture):
    ctx.block_model(name, {"parent": "minecraft:block/cross", "textures": {"cross": ctx.ref(texture, "block")}})
    ctx.simple_state(name)
    ctx.generated_item(name, texture, texture_kind="block")
    ctx.self_drop(name)


def fluid_item(ctx, item, texture):
    ctx.generated_item(item, texture)


def generate(ctx):
    write_damage_types(ctx)
    write_spawn_modifiers(ctx)

    for name, texture in [
        ("crusted_taint", "taint_crust"),
        ("tainted_soil", "taint_soil"),
        ("flesh_block", "fleshblock"),
    ]:
        block(ctx, name, texture)
    silk_touch_drop(ctx, "crusted_taint")
    silk_touch_drop(ctx, "tainted_soil", "minecraft:dirt")
    silk_touch_drop(ctx, "flesh_block", "minecraft:rotten_flesh", 9)

    for name, texture in [
        ("fibrous_taint", "taint_fibres"),
        ("tainted_grass", "taintgrass1"),
        ("tainted_plant", "taintgrass2"),
        ("spore_stalk", "taint_spore_stalk_1"),
        ("mature_spore_stalk", "taint_spore_stalk_2"),
    ]:
        cross(ctx, name, texture)
        no_drop(ctx, name)

    for name, texture in [
        ("purifying_fluid_bucket", "bucket_pure"),
        ("liquid_death_bucket", "bucket_death"),
        ("bath_salts", "bath_salts"),
        ("sanitizing_soap", "soap"),
        ("bottled_taint", "bottle_taint"),
    ]:
        fluid_item(ctx, name, texture)

    fluid_item(ctx, "crimson_rites", "crimson_rites")

    for name in ["purifying_fluid", "liquid_death"]:
        ctx.block_model(name, {"parent": "minecraft:block/block", "textures": {"particle": ctx.ref("fluidpure" if name == "purifying_fluid" else "fluiddeath", "block")}})
        ctx.simple_state(name)
