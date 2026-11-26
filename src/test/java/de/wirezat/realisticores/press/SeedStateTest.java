package de.wirezat.realisticores.press;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeedStateTest {
    @Test
    void seedSpeedsUpTheRunAndMultipliesTheYieldUntilItIsWornOut() {
        SeedState state = new SeedState();
        assertEquals(4800, state.time(4800));
        assertEquals(1, state.crystals(1));
        state.load(new PressSeed(16, 4, 2));
        assertEquals(1200, state.time(4800));
        assertEquals(18, state.crystals(9));
        state.finishRun(9);
        assertEquals(7, state.left());
        state.finishRun(9);
        assertEquals(0, state.left());
        assertEquals(4800, state.time(4800));
        assertEquals(9, state.crystals(9));
    }

    @Test
    void aNewSeedIsOnlyTakenBetweenTwoRuns() {
        SeedState state = new SeedState();
        assertTrue(state.wantsSeed(0));
        assertFalse(state.wantsSeed(1));
        state.load(new PressSeed(8, 2, 1));
        assertFalse(state.wantsSeed(0));
    }

    @Test
    void aSlowSeedNeverStretchesTheRunBeyondWhatTheMenuCanShow() {
        SeedState state = new SeedState();
        state.load(new PressSeed(4, 0.5, 1));
        assertEquals(PressRecipe.MAX_TIME, state.time(PressRecipe.MAX_TIME));
    }

    @Test
    void fractionsOfTheYieldAddUpOverSeveralRuns() {
        SeedState state = new SeedState();
        state.load(new PressSeed(16, 1.5, 1.5));
        assertEquals(3200, state.time(4800));
        assertEquals(1, state.finishRun(1));
        assertEquals(2, state.crystals(1));
        assertEquals(2, state.finishRun(1));
        assertEquals(1, state.finishRun(1));
    }
}
