package net.rizen.submarines.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.rizen.submarines.api.client.render.BaseOBJEntityRenderer;
import net.rizen.submarines.api.submarine.BaseSubmarine;

public class TacticalSubmarineOBJRenderer extends BaseOBJEntityRenderer<BaseSubmarine> {
    private static final Identifier MODEL = Identifier.of("submarines", "tactical_submarine.obj");
    private static final Identifier TEXTURE = Identifier.of("submarines", "textures/entity/tactical_submarine.png");

    public TacticalSubmarineOBJRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, MODEL, TEXTURE);
    }
}