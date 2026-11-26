package de.wirezat.realisticores.kimberlite;

import de.wirezat.realisticores.config.MineralConfig;
import de.wirezat.realisticores.indicator.IndicatorSource;
import de.wirezat.realisticores.ore.DepositType;
import de.wirezat.realisticores.ore.LocateTarget;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.ModConfigSpec;

/** The kimberlite pipe: fields of volcanic pipes on cratons, with craters and indicator trails. */
public final class KimberlitePipe implements DepositType {
    public static final String ID = "kimberlite_pipe";
    public static final KimberlitePipe INSTANCE = new KimberlitePipe();

    private static final Set<String> ZONES = Arrays.stream(Facies.values()).filter(rock -> rock != Facies.HOST)
            .map(Facies::name).collect(Collectors.toUnmodifiableSet());

    private KimberlitePipe() {
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ModConfigSpec spec() {
        return KimberliteConfig.SPEC;
    }

    @Override
    public MineralConfig minerals() {
        return KimberliteConfig.MINERALS;
    }

    @Override
    public Set<String> zones() {
        return ZONES;
    }

    @Override
    public void invalidate() {
        Kimberlite.invalidate();
    }

    @Override
    public void carve(ChunkAccess chunk, long seed, TerrainProbe probe, int seaLevel) {
        KimberliteCarver.carve(chunk, seed, probe, seaLevel);
    }

    @Override
    public List<LocateTarget> locateTargets() {
        return List.of(new LocateTarget(ID, (seed, probe, origin, limit) -> Kimberlite.generator()
                .nearestPipe(seed, origin.getX(), origin.getZ(), limit)
                .map(pipe -> new BlockPos(pipe.x(), 0, pipe.z()))));
    }

    @Override
    public Optional<IndicatorSource> indicators() {
        return Optional.of(KimberliteIndicators.INSTANCE);
    }
}
