package de.wirezat.realisticores.indicator;

/** A deposit at the head of an indicator trail. */
public interface TrailSource {
    /**
     * Gives the X of the deposit centre.
     *
     * @return centre X in blocks
     */
    int x();

    /**
     * Gives the Z of the deposit centre.
     *
     * @return centre Z in blocks
     */
    int z();

    /**
     * Gives how far the deposit reaches from its centre at the surface.
     *
     * @return outcrop radius in blocks
     */
    double outcrop();
}
