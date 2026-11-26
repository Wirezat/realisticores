package de.wirezat.realisticores.coal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** A coal seam on fire: hurts like magma, glows and gives off smoke where it meets the air. */
public class BurningSeamBlock extends MagmaBlock {
    /**
     * Creates the block.
     *
     * @param properties block properties
     */
    public BurningSeamBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (random.nextInt(3) == 0 && level.getBlockState(above).isAir()) {
            level.addParticle(random.nextInt(4) == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE,
                    above.getX() + random.nextDouble(), above.getY() + 0.05, above.getZ() + random.nextDouble(),
                    0.0, 0.04, 0.0);
        }
    }
}
