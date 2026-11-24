package de.wirezat.realisticores;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OreMatcherTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static ConfiguredFeature<?, ?> oreFeature() {
        OreConfiguration config = new OreConfiguration(new BlockMatchTest(Blocks.STONE), Blocks.IRON_ORE.defaultBlockState(), 9);
        return new ConfiguredFeature<>(Feature.ORE, config);
    }

    @Test
    void suppressesOreFeatureWhoseTargetIsListed() {
        assertTrue(new OreMatcher(List.of("minecraft:iron_ore")).suppresses(Level.OVERWORLD, oreFeature()));
    }

    @Test
    void keepsOreFeatureWhoseTargetIsNotListed() {
        assertFalse(new OreMatcher(List.of("minecraft:coal_ore")).suppresses(Level.OVERWORLD, oreFeature()));
    }

    @Test
    void keepsOreFeatureOfAnotherDimensionEvenIfTargetIsListed() {
        assertFalse(new OreMatcher(List.of("minecraft:iron_ore")).suppresses(Level.NETHER, oreFeature()));
    }
}
