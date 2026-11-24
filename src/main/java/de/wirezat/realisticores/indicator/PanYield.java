package de.wirezat.realisticores.indicator;

import de.wirezat.realisticores.ModItems;
import de.wirezat.realisticores.OreConfig;
import de.wirezat.realisticores.kimberlite.Kimberlite;
import de.wirezat.realisticores.kimberlite.KimberliteParams;
import de.wirezat.realisticores.kimberlite.Pipe;
import de.wirezat.realisticores.kimberlite.Plume;
import de.wirezat.realisticores.kimberlite.PlumePath;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.neoforged.neoforge.registries.DeferredItem;

/** Works out which indicator minerals one pan of sediment yields at a position. */
public final class PanYield {
    private PanYield() {
    }

    /**
     * Rolls the minerals of one pan.
     *
     * @param level the world
     * @param pos the sediment block being panned
     * @param random source of the rolls
     * @param chanceFactor factor applied to every find chance, 1 for a normal pan
     * @param fortune extra pieces per found mineral: 1 plus 0 up to this many
     * @return found minerals, empty when the pan holds none
     */
    public static List<ItemStack> pan(ServerLevel level, BlockPos pos, RandomSource random, double chanceFactor,
            int fortune) {
        List<ItemStack> found = new ArrayList<>();
        if (level.dimension() != Level.OVERWORLD || !OreConfig.resolved().kimberlitePipeActive()
                || !(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator generator)) {
            return found;
        }
        KimberliteParams.PlumeParams params = Kimberlite.params().plume();
        int reach = (int) Math.ceil(params.maxLength() * Plume.maxFactor(params));
        List<Pipe> pipes = Kimberlite.generator().pipesNear(level.getSeed(), pos.getX() - reach, pos.getZ() - reach,
                pos.getX() + reach, pos.getZ() + reach);
        LevelTerrainProbe probe = new LevelTerrainProbe(level, generator);
        double pyrope = 1.0;
        double chromite = 1.0;
        double ilmenite = 1.0;
        for (Pipe pipe : pipes) {
            double range = params.maxLength() * Kimberlite.plumeFactor(level.getSeed(), pipe, probe) + pipe.diameter();
            if (Math.hypot(pipe.x() - pos.getX(), pipe.z() - pos.getZ()) > range) {
                continue;
            }
            PlumePath path = Kimberlite.plume(level.getSeed(), pipe, probe, level.getSeaLevel());
            pyrope *= 1.0 - Plume.density(path, pos.getX(), pos.getZ(), params, params.pyrope());
            chromite *= 1.0 - Plume.density(path, pos.getX(), pos.getZ(), params, params.chromite());
            ilmenite *= 1.0 - Plume.density(path, pos.getX(), pos.getZ(), params, params.ilmenite());
        }
        roll(found, ModItems.PYROPE, (1.0 - pyrope) * chanceFactor, random, fortune);
        roll(found, ModItems.CHROMITE, (1.0 - chromite) * chanceFactor, random, fortune);
        roll(found, ModItems.ILMENITE, (1.0 - ilmenite) * chanceFactor, random, fortune);
        return found;
    }

    private static void roll(List<ItemStack> found, DeferredItem<Item> mineral, double chance, RandomSource random,
            int fortune) {
        if (random.nextDouble() < chance) {
            found.add(new ItemStack(mineral.get(), 1 + random.nextInt(fortune + 1)));
        }
    }
}
