package dev.matthiesen.poke_power.common.block.entity;

import dev.matthiesen.matthiesen_core.common.api.energy.AbstractCommonEnergyStorage;
import dev.matthiesen.matthiesen_core.common.api.energy.AbstractEnergyBlockEntity;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.CableBlock;
import dev.matthiesen.poke_power.common.energy.PokeEnergyCable;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CableBlockEntity extends AbstractEnergyBlockEntity {

    private final PokeEnergyCable energyStorage = new PokeEnergyCable(32000L, 16000L);

    public CableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.POWER_CABLE_BE.get(), blockPos, blockState);
    }

    @Override
    public AbstractCommonEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        if (!(blockEntity instanceof CableBlockEntity cable)) return;
        if (level.isClientSide) return;

        long available = cable.energyStorage.getEnergy();
        if (available <= 0) {
            setChanged(level, blockPos, blockState);
            return;
        }

        long maxTransfer = Math.min(available, cable.energyStorage.getMaxExtract());

        // BFS through the connected cable network to collect all cables and reachable
        // energy consumers. This avoids tick-order problems: a cable with energy can route
        // it to any consumer in the network in a single tick.
        Set<BlockPos> visitedCables = new HashSet<>();
        Set<BlockPos> visitedConsumers = new HashSet<>();
        List<CableBlockEntity> networkCables = new ArrayList<>();
        List<ConsumerTarget> consumers = new ArrayList<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        visitedCables.add(blockPos);
        queue.add(blockPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (level.getBlockEntity(current) instanceof CableBlockEntity cableBE) {
                networkCables.add(cableBE);
            }
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (visitedCables.contains(neighbor)) continue;

                if (level.getBlockState(neighbor).getBlock() instanceof CableBlock) {
                    visitedCables.add(neighbor);
                    queue.add(neighbor);
                } else if (visitedConsumers.add(neighbor)
                        && PokePowerCommon.POWER_TOOLS.supportsEnergyTransfer(level, current, dir)) {
                    consumers.add(new ConsumerTarget(neighbor, dir.getOpposite()));
                }
            }
        }

        // Push energy directly to each consumer found in the network
        for (ConsumerTarget consumer : consumers) {
            if (maxTransfer <= 0) break;
            long pushed = PokePowerCommon.POWER_TOOLS.pushEnergyTo(maxTransfer, level, consumer.pos(), consumer.fromSide());
            if (pushed > 0) {
                cable.energyStorage.setEnergy(cable.energyStorage.getEnergy() - pushed);
                maxTransfer -= pushed;
            }
        }

        // Equalize remaining energy across all cables in the network so every cable
        // holds power for Fabric/NeoForge energy API compatibility.
        if (networkCables.size() > 1) {
            long totalEnergy = 0;
            for (CableBlockEntity cableBE : networkCables) {
                totalEnergy += cableBE.energyStorage.getEnergy();
            }
            long share = totalEnergy / networkCables.size();
            long leftover = totalEnergy % networkCables.size();
            for (int i = 0; i < networkCables.size(); i++) {
                networkCables.get(i).energyStorage.setEnergy(share + (i < leftover ? 1 : 0));
                setChanged(level, networkCables.get(i).getBlockPos(), networkCables.get(i).getBlockState());
            }
        } else {
            setChanged(level, blockPos, blockState);
        }
    }

    private record ConsumerTarget(BlockPos pos, Direction fromSide) {}

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
    }
}

