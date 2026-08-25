package dev.matthiesen.poke_power.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractMenuTypeRegistry;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.menu.PowerGeneratorMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class MenuRegistry extends AbstractMenuTypeRegistry {
    private static final MenuRegistry INSTANCE = new MenuRegistry();

    private MenuRegistry() {
        super(PokePowerCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<MenuType<PowerGeneratorMenu>> POWER_GENERATOR_MENU;

    static {
        POWER_GENERATOR_MENU = INSTANCE.register("power_generator", PowerGeneratorMenu::new);
    }
}
