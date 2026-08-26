package dev.matthiesen.poke_power.common.util;

/**
 * Utility class for energy-related operations, such as converting energy values to human-readable strings.
 */
@SuppressWarnings("unused")
public final class EnergyUtil {
    private EnergyUtil() {}

    /**
     * Converts a long energy value into a human-readable string with appropriate suffixes.
     * For example, 1500 becomes "1.5k", 2_500_000 becomes "2.5M", and 3_000_000_000 becomes "3B".
     * @param energy The energy value to convert.
     * @return A human-readable string representation of the energy value.
     */
    public static String toEnergyString(long energy) {
        String parsed = parseEnergyValue(energy);
        return formatEnergyValue(parsed);
    }

    /**
     * Converts a long energy value into a human-readable string with appropriate suffixes and optionally includes the unit.
     * For example, 1500 becomes "1.5k FE" if includeUnit is true, or "1.5" if includeUnit is false.
     * @param energy The energy value to convert.
     * @param includeUnit Whether to include the unit "FE" in the returned string.
     * @return A human-readable string representation of the energy value, optionally including the unit.
     */
    public static String toEnergyString(long energy, boolean includeUnit) {
        String parsed = parseEnergyValue(energy);
        return formatEnergyValue(parsed) + (includeUnit ? " FE" : "");
    }

    /**
     * Parses a long energy value into a human-readable string with appropriate suffixes.
     * For example, 1500 becomes "1.5k", 2_500_000 becomes "2.5M", and 3_000_000_000 becomes "3B".
     * @param energy The energy value to parse.
     * @return A human-readable string representation of the energy value.
     */
    private static String parseEnergyValue(long energy) {
        if (energy < 1000) {
            return String.valueOf(energy);
        } else if (energy < 1_000_000) {
            return String.format("%.1fk", energy / 1000.0);
        } else if (energy < 1_000_000_000) {
            return String.format("%.1fM", energy / 1_000_000.0);
        } else {
            return String.format("%.1fB", energy / 1_000_000_000.0);
        }
    }

    /**
     * Removes the ".0" from the end of a parsed energy value if it exists.
     * For example, "1.0k" becomes "1k", "2.5M" remains "2.5M".
     * @param parsedValue The parsed energy value to format.
     * @return A formatted string representation of the energy value without unnecessary decimal points.
     */
    private static String formatEnergyValue(String parsedValue) {
        String substring = parsedValue.substring(0, parsedValue.length() - 3);
        if (parsedValue.endsWith(".0k")) {
            return substring + "k";
        } else if (parsedValue.endsWith(".0M")) {
            return substring + "M";
        } else if (parsedValue.endsWith(".0B")) {
            return substring + "B";
        }
        return parsedValue;
    }
}
