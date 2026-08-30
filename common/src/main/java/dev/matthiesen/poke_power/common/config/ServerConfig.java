package dev.matthiesen.poke_power.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {

    public ModConfigSpec.IntValue powerPerPokeLevel;

    public ModConfigSpec.LongValue blocks_powerBlock_capacity;
    public ModConfigSpec.LongValue blocks_powerMax_extract;
    public ModConfigSpec.LongValue blocks_cable_capacity;
    public ModConfigSpec.LongValue blocks_cable_maxExtract;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Server Config").push("server");

        powerPerPokeLevel = builder
                .comment("The amount of power gained per poke level")
                .defineInRange("powerPerPokeLevel", 50, 1, Integer.MAX_VALUE);

        builder.comment("Blocks Config").push("blocks");

        builder.comment("Power Block Config").push("powerBlock");
        blocks_powerBlock_capacity = builder
                .comment("The amount of power that a power block can store")
                .defineInRange("powerBlockCapacity", 120000L, 1L, Long.MAX_VALUE);
        blocks_powerMax_extract = builder
                .comment("The maximum amount of power that can be extracted from a power block per tick")
                .defineInRange("powerMaxExtract", 16000L, 1L, Long.MAX_VALUE);
        builder.pop();

        builder.comment("Cable Config").push("cable");
        blocks_cable_capacity = builder
                .comment("The amount of power that a cable can store")
                .defineInRange("cableCapacity", 32000L, 1L, Long.MAX_VALUE);
        blocks_cable_maxExtract = builder
                .comment("The maximum amount of power that can be extracted from a cable per tick")
                .defineInRange("cableMaxExtract", 16000L, 1L, Long.MAX_VALUE);
        builder.pop();

        builder.pop();

        builder.pop();
    }
}
