package net.rizen.submarines.api.client.model;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses OBJ model files and converts them into renderable data.
 *
 * <p><b>Supported:</b> Vertices (v), texture coordinates (vt), normals (vn), triangular faces (f)
 *
 * <p><b>Unsupported:</b> Materials (.mtl), groups, smooth shading groups, quads/n-gons
 *
 * <p><b>Requirements:</b> Models must be fully triangulated (all faces must have exactly 3 vertices).
 * Quads and n-gons are silently ignored. See {@link OBJEntityModel} for rendering details.
 */
public class OBJLoader {

    public static class OBJModel {
        public List<Vector3f> vertices = new ArrayList<>();
        public List<Vector2f> texCoords = new ArrayList<>();
        public List<Vector3f> normals = new ArrayList<>();
        public List<Triangle> triangles = new ArrayList<>();

        public static class Triangle {
            public int[] vertexIndices = new int[3];
            public int[] texCoordIndices = new int[3];
            public int[] normalIndices = new int[3];
        }
    }

    public static OBJModel loadModel(InputStream inputStream) throws IOException {
        OBJModel model = new OBJModel();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                try {
                    switch (parts[0]) {
                    case "v":
                        model.vertices.add(new Vector3f(
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2]),
                                Float.parseFloat(parts[3])
                        ));
                        break;

                    case "vt":
                        float u = Float.parseFloat(parts[1]);
                        float v = Float.parseFloat(parts[2]);
                        model.texCoords.add(new Vector2f(u, 1.0f - v));
                        break;

                    case "vn":
                        model.normals.add(new Vector3f(
                                Float.parseFloat(parts[1]),
                                Float.parseFloat(parts[2]),
                                Float.parseFloat(parts[3])
                        ));
                        break;

                    case "f":
                        if (parts.length < 4) break;

                        OBJModel.Triangle tri = new OBJModel.Triangle();

                        for (int i = 0; i < 3; i++) {
                            String[] indices = parts[i + 1].split("/");

                            tri.vertexIndices[i] = Integer.parseInt(indices[0]) - 1;

                            if (indices.length > 1 && !indices[1].isEmpty()) {
                                tri.texCoordIndices[i] = Integer.parseInt(indices[1]) - 1;
                            } else {
                                tri.texCoordIndices[i] = -1;
                            }

                            if (indices.length > 2 && !indices[2].isEmpty()) {
                                tri.normalIndices[i] = Integer.parseInt(indices[2]) - 1;
                            } else {
                                tri.normalIndices[i] = -1;
                            }
                        }
                        model.triangles.add(tri);
                        break;
                    }
                } catch (NumberFormatException e) {
                    throw new IOException(String.format(
                        "Malformed OBJ file: Invalid number format at line %d: '%s'",
                        lineNumber, line
                    ), e);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IOException(String.format(
                        "Malformed OBJ file: Missing required data at line %d: '%s'",
                        lineNumber, line
                    ), e);
                }
            }
        }
        return model;
    }
}