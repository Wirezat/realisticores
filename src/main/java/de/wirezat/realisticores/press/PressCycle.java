package de.wirezat.realisticores.press;

/** Burn and progress state of the crystal press, advanced once per tick like a furnace. */
public final class PressCycle {
    private int litTime;
    private int litDuration;
    private int progress;
    private int total;

    /**
     * Outcome of one tick.
     *
     * @param burnFuel true if a fuel item has to be consumed now
     * @param craft true if the recipe finished and its items have to be exchanged now
     */
    public record Result(boolean burnFuel, boolean craft) {
    }

    /**
     * Advances the press by one tick. Progress only grows while the press is lit; without a running recipe
     * the progress decays by two per tick.
     *
     * @param canWork true if a recipe matches and its output fits
     * @param fuelBurnTime burn time in ticks of the fuel in the fuel slot, 0 if there is none
     * @param recipeTime duration of the current recipe in ticks
     * @return what the caller has to do with the inventory
     */
    public Result tick(boolean canWork, int fuelBurnTime, int recipeTime) {
        boolean burnFuel = false;
        boolean craft = false;
        if (litTime > 0) {
            litTime--;
        }
        if (canWork) {
            total = recipeTime;
            if (litTime == 0 && fuelBurnTime > 0) {
                litTime = fuelBurnTime;
                litDuration = fuelBurnTime;
                burnFuel = true;
            }
            if (litTime > 0) {
                progress++;
                if (progress >= total) {
                    progress = 0;
                    craft = true;
                }
            }
        } else {
            progress = Math.max(0, progress - 2);
        }
        return new Result(burnFuel, craft);
    }

    /**
     * Tells whether the press is burning fuel.
     *
     * @return true while burn time is left
     */
    public boolean isLit() {
        return litTime > 0;
    }

    /**
     * Gives the remaining burn time.
     *
     * @return ticks of fuel left
     */
    public int litTime() {
        return litTime;
    }

    /**
     * Gives the burn time of the fuel item that is burning.
     *
     * @return ticks the current fuel lasts in total
     */
    public int litDuration() {
        return litDuration;
    }

    /**
     * Gives the current progress.
     *
     * @return ticks the recipe has run
     */
    public int progress() {
        return progress;
    }

    /**
     * Gives the duration of the running recipe.
     *
     * @return ticks the recipe needs
     */
    public int total() {
        return total;
    }

    /**
     * Restores a saved state.
     *
     * @param litTime remaining burn time
     * @param litDuration burn time of the current fuel
     * @param progress recipe progress
     * @param total recipe duration
     */
    public void restore(int litTime, int litDuration, int progress, int total) {
        this.litTime = litTime;
        this.litDuration = litDuration;
        this.progress = progress;
        this.total = total;
    }
}
