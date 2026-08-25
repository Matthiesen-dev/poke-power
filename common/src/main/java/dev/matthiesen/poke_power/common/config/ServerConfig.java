package dev.matthiesen.poke_power.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {

    public ModConfigSpec.IntValue powerPerPokeLevel;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Server Config").push("server");

        powerPerPokeLevel = builder
                .comment("The amount of power gained per poke level")
                .defineInRange("powerPerPokeLevel", 50, 1, Integer.MAX_VALUE);

        builder.pop();
    }
}
