package dev.matthiesen.poke_power.common.menu.container.data;

import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ContainerData implementation for the PowerBlockMenu.
 * This class handles the synchronization of energy, capacity, and generation values between the server and client.
 */
public final class ChargingContainerData implements ContainerData {
    private static final int DATA_ENERGY_LO   = 0;
    private static final int DATA_ENERGY_HI   = 1;
    private static final int DATA_CAPACITY_LO = 2;
    private static final int DATA_CAPACITY_HI = 3;
    private static final int DATA_GEN_LO      = 4;
    private static final int DATA_GEN_HI      = 5;
    private static final int DATA_COUNT       = 6;

    // Getters for cached values
    private final Supplier<Long> getCachedEnergy;
    private final Supplier<Long> getCachedCapacity;
    private final Supplier<Long> getCachedGen;

    // Setters for cached values
    private final Consumer<Long> setCachedEnergy;
    private final Consumer<Long> setCachedCapacity;
    private final Consumer<Long> setCachedGen;

    private final Supplier<Level> level;
    private final Supplier<BlockPos> blockPos;

    public ChargingContainerData(
        Supplier<Long> getCachedEnergy,
        Supplier<Long> getCachedCapacity,
        Supplier<Long> getCachedGen,
        Consumer<Long> setCachedEnergy,
        Consumer<Long> setCachedCapacity,
        Consumer<Long> setCachedGen,
        Supplier<Level> level,
        Supplier<BlockPos> blockPos
    ) {
        this.getCachedEnergy = getCachedEnergy;
        this.getCachedCapacity = getCachedCapacity;
        this.getCachedGen = getCachedGen;
        this.setCachedEnergy = setCachedEnergy;
        this.setCachedCapacity = setCachedCapacity;
        this.setCachedGen = setCachedGen;
        this.level = level;
        this.blockPos = blockPos;
    }

    /**
     * (Server Side) Get the energy, capacity, and generation values from the PowerBlockEntity at the given position in the given level.
     *
     * @param index The index of the value to get
     * @return The value of the requested data, or 0 if the index is invalid.
     */
    @Override
    public int get(int index) {
        // Server: read live from entity so broadcastChanges() detects changes each tick.
        // Client: return cached values written by set().
        long energy = getCachedEnergy.get();
        long capacity = getCachedCapacity.get();
        long generation = getCachedGen.get();

        Level lvl = level.get();
        BlockPos pos = blockPos.get();
        if (lvl != null && !lvl.isClientSide() && pos != null) {
            if (lvl.getBlockEntity(pos) instanceof PowerBlockEntity entity) {
                energy   = entity.getEnergyStorage().getEnergy();
                capacity = entity.getEnergyStorage().getCapacity();
                generation = entity.getActiveGenerationValue();
            }
        }

        return switch (index) {
            case DATA_ENERGY_LO   -> (int) (energy & 0xFFFFL);
            case DATA_ENERGY_HI   -> (int) (energy >> 16);
            case DATA_CAPACITY_LO -> (int) (capacity & 0xFFFFL);
            case DATA_CAPACITY_HI -> (int) (capacity >> 16);
            case DATA_GEN_LO      -> (int) (generation & 0xFFFFL);
            case DATA_GEN_HI      -> (int) (generation >> 16);
            default -> 0;
        };
    }

    /**
     * (Client Side) Set the cached energy, capacity, and generation values.
     *
     * @param index The index of the value to set
     * @param value The value to set
     */
    @Override
    public void set(int index, int value) {
        // Client-side: MC calls this when the server sends an update.
        // Each value arrives as a signed short; mask to 16 bits before reassembling.
        int bits = value & 0xFFFF;

        switch (index) {
            case DATA_ENERGY_LO   -> setCachedEnergy.accept((getCachedEnergy.get() & 0xFFFF0000L) | bits);
            case DATA_ENERGY_HI   -> setCachedEnergy.accept((getCachedEnergy.get() & 0x0000FFFFL) | ((long) bits << 16));
            case DATA_CAPACITY_LO -> setCachedCapacity.accept((getCachedCapacity.get() & 0xFFFF0000L) | bits);
            case DATA_CAPACITY_HI -> setCachedCapacity.accept((getCachedCapacity.get() & 0x0000FFFFL) | ((long) bits << 16));
            case DATA_GEN_LO      -> setCachedGen.accept((getCachedGen.get() & 0xFFFF0000L) | bits);
            case DATA_GEN_HI      -> setCachedGen.accept((getCachedGen.get() & 0x0000FFFFL) | ((long) bits << 16));
        }
    }

    @Override
    public int getCount() {
        return DATA_COUNT;
    }
}
