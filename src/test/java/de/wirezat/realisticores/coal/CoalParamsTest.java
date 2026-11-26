package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CoalParamsTest {
    @Test
    void sanitizedKeepsBasinsWithinTheirNeighbourCells() {
        CoalParams d = CoalParams.defaults();
        CoalParams.BasinParams b = new CoalParams.BasinParams(256, 1.0, 0.4, 5000, 20000, 0.25, 0.5, 250.0, 0.65);
        CoalParams.BasinParams fixed = new CoalParams(b, d.relief(), d.hardCoal(), d.lignite(), d.folded(), d.fold(),
                d.seam(), d.rock(), d.surface(), d.accessories()).sanitized().basin();
        assertEquals(70, fixed.minRadius());
        assertEquals(70, fixed.maxRadius());
    }

    @Test
    void sanitizedLiftsUpperBoundsToTheirLowerBounds() {
        CoalParams d = CoalParams.defaults();
        CoalParams.BasinParams b = d.basin();
        CoalParams.TypeParams t = d.hardCoal();
        CoalParams.RockParams r = d.rock();
        CoalParams fixed = new CoalParams(
                new CoalParams.BasinParams(b.cellSize(), b.chance(), b.jitter(), 600, 100, b.minAspect(),
                        b.outlineNoise(), b.outlineWavelength(), b.hardCoalShare()),
                d.relief(),
                new CoalParams.TypeParams(40, 10, 3, 1, t.minThickness(), t.maxThickness(), t.minSpacing(),
                        t.maxSpacing(), t.maxFaults()),
                d.lignite(), d.folded(), new CoalParams.FoldParams(10, 18, 200, 50, 0.55), d.seam(),
                new CoalParams.RockParams(4, 1, r.minBlackShale(), r.maxBlackShale(), r.minFireclay(),
                        r.maxFireclay(), r.minRoofMudstone(), r.maxRoofMudstone(), r.minFloorMudstone(),
                        r.maxFloorMudstone(), r.wavelength()), d.surface(), d.accessories()).sanitized();
        assertEquals(600, fixed.basin().maxRadius());
        assertEquals(40, fixed.hardCoal().maxDepth());
        assertEquals(3, fixed.hardCoal().maxSeams());
        assertEquals(200, fixed.fold().maxWavelength());
        assertEquals(4, fixed.rock().maxShale());
    }
}
