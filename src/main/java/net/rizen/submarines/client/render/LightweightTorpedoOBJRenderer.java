package net.rizen.submarines.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.rizen.submarines.api.client.render.BaseOBJEntityRenderer;
import net.rizen.submarines.api.torpedo.BaseTorpedo;

public class LightweightTorpedoOBJRenderer extends BaseOBJEntityRenderer<BaseTorpedo> {
    private static final Identifier MODEL = Identifier.of("submarines", "lightweight_torpedo.obj");
    private static final Identifier TEXTURE = Identifier.of("submarines", "textures/entity/lightweight_torpedo.png");

    public LightweightTorpedoOBJRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, MODEL, TEXTURE);
    }

    @Override
    protected void applyTransformations(BaseTorpedo entity, float yaw, float tickDelta, MatrixStack matrices) {

        matrices.translate(0, 0, 1.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw + 180));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch(tickDelta)));
        matrices.translate(0, 0, -1.0);
    }
}