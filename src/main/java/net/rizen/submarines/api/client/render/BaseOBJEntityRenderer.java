package net.rizen.submarines.api.client.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.rizen.submarines.api.client.model.OBJEntityModel;

/**
 * Base renderer for entities using OBJ models. Handles all loading and rendering boilerplate for
 * OBJ models, requiring only model and texture paths to be specified.
 *
 * <p><b>Renderer Implementation:</b></p>
 * <pre>{@code
 * public class SubmarineRenderer extends BaseOBJEntityRenderer<BaseSubmarine> {
 *     public SubmarineRenderer(EntityRendererFactory.Context ctx) {
 *         super(ctx,
 *             Identifier.of("modid", "submarine.obj"),
 *             Identifier.of("modid", "textures/entity/submarine.png")
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p><b>Automatic Handling:</b></p>
 * <ul>
 *   <li>Loading the OBJ model from resources</li>
 *   <li>Applying the texture</li>
 *   <li>Rotation to match entity yaw</li>
 *   <li>Lighting and overlay effects</li>
 * </ul>
 *
 * <p><b>File Locations:</b></p>
 * <ul>
 *   <li>OBJ model: {@code resources/assets/<namespace>/<model_path>.obj}</li>
 *   <li>Texture: {@code resources/assets/<namespace>/<texture_path>.png}</li>
 * </ul>
 *
 * <p><b>Important:</b> OBJ models are static and do not support animation. See {@link OBJEntityModel}
 * for additional information.</p>
 *
 * <p><b>Technical Implementation:</b></p>
 * <p>Two techniques fix OBJ rendering artifacts in Minecraft:</p>
 * <ol>
 *   <li><b>Degenerate Quad Rendering:</b> Triangles are rendered as quads with the last vertex duplicated
 *       (v0, v1, v2, v2). This fixes rendering artifacts that occur with standard triangle rendering.
 *       See {@link OBJEntityModel} for full details.</li>
 *   <li><b>No Backface Culling:</b> Uses {@code EntityCutoutNoCull} render layer to prevent holes
 *       caused by inconsistent face winding in OBJ models. See {@link #getRenderLayer()}.</li>
 * </ol>
 * <p>Both techniques are required for artifact-free rendering.</p>
 *
 * @param <T> The entity type this renderer handles
 */
public abstract class BaseOBJEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    private final Identifier textureId;
    private final OBJEntityModel model;

    /**
     * Creates a new OBJ entity renderer with the specified model and texture.
     *
     * @param context the renderer factory context
     * @param modelId the identifier for the OBJ model file
     * @param textureId the identifier for the texture file
     */
    public BaseOBJEntityRenderer(EntityRendererFactory.Context context, Identifier modelId, Identifier textureId) {
        super(context);
        this.textureId = textureId;
        this.model = new OBJEntityModel(modelId);
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {

        matrices.push();
        applyTransformations(entity, yaw, tickDelta, matrices);

        this.model.render(
                matrices,
                vertexConsumers.getBuffer(getRenderLayer()),
                light,
                getOverlay(entity),
                getColor(entity)
        );

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    /**
     * Applies transformations to the model before rendering. Default implementation applies yaw rotation
     * to orient the model correctly.
     *
     * @param entity the entity being rendered
     * @param yaw the entity's yaw rotation
     * @param tickDelta the tick delta for smooth interpolation
     * @param matrices the matrix stack for applying transformations
     */
    protected void applyTransformations(T entity, float yaw, float tickDelta, MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw + 180));
    }

    /**
     * Returns the render layer to use for this entity. Default implementation uses {@link RenderLayer#getEntityCutoutNoCull}
     * to prevent rendering artifacts in OBJ models.
     *
     * @return the render layer
     */
    protected RenderLayer getRenderLayer() {
        return RenderLayer.getEntityCutoutNoCull(textureId);
    }

    /**
     * Returns the overlay texture coordinates for this entity. Default implementation returns the default overlay.
     *
     * @param entity the entity being rendered
     * @return the overlay UV coordinates
     */
    protected int getOverlay(T entity) {
        return OverlayTexture.DEFAULT_UV;
    }

    /**
     * Returns the color tint to apply to this entity. Default implementation returns white (0xFFFFFFFF),
     * meaning no tinting is applied.
     *
     * <p><b>Color Format:</b> 0xAARRGGBB (alpha, red, green, blue)</p>
     *
     * @param entity the entity being rendered
     * @return the color as an ARGB integer
     */
    protected int getColor(T entity) {
        return 0xFFFFFFFF;
    }

    @Override
    public Identifier getTexture(T entity) {
        return textureId;
    }
}
