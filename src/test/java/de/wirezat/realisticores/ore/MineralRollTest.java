package de.wirezat.realisticores.ore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MineralRollTest {
    @Test
    void differentOresRollIndependentlyAtTheSamePosition() {
        int both = 0;
        int first = 0;
        for (int i = 0; i < 100000; i++) {
            int x = i % 317;
            int z = i / 317;
            boolean a = MineralRoll.roll(0L, "diamond", x, 10, z) < 0.2;
            boolean b = MineralRoll.roll(0L, "graphite", x, 10, z) < 0.2;
            if (a) {
                first++;
                if (b) {
                    both++;
                }
            }
        }
        assertEquals(0.2, both / (double) first, 0.03);
    }

    @Test
    void anotherWorldRollsAnotherPatternAtTheSamePosition() {
        int same = 0;
        for (int x = 0; x < 1000; x++) {
            if ((MineralRoll.roll(1L, "diamond", x, 10, 0) < 0.2) == (MineralRoll.roll(2L, "diamond", x, 10, 0) < 0.2)) {
                same++;
            }
        }
        assertEquals(0.68, same / 1000.0, 0.05);
    }
}
