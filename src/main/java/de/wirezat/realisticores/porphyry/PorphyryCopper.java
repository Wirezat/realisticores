package de.wirezat.realisticores.porphyry;

import de.wirezat.realisticores.config.MineralConfig;
import de.wirezat.realisticores.ore.DepositType;
import de.wirezat.realisticores.ore.LocateTarget;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.ModConfigSpec;

/** The porphyry copper deposit: alteration shells around a stock with a weathered, enriched top. */
public final class PorphyryCopper implements DepositType {
    public static final String ID = "porphyry_copper";
    public static final PorphyryCopper INSTANCE = new PorphyryCopper();

    private static final Set<String> ZONES = Arrays.stream(Zone.values()).filter(zone -> zone != Zone.HOST)
            .map(Zone::name).collect(Collectors.toUnmodifiableSet());

    private PorphyryCopper() {
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ModConfigSpec spec() {
        return PorphyryConfig.SPEC;
    }

    @Override
    public MineralConfig minerals() {
        return PorphyryConfig.MINERALS;
    }

    @Override
    public Set<String> zones() {
        return ZONES;
    }

    @Override
    public void invalidate() {
        Porphyry.invalidate();
    }

    @Override
    public void carve(ChunkAccess chunk, long seed, TerrainProbe probe, int seaLevel) {
        PorphyryCarver.carve(chunk, seed, probe);
    }

    @Override
    public List<LocateTarget> locateTargets() {
        return List.of(new LocateTarget("porphyry", (seed, probe, origin, limit) -> Porphyry.generator()
                .nearest(seed, probe, origin.getX(), origin.getZ(), limit)
                .map(body -> new BlockPos(body.x(), 0, body.z()))));
    }
}
