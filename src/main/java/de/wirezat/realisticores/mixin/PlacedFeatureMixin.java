package de.wirezat.realisticores.mixin;

import de.wirezat.realisticores.OreConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {
    /**
     * Skips placement of ore features whose targets are in the configured ore list.
     *
     * @param context placement context of the feature
     * @param random random source of the placement
     * @param pos origin of the placement
     * @param cir callback; set to false to report that nothing was placed
     */
    @Inject(method = "placeWithContext", at = @At("HEAD"), cancellable = true)
    private void realisticores$skipSuppressedOres(PlacementContext context, RandomSource random, BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        PlacedFeature self = (PlacedFeature) (Object) this;
        if (OreConfig.matcher().suppresses(self.feature().value())) {
            cir.setReturnValue(false);
        }
    }
}
