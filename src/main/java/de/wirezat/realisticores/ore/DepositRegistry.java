package de.wirezat.realisticores.ore;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All ores and deposits shipped with the mod.
 *
 * @param ores ore definitions by id
 * @param deposits deposits by id
 */
public record DepositRegistry(Map<String, OreDefinition> ores, Map<String, Deposit> deposits) {
    private static final String ROOT = "/data/realisticores/";

    /**
     * Loads the registry from the mod resources: the index {@code registry.json} names the ore files in
     * {@code ores/} (block, tag and experience range) and the deposit files in {@code deposits/}.
     *
     * @return the registry
     * @throws IllegalStateException if a resource is missing or malformed
     */
    public static DepositRegistry load() {
        JsonObject index = read(ROOT + "registry.json");
        Map<String, OreDefinition> ores = new LinkedHashMap<>();
        for (JsonElement id : index.getAsJsonArray("ores")) {
            JsonObject file = read(ROOT + "ores/" + id.getAsString() + ".json");
            JsonArray xp = file.getAsJsonArray("xp");
            ores.put(id.getAsString(), new OreDefinition(id.getAsString(), file.get("block").getAsString(),
                    file.get("tag").getAsString(), xp.get(0).getAsInt(), xp.get(1).getAsInt()));
        }
        Map<String, Deposit> deposits = new LinkedHashMap<>();
        for (JsonElement id : index.getAsJsonArray("deposits")) {
            JsonObject file = read(ROOT + "deposits/" + id.getAsString() + ".json");
            deposits.put(id.getAsString(), new Deposit(id.getAsString(), minerals(file.getAsJsonArray("minerals"))));
        }
        return new DepositRegistry(Map.copyOf(ores), Map.copyOf(deposits));
    }

    private static List<Mineral> minerals(JsonArray array) {
        List<Mineral> minerals = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject mineral = element.getAsJsonObject();
            List<String> zones = new ArrayList<>();
            mineral.getAsJsonArray("zones").forEach(zone -> zones.add(zone.getAsString()));
            Map<String, String> zoneBlocks = new LinkedHashMap<>();
            if (mineral.has("blocks")) {
                mineral.getAsJsonObject("blocks").entrySet()
                        .forEach(entry -> zoneBlocks.put(entry.getKey(), entry.getValue().getAsString()));
            }
            minerals.add(new Mineral(mineral.get("ore").getAsString(), List.copyOf(zones),
                    mineral.get("fraction").getAsDouble(),
                    mineral.has("graded") && mineral.get("graded").getAsBoolean(), Map.copyOf(zoneBlocks)));
        }
        return List.copyOf(minerals);
    }

    private static JsonObject read(String path) {
        try (InputStream stream = DepositRegistry.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource " + path);
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }
}
