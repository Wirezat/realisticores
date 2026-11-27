package de.wirezat.realisticores.indicator;

import static de.wirezat.realisticores.config.ConfigValues.camelCase;
import static de.wirezat.realisticores.config.ConfigValues.decimal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Config section {@code [plume]} of a deposit that sheds indicator grains. */
public final class PlumeConfig {
    private PlumeConfig() {
    }

    /**
     * Defines the trail settings and the spread of every indicator mineral.
     *
     * @param b config builder
     * @param d default settings
     * @param source name of the deposit kind in the comments, such as {@code pipe}
     * @return reader for the current settings
     */
    public static Supplier<PlumeParams> define(ModConfigSpec.Builder b, PlumeParams d, String source) {
        b.comment("Indicator mineral trails: grains washed downhill from a " + source + ", found by panning.")
                .push("plume");
        Supplier<Double> stepLength = decimal(b, "stepLength", d.stepLength(), 8.0, 128.0,
                "Distance in blocks between two points of the trail path.");
        Supplier<Double> maxLength = decimal(b, "maxLength", d.maxLength(), 100.0, 5000.0,
                "Longest trail in blocks.");
        Supplier<Double> inertia = decimal(b, "inertia", d.inertia(), 0.0, 0.95,
                "Share of the previous heading kept at each step; higher values give straighter trails.");
        Supplier<Double> flatSlope = decimal(b, "flatSlope", d.flatSlope(), 0.001, 1.0,
                "Terrain slope in blocks per block at which the trail follows the downhill direction fully.");
        Supplier<Double> startWidth = decimal(b, "startWidth", d.startWidth(), 1.0, 200.0,
                "Half width of the trail at the " + source + " in blocks.");
        Supplier<Double> widthGrowth = decimal(b, "widthGrowth", d.widthGrowth(), 0.0, 1.0,
                "Growth of the half width per block of trail length.");
        Supplier<Double> coldFactor = decimal(b, "coldFactor", d.coldFactor(), 0.0, 10.0,
                "Trail length factor for " + source + "s in cold biomes; glaciers carry grains far.");
        Supplier<Double> dryFactor = decimal(b, "dryFactor", d.dryFactor(), 0.0, 10.0,
                "Trail length factor for " + source + "s in dry biomes.");
        Supplier<Double> oceanFactor = decimal(b, "oceanFactor", d.oceanFactor(), 0.0, 10.0,
                "Trail length factor for " + source + "s in oceans; 0 leaves grains only at the " + source + ".");
        Supplier<Double> maxSeaDepth = decimal(b, "maxSeaDepth", d.maxSeaDepth(), 0.0, 200.0,
                "Depth in blocks below the sea surface at which a trail ends; trails run on over shallow sea floor.");
        List<Supplier<PlumeParams.IndicatorParams>> indicators = new ArrayList<>();
        for (PlumeParams.IndicatorParams indicator : d.indicators()) {
            String key = camelCase(indicator.id());
            Supplier<Double> length = decimal(b, key + "Length", indicator.length(), 10.0, 5000.0,
                    "Length in blocks over which " + indicator.id() + " falls to 1/e.");
            Supplier<Double> peak = decimal(b, key + "Peak", indicator.peak(), 0.0, 1.0,
                    "Measured share of " + indicator.id() + " next to the " + source + "; 1 is 100 g per wash.");
            indicators.add(() -> new PlumeParams.IndicatorParams(indicator.id(), length.get(), peak.get()));
        }
        b.pop();
        return () -> new PlumeParams(stepLength.get(), maxLength.get(), inertia.get(), flatSlope.get(),
                startWidth.get(), widthGrowth.get(), coldFactor.get(), dryFactor.get(), oceanFactor.get(),
                maxSeaDepth.get(), indicators.stream().map(Supplier::get).toList());
    }
}
