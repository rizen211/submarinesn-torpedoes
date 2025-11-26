package net.rizen.submarines.api.client.model;

import net.minecraft.util.Identifier;
import net.rizen.submarines.Mod;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches loaded OBJ models so they do not get parsed multiple times.
 * Models are loaded once and stored in memory for reuse.
 */
public class OBJModelManager {
    private static final Logger LOGGER = Mod.LOGGER;
    private static final Map<Identifier, OBJLoader.OBJModel> CACHE = new HashMap<>();

    public static OBJLoader.OBJModel getOrLoadModel(Identifier id) {
        if (CACHE.containsKey(id)) {
            return CACHE.get(id);
        }

        try {
            String path = "/assets/" + id.getNamespace() + "/models/" + id.getPath();
            InputStream stream = OBJModelManager.class.getResourceAsStream(path);

            if (stream == null) {
                LOGGER.error("Could not find model at path: {}", path);
                return null;
            }

            OBJLoader.OBJModel model = OBJLoader.loadModel(stream);
            CACHE.put(id, model);

            return model;

        } catch (IOException e) {
            LOGGER.error("Failed to load model {}: {}", id, e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while loading model {}: {}", id, e);
            return null;
        }
    }
}