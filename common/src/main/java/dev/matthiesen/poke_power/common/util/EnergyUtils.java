package dev.matthiesen.poke_power.common.util;

import dev.matthiesen.matthiesen_core.common.utility.EnergyUtilities;

public final class EnergyUtils {
    public static final long MIN_FORMATTABLE_ENERGY = 1L;

    public static String formatEnergyValueSafe(long value) {
        return value < MIN_FORMATTABLE_ENERGY ? "0" : EnergyUtilities.toParsedString(value);
    }
}
