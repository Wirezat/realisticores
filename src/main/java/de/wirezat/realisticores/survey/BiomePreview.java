package de.wirezat.realisticores.survey;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.material.MapColor;

/** Draws the water outlines of an explorer map like the vanilla biome preview, safe on any thread. */
final class BiomePreview {
    static final int SIZE = 128;

    private BiomePreview() {
    }

    /**
     * Computes the preview of a map area from the biome source, without loading chunks.
     *
     * @param level the world
     * @param west west edge of the map in blocks
     * @param north north edge of the map in blocks
     * @param blocksPerPixel blocks covered by one map pixel
     * @return packed map colours by row; 0 leaves a pixel empty
     */
    static byte[] colors(ServerLevel level, int west, int north, int blocksPerPixel) {
        BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        BiomeManager biomes = level.getBiomeManager()
                .withDifferentSource((x, y, z) -> source.getNoiseBiome(x, y, z, sampler));
        boolean[] water = new boolean[SIZE * SIZE];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < water.length; i++) {
            pos.set(west + i % SIZE * blocksPerPixel, 0, north + i / SIZE * blocksPerPixel);
            water[i] = biomes.getBiome(pos).is(BiomeTags.WATER_ON_MAP_OUTLINES);
        }
        byte[] colors = new byte[SIZE * SIZE];
        for (int z = 1; z < SIZE - 1; z++) {
            for (int x = 1; x < SIZE - 1; x++) {
                colors[z * SIZE + x] = color(water, x, z);
            }
        }
        return colors;
    }

    private static byte color(boolean[] water, int x, int z) {
        int neighbours = 0;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                if ((dx != 0 || dz != 0) && water[(z + dz) * SIZE + x + dx]) {
                    neighbours++;
                }
            }
        }
        if (!water[z * SIZE + x]) {
            return neighbours == 0 ? 0 : MapColor.COLOR_BROWN.getPackedId(
                    neighbours > 3 ? MapColor.Brightness.NORMAL : MapColor.Brightness.LOWEST);
        }
        if (neighbours == 8) {
            return z % 2 == 0 ? MapColor.COLOR_ORANGE.getPackedId(hatching(x, z)) : 0;
        }
        return MapColor.COLOR_ORANGE.getPackedId(neighbours > 5 ? MapColor.Brightness.NORMAL
                : neighbours > 1 ? MapColor.Brightness.LOW : MapColor.Brightness.LOWEST);
    }

    private static MapColor.Brightness hatching(int x, int z) {
        return switch ((x + (int) (Mth.sin(z) * 7.0F)) / 8 % 5) {
            case 0, 4 -> MapColor.Brightness.LOW;
            case 1, 3 -> MapColor.Brightness.NORMAL;
            case 2 -> MapColor.Brightness.HIGH;
            default -> MapColor.Brightness.LOWEST;
        };
    }
}
