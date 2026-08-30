package dev.matthiesen.poke_power.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {

    // Blocks Config

    //// Power Block Config
    public ModConfigSpec.LongValue blocks_powerBlock_capacity;
    public ModConfigSpec.LongValue blocks_powerBlock_maxExtract;
    public ModConfigSpec.IntValue blocks_powerBlock_powerPerPokeLevel;

    //// Cable Config
    public ModConfigSpec.LongValue blocks_cable_capacity;
    public ModConfigSpec.LongValue blocks_cable_maxExtract;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Blocks Config")
                .translation("poke_power.configuration.server.blocks")
                .push("blocks");

        builder.comment("Power Block Config")
                .translation("poke_power.configuration.server.blocks.powerBlock")
                .push("powerBlock");
        blocks_powerBlock_powerPerPokeLevel = builder
                .comment("The amount of power gained per poke level")
                .translation("poke_power.configuration.server.blocks.powerBlock.powerPerPokeLevel")
                .defineInRange("powerPerPokeLevel", 50, 1, Integer.MAX_VALUE);
        blocks_powerBlock_capacity = builder
                .comment("The amount of power that a power block can store")
                .translation("poke_power.configuration.server.blocks.powerBlock.capacity")
                .defineInRange("capacity", 120000L, 1L, Long.MAX_VALUE);
        blocks_powerBlock_maxExtract = builder
                .comment("The maximum amount of power that can be extracted from a power block per tick")
                .translation("poke_power.configuration.server.blocks.powerBlock.maxExtract")
                .defineInRange("maxExtract", 16000L, 1L, Long.MAX_VALUE);
        builder.pop();

        builder.comment("Cable Config")
                .translation("poke_power.configuration.server.blocks.cable")
                .push("cable");
        blocks_cable_capacity = builder
                .comment("The amount of power that a cable can store")
                .translation("poke_power.configuration.server.blocks.cable.capacity")
                .defineInRange("capacity", 32000L, 1L, Long.MAX_VALUE);
        blocks_cable_maxExtract = builder
                .comment("The maximum amount of power that can be extracted from a cable per tick")
                .translation("poke_power.configuration.server.blocks.cable.maxExtract")
                .defineInRange("maxExtract", 16000L, 1L, Long.MAX_VALUE);
        builder.pop();

        builder.pop();
    }
}
