package dev.matthiesen.poke_power.common.block;

import dev.matthiesen.matthiesen_core.common.utility.ui.menu.MenuProvider;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import dev.matthiesen.poke_power.common.menu.PowerGeneratorMenu;
import dev.matthiesen.poke_power.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PowerBlock extends Block implements EntityBlock {
    public PowerBlock() {
        super(
                BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .strength(4f)
                        .requiresCorrectToolForDrops()
        );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof PowerBlockEntity entity) {
                MenuProvider.openMenu(serverPlayer,
                        MenuProvider.createProvider(
                                (containerId, inv, p) -> new PowerGeneratorMenu(containerId, inv, pos, entity),
                                Component.translatable("container.poke_power.power_generator")
                        )
                );
                // Sync current generator + party state to the client
                if (serverPlayer.containerMenu instanceof PowerGeneratorMenu genMenu) {
                    PokePowerCommon.INSTANCE.getNetworkingManager()
                            .sendToPlayer(serverPlayer, entity.buildSyncPacket(serverPlayer, genMenu.containerId));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.POWER_BLOCK_BE.get().create(blockPos, blockState);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof PowerBlockEntity entity) {
            entity.returnStoredPokemonToOwners();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        if (blockEntityType.equals(BlockEntityRegistry.POWER_BLOCK_BE.get())) {
            return PowerBlockEntity::tick;
        }
        return null;
    }
}
