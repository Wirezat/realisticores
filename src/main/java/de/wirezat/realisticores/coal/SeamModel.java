package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.ValueNoise;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Computes the rock layers of coal basins column by column. */
public final class SeamModel {
    private static final int WAVE_SALT = 10;
    private static final int FAULT_SALT = 20;
    private static final int THICKNESS_SALT = 100;
    private static final int SPLIT_SALT = 200;
    private static final int HULL_SALT = 300;
    private static final int HULL_NOISES = 8;
    private static final int SAND_SALT = 500;
    private static final int BANK_SALT = 600;
    private static final long OUTCROP_SALT = 0x434F414C4F555443L;

    private final CoalParams params;

    /**
     * Creates a model.
     *
     * @param params generation settings
     */
    public SeamModel(CoalParams params) {
        this.params = params;
    }

    /**
     * Gives the layers of the basin a column lies in.
     *
     * @param basins basins that may reach the column
     * @param x world X of the column
     * @param z world Z of the column
     * @return the layers of the nearest basin, empty outside all basins or where no seam exists
     */
    public Optional<SeamColumn> column(List<ResolvedBasin> basins, int x, int z) {
        ResolvedBasin nearest = null;
        double edge = 1.0;
        for (ResolvedBasin basin : basins) {
            double distance = basin.basin().edgeDistance(x, z, params.basin());
            if (distance < edge) {
                edge = distance;
                nearest = basin;
            }
        }
        if (nearest == null) {
            return Optional.empty();
        }
        List<Layer> claims = new ArrayList<>();
        double floor = floorY(nearest, x, z);
        List<BasinLayout.Seam> seams = nearest.layout().seams();
        for (int i = 0; i < seams.size(); i++) {
            claimSeam(claims, nearest, i, (int) Math.round(floor + seams.get(i).offset()), edge, x, z);
        }
        return claims.isEmpty() ? Optional.empty()
                : Optional.of(new SeamColumn(rank(nearest, edge), withoutOverlap(claims), edge, nearest.basin().seed(),
                        Math.sqrt(nearest.basin().longRadius() * nearest.basin().shortRadius())));
    }

    /**
     * Gives the height of the basin floor, which all seams of the basin follow.
     *
     * @param resolved the basin
     * @param x world X
     * @param z world Z
     * @return Y of the floor
     */
    public double floorY(ResolvedBasin resolved, double x, double z) {
        CoalParams.SeamParams s = params.seam();
        Basin basin = resolved.basin();
        BasinLayout layout = resolved.layout();
        double d = Math.min(1.0, basin.distance(x, z));
        double bowl = 1.0 - d * d;
        double y = resolved.datum() - layout.depth() * bowl
                + s.waveAmplitude() * noise(basin, WAVE_SALT, x, z, s.waveWavelength());
        if (layout.foldAmplitude() > 0.0) {
            y += layout.foldAmplitude() * bowl
                    * Math.sin(2.0 * Math.PI * basin.across(x, z) / layout.foldWavelength() + layout.foldPhase());
        }
        for (int i = 0; i < layout.faults().size(); i++) {
            BasinLayout.Fault fault = layout.faults().get(i);
            double side = (x - basin.x()) * fault.normalX() + (z - basin.z()) * fault.normalZ() - fault.distance()
                    + s.faultWaveAmplitude() * noise(basin, FAULT_SALT + i, x, z, s.faultWaveWavelength());
            if (side > 0.0) {
                y += fault.offset();
            }
        }
        return y;
    }

    /**
     * Checks whether a seam shows through the soil in a column.
     *
     * @param x world X of the column
     * @param z world Z of the column
     * @return true if the column is an outcrop
     */
    public boolean isOutcrop(int x, int z) {
        double wavelength = params.seam().outcropWavelength();
        return ValueNoise.sample2(OUTCROP_SALT, x, z, wavelength) > params.seam().outcropThreshold();
    }

    private Rank rank(ResolvedBasin basin, double edge) {
        return switch (basin.type()) {
            case LIGNITE -> Rank.LIGNITE;
            case HARD_COAL -> Rank.HARD_COAL;
            case FOLDED -> edge < params.fold().coreShare() ? Rank.ANTHRACITE : Rank.HARD_COAL;
        };
    }

    private void claimSeam(List<Layer> claims, ResolvedBasin resolved, int index, int bottom, double edge, int x,
            int z) {
        CoalParams.SeamParams s = params.seam();
        Basin basin = resolved.basin();
        double raw = resolved.layout().seams().get(index).thickness()
                * (s.thicknessBase() + s.thicknessNoise() * noise(basin, THICKNESS_SALT + index, x, z,
                        s.thicknessWavelength()))
                * Math.min(1.0, (1.0 - edge) / s.pinchOut());
        if (raw < s.minThickness()) {
            return;
        }
        boolean lignite = resolved.type() == BasinType.LIGNITE;
        int thickness = Math.max(1, (int) Math.round(raw));
        int top = bottom + thickness - 1;
        double split = noise(basin, SPLIT_SALT + index, x, z, s.splitWavelength());
        if (thickness >= 2 && split > s.splitThreshold()) {
            int parting = Math.min(s.maxParting(), 1 + (int) Math.floor((split - s.splitThreshold())
                    / (1.0 - s.splitThreshold()) * s.maxParting()));
            int lower = bottom + thickness / 2 - 1;
            top += parting;
            claims.add(new Layer(bottom, lower, Rock.SEAM));
            claims.add(new Layer(lower + 1, lower + parting, lignite ? Rock.MUDSTONE : Rock.SHALE));
            claims.add(new Layer(lower + parting + 1, top, Rock.SEAM));
        } else {
            claims.add(new Layer(bottom, top, Rock.SEAM));
        }
        CoalParams.RockParams r = params.rock();
        int salt = HULL_SALT + index * HULL_NOISES;
        if (lignite) {
            int roof = hull(basin, salt, x, z, r.minRoofMudstone(), r.maxRoofMudstone());
            int floor = hull(basin, salt + 1, x, z, r.minFloorMudstone(), r.maxFloorMudstone());
            claim(claims, top + 1, roof, Rock.MUDSTONE);
            claim(claims, bottom - floor, floor, Rock.MUDSTONE);
            claimSandstone(claims, basin, index, top + roof + 1, true, x, z);
            return;
        }
        int black = hull(basin, salt, x, z, r.minBlackShale(), r.maxBlackShale());
        int roof = hull(basin, salt + 1, x, z, r.minShale(), r.maxShale());
        int fireclay = hull(basin, salt + 2, x, z, r.minFireclay(), r.maxFireclay());
        int floor = hull(basin, salt + 3, x, z, r.minShale(), r.maxShale());
        boolean topmost = index == resolved.layout().seams().size() - 1;
        int bank = topmost && noise(basin, BANK_SALT, x, z, params.accessories().sandWavelength())
                > 1.0 - 2.0 * params.accessories().blackbandCover() ? 1 : 0;
        claim(claims, top + 1, bank, Rock.BLACKBAND);
        claim(claims, top + 1 + bank, black, Rock.BLACK_SHALE);
        claim(claims, top + 1 + bank + black, roof, Rock.SHALE);
        claim(claims, bottom - fireclay, fireclay, Rock.FIRECLAY);
        claim(claims, bottom - fireclay - floor, floor, Rock.SHALE);
        claimSandstone(claims, basin, index, top + 1 + bank + black + roof, false, x, z);
    }

    private void claimSandstone(List<Layer> claims, Basin basin, int index, int bottom, boolean lignite, int x,
            int z) {
        CoalParams.AccessoryParams a = params.accessories();
        if (noise(basin, SAND_SALT + index, x, z, a.sandWavelength()) <= 1.0 - 2.0 * a.sandCover()) {
            return;
        }
        int thickness = hull(basin, SAND_SALT + HULL_NOISES + index, x, z, lignite ? a.ligniteSandMin() : a.sandMin(),
                lignite ? a.ligniteSandMax() : a.sandMax());
        claim(claims, bottom, thickness, Rock.SANDSTONE);
    }

    private static void claim(List<Layer> claims, int minY, int height, Rock rock) {
        if (height > 0) {
            claims.add(new Layer(minY, minY + height - 1, rock));
        }
    }

    private int hull(Basin basin, int salt, int x, int z, int min, int max) {
        double share = (noise(basin, salt, x, z, params.rock().wavelength()) + 1.0) / 2.0;
        return min + Math.min(max - min, (int) (share * (max - min + 1)));
    }

    private static List<Layer> withoutOverlap(List<Layer> claims) {
        int minY = claims.stream().mapToInt(Layer::minY).min().orElseThrow();
        int maxY = claims.stream().mapToInt(Layer::maxY).max().orElseThrow();
        Rock[] rocks = new Rock[maxY - minY + 1];
        for (Layer claim : claims) {
            for (int y = claim.minY(); y <= claim.maxY(); y++) {
                Rock old = rocks[y - minY];
                if (old == null || claim.rock().ordinal() < old.ordinal()) {
                    rocks[y - minY] = claim.rock();
                }
            }
        }
        List<Layer> layers = new ArrayList<>();
        int start = 0;
        for (int i = 1; i <= rocks.length; i++) {
            if (i == rocks.length || rocks[i] != rocks[start]) {
                if (rocks[start] != null) {
                    layers.add(new Layer(minY + start, minY + i - 1, rocks[start]));
                }
                start = i;
            }
        }
        return layers;
    }

    private static double noise(Basin basin, int salt, double x, double z, double wavelength) {
        return ValueNoise.sample2(SplitMix64.hash(basin.seed(), salt, 0), x, z, wavelength);
    }
}
