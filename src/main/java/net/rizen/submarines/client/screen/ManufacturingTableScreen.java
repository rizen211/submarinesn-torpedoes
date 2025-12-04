package net.rizen.submarines.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.rizen.submarines.api.crafting.ManufacturingRecipe;
import net.rizen.submarines.api.crafting.ManufacturingRecipeRegistry;
import net.rizen.submarines.api.network.packet.ManufacturingCraftPacket;
import net.rizen.submarines.screen.ManufacturingTableScreenHandler;

import java.util.*;

public class ManufacturingTableScreen extends HandledScreen<ManufacturingTableScreenHandler> {

    private static final List<String> CATEGORIES = Arrays.asList(
        ManufacturingRecipe.CATEGORY_SUBMARINES,
        ManufacturingRecipe.CATEGORY_WEAPONS,
        ManufacturingRecipe.CATEGORY_COMPONENTS
    );
    private static final int BUTTON_HEIGHT = 24;
    private static final int RECIPE_BUTTON_HEIGHT = 20;

    private static final int COLOR_BACKGROUND = 0xFF1E1E2E;
    private static final int COLOR_PANEL = 0xFF2A2A3E;
    private static final int COLOR_PANEL_LIGHT = 0xFF353550;
    private static final int COLOR_HEADER = 0xFF181825;
    private static final int COLOR_BORDER_DARK = 0xFF0F0F18;
    private static final int COLOR_BORDER_LIGHT = 0xFF3E3E5E;
    private static final int COLOR_ACCENT = 0xFF89B4FA;
    private static final int COLOR_SUCCESS = 0xFF94E2D5;
    private static final int COLOR_ERROR = 0xFFF38BA8;
    private static final int COLOR_TEXT = 0xFFCDD6F4;
    private static final int COLOR_TEXT_DIM = 0xFF9399B2;
    private static final int COLOR_SCROLLBAR = 0xFF45475A;
    private static final int COLOR_SCROLLBAR_THUMB = 0xFF585B70;

    private String selectedCategory = null;
    private ManufacturingRecipe selectedRecipe = null;
    private List<String> categories = new ArrayList<>();
    private Map<String, List<ButtonWidget>> categoryButtons = new HashMap<>();
    private List<ButtonWidget> recipeButtons = new ArrayList<>();
    private ButtonWidget craftButton;


    private float scrollOffset = 0.0f;
    private int maxScroll = 0;
    private boolean isDraggingScrollbar = false;
    private int recipeListHeight = 0;

    public ManufacturingTableScreen(ManufacturingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 260;
        this.backgroundHeight = 240;
    }

    @Override
    protected void init() {
        super.init();

        categories = new ArrayList<>(CATEGORIES);

        if (!categories.isEmpty() && selectedCategory == null) {
            selectedCategory = categories.get(0);
        }

        int leftX = this.x + 14;
        int startY = this.y + 35;

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            int yPos = startY + (i * (BUTTON_HEIGHT + 4));

            ButtonWidget button = ButtonWidget.builder(
                Text.literal(capitalizeFirstLetter(category)),
                btn -> {
                    this.selectedCategory = category;
                    this.selectedRecipe = null;
                    this.scrollOffset = 0;
                    rebuildRecipeButtons();
                    updateButtonStates();
                }
            ).dimensions(leftX, yPos, 88, BUTTON_HEIGHT).build();

            this.addDrawableChild(button);
            categoryButtons.computeIfAbsent(category, k -> new ArrayList<>()).add(button);
        }

        int craftButtonX = this.x + (this.backgroundWidth - 110) / 2 + 55;
        int craftButtonY = this.y + 208;
        craftButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Craft"),
            button -> handleCraft()
        ).dimensions(craftButtonX, craftButtonY, 110, 26).build());

        rebuildRecipeButtons();
        updateButtonStates();
    }

    private void rebuildRecipeButtons() {
        for (ButtonWidget button : recipeButtons) {
            this.remove(button);
        }
        recipeButtons.clear();

        if (selectedCategory == null) {
            return;
        }

        List<ManufacturingRecipe> recipes = ManufacturingRecipeRegistry.getRecipesByCategory(selectedCategory);

        int leftX = this.x + 14;
        int startY = this.y + 120;
        recipeListHeight = 75;

        int totalHeight = recipes.size() * (RECIPE_BUTTON_HEIGHT + 2);
        maxScroll = Math.max(0, totalHeight - recipeListHeight);

        for (int i = 0; i < recipes.size(); i++) {
            ManufacturingRecipe recipe = recipes.get(i);
            int yPos = startY + (i * (RECIPE_BUTTON_HEIGHT + 2)) - (int)scrollOffset;

            ButtonWidget button = ButtonWidget.builder(
                Text.literal(recipe.getDisplayName()),
                btn -> {
                    this.selectedRecipe = recipe;
                    updateButtonStates();
                }
            ).dimensions(leftX, yPos, 88, RECIPE_BUTTON_HEIGHT).build();

            button.visible = yPos >= startY && yPos + RECIPE_BUTTON_HEIGHT <= startY + recipeListHeight;

            this.addDrawableChild(button);
            recipeButtons.add(button);
        }
    }

    private void updateButtonStates() {
        for (Map.Entry<String, List<ButtonWidget>> entry : categoryButtons.entrySet()) {
            boolean selected = entry.getKey().equals(selectedCategory);
            for (ButtonWidget btn : entry.getValue()) {
                btn.active = !selected;
            }
        }

        for (int i = 0; i < recipeButtons.size(); i++) {
            ButtonWidget btn = recipeButtons.get(i);
            List<ManufacturingRecipe> recipes = ManufacturingRecipeRegistry.getRecipesByCategory(selectedCategory);
            if (i < recipes.size()) {
                btn.active = recipes.get(i) != selectedRecipe;
            }
        }

        craftButton.active = selectedRecipe != null && this.client.player != null &&
                             ManufacturingRecipeRegistry.hasIngredients(this.client.player.getInventory(), selectedRecipe);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, COLOR_BACKGROUND);
        context.drawBorder(x, y, backgroundWidth, backgroundHeight, COLOR_BORDER_DARK);
        context.drawBorder(x + 1, y + 1, backgroundWidth - 2, backgroundHeight - 2, COLOR_BORDER_LIGHT);

        context.fill(x + 2, y + 2, x + backgroundWidth - 2, y + 24, COLOR_HEADER);
        context.fill(x + 2, y + 22, x + backgroundWidth - 2, y + 24, COLOR_BORDER_LIGHT);

        int categoryPanelX = x + 10;
        int categoryPanelY = y + 30;
        int categoryPanelWidth = 106;
        int categoryPanelHeight = 170;

        drawPanel(context, categoryPanelX, categoryPanelY, categoryPanelWidth, categoryPanelHeight);

        int recipePanelX = x + 122;
        int recipePanelY = y + 30;
        int recipePanelWidth = 130;
        int recipePanelHeight = 170;

        drawPanel(context, recipePanelX, recipePanelY, recipePanelWidth, recipePanelHeight);
        context.fill(x + 10, y + 204, x + backgroundWidth - 10, y + 205, COLOR_ACCENT);

        int separatorY = categoryPanelY + 86;
        context.fill(categoryPanelX + 4, separatorY, categoryPanelX + categoryPanelWidth - 4, separatorY + 1, COLOR_BORDER_LIGHT);

        if (maxScroll > 0) {
            drawScrollbar(context, categoryPanelX + categoryPanelWidth - 12, categoryPanelY + 90, recipeListHeight);
        }
    }

    private void drawPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, COLOR_PANEL);
        context.fill(x + 2, y + 2, x + width - 2, y + height - 2, COLOR_PANEL_LIGHT);

        context.drawBorder(x, y, width, height, COLOR_BORDER_DARK);
        context.drawBorder(x + 1, y + 1, width - 2, height - 2, COLOR_BORDER_LIGHT);
    }

    private void drawScrollbar(DrawContext context, int x, int y, int height) {
        context.fill(x, y, x + 6, y + height, COLOR_SCROLLBAR);

        if (maxScroll > 0) {
            int thumbHeight = Math.max(10, (int)(height * (height / (float)(height + maxScroll))));
            int thumbY = y + (int)((height - thumbHeight) * (scrollOffset / maxScroll));
            context.fill(x + 1, thumbY, x + 5, thumbY + thumbHeight, COLOR_SCROLLBAR_THUMB);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (selectedRecipe != null) {
            renderRecipeDetails(context, this.x + 130, this.y + 40);
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void renderRecipeDetails(DrawContext context, int x, int y) {
        if (this.client.player == null) return;
        PlayerInventory inventory = this.client.player.getInventory();

        context.drawText(this.textRenderer, "Required Materials:", x, y, COLOR_ACCENT, true);

        int ingredientY = y + 14;
        for (ManufacturingRecipe.Ingredient ingredient : selectedRecipe.getIngredients()) {
            int count = countItemInInventory(inventory, ingredient.getItem());
            boolean hasEnough = count >= ingredient.getCount();
            int color = hasEnough ? COLOR_SUCCESS : COLOR_ERROR;

            context.drawItem(new ItemStack(ingredient.getItem()), x, ingredientY);
            context.drawText(this.textRenderer, "x" + ingredient.getCount(), x + 20, ingredientY, COLOR_TEXT_DIM, false);
            String itemName = new ItemStack(ingredient.getItem()).getName().getString();
            context.drawText(this.textRenderer, itemName, x + 20, ingredientY + 8, color, false);

            ingredientY += 20;
        }

        int separatorY = ingredientY + 4;
        context.fill(x - 5, separatorY, x - 5 + 125, separatorY + 1, COLOR_BORDER_LIGHT);

        context.drawText(this.textRenderer, "Result:", x, separatorY + 6, COLOR_ACCENT, true);
        context.drawItem(selectedRecipe.getResult(), x + 45, separatorY + 16);

        boolean canCraft = ManufacturingRecipeRegistry.hasIngredients(inventory, selectedRecipe);
        if (canCraft) {
            context.drawText(this.textRenderer, "Ready!", x + 69, separatorY + 22, COLOR_SUCCESS, true);
        } else {
            context.drawText(this.textRenderer, "Missing", x + 69, separatorY + 22, COLOR_ERROR, false);
        }
    }

    private int countItemInInventory(PlayerInventory inventory, Item item) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void handleCraft() {
        if (selectedRecipe == null) {
            return;
        }

        if (ManufacturingRecipeRegistry.hasIngredients(this.client.player.getInventory(), selectedRecipe)) {
            craftButton.active = false;
            ClientPlayNetworking.send(new ManufacturingCraftPacket(
                selectedRecipe.getId(),
                this.handler.getTablePos()
            ));
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        context.drawText(this.textRenderer, this.title, titleX, 8, COLOR_TEXT, true);

        if (selectedCategory != null) {
            int indicatorX = 12;
            int categoryIndex = categories.indexOf(selectedCategory);
            if (categoryIndex >= 0) {
                int indicatorY = 35 + (categoryIndex * 28);
                context.fill(indicatorX, indicatorY, indicatorX + 2, indicatorY + BUTTON_HEIGHT, COLOR_ACCENT);
            }
        }

        if (selectedRecipe != null && selectedCategory != null) {
            List<ManufacturingRecipe> recipes = ManufacturingRecipeRegistry.getRecipesByCategory(selectedCategory);
            int recipeIndex = recipes.indexOf(selectedRecipe);
            if (recipeIndex >= 0) {
                int indicatorX = 12;
                int indicatorY = 120 + (recipeIndex * (RECIPE_BUTTON_HEIGHT + 2)) - (int)scrollOffset;

                if (indicatorY >= 120 && indicatorY < 120 + recipeListHeight) {
                    context.fill(indicatorX, indicatorY, indicatorX + 2, indicatorY + RECIPE_BUTTON_HEIGHT, COLOR_SUCCESS);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (float)(verticalAmount * 10)));
            updateRecipeButtonVisibility();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (maxScroll > 0) {
            int scrollbarX = this.x + 10 + 106 - 12;
            int scrollbarY = this.y + 90;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 6 &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + recipeListHeight) {
                isDraggingScrollbar = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingScrollbar && maxScroll > 0) {
            int scrollbarY = this.y + 90;
            float relativeY = (float)(mouseY - scrollbarY) / recipeListHeight;
            scrollOffset = Math.max(0, Math.min(maxScroll, relativeY * maxScroll));
            updateRecipeButtonVisibility();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateRecipeButtonVisibility() {
        int startY = this.y + 120;
        for (int i = 0; i < recipeButtons.size(); i++) {
            ButtonWidget button = recipeButtons.get(i);
            int yPos = startY + (i * (RECIPE_BUTTON_HEIGHT + 2)) - (int)scrollOffset;
            button.setY(yPos);
            button.visible = yPos >= startY && yPos + RECIPE_BUTTON_HEIGHT <= startY + recipeListHeight;
        }
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
