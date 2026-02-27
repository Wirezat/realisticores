package wirezat.realisticores.config;

import wirezat.realisticores.worldgen.kimberlite.KimberliteConfig;

/**
 * Root config object. Corresponds to the top-level keys in config.json.
 * Loaded once by RealisticOresConfigLoader and cached.
 */
public class RealisticOresConfig {
    public KimberliteConfig kimberlite = new KimberliteConfig();
}