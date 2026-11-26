package de.wirezat.realisticores.kimberlite;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PipeShapeTest {
    private static final KimberliteParams.PipeParams PARAMS = KimberliteParams.defaults().pipe();
    private static final PipeShape SHAPE = new PipeShape(PARAMS, new PipeProfile(PARAMS));

    private static double boundaryDistance(Pipe pipe, double angle, int y) {
        for (double distance = 0; distance < SHAPE.reach(pipe) + 2; distance += 0.25) {
            int x = pipe.x() + (int) Math.round(Math.cos(angle) * distance);
            int z = pipe.z() + (int) Math.round(Math.sin(angle) * distance);
            if (SHAPE.normalizedDistance(SHAPE.outline(pipe), x, y, z) > 1.0) {
                return distance;
            }
        }
        return SHAPE.reach(pipe);
    }

    @Test
    void mostOutlinesAreNotCircular() {
        int irregular = 0;
        for (int i = 0; i < 10; i++) {
            Pipe pipe = new Pipe(1000 + i * 977, 2000 - i * 1301, 120);
            double min = Double.MAX_VALUE;
            double max = 0;
            for (int step = 0; step < 24; step++) {
                double distance = boundaryDistance(pipe, step * Math.PI / 12, 20);
                min = Math.min(min, distance);
                max = Math.max(max, distance);
            }
            if (max > min * 1.25) {
                irregular++;
            }
        }
        assertTrue(irregular >= 8, "irregular=" + irregular);
    }

    @Test
    void mostSurfaceColumnsStayCovered() {
        int outcrops = 0;
        int total = 0;
        for (int x = 0; x < 200; x++) {
            for (int z = 0; z < 200; z++) {
                total++;
                if (SHAPE.isOutcrop(x, z)) {
                    outcrops++;
                }
            }
        }
        double fraction = outcrops / (double) total;
        assertTrue(fraction > 0.05 && fraction < 0.3, "fraction=" + fraction);
    }
}
