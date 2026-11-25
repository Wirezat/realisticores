package de.wirezat.realisticores.indicator;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/** Colour of the pyrope in a wash, which tells how rich in diamonds its pipe is. */
public enum PyropeTone implements StringRepresentable {
    /** Too little pyrope to tell. */
    PALE,
    /** Barren pipe. */
    BROWN,
    /** Poor pipe. */
    RED,
    /** Rich pipe. */
    VIOLET;

    public static final Codec<PyropeTone> CODEC = StringRepresentable.fromEnum(PyropeTone::values);
    public static final StreamCodec<ByteBuf, PyropeTone> STREAM_CODEC =
            ByteBufCodecs.idMapper(index -> values()[Math.floorMod(index, values().length)], PyropeTone::ordinal);

    private static final int MIN_PYROPE = 50;

    /**
     * Gives the tone of a wash.
     *
     * @param pipeFactor grade of the pipe, 0 for barren and 1 for rich pipes
     * @param pyrope measured pyrope in tenths
     * @return the tone, pale below 5 g of pyrope
     */
    public static PyropeTone of(double pipeFactor, int pyrope) {
        if (pyrope < MIN_PYROPE) {
            return PALE;
        }
        return pipeFactor == 0.0 ? BROWN : pipeFactor < 1.0 ? RED : VIOLET;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
