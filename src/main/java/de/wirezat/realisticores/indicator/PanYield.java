package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.KimberliteParams;
import de.wirezat.realisticores.kimberlite.Plume;
import de.wirezat.realisticores.kimberlite.PlumePath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

/** Works out which indicator minerals one pan of sediment yields at a position. */
public final class PanYield {
    private static final double TRAIL_MIN_CHANCE = 0.02;

    private PanYield() {
    }

    /**
     * Find chances at one position.
     *
     * @param pyrope chance of pyrope per pan
     * @param chromite chance of chromite per pan
     * @param ilmenite chance of ilmenite per pan
     * @param source trail of the pipe that contributes most at the position, or null when no trail is strong enough
     */
    public record Odds(double pyrope, double chromite, double ilmenite, @Nullable PlumePath source) {
    }

    /**
     * Works out the find chances at a position in the background; trails not traced yet can take seconds.
     *
     * @param level the world
     * @param pos the sediment block being panned
     * @return future of the chances
     */
    public static CompletableFuture<Odds> odds(ServerLevel level, BlockPos pos) {
        if (level.dimension() != Level.OVERWORLD || !OreConfig.resolved().kimberlitePipeActive()
                || !(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            return CompletableFuture.completedFuture(new Odds(0.0, 0.0, 0.0, null));
        }
        KimberliteParams.PlumeParams params = Kimberlite.params().plume();
        return Kimberlite.plumesReaching(level.getSeed(), new LevelTerrainProbe(level, generator),
                level.getSeaLevel(), pos.getX(), pos.getZ(), pos.getX(), pos.getZ())
                .thenApply(paths -> combine(paths, pos, params));
    }

    private static Odds combine(List<PlumePath> paths, BlockPos pos, KimberliteParams.PlumeParams params) {
        double pyrope = 1.0;
        double chromite = 1.0;
        double ilmenite = 1.0;
        double strongest = 0.0;
        PlumePath source = null;
        for (PlumePath path : paths) {
            double p = Plume.density(path, pos.getX(), pos.getZ(), params, params.pyrope());
            double c = Plume.density(path, pos.getX(), pos.getZ(), params, params.chromite());
            double i = Plume.density(path, pos.getX(), pos.getZ(), params, params.ilmenite());
            pyrope *= 1.0 - p;
            chromite *= 1.0 - c;
            ilmenite *= 1.0 - i;
            double score = Math.max(p, Math.max(c, i));
            if (score > strongest) {
                strongest = score;
                source = path;
            }
        }
        return new Odds(1.0 - pyrope, 1.0 - chromite, 1.0 - ilmenite, strongest >= TRAIL_MIN_CHANCE ? source : null);
    }

    /**
     * Rolls the minerals of one pan.
     *
     * @param odds find chances at the position
     * @param random source of the rolls
     * @param chanceFactor factor applied to every find chance, 1 for a normal pan
     * @param fortune extra pieces per found mineral: 1 plus 0 up to this many
     * @return the minerals found, empty when the pan holds none
     */
    public static List<ItemStack> roll(Odds odds, RandomSource random, double chanceFactor, int fortune) {
        List<ItemStack> found = new ArrayList<>();
        roll(found, ModItems.PYROPE, odds.pyrope() * chanceFactor, random, fortune);
        roll(found, ModItems.CHROMITE, odds.chromite() * chanceFactor, random, fortune);
        roll(found, ModItems.ILMENITE, odds.ilmenite() * chanceFactor, random, fortune);
        return found;
    }

    private static void roll(List<ItemStack> found, DeferredItem<Item> mineral, double chance, RandomSource random,
            int fortune) {
        if (random.nextDouble() < chance) {
            found.add(new ItemStack(mineral.get(), 1 + random.nextInt(fortune + 1)));
        }
    }
}
