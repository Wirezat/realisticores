package de.wirezat.realisticores.indicator;

import org.jetbrains.annotations.Nullable;

/**
 * Indicator grains measured at one position.
 *
 * @param grains the measured grains
 * @param source trail the grains belong to, or null without grains
 * @param tone grade tone of the toned indicator
 */
public record Measurement(Grains grains, @Nullable Trail source, GradeTone tone) {
    public static final Measurement NONE = new Measurement(Grains.NONE, null, GradeTone.PALE);
}
