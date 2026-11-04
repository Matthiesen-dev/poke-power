package dev.matthiesen.poke_power.common.util;

import dev.matthiesen.matthiesen_core.common.utility.EnergyUtilities;

public final class EnergyUtils {
    public static final long MIN_FORMATTABLE_ENERGY = 1L;

    public static String formatEnergyValueSafe(long value) {
        if (value < MIN_FORMATTABLE_ENERGY) return "0";
        // EnergyUtilities.formatEnergyValue throws on strings shorter than 3 chars (values < 100)
        if (value < 1000) return String.valueOf(value);
        return EnergyUtilities.toParsedString(value);
    }
}
