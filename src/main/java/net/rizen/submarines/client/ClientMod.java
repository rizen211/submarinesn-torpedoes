package net.rizen.submarines.client;

import net.rizen.submarines.Mod;
import net.rizen.submarines.api.client.hud.SubmarineHud;
import net.rizen.submarines.api.client.input.SubmarineInputHandler;
import net.rizen.submarines.api.client.screen.SubmarineScreen;
import net.rizen.submarines.client.render.TacticalSubmarineOBJRenderer;
import net.rizen.submarines.client.render.LightweightTorpedoOBJRenderer;
import net.rizen.submarines.client.screen.ManufacturingTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Mod.TACTICAL_SUBMARINE_ENTITY, TacticalSubmarineOBJRenderer::new);
        EntityRendererRegistry.register(Mod.LIGHTWEIGHT_TORPEDO_ENTITY, LightweightTorpedoOBJRenderer::new);

        SubmarineInputHandler.register();
        SubmarineHud.register();
        HandledScreens.register(Mod.SUBMARINE_SCREEN_HANDLER, SubmarineScreen::new);
        HandledScreens.register(Mod.MANUFACTURING_TABLE_SCREEN_HANDLER, ManufacturingTableScreen::new);
    }
}