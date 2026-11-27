package dev.matthiesen.poke_power.common.energy;

import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.CableBlock;
import dev.matthiesen.poke_power.common.block.entity.CableBlockEntity;
import dev.matthiesen.poke_power.common.defs.BlockChangeCallback;
import dev.matthiesen.poke_power.common.defs.NetworkTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * The EnergyNetworkTransfer class is responsible for managing energy transfer in a network of cables and consumers.
 * It performs a breadth-first search (BFS) to identify connected cables and reachable consumers, transfers energy to
 * consumers, and equalizes energy across the network.
 */
public final class EnergyNetworkTransfer {
    private final Set<BlockPos> visitedCables = new HashSet<>();
    private final Set<BlockPos> visitedConsumers = new HashSet<>();
    private final List<CableBlockEntity> networkCables = new ArrayList<>();
    private final List<NetworkTarget> consumers = new ArrayList<>();
    private final Deque<BlockPos> queue = new ArrayDeque<>();

    private final Level level;
    private final BlockPos blockPos;
    private final BlockState blockState;
    private long maxTransfer;
    private final CableBlockEntity cable;
    private final BlockChangeCallback blockChangeCallback;

    /**
     * Constructs an EnergyNetworkTransfer instance for managing energy transfer in a network of cables and consumers.
     *
     * @param level               The level in which the energy transfer is taking place.
     * @param blockPos            The position of the cable initiating the transfer.
     * @param blockState          The state of the cable block at the initiating position.
     * @param maxTransfer         The maximum amount of energy to transfer in this operation.
     * @param cable               The CableBlockEntity initiating the transfer.
     * @param blockChangeCallback A callback function to be invoked when a block change occurs during the transfer process.
     */
    public EnergyNetworkTransfer(
            Level level,
            BlockPos blockPos,
            BlockState blockState,
            long maxTransfer,
            CableBlockEntity cable,
            BlockChangeCallback blockChangeCallback
    ) {
        this.level = level;
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.maxTransfer = maxTransfer;
        this.cable = cable;
        this.blockChangeCallback = blockChangeCallback;
    }

    /**
     * Runs the energy network transfer process, which includes polling the network for connected cables and consumers,
     * transferring energy to consumers, and equalizing energy across the network.
     */
    public void run() {
        visitedCables.add(blockPos);
        queue.add(blockPos);

        pollNetwork();
        transferEnergy();
        equalizeEnergy();
    }

    /**
     * Polls the network for connected cables and reachable consumers using a breadth-first search (BFS) algorithm.
     */
    private void pollNetwork() {
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
                    consumers.add(new NetworkTarget(neighbor, dir.getOpposite()));
                }
            }
        }
    }

    /**
     * Transfers energy to all reachable consumers in the network.
     */
    private void transferEnergy() {
        for (NetworkTarget consumer : consumers) {
            if (maxTransfer <= 0) break;
            long pushed = PokePowerCommon.POWER_TOOLS.pushEnergyTo(maxTransfer, level, consumer.pos(), consumer.fromSide());
            if (pushed > 0) {
                cable.getEnergyStorage().setEnergy(cable.getEnergyStorage().getEnergy() - pushed);
                maxTransfer -= pushed;
            }
        }
    }

    /**
     * Equalizes the remaining energy across all cables in the network.
     */
    private void equalizeEnergy() {
        if (networkCables.size() > 1) {
            long totalEnergy = 0;
            for (CableBlockEntity cableBE : networkCables) {
                totalEnergy += cableBE.getEnergyStorage().getEnergy();
            }
            long share = totalEnergy / networkCables.size();
            long leftover = totalEnergy % networkCables.size();
            for (int i = 0; i < networkCables.size(); i++) {
                networkCables.get(i).getEnergyStorage().setEnergy(share + (i < leftover ? 1 : 0));
                blockChangeCallback.accept(level, networkCables.get(i).getBlockPos(), networkCables.get(i).getBlockState());
            }
        } else {
            blockChangeCallback.accept(level, blockPos, blockState);
        }
    }

}
