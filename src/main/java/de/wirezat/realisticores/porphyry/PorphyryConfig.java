package de.wirezat.realisticores.porphyry;

import static de.wirezat.realisticores.config.ConfigValues.decimal;
import static de.wirezat.realisticores.config.ConfigValues.integer;

import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.config.MineralConfig;
import de.wirezat.realisticores.worldgen.IntRange;
import java.util.Map;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Feature config of the porphyry copper deposit: how often and where the groups of deposits form. */
public final class PorphyryConfig {
    public static final ModConfigSpec SPEC;
    public static final MineralConfig MINERALS;

    private static final Supplier<PorphyryParams> PARAMS;

    static {
        PorphyryParams d = PorphyryParams.defaults();
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Groups of deposits, like a stretch of a porphyry belt.").push("cluster");
        Supplier<Integer> cellSize = integer(b, "cellSize", d.cellSize(), 256, 100000,
                "Grid cell size in blocks; each cell holds at most one group.");
        Supplier<Double> chance = decimal(b, "chance", d.chance(), 0.0, 1.0,
                "Probability that a cell holds a group.");
        Supplier<Double> jitter = decimal(b, "jitter", d.jitter(), 0.0, 0.5,
                "Largest offset of a group from its cell centre, as a fraction of the cell size.");
        Supplier<Integer> minDeposits = integer(b, "minDeposits", d.deposits().min(), 1, 20,
                "Fewest deposits tried per group.");
        Supplier<Integer> maxDeposits = integer(b, "maxDeposits", d.deposits().max(), 1, 20,
                "Most deposits tried per group.");
        Supplier<Integer> minSpacing = integer(b, "minSpacing", d.spacing().min(), 50, 5000,
                "Smallest distance between neighbouring deposits of a group, in blocks.");
        Supplier<Integer> maxSpacing = integer(b, "maxSpacing", d.spacing().max(), 50, 5000,
                "Largest distance between neighbouring deposits of a group, in blocks.");
        Supplier<Integer> minRadius = integer(b, "minRadius", d.radius().min(), 10, 300,
                "Smallest radius of the ore shell in blocks; the whole deposit reaches about twice as far.");
        Supplier<Integer> maxRadius = integer(b, "maxRadius", d.radius().max(), 10, 300,
                "Largest radius of the ore shell in blocks.");
        Supplier<Integer> minHeight = integer(b, "minHeight", d.minHeight(), -64, 320,
                "Lowest terrain height at a deposit's centre; deposits form in highlands and mountains.");
        Supplier<Integer> placementAttempts = integer(b, "placementAttempts", d.placementAttempts(), 1, 100,
                "Tries to find high enough ground for each deposit before it is skipped.");
        b.pop();
        MINERALS = MineralConfig.define(b, OreConfig.registry().deposits().get(PorphyryCopper.ID), mineral -> true,
                Map.of());
        PARAMS = () -> new PorphyryParams(cellSize.get(), chance.get(), jitter.get(),
                new IntRange(minDeposits.get(), maxDeposits.get()).ordered(),
                new IntRange(minSpacing.get(), maxSpacing.get()).ordered(),
                new IntRange(minRadius.get(), maxRadius.get()).ordered(), minHeight.get(), placementAttempts.get());
        SPEC = b.build();
    }

    private PorphyryConfig() {
    }

    /**
     * Reads the current placement parameters from the config.
     *
     * @return parameters with min/max pairs ordered
     */
    public static PorphyryParams params() {
        return PARAMS.get();
    }
}
