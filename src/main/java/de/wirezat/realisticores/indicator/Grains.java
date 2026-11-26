package de.wirezat.realisticores.indicator;

import com.mojang.serialization.Codec;
import de.wirezat.realisticores.worldgen.SplitMix64;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Amounts of indicator minerals in tenths: a measurement of one wash or what has collected in a pan.
 *
 * @param tenths tenths of grains by indicator id; minerals without grains are absent
 */
public record Grains(Map<String, Integer> tenths) {
    public static final Grains NONE = new Grains(Map.of());
    public static final int PER_ITEM = 1000;

    public static final Codec<Grains> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(Grains::new, Grains::tenths);
    public static final StreamCodec<ByteBuf, Grains> STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT)
                    .map(Grains::new, grains -> new HashMap<>(grains.tenths()));

    private static final long SALT = 0x50414E5F4E4F4953L;
    private static final double NOISE = 0.05;

    /**
     * Creates grains, leaving out minerals without grains.
     *
     * @param tenths tenths of grains by indicator id
     */
    public Grains {
        Map<String, Integer> present = new HashMap<>();
        tenths.forEach((id, amount) -> {
            if (amount != 0) {
                present.put(id, amount);
            }
        });
        tenths = Map.copyOf(present);
    }

    /**
     * Turns grain densities into a measurement with a noise that is fixed per column.
     *
     * @param densities density from 0 to 1 by indicator id, in a fixed order
     * @param seed world seed
     * @param x world X in blocks
     * @param z world Z in blocks
     * @return the measured grains
     */
    public static Grains measure(Map<String, Double> densities, long seed, int x, int z) {
        SplitMix64 random = new SplitMix64(SplitMix64.hash(seed ^ SALT, x, z));
        Map<String, Integer> tenths = new HashMap<>();
        densities.forEach((id, density) -> tenths.put(id,
                (int) Math.round(density * PER_ITEM * (1.0 - NOISE + 2.0 * NOISE * random.nextDouble()))));
        return new Grains(tenths);
    }

    /**
     * Gives the amount of one mineral.
     *
     * @param id indicator id
     * @return tenths of grains, 0 if absent
     */
    public int get(String id) {
        return tenths.getOrDefault(id, 0);
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
        return each(amount -> (int) Math.round(amount * factor));
    }

    /**
     * Adds other grains.
     *
     * @param other grains to add
     * @return the sum per mineral
     */
    public Grains plus(Grains other) {
        Map<String, Integer> sum = new HashMap<>(tenths);
        other.tenths.forEach((id, amount) -> sum.merge(id, amount, Integer::sum));
        return new Grains(sum);
    }

    /**
     * Gives how many items these grains fill.
     *
     * @return number of whole items per mineral
     */
    public Grains whole() {
        return each(amount -> amount / PER_ITEM);
    }

    /**
     * Gives what stays in the pan once the whole items are taken out.
     *
     * @return the rest per mineral in tenths
     */
    public Grains rest() {
        return each(amount -> amount % PER_ITEM);
    }

    /**
     * Gives the amount of all minerals together.
     *
     * @return tenths of grains
     */
    public int total() {
        return tenths.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Tells whether there are no grains at all.
     *
     * @return true if every mineral is zero
     */
    public boolean isEmpty() {
        return tenths.isEmpty();
    }

    /**
     * Changes the amount of every mineral.
     *
     * @param change new amount from the old one
     * @return the changed grains
     */
    private Grains each(IntUnaryOperator change) {
        Map<String, Integer> changed = new HashMap<>();
        tenths.forEach((id, amount) -> changed.put(id, change.applyAsInt(amount)));
        return new Grains(changed);
    }
}
