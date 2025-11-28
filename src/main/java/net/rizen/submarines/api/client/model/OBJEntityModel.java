package net.rizen.submarines.api.client.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Renders OBJ models for entities using a degenerate quad technique.
 *
 * <p>Each triangle (3 vertices) is rendered as a quad (4 vertices) with the last vertex duplicated
 * (v0, v1, v2, v2). This technique prevents rendering artifacts in Minecraft's quad-based entity
 * rendering pipeline, which does not handle pure triangles correctly.
 *
 * <p>Models are loaded via {@link OBJLoader}, cached for performance, and rendered as a single mesh.
 * The entire model can be transformed using MatrixStack, but individual parts cannot be animated.
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