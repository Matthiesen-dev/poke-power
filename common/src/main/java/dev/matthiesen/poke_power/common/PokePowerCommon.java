package dev.matthiesen.poke_power.common;

import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.poke_power.common.platform.PowerTools;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import dev.matthiesen.poke_power.common.registry.BlockRegistry;
import dev.matthiesen.poke_power.common.registry.CreativeModeTabRegistry;
import dev.matthiesen.poke_power.common.registry.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public final class PokePowerCommon extends AbstractCommonMod {
    public static final String MOD_ID = "poke_power";
    public static final String MOD_NAME = "Cobblemon Poke Power";
    public static @Token final String METRICS_TOKEN = "7a042fa271e265078aacb04f4e79e1d9";
    public static final PokePowerCommon INSTANCE = new PokePowerCommon();

    public static final PowerTools POWER_TOOLS = ServiceLoader.load(PowerTools.class).findFirst().orElseThrow();

    public static ResourceLocation modResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public PokePowerCommon() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    public void initialize() {
        super.initialize();

        BlockRegistry.init();
        BlockEntityRegistry.init();
        ItemRegistry.init();
        CreativeModeTabRegistry.init();
        // MenuRegistry.init();
        // NetworkRegistry.init();

        createInfoLog("Initialized");
    }
}
