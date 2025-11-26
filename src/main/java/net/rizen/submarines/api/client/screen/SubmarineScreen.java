package net.rizen.submarines.api.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.rizen.submarines.api.submarine.SubmarineScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * The inventory screen that shows up when you open the submarine inventory.
 */
public class SubmarineScreen extends HandledScreen<SubmarineScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");

    public SubmarineScreen(SubmarineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 168;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, 3 * 18 + 17);
        context.drawTexture(TEXTURE, x, y + 3 * 18 + 17, 0, 126, this.backgroundWidth, 96);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}