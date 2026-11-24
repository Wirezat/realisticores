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
            boolean a = MineralRoll.roll("diamond", x, 10, z) < 0.2;
            boolean b = MineralRoll.roll("graphite", x, 10, z) < 0.2;
            if (a) {
                first++;
                if (b) {
                    both++;
                }
            }
        }
        assertEquals(0.2, both / (double) first, 0.03);
    }
}
