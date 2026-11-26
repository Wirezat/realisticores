package de.wirezat.realisticores.config;

import java.util.function.Supplier;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Defines config values with a comment and a range and hands them out as plain suppliers. */
public final class ConfigValues {
    private ConfigValues() {
    }

    /**
     * Defines a whole number.
     *
     * @param builder builder positioned in the section of the value
     * @param name key in the config file
     * @param value default value
     * @param min smallest allowed value
     * @param max largest allowed value
     * @param comment explanation written above the key
     * @return reader of the current value
     */
    public static Supplier<Integer> integer(ModConfigSpec.Builder builder, String name, int value, int min, int max,
            String comment) {
        ModConfigSpec.IntValue config = builder.comment(comment).defineInRange(name, value, min, max);
        return config::get;
    }

    /**
     * Defines a decimal number.
     *
     * @param builder builder positioned in the section of the value
     * @param name key in the config file
     * @param value default value
     * @param min smallest allowed value
     * @param max largest allowed value
     * @param comment explanation written above the key
     * @return reader of the current value
     */
    public static Supplier<Double> decimal(ModConfigSpec.Builder builder, String name, double value, double min,
            double max, String comment) {
        ModConfigSpec.DoubleValue config = builder.comment(comment).defineInRange(name, value, min, max);
        return config::get;
    }

    /**
     * Defines a switch.
     *
     * @param builder builder positioned in the section of the value
     * @param name key in the config file
     * @param value default value
     * @param comment explanation written above the key
     * @return reader of the current value
     */
    public static Supplier<Boolean> flag(ModConfigSpec.Builder builder, String name, boolean value, String comment) {
        ModConfigSpec.BooleanValue config = builder.comment(comment).define(name, value);
        return config::get;
    }

    /**
     * Turns an id with underscores into the camel case used for config keys.
     *
     * @param id id such as {@code sal_ammoniac}
     * @return key part such as {@code salAmmoniac}
     */
    public static String camelCase(String id) {
        StringBuilder name = new StringBuilder();
        for (String part : id.split("_")) {
            name.append(name.isEmpty() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1));
        }
        return name.toString();
    }
}
