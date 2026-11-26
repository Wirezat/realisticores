package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class SurfaceHintTest {
    private static final SurfaceHint HINT = new SurfaceHint(CoalParams.defaults().surface());
    private static final long SEED = 7L;
    private static final int SAMPLES = 40000;

    private static SeamColumn column(Rank rank, Layer... layers) {
        return new SeamColumn(rank, List.of(layers), 0.0, 0L, 1.0);
    }

    private static SeamColumn seamAt(Rank rank, int minY) {
        return column(rank, new Layer(minY - 2, minY - 1, Rock.FIRECLAY), new Layer(minY, minY + 1, Rock.SEAM));
    }

    private static double share(SeamColumn column, boolean soil) {
        int hits = 0;
        for (int i = 0; i < SAMPLES; i++) {
            int x = i % 200;
            int z = i / 200;
            if (soil ? HINT.soil(SEED, column, 70, x, z) : HINT.pieces(SEED, column, 70, x, z) > 0) {
                hits++;
            }
        }
        return (double) hits / SAMPLES;
    }

    @Test
    void heightIsZeroWhereASeamMeetsTheSurface() {
        assertEquals(OptionalInt.of(0), SurfaceHint.height(seamAt(Rank.HARD_COAL, 70), 70));
        assertEquals(OptionalInt.of(0), SurfaceHint.height(seamAt(Rank.HARD_COAL, 66), 70));
        assertEquals(OptionalInt.empty(), SurfaceHint.height(seamAt(Rank.HARD_COAL, 65), 70));
    }

    @Test
    void heightCountsToTheLowestSeamAbove() {
        SeamColumn two = column(Rank.HARD_COAL, new Layer(40, 41, Rock.SEAM), new Layer(75, 76, Rock.SEAM),
                new Layer(90, 91, Rock.SEAM));
        assertEquals(OptionalInt.of(5), SurfaceHint.height(two, 70));
    }

    @Test
    void piecesThinOutWithHeight() {
        double outcrop = share(seamAt(Rank.HARD_COAL, 70), false);
        double top = share(seamAt(Rank.HARD_COAL, 78), false);
        assertEquals(0.04, outcrop, 0.004);
        assertTrue(top > 0.0 && top < outcrop / 4.0);
        assertEquals(0.0, share(seamAt(Rank.HARD_COAL, 79), false));
    }

    @Test
    void soilCoversMostOfTheOutcropAndFraysOut() {
        double outcrop = share(seamAt(Rank.LIGNITE, 70), true);
        double fringe = share(seamAt(Rank.LIGNITE, 72), true);
        assertEquals(0.6, outcrop, 0.1);
        assertTrue(fringe > 0.0 && fringe < outcrop / 2.0);
        assertEquals(0.0, share(seamAt(Rank.LIGNITE, 73), true));
    }

    @Test
    void anthraciteLeavesNoHint() {
        assertEquals(0.0, share(seamAt(Rank.ANTHRACITE, 70), false));
        assertFalse(share(seamAt(Rank.ANTHRACITE, 70), true) > 0.0);
    }
}
