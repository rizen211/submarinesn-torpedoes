package net.rizen.submarines.crafting;

import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.rizen.submarines.Mod;
import net.rizen.submarines.api.crafting.ManufacturingRecipe;
import net.rizen.submarines.api.crafting.ManufacturingRecipeRegistry;

public class DefaultRecipes {

    public static void register() {
        registerTacticalSubmarine();
        registerLightweightTorpedo();
        registerComponents();
        registerRepairTool();
    }

    private static void registerTacticalSubmarine() {
        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of(Mod.MOD_ID, "tactical_submarine"))
                .ingredient(Mod.TRANSDUCER_ITEM, 1)
                .ingredient(Mod.STEEL_CASING_ITEM, 36)
                .ingredient(Items.CHEST, 1)
                .ingredient(Items.LAVA_BUCKET, 1)
                .result(Mod.SUBMARINE_ITEM)
                .category(ManufacturingRecipe.CATEGORY_SUBMARINES)
                .displayName("Tactical Submarine")
                .build()
        );
    }

    private static void registerLightweightTorpedo() {
        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of(Mod.MOD_ID, "lightweight_torpedo"))
                .ingredient(Mod.WARHEAD_ITEM, 1)
                .ingredient(Mod.STEEL_CASING_ITEM, 2)
                .ingredient(Items.GREEN_DYE, 2)
                .result(Mod.TORPEDO_ITEM)
                .category(ManufacturingRecipe.CATEGORY_WEAPONS)
                .displayName("Lightweight Torpedo")
                .build()
        );
    }

    private static void registerComponents() {
        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of(Mod.MOD_ID, "transducer"))
                .ingredient(Items.COPPER_INGOT, 9)
                .ingredient(Items.AMETHYST_SHARD, 9)
                .ingredient(Items.IRON_INGOT, 4)
                .result(Mod.TRANSDUCER_ITEM)
                .category(ManufacturingRecipe.CATEGORY_COMPONENTS)
                .displayName("Transducer")
                .build()
        );

        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of(Mod.MOD_ID, "warhead"))
                .ingredient(Items.TNT, 1)
                .ingredient(Items.IRON_INGOT, 2)
                .result(Mod.WARHEAD_ITEM)
                .category(ManufacturingRecipe.CATEGORY_COMPONENTS)
                .displayName("Warhead")
                .build()
        );

        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of(Mod.MOD_ID, "steel_casing"))
                .ingredient(Items.IRON_INGOT, 3)
                .result(Mod.STEEL_CASING_ITEM)
                .category(ManufacturingRecipe.CATEGORY_COMPONENTS)
                .displayName("Steel Casing")
                .build()
        );
    }

    private static void registerRepairTool() {
        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of(Mod.MOD_ID, "submarine_repair_tool"))
                .ingredient(Items.IRON_INGOT, 4)
                .result(Mod.SUBMARINE_REPAIR_TOOL)
                .category(ManufacturingRecipe.CATEGORY_COMPONENTS)
                .displayName("Repair Tool")
                .build()
        );
    }
}
