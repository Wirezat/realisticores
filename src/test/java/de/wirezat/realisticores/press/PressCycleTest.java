package de.wirezat.realisticores.press;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class PressCycleTest {
    @Test
    void recipeFinishesAfterItsTimeAndConsumesFuelAsItBurnsOut() {
        PressCycle cycle = new PressCycle();
        int crafts = 0;
        int fuelItems = 0;
        for (int tick = 0; tick < 4800; tick++) {
            PressCycle.Result result = cycle.tick(true, 1600, 4800);
            crafts += result.craft() ? 1 : 0;
            fuelItems += result.burnFuel() ? 1 : 0;
        }
        assertEquals(1, crafts);
        assertEquals(3, fuelItems);
    }

    @Test
    void withoutFuelThereIsNoProgressAndExistingProgressDecays() {
        PressCycle noFuel = new PressCycle();
        for (int tick = 0; tick < 50; tick++) {
            noFuel.tick(true, 0, 100);
        }
        assertEquals(0, noFuel.progress());
        assertFalse(noFuel.isLit());

        PressCycle running = new PressCycle();
        for (int tick = 0; tick < 5; tick++) {
            running.tick(true, 1600, 100);
        }
        assertEquals(5, running.progress());
        running.tick(false, 0, 100);
        assertEquals(3, running.progress());
    }
}
