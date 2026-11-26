package de.wirezat.realisticores.indicator;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/** Colour of the toned indicator in a wash, which tells how rich the deposit of its grains is. */
public enum GradeTone implements StringRepresentable {
    /** Too few grains to tell. */
    PALE,
    /** Barren deposit. */
    BROWN,
    /** Poor deposit. */
    RED,
    /** Rich deposit. */
    VIOLET;

    public static final Codec<GradeTone> CODEC = StringRepresentable.fromEnum(GradeTone::values);
    public static final StreamCodec<ByteBuf, GradeTone> STREAM_CODEC =
            ByteBufCodecs.idMapper(index -> values()[Math.floorMod(index, values().length)], GradeTone::ordinal);

    private static final int MIN_GRAINS = 50;

    /**
     * Gives the tone of a wash.
     *
     * @param grade grade of the deposit, 0 for barren and 1 for rich deposits
     * @param grains measured grains of the toned indicator in tenths
     * @return the tone, pale below 5 g of grains
     */
    public static GradeTone of(double grade, int grains) {
        if (grains < MIN_GRAINS) {
            return PALE;
        }
        return grade == 0.0 ? BROWN : grade < 1.0 ? RED : VIOLET;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
