package net.rizen.submarines.api.crafting;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.rizen.submarines.Mod;
import org.slf4j.Logger;

import java.util.*;

/**
 * Global registry for all Manufacturing Table recipes. Provides recipe registration
 * for custom crafting of submarines, torpedoes, and other items.
 */
public class ManufacturingRecipeRegistry {
    private static final Logger LOGGER = Mod.LOGGER;
    private static final Map<Identifier, ManufacturingRecipe> RECIPES = new HashMap<>();

    /**
     * Registers a new recipe in the manufacturing table. Recipes with duplicate IDs will be replaced
     * and a warning will be logged.
     *
     * @param recipe the recipe to register
     */
    public static void register(ManufacturingRecipe recipe) {
        if (RECIPES.containsKey(recipe.getId())) {
            LOGGER.warn("Replacing existing recipe with ID: {}", recipe.getId());
        }
        RECIPES.put(recipe.getId(), recipe);
        LOGGER.info("Registered manufacturing recipe: {}", recipe.getId());
    }

    /**
     * Gets a recipe by its identifier.
     *
     * @param id the recipe ID
     * @return the recipe, or null if not found
     */
    public static ManufacturingRecipe getRecipe(Identifier id) {
        return RECIPES.get(id);
    }

    /**
     * Gets all recipes in a specific category.
     *
     * @param category the category to filter by
     * @return a list of recipes in that category
     */
    public static List<ManufacturingRecipe> getRecipesByCategory(String category) {
        List<ManufacturingRecipe> result = new ArrayList<>();
        for (ManufacturingRecipe recipe : RECIPES.values()) {
            if (recipe.getCategory().equals(category)) {
                result.add(recipe);
            }
        }
        return result;
    }

    /**
     * Checks if a player inventory contains all required ingredients for a recipe.
     *
     * @param inventory the player's inventory
     * @param recipe the recipe to check
     * @return true if all ingredients are present in sufficient quantities
     */
    public static boolean hasIngredients(PlayerInventory inventory, ManufacturingRecipe recipe) {
        for (ManufacturingRecipe.Ingredient ingredient : recipe.getIngredients()) {
            if (!hasItems(inventory, ingredient.getItem(), ingredient.getCount())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Consumes all ingredients for a recipe from the player inventory.
     * Should only be called after verifying ingredient availability.
     *
     * @param inventory the player's inventory
     * @param recipe the recipe being crafted
     */
    public static void consumeIngredients(PlayerInventory inventory, ManufacturingRecipe recipe) {
        for (ManufacturingRecipe.Ingredient ingredient : recipe.getIngredients()) {
            consumeItems(inventory, ingredient.getItem(), ingredient.getCount());
        }
    }

    /**
     * Attempts to craft a recipe. Checks ingredient availability, consumes them, and returns the result.
     *
     * @param inventory the player's inventory
     * @param recipe the recipe to craft
     * @return the crafted item stack, or an empty stack if crafting failed
     */
    public static ItemStack tryCraft(PlayerInventory inventory, ManufacturingRecipe recipe) {
        if (!hasIngredients(inventory, recipe)) {
            return ItemStack.EMPTY;
        }

        consumeIngredients(inventory, recipe);
        return recipe.getResult().copy();
    }

    private static boolean hasItems(PlayerInventory inventory, Item item, int amount) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
                if (count >= amount) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void consumeItems(PlayerInventory inventory, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.size() && remaining > 0; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
    }
}
