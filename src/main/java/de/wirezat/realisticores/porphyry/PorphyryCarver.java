package de.wirezat.realisticores.porphyry;

import de.wirezat.realisticores.ModBlocks;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.ore.ActiveMineral;
import de.wirezat.realisticores.ore.BlockIds;
import de.wirezat.realisticores.ore.MineralRoll;
import de.wirezat.realisticores.worldgen.ChunkColumns;
import de.wirezat.realisticores.worldgen.SplitMix64;
import de.wirezat.realisticores.worldgen.TerrainProbe;
import de.wirezat.realisticores.worldgen.ValueNoise;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

/** Writes porphyry copper deposits into a chunk: alteration rock, weathering zones and ore nests. */
public final class PorphyryCarver {
    private static final Map<String, double[]> NESTS = Map.of(
            "malachite", new double[] {5.0, 2.5}, "azurite", new double[] {4.0, 2.5},
            "chalcocite", new double[] {7.0, 2.0}, "chalcopyrite", new double[] {2.5, 2.5},
            "pyrite", new double[] {1.8, 1.8}, "molybdenite", new double[] {1.2, 4.0});
    private static final double[] QUANTILES = quantiles();
    private static final double OXIDE_RIM = 1.15;
    private static final double CEMENT_RIM = 1.25;
    private static final double GOLD_RIM = 1.3;
    private static final double RIM_SHARE = 0.4;
    private static final double CEMENT_RIM_SHARE = 0.5;
    private static final double LEACHED_RELICS = 4.0 / 7.5;
    private static final double PHYLLIC_COPPER = 2.0 / 7.5;
    private static final double POTASSIC_PEAK = 0.8;
    private static final double POTASSIC_FALL = 0.35;
    private static final double POTASSIC_FLOOR = 0.25;
    private static final double MOLY_SHELL = 0.75;
    private static final double MOLY_DEPTH = 0.4;

    private PorphyryCarver() {
    }

    private record OreBlock(ActiveMineral mineral, BlockState block) {
    }

    /**
     * Writes every deposit that touches the chunk; only base overworld stone below the surface is replaced.
     *
     * @param chunk the chunk after its surface is built
     * @param seed world seed
     * @param probe reader for the original terrain
     */
    public static void carve(ChunkAccess chunk, long seed, TerrainProbe probe) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        List<Body> bodies = Porphyry.generator().bodiesNear(seed, probe, minX, minZ, minX + 15, minZ + 15);
        if (bodies.isEmpty()) {
            return;
        }
        Map<Zone, List<OreBlock>> ores = oreBlocks(OreConfig.resolved().minerals(PorphyryCopper.ID));
        PorphyryShape shape = Porphyry.shape();
        int bottomY = Math.max(chunk.getMinBuildHeight(), PorphyryShape.bottomLimit());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Body body : bodies) {
            double reach = PorphyryShape.reach(body);
            double centreY = shape.centreY(body);
            ChunkColumns.forEach(chunkPos, (localX, localZ, x, z) -> {
                double dx = x - body.x();
                double dz = z - body.z();
                if (dx * dx + dz * dz > reach * reach) {
                    return false;
                }
                int surfaceY = Math.min(chunk.getMaxBuildHeight() - 1,
                        chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ));
                double smoothY = Porphyry.smoothHeight(seed, probe, x, z);
                boolean changed = false;
                for (int y = bottomY; y <= surfaceY; y++) {
                    if (!chunk.getBlockState(pos.set(x, y, z)).is(BlockTags.BASE_STONE_OVERWORLD)) {
                        continue;
                    }
                    double q = shape.shell(seed, body, x, y, z);
                    if (q > PorphyryShape.OUTER) {
                        continue;
                    }
                    Zone zone = shape.zone(seed, body, q, x, y, z, surfaceY, smoothY);
                    if (zone != Zone.HOST) {
                        BlockState block = ore(seed, ores.get(zone), zone, q, y > centreY + MOLY_DEPTH * body.radius(),
                                x, y, z);
                        chunk.setBlockState(pos, block != null ? block : rock(zone), false);
                        changed = true;
                    }
                }
                return changed;
            });
        }
    }

    /**
     * Resolves the block of every active mineral in each of its zones once per chunk.
     *
     * @param minerals active minerals of the deposit
     * @return ore blocks by zone, in the order of the data file; minerals whose block is missing are left out
     */
    private static Map<Zone, List<OreBlock>> oreBlocks(List<ActiveMineral> minerals) {
        Map<Zone, List<OreBlock>> byZone = new EnumMap<>(Zone.class);
        for (Zone zone : Zone.values()) {
            List<OreBlock> list = new ArrayList<>();
            for (ActiveMineral mineral : minerals) {
                if (mineral.zones().contains(zone.name())) {
                    BlockIds.state(mineral.blockFor(zone.name())).ifPresent(state ->
                            list.add(new OreBlock(mineral, state)));
                }
            }
            byZone.put(zone, list);
        }
        return byZone;
    }

    /**
     * Picks the first ore whose nest covers the block.
     *
     * @param seed world seed
     * @param ores ore blocks of the zone
     * @param zone zone of the block
     * @param q shell value of the block
     * @param highInCore true if the block lies too high in the dome for molybdenite
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return the ore block, null to keep the zone rock
     */
    private static BlockState ore(long seed, List<OreBlock> ores, Zone zone, double q, boolean highInCore, int x,
            int y, int z) {
        for (OreBlock ore : ores) {
            ActiveMineral mineral = ore.mineral();
            double share = mineral.fraction()
                    * (mineral.graded() ? richness(mineral.ore(), zone, q, highInCore) : 1.0);
            if (share > 0 && inNest(seed, mineral.ore(), share, x, y, z)) {
                return ore.block();
            }
        }
        return null;
    }

    /**
     * Scales an ore's share by where the block lies in the deposit.
     *
     * @param ore id of the ore
     * @param zone zone of the block
     * @param q shell value of the block
     * @param highInCore true if the block lies too high in the dome for molybdenite
     * @return factor on the shipped share
     */
    private static double richness(String ore, Zone zone, double q, boolean highInCore) {
        double copperRim = q < PorphyryShape.CORE_EDGE ? 0.0 : q < OXIDE_RIM ? 1.0 : RIM_SHARE;
        return switch (zone) {
            case OXIDE -> copperRim;
            case OXIDE_BASE -> ore.equals("chalcopyrite") ? copperRim * LEACHED_RELICS : copperRim;
            case CEMENTATION -> q < PorphyryShape.CORE_EDGE ? 0.0 : q < CEMENT_RIM ? 1.0 : CEMENT_RIM_SHARE;
            case GOSSAN -> q < GOLD_RIM ? 1.0 : 0.0;
            case PHYLLIC -> ore.equals("chalcopyrite") ? PHYLLIC_COPPER : 1.0;
            case POTASSIC -> ore.equals("molybdenite") ? (q < MOLY_SHELL && !highInCore ? 1.0 : 0.0)
                    : Math.max(POTASSIC_FLOOR, 1.0 - Math.abs(q - POTASSIC_PEAK) / POTASSIC_FALL);
            default -> 1.0;
        };
    }

    /**
     * Tells whether a block lies in a nest of an ore, so ore comes in small clumps rather than single blocks.
     *
     * @param seed world seed
     * @param ore id of the ore
     * @param share wanted share of blocks
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return true if the block becomes the ore
     */
    private static boolean inNest(long seed, String ore, double share, int x, int y, int z) {
        double[] size = NESTS.get(ore);
        if (size == null) {
            return MineralRoll.roll(seed, ore, x, y, z) < share;
        }
        double noise = ValueNoise.sample3(seed ^ SplitMix64.mix(ore.hashCode()), x / size[0], y / size[1],
                z / size[0]);
        int index = (int) ((1.0 - Math.min(1.0, share)) * (QUANTILES.length - 1));
        return noise > QUANTILES[index];
    }

    /**
     * Samples the value distribution of the nest noise once, so a share can be turned into a threshold.
     *
     * @return sorted noise values
     */
    private static double[] quantiles() {
        SplitMix64 rng = new SplitMix64(0x4E455354L);
        double[] values = new double[20000];
        for (int i = 0; i < values.length; i++) {
            values[i] = ValueNoise.sample3(1L, rng.nextDouble(-5000, 5000), rng.nextDouble(-5000, 5000),
                    rng.nextDouble(-5000, 5000));
        }
        Arrays.sort(values);
        return values;
    }

    /**
     * Gives the rock of a zone.
     *
     * @param zone the zone, not HOST
     * @return its block
     */
    private static BlockState rock(Zone zone) {
        return switch (zone) {
            case GOSSAN -> ModBlocks.GOSSAN.get().defaultBlockState();
            case LEACHED, OXIDE, OXIDE_BASE -> ModBlocks.BLEACHED_PORPHYRY.get().defaultBlockState();
            case CEMENTATION, PHYLLIC -> ModBlocks.PHYLLIC_ROCK.get().defaultBlockState();
            case CORE -> ModBlocks.PORPHYRY.get().defaultBlockState();
            case POTASSIC -> ModBlocks.POTASSIC_PORPHYRY.get().defaultBlockState();
            case PROPYLITIC -> ModBlocks.PROPYLITE.get().defaultBlockState();
            case HOST -> throw new IllegalArgumentException("HOST keeps the original block");
        };
    }
}
