package dev.matthiesen.poke_power.common.registry;

import dev.matthiesen.matthiesen_core.common.registry.AbstractItemRegistry;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.item.CableBlockItem;
import dev.matthiesen.poke_power.common.item.PowerBlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public final class ItemRegistry extends AbstractItemRegistry {
    private static final ItemRegistry INSTANCE = new ItemRegistry();

    private ItemRegistry() {
        super(PokePowerCommon.MOD_ID);
    }

    public static void init() {}

    public static final Supplier<PowerBlockItem> POWER_BLOCK;
    public static final Supplier<CableBlockItem> POWER_CABLE;

    static {
        POWER_BLOCK = INSTANCE.register("power_block", PowerBlockItem::new);
        POWER_CABLE = INSTANCE.register("power_cable", CableBlockItem::new);
    }

    public static Supplier<ItemStack> getCreativeModeTabIcon() {
        return () -> new ItemStack(POWER_BLOCK.get());
    }

    public static Supplier<List<ItemStack>> getCreativeModeTabItems() {
        return () -> List.of(new ItemStack(POWER_BLOCK.get()), new ItemStack(POWER_CABLE.get()));
    }
}
