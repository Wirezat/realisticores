package de.wirezat.realisticores.ore;

import de.wirezat.realisticores.config.MineralConfig;
import de.wirezat.realisticores.indicator.IndicatorSource;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.ModConfigSpec;

/** One kind of deposit with its own generation; everything the mod needs to wire it in. */
public interface DepositType {
    /**
     * Gives the id shared by the deposit data file, the config folder and the ore data.
     *
     * @return deposit id such as {@code coal_basin}
     */
    String id();

    /**
     * Gives the feature config of the deposit.
     *
     * @return spec registered as {@code realisticores/<id>/<id>.toml}
     */
    ModConfigSpec spec();

    /**
     * Gives the {@code [minerals]} section of the feature config.
     *
     * @return shares and replacement blocks of the deposit's ores
     */
    MineralConfig minerals();

    /**
     * Lists the rock zones a mineral of this deposit may name in the deposit data file.
     *
     * @return zone names
     */
    Set<String> zones();

    /** Drops cached generator objects so the next use rebuilds them from the config. */
    void invalidate();

    /**
     * Writes every deposit of this kind that touches an overworld chunk; only called while the deposit is active.
     *
     * @param chunk the chunk after its surface is built
     * @param seed world seed
     * @param probe reader for the original terrain and climate
     * @param seaLevel sea level of the world
     */
    void carve(ChunkAccess chunk, long seed, TerrainProbe probe, int seaLevel);

    /**
     * Lists what {@code /locate realisticores} can find of this deposit.
     *
     * @return search targets, one subcommand each
     */
    List<LocateTarget> locateTargets();

    /**
     * Gives the indicator grains this deposit sheds for the pan.
     *
     * @return the source of its grains, empty for deposits without indicator minerals
     */
    default Optional<IndicatorSource> indicators() {
        return Optional.empty();
    }
}
