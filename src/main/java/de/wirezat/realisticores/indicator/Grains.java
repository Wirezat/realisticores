package de.wirezat.realisticores.indicator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.wirezat.realisticores.worldgen.SplitMix64;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Amounts of the indicator minerals in tenths: a measurement of one wash or what has collected in a pan.
 *
 * @param pyrope tenths of pyrope, 600 right at a pipe
 * @param chromite tenths of chromite
 * @param ilmenite tenths of ilmenite
 */
public record Grains(int pyrope, int chromite, int ilmenite) {
    public static final Grains NONE = new Grains(0, 0, 0);
    public static final int PER_ITEM = 1000;

    public static final Codec<Grains> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("pyrope").forGetter(Grains::pyrope),
            Codec.INT.fieldOf("chromite").forGetter(Grains::chromite),
            Codec.INT.fieldOf("ilmenite").forGetter(Grains::ilmenite)).apply(instance, Grains::new));
    public static final StreamCodec<ByteBuf, Grains> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Grains::pyrope,
            ByteBufCodecs.VAR_INT, Grains::chromite,
            ByteBufCodecs.VAR_INT, Grains::ilmenite, Grains::new);

    private static final long SALT = 0x50414E5F4E4F4953L;
    private static final double NOISE = 0.05;

    /**
     * Turns grain densities into a measurement with a noise that is fixed per column.
     *
     * @param pyrope density of pyrope from 0 to 1
     * @param chromite density of chromite from 0 to 1
     * @param ilmenite density of ilmenite from 0 to 1
     * @param seed world seed
     * @param x world X in blocks
     * @param z world Z in blocks
     * @return the measured grains
     */
    public static Grains measure(double pyrope, double chromite, double ilmenite, long seed, int x, int z) {
        SplitMix64 random = new SplitMix64(SplitMix64.hash(seed ^ SALT, x, z));
        return new Grains(noisy(pyrope, random), noisy(chromite, random), noisy(ilmenite, random));
    }

    private static int noisy(double density, SplitMix64 random) {
        return (int) Math.round(density * PER_ITEM * (1.0 - NOISE + 2.0 * NOISE * random.nextDouble()));
    }

    /**
     * Gives how much of a measurement collects in the pan.
     *
     * @param fortune fortune level of the pan, 0 if absent
     * @param silkTouch true if the pan has silk touch
     * @return the grains to add to the content
     */
    public Grains gain(int fortune, boolean silkTouch) {
        double factor = (1.0 + 0.5 * fortune) * (silkTouch ? 0.5 : 1.0);
        return new Grains((int) Math.round(pyrope * factor), (int) Math.round(chromite * factor),
                (int) Math.round(ilmenite * factor));
    }

    /**
     * Adds other grains.
     *
     * @param other grains to add
     * @return the sum per mineral
     */
    public Grains plus(Grains other) {
        return new Grains(pyrope + other.pyrope, chromite + other.chromite, ilmenite + other.ilmenite);
    }

    /**
     * Gives how many items these grains fill.
     *
     * @return number of whole items per mineral
     */
    public Grains whole() {
        return new Grains(pyrope / PER_ITEM, chromite / PER_ITEM, ilmenite / PER_ITEM);
    }

    /**
     * Gives what stays in the pan once the whole items are taken out.
     *
     * @return the rest per mineral in tenths
     */
    public Grains rest() {
        return new Grains(pyrope % PER_ITEM, chromite % PER_ITEM, ilmenite % PER_ITEM);
    }

    /**
     * Tells whether there are no grains at all.
     *
     * @return true if every mineral is zero
     */
    public boolean isEmpty() {
        return equals(NONE);
    }
}
