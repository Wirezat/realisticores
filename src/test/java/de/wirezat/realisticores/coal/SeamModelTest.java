package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SeamModelTest {
    private static final CoalParams PARAMS = CoalParams.defaults();
    private static final SeamModel MODEL = new SeamModel(PARAMS);

    private static ResolvedBasin basin(BasinType type, long seed, int x) {
        Basin basin = new Basin(x, 0, 900.0, 600.0, 0.4, type, seed);
        return new ResolvedBasin(basin, type, 117, BasinLayout.of(basin, type, PARAMS));
    }

    private static List<SeamColumn> columns(BasinType type) {
        List<SeamColumn> columns = new ArrayList<>();
        for (int x = -880; x <= 880; x += 4) {
            MODEL.column(List.of(basin(type, 4L, 0)), x, 0).ifPresent(columns::add);
        }
        return columns;
    }

    @Test
    void layersAscendWithoutOverlap() {
        for (BasinType type : BasinType.values()) {
            for (SeamColumn column : columns(type)) {
                int last = Integer.MIN_VALUE;
                for (Layer layer : column.layers()) {
                    assertTrue(layer.minY() > last && layer.maxY() >= layer.minY());
                    last = layer.maxY();
                }
            }
        }
    }

    @Test
    void seamsLieOnTheirFloorRock() {
        for (BasinType type : BasinType.values()) {
            Rock floor = type == BasinType.LIGNITE ? Rock.MUDSTONE : Rock.FIRECLAY;
            for (SeamColumn column : columns(type)) {
                List<Layer> layers = column.layers();
                for (int i = 0; i < layers.size(); i++) {
                    if (layers.get(i).rock() != Rock.SEAM) {
                        continue;
                    }
                    Layer below = layers.get(i - 1);
                    boolean parting = i >= 2 && layers.get(i - 2).rock() == Rock.SEAM
                            && below.maxY() - below.minY() < PARAMS.seam().maxParting();
                    assertTrue(below.rock() == floor || parting, below.toString());
                }
            }
        }
    }

    @Test
    void seamEndsAtTheEdge() {
        ResolvedBasin basin = basin(BasinType.HARD_COAL, 3L, 0);
        for (int x = -1200; x <= 1200; x++) {
            double edge = basin.basin().edgeDistance(x, 40, PARAMS.basin());
            Optional<SeamColumn> column = MODEL.column(List.of(basin), x, 40);
            if (edge >= 1.0) {
                assertTrue(column.isEmpty());
            } else if (edge > 0.995) {
                assertTrue(column.isEmpty() || column.get().layers().stream().noneMatch(l -> l.rock() == Rock.SEAM));
            }
        }
    }

    @Test
    void anthraciteOnlyInTheCoreOfFoldedBasins() {
        for (BasinType type : BasinType.values()) {
            ResolvedBasin basin = basin(type, 5L, 0);
            for (int x = -800; x <= 800; x += 10) {
                boolean core = type == BasinType.FOLDED
                        && basin.basin().edgeDistance(x, 0, PARAMS.basin()) < PARAMS.fold().coreShare();
                MODEL.column(List.of(basin), x, 0)
                        .ifPresent(column -> assertEquals(core, column.rank() == Rank.ANTHRACITE));
            }
        }
    }

    @Test
    void neighbouringColumnsOfTheSmallestUnfaultedBasinTouch() {
        double root = Math.sqrt(PARAMS.basin().minAspect());
        for (BasinType type : BasinType.values()) {
            Basin smallest = new Basin(0, 0, PARAMS.basin().minRadius() / root, PARAMS.basin().minRadius() * root, 0.4,
                    type, 5L);
            BasinLayout l = BasinLayout.of(smallest, type, PARAMS);
            ResolvedBasin basin = new ResolvedBasin(smallest, type, 117, new BasinLayout(l.depth(),
                    l.seams(), List.of(), l.foldAmplitude(), l.foldWavelength(), l.foldPhase()));
            for (int x = -840; x < 840; x += 7) {
                for (int z = -560; z < 560; z += 7) {
                    if (basin.basin().distance(x, z) < 0.95) {
                        double here = MODEL.floorY(basin, x, z);
                        assertTrue(Math.abs(here - MODEL.floorY(basin, x + 1, z)) <= 1.0);
                        assertTrue(Math.abs(here - MODEL.floorY(basin, x, z + 1)) <= 1.0);
                    }
                }
            }
        }
    }

    @Test
    void nearerBasinWinsAndAMissingOneDoesNotBlock() {
        ResolvedBasin near = basin(BasinType.LIGNITE, 1L, 0);
        ResolvedBasin far = basin(BasinType.HARD_COAL, 2L, 500);
        assertEquals(Rank.LIGNITE, MODEL.column(List.of(far, near), 100, 0).orElseThrow().rank());
        assertEquals(Rank.HARD_COAL, MODEL.column(List.of(far), 100, 0).orElseThrow().rank());
    }

    @Test
    void sandstoneLiesAboveASeamAndNeverReplacesOne() {
        int sandstone = 0;
        for (BasinType type : BasinType.values()) {
            for (SeamColumn column : columns(type)) {
                List<Layer> layers = column.layers();
                for (int i = 0; i < layers.size(); i++) {
                    if (layers.get(i).rock() != Rock.SANDSTONE) {
                        continue;
                    }
                    sandstone++;
                    int y = layers.get(i).minY();
                    assertTrue(layers.stream().anyMatch(l -> l.rock() == Rock.SEAM && l.maxY() < y));
                }
            }
        }
        assertTrue(sandstone > 0);
    }
}
