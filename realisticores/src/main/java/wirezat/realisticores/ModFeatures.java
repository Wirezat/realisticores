    package wirezat.realisticores;

    import net.minecraft.util.Identifier;
    import net.minecraft.registry.Registries;
    import net.minecraft.registry.Registry;
    import net.minecraft.world.gen.feature.Feature;
    import wirezat.realisticores.feature.KimberliteMagmaFeature;
    import wirezat.realisticores.feature.config.KimberliteMagmaFeatureConfig;

    public class ModFeatures {

        // Die Variable wird nur deklariert
        public static Feature<KimberliteMagmaFeatureConfig> KIMBERLITE_MAGMA;

        // Die Registrierung findet in dieser Methode statt
        public static void registerModFeatures() {
            KIMBERLITE_MAGMA = Registry.register(
                    Registries.FEATURE,
                    new Identifier(RealisticOres.MOD_ID, "kimberlite_magma"),
                    new KimberliteMagmaFeature(KimberliteMagmaFeatureConfig.CODEC)
            );
        }
    }