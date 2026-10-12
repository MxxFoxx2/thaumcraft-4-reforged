import unittest

from validate_ids import Validator


class BlockStateValidatorTests(unittest.TestCase):
    def setUp(self):
        self.validator = Validator.__new__(Validator)
        self.validator.problems = []
        self.properties = {
            "facing": {"north", "east", "south", "west"},
            "half": {"top", "bottom"},
        }

    def test_multipart_string_value_is_single_value(self):
        self.validator.check_state_condition(
            "blockstates/oak_stairs.json",
            "minecraft:oak_stairs",
            {"facing": "north"},
            self.properties,
        )
        self.assertEqual([], self.validator.problems)

    def test_multipart_and_or_conditions_are_checked_recursively(self):
        self.validator.check_state_condition(
            "blockstates/oak_stairs.json",
            "minecraft:oak_stairs",
            {"OR": [{"facing": "north"}, {"AND": [{"half": "top|bottom"}, {"facing": "south"}]}]},
            self.properties,
        )
        self.assertEqual([], self.validator.problems)

        self.validator.check_state_condition(
            "blockstates/oak_stairs.json",
            "minecraft:oak_stairs",
            {"OR": [{"facing": "north"}, {"half": "middle"}]},
            self.properties,
        )
        self.assertEqual(
            [("blockstates/oak_stairs.json", "minecraft:oak_stairs[half=middle]", "block-state property value")],
            self.validator.problems,
        )

    def test_shader_references_are_checked_against_shader_stages(self):
        self.validator.shader_stages = {"minecraft:sobel"}
        self.validator.check("assets/thaumcraft/shaders/program/example.json", "minecraft:sobel", "shader")
        self.validator.check("assets/thaumcraft/shaders/program/example.json", "minecraft:missing", "shader")
        self.assertEqual(
            [("assets/thaumcraft/shaders/program/example.json", "minecraft:missing", "shader")],
            self.validator.problems,
        )

    def test_modern_post_effect_validates_shader_sources_and_main_target(self):
        self.assertEqual("shader", self.validator.classify("minecraft:core/screenquad", ["vertex_shader"], "post_effect"))
        self.assertEqual("skip", self.validator.classify("minecraft:main", ["output"], "post_effect"))
        self.assertEqual("flat", self.validator.classify("minecraft:missing", ["vertex_shader"], "other"))

    def test_main_framebuffer_is_a_valid_special_shader_target(self):
        self.assertEqual("skip", self.validator.classify("minecraft:main", ["intarget"], "shader_post"))
        self.assertEqual("flat", self.validator.classify("minecraft:missing", ["name"], "shader_post"))

    def test_recipe_serializer_ids_are_not_skipped(self):
        self.validator.registry = {"recipe_serializer": {"minecraft:crafting_shaped"}}
        self.validator.check("recipes/example.json", "minecraft:crafting_shaped", "recipe_serializer")
        self.validator.check("recipes/example.json", "minecraft:missing_recipe_for_test", "recipe_serializer")
        self.assertEqual(
            [("recipes/example.json", "minecraft:missing_recipe_for_test", "recipe_serializer")],
            self.validator.problems,
        )


if __name__ == "__main__":
    unittest.main()
