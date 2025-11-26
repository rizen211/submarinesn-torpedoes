package net.rizen.submarines.api.crafting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a recipe for the Manufacturing Table. Each recipe has a unique ID, required ingredients,
 * and a result item. Used for custom crafting recipes for submarines, torpedoes, and components.
 */
public class ManufacturingRecipe {

    public static final String CATEGORY_SUBMARINES = "submarines";
    public static final String CATEGORY_WEAPONS = "weapons";
    public static final String CATEGORY_COMPONENTS = "components";
    private final Identifier id;
    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private final String category;
    private final String displayName;

    private ManufacturingRecipe(Identifier id, List<Ingredient> ingredients, ItemStack result, String category, String displayName) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
        this.category = category;
        this.displayName = displayName;
    }

    public Identifier getId() {
        return id;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result;
    }

    public String getCategory() {
        return category;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Creates a new builder for constructing a manufacturing recipe.
     *
     * @param id unique identifier for this recipe
     * @return a new recipe builder
     */
    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static class Builder {
        private final Identifier id;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private ItemStack result;
        private String category = "general";
        private String displayName;

        private Builder(Identifier id) {
            this.id = id;
        }

        /**
         * Adds an ingredient requirement to this recipe.
         *
         * @param item the item required
         * @param count how many of this item are needed
         * @return this builder for chaining
         */
        public Builder ingredient(Item item, int count) {
            this.ingredients.add(new Ingredient(item, count));
            return this;
        }

        /**
         * Sets the result item for this recipe. Produces exactly one of this item per craft.
         *
         * @param item the item to produce
         * @return this builder for chaining
         */
        public Builder result(Item item) {
            this.result = new ItemStack(item);
            return this;
        }

        /**
         * Sets the result item stack for this recipe. Used for crafting multiple items at once
         * or when specific NBT data is required.
         *
         * @param stack the item stack to produce
         * @return this builder for chaining
         */
        public Builder result(ItemStack stack) {
            this.result = stack;
            return this;
        }

        /**
         * Sets the category for this recipe. Recipes are grouped by category in the GUI.
         * Must be one of: CATEGORY_SUBMARINES, CATEGORY_WEAPONS, or CATEGORY_COMPONENTS
         *
         * @param category the category name (use ManufacturingRecipe.CATEGORY_* constants)
         * @return this builder for chaining
         */
        public Builder category(String category) {
            if (!category.equals(CATEGORY_SUBMARINES) &&
                !category.equals(CATEGORY_WEAPONS) &&
                !category.equals(CATEGORY_COMPONENTS)) {
                throw new IllegalArgumentException("Invalid category: " + category +
                    ". Must be one of: CATEGORY_SUBMARINES, CATEGORY_WEAPONS, CATEGORY_COMPONENTS");
            }
            this.category = category;
            return this;
        }

        /**
         * Sets the display name for this recipe shown in the GUI.
         *
         * @param displayName the name to display
         * @return this builder for chaining
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Builds the recipe. Throws an exception if the result hasn't been set.
         *
         * @return the completed recipe
         */
        public ManufacturingRecipe build() {
            if (result == null || result.isEmpty()) {
                throw new IllegalStateException("Recipe " + id + " must have a result");
            }
            if (ingredients.isEmpty()) {
                throw new IllegalStateException("Recipe " + id + " must have at least one ingredient");
            }
            if (displayName == null || displayName.isEmpty()) {
                // Default to result item name if no display name provided
                displayName = result.getName().getString();
            }
            return new ManufacturingRecipe(id, new ArrayList<>(ingredients), result.copy(), category, displayName);
        }
    }

    /**
     * Represents a single ingredient in a recipe - what item and how many are needed.
     */
    public static class Ingredient {
        private final Item item;
        private final int count;

        public Ingredient(Item item, int count) {
            this.item = item;
            this.count = count;
        }

        public Item getItem() {
            return item;
        }

        public int getCount() {
            return count;
        }
    }
}
