package de.wirezat.realisticores.mixin;

import de.wirezat.realisticores.indicator.LevelTerrainProbe;
import de.wirezat.realisticores.kimberlite.KimberliteCarver;
import de.wirezat.realisticores.kimberlite.TerrainProbe;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    /**
     * Carves kimberlite pipes, crater bowls and crater lakes into overworld chunks after their surface is built.
     *
     * @param level region the chunk is generated in
     * @param structureManager structure manager of the level
     * @param random random state of the generator
     * @param chunk the chunk being generated
     * @param ci callback info
     */
    @Inject(method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
            at = @At("TAIL"))
    private void realisticores$carveKimberlite(WorldGenRegion level, StructureManager structureManager,
            RandomState random, ChunkAccess chunk, CallbackInfo ci) {
        if (level.getLevel().dimension() != Level.OVERWORLD) {
            return;
        }
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator) (Object) this;
        TerrainProbe probe = new LevelTerrainProbe(level.getLevel(), generator);
        KimberliteCarver.carve(chunk, level.getSeed(), probe, generator.getSeaLevel());
    }
}
