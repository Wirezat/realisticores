package de.wirezat.realisticores.coal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.wirezat.realisticores.worldgen.IntRange;
import org.junit.jupiter.api.Test;

class CoalParamsTest {
    @Test
    void sanitizedKeepsBasinsWithinTheirNeighbourCells() {
        CoalParams d = CoalParams.defaults();
        CoalParams.BasinParams b = new CoalParams.BasinParams(256, 1.0, 0.4, new IntRange(5000, 20000), 0.25, 0.5, 250.0,
                0.65);
        CoalParams.BasinParams fixed = new CoalParams(b, d.relief(), d.hardCoal(), d.lignite(), d.folded(), d.fold(),
                d.seam(), d.rock(), d.surface(), d.accessories()).sanitized().basin();
        assertEquals(new IntRange(70, 70), fixed.radius());
    }

    @Test
    void sanitizedLiftsUpperBoundsToTheirLowerBounds() {
        CoalParams d = CoalParams.defaults();
        CoalParams.BasinParams b = d.basin();
        CoalParams.TypeParams t = d.hardCoal();
        CoalParams.RockParams r = d.rock();
        CoalParams fixed = new CoalParams(
                new CoalParams.BasinParams(b.cellSize(), b.chance(), b.jitter(), new IntRange(600, 100),
                        b.minAspect(), b.outlineNoise(), b.outlineWavelength(), b.hardCoalShare()),
                d.relief(),
                new CoalParams.TypeParams(new IntRange(40, 10), t.seams(), t.thickness(), t.spacing(), t.maxFaults()),
                d.lignite(), d.folded(), d.fold(), d.seam(),
                new CoalParams.RockParams(new IntRange(4, 1), r.blackShale(), r.fireclay(), r.roofMudstone(),
                        r.floorMudstone(), r.wavelength()), d.surface(), d.accessories()).sanitized();
        assertEquals(600, fixed.basin().radius().max());
        assertEquals(40, fixed.hardCoal().depth().max());
        assertEquals(4, fixed.rock().shale().max());
    }
}
