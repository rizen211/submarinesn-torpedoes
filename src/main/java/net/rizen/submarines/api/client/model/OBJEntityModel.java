package net.rizen.submarines.api.client.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Renders an OBJ model for an entity. This handles transforming the model vertices with matrices and
 * sending them to the GPU for rendering with proper lighting, overlay, and color tinting.
 *
 * <p><b>Important Notes:</b></p>
 * <ul>
 *   <li><b>Model Triangulation Required:</b> All OBJ models must be triangulated (all faces must be triangles,
 *       not quads or n-gons). Models in this project were created in Blockbench and triangulated using the
 *       MTools plugin by Malik12tree. While triangulation is required for the degenerate quad technique to work,
 *       triangulation alone does not fix rendering artifacts - it simply ensures the geometry is in a consistent
 *       triangle-based format for processing.</li>
 *   <li><b>No Animation Support</b> - OBJ models are static and cannot be animated. The model is
 *       rendered in the exact pose defined in the .obj file. The entire model can be rotated and
 *       positioned using MatrixStack transformations in the renderer, but individual parts cannot move.</li>
 *   <li>Models are loaded once and cached for performance.</li>
 *   <li>All geometry is rendered as a single mesh.</li>
 * </ul>
 *
 * <p><b>Technical Implementation - Degenerate Quad Rendering:</b></p>
 * <p>This renderer uses a "degenerate quad" technique to fix rendering artifacts. Each triangle (3 vertices)
 * is rendered as a quad (4 vertices) with the last vertex duplicated (v0, v1, v2, v2).</p>
 *
 * <p><b>Development History:</b> Initially, models were exported from Blockbench with quads (the default),
 * which caused severe rendering artifacts - holes in the model, weird internal triangles connecting to random
 * vertices, and missing faces. Triangulating the models was attempted as a fix, but this barely improved the
 * situation. The degenerate quad rendering technique was the actual solution - by rendering each triangle as
 * a degenerate quad (4 vertices with the last duplicated), Minecraft's quad-based entity rendering pipeline
 * processes them correctly without artifacts.</p>
 */
public class OBJEntityModel {
    private final Identifier modelId;
    private OBJLoader.OBJModel modelData;

    public OBJEntityModel(Identifier modelId) {
        this.modelId = modelId;
        this.modelData = OBJModelManager.getOrLoadModel(modelId);
    }

    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        if (modelData == null) {
            modelData = OBJModelManager.getOrLoadModel(modelId);
            if (modelData == null) return;
        }

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        for (OBJLoader.OBJModel.Triangle tri : modelData.triangles) {
            for (int i = 0; i < 3; i++) {
                renderVertex(tri, i, positionMatrix, normalMatrix, vertexConsumer, light, overlay, r, g, b, a);
            }
            renderVertex(tri, 2, positionMatrix, normalMatrix, vertexConsumer, light, overlay, r, g, b, a);
        }
    }

    private void renderVertex(OBJLoader.OBJModel.Triangle tri, int vertexCornerIndex,
                              Matrix4f positionMatrix, Matrix3f normalMatrix,
                              VertexConsumer vertexConsumer, int light, int overlay,
                              float r, float g, float b, float a) {

        int vIndex = tri.vertexIndices[vertexCornerIndex];
        int tIndex = tri.texCoordIndices[vertexCornerIndex];
        int nIndex = tri.normalIndices[vertexCornerIndex];

        if (vIndex >= modelData.vertices.size()) return;

        Vector3f pos = modelData.vertices.get(vIndex);

        float u = 0, v = 0;
        if (tIndex >= 0 && tIndex < modelData.texCoords.size()) {
            Vector2f tex = modelData.texCoords.get(tIndex);
            u = tex.x;
            v = tex.y;
        }

        float nx = 0, ny = 1, nz = 0;
        if (nIndex >= 0 && nIndex < modelData.normals.size()) {
            Vector3f norm = modelData.normals.get(nIndex);
            Vector3f rotNorm = new Vector3f(norm);
            rotNorm.mul(normalMatrix);
            if (rotNorm.lengthSquared() > 0.000001f) {
                rotNorm.normalize();
                nx = rotNorm.x;
                ny = rotNorm.y;
                nz = rotNorm.z;
            }
        }

        vertexConsumer.vertex(positionMatrix, pos.x, pos.y, pos.z)
                .color(r, g, b, a)
                .texture(u, v)
                .overlay(overlay)
                .light(light)
                .normal(nx, ny, nz);
    }
}