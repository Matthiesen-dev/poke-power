package dev.matthiesen.poke_power.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class PokePowerConfig {
    public static final ServerConfig SERVER_CONFIG;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> serverSpecPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG = serverSpecPair.getLeft();
        SERVER_SPEC = serverSpecPair.getRight();
    }
}
