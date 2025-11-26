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
 * Parses OBJ model files and converts them into renderable data. This reads the text-based OBJ format
 * and extracts vertex positions, texture coordinates, normals, and face definitions.
 *
 * <p><b>Supported Features:</b></p>
 * <ul>
 *   <li>Vertices (v)</li>
 *   <li>Texture coordinates (vt)</li>
 *   <li>Normals (vn)</li>
 *   <li>Faces (f) - triangles only</li>
 * </ul>
 *
 * <p><b>Unsupported Features:</b></p>
 * <ul>
 *   <li><b>Animations</b> - OBJ is a static format; models are rendered in a fixed pose</li>
 *   <li>Materials (.mtl files) - Materials are ignored; texture parameter should be used instead</li>
 *   <li>Groups and objects - All geometry is merged into a single mesh</li>
 *   <li>Smooth shading groups - Normals are used as-is from the file</li>
 *   <li>Quads and n-gons - Only triangles are supported (faces with more than 3 vertices are ignored)</li>
 * </ul>
 *
 * <p><b>Important:</b> Models must be fully triangulated before export. This loader only reads triangle faces
 * (quads and n-gons are ignored). However, triangulation alone does not prevent rendering artifacts - models
 * exported as quads had severe artifacts, and triangulating them did not significantly improve rendering.
 * The degenerate quad technique in {@link OBJEntityModel} is the actual fix required for proper rendering
 * in Minecraft's entity system.</p>
 *
 * <p>For animated entities, Minecraft's built-in model format or custom vertex manipulation
 * in the renderer should be considered.</p>
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
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

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
            }
        }
        return model;
    }
}