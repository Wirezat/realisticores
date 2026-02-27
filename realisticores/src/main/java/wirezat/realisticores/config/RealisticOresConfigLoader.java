package wirezat.realisticores.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RealisticOresConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("realisticores");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CONFIG_PATH = "realisticores/ores/config.json";
    private static final String RESOURCE_PATH = "data/realisticores/ores/config.json";

    private static RealisticOresConfig cached;

    private RealisticOresConfigLoader() {}

    public static RealisticOresConfig get() {
        if (cached == null) cached = load();
        return cached;
    }

    public static void invalidate() {
        cached = null;
    }

    private static RealisticOresConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_PATH);
        LOGGER.info("Loading config from {}", path.toAbsolutePath());

        try {
            Files.createDirectories(path.getParent());

            if (Files.notExists(path)) {
                copyDefault(path);
            }

            try (InputStreamReader reader =
                         new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {

                RealisticOresConfig config =
                        GSON.fromJson(reader, RealisticOresConfig.class);

                if (config == null) throw new IOException("Config JSON malformed");

                LOGGER.info("Loaded config: {} seam(s), regionThreshold={}",
                        config.coal.seams.size(),
                        config.coal.geology.regionThreshold);

                return config;
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load config, using defaults", e);
            return new RealisticOresConfig();
        }
    }

    private static void copyDefault(Path target) throws IOException {
        try (InputStream in =
                     RealisticOresConfigLoader.class.getClassLoader()
                             .getResourceAsStream(RESOURCE_PATH)) {

            if (in == null) {
                throw new IOException("Default config not found in JAR: " + RESOURCE_PATH);
            }

            Files.copy(in, target);
            LOGGER.info("Default config copied to {}", target);
        }
    }
}