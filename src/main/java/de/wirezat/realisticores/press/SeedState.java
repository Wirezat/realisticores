package de.wirezat.realisticores.press;

/** The seed crystal at work in a press and how much of it is left. */
public final class SeedState {
    private static final double ROUNDING = 1.0e-6;

    private PressSeed seed = new PressSeed(1, 1.0, 1.0);
    private int left;
    private double remainder;

    /**
     * Tells whether the press should take a new seed.
     *
     * @param progress progress of the current run in ticks
     * @return true between two runs once the old seed is worn out
     */
    public boolean wantsSeed(int progress) {
        return left == 0 && progress == 0;
    }

    /**
     * Puts a fresh seed to work.
     *
     * @param fresh the seed
     */
    public void load(PressSeed fresh) {
        restore(fresh, fresh.uses(), remainder);
    }

    /**
     * Restores a saved state.
     *
     * @param saved the seed at work
     * @param remaining crystals it still lasts for
     * @param fraction part of a crystal left over from earlier runs, 0 to 1
     */
    public void restore(PressSeed saved, int remaining, double fraction) {
        seed = saved;
        left = remaining;
        remainder = fraction;
    }

    /**
     * Gives the duration of a run.
     *
     * @param recipeTime duration without a seed in ticks
     * @return duration with the seed at work, from one tick up to {@link PressRecipe#MAX_TIME}
     */
    public int time(int recipeTime) {
        return left > 0
                ? (int) Math.max(1, Math.min(PressRecipe.MAX_TIME, Math.round(recipeTime / seed.speed())))
                : recipeTime;
    }

    /**
     * Gives the number of crystals of a run.
     *
     * @param count number without a seed
     * @return number with the seed at work
     */
    public int crystals(int count) {
        return left > 0 ? (int) Math.floor(count * seed.yield() + remainder + ROUNDING) : count;
    }

    /**
     * Finishes a run: wears the seed down and keeps the fraction of a crystal for the next run.
     *
     * @param count number of crystals without a seed
     * @return number of crystals the run yields
     */
    public int finishRun(int count) {
        int crystals = crystals(count);
        if (left > 0) {
            remainder += count * seed.yield() - crystals;
            left = Math.max(0, left - count);
        }
        return crystals;
    }

    /**
     * Gives the part of a crystal left over from earlier runs.
     *
     * @return fraction from 0 to 1
     */
    public double remainder() {
        return remainder;
    }

    /**
     * Gives the seed at work.
     *
     * @return the seed, meaningless once nothing is left of it
     */
    public PressSeed seed() {
        return seed;
    }

    /**
     * Gives how long the seed still lasts.
     *
     * @return remaining crystals, 0 without a seed
     */
    public int left() {
        return left;
    }
}
