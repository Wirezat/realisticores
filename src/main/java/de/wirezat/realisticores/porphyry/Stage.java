package de.wirezat.realisticores.porphyry;

/**
 * How deeply erosion has cut into a deposit, with the thickness of its weathering zones in blocks.
 *
 * <p>{@code cover} is the rock left above the potassic dome; negative values cut the dome off.
 */
public enum Stage {
    SLIGHT(22, 7, 8, 10),
    MODERATE(14, 5, 15, 8),
    DEEP(-20, 4, 5, 4);

    private final int cover;
    private final int gossan;
    private final int oxide;
    private final int cementation;

    Stage(int cover, int gossan, int oxide, int cementation) {
        this.cover = cover;
        this.gossan = gossan;
        this.oxide = oxide;
        this.cementation = cementation;
    }

    /**
     * Gives the rock left above the potassic dome.
     *
     * @return cover in blocks
     */
    public int cover() {
        return cover;
    }

    /**
     * Gives the thickness of the gossan cap.
     *
     * @return mean thickness in blocks
     */
    public int gossan() {
        return gossan;
    }

    /**
     * Gives the thickness of the oxide zone above the water table.
     *
     * @return largest thickness in blocks
     */
    public int oxide() {
        return oxide;
    }

    /**
     * Gives the thickness of the cementation zone below the water table.
     *
     * @return mean thickness in blocks
     */
    public int cementation() {
        return cementation;
    }
}
