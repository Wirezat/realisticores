package de.wirezat.realisticores.coal;

import de.wirezat.realisticores.config.MineralConfig;
import de.wirezat.realisticores.ore.DepositType;
import de.wirezat.realisticores.ore.LocateTarget;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.ModConfigSpec;

/** The coal basin: bowls of layered rock with hard coal, lignite or anthracite seams and their companions. */
public final class CoalBasin implements DepositType {
    public static final String ID = "coal_basin";
    public static final CoalBasin INSTANCE = new CoalBasin();

    private static final int SEARCH_LIMIT = 100000;
    private static final Set<String> ZONES = Stream.concat(Stream.concat(Stream.of(Rank.values()).map(Rank::name),
            Stream.of(Rock.values()).filter(rock -> rock != Rock.SEAM).map(Rock::name)),
            Stream.of(CoalCarver.SEAM_FIRE))
            .collect(Collectors.toUnmodifiableSet());

    private CoalBasin() {
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ModConfigSpec spec() {
        return CoalConfig.SPEC;
    }

    @Override
    public MineralConfig minerals() {
        return CoalConfig.MINERALS;
    }

    @Override
    public Set<String> zones() {
        return ZONES;
    }

    @Override
    public void invalidate() {
        Coal.invalidate();
    }

    @Override
    public void carve(ChunkAccess chunk, long seed, TerrainProbe probe, int seaLevel) {
        CoalCarver.carve(chunk, seed, probe);
    }

    @Override
    public List<LocateTarget> locateTargets() {
        return List.of(target("hard_coal", BasinType.HARD_COAL), target("lignite", BasinType.LIGNITE),
                target("anthracite", BasinType.FOLDED));
    }

    /**
     * Creates the locate target of one basin type.
     *
     * @param name subcommand name
     * @param type wanted basin type
     * @return the target
     */
    private static LocateTarget target(String name, BasinType type) {
        return new LocateTarget(name, (seed, probe, origin, limit) -> Coal.nearest(seed, probe, type,
                origin.getX(), origin.getZ(), Math.min(SEARCH_LIMIT, limit))
                .map(found -> new BlockPos(found.basin().x(), 0, found.basin().z())));
    }
}
