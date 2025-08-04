package wirezat.realisticores.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(OreFeature.class)
public class OreFeatureMixin {

    @Unique
    private static final Set<Block> BLACKLISTED_ORES = Set.of(
            Blocks.COAL_ORE,
            Blocks.IRON_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.GOLD_ORE,
            Blocks.EMERALD_ORE,
            Blocks.LAPIS_ORE,
            Blocks.COPPER_ORE,
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DEEPSLATE_COPPER_ORE
    );


    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void onGenerate(FeatureContext<OreFeatureConfig> context, CallbackInfoReturnable<Boolean> cir) {
        OreFeatureConfig config = context.getConfig();

        for (OreFeatureConfig.Target target : config.targets) {
            if (BLACKLISTED_ORES.contains(target.state.getBlock())) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }
        }

    }
}