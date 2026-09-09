package dev.matthiesen.poke_power.common.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.matthiesen.poke_power.common.block.entity.PowerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public final class PowerBlockEntityRenderer implements BlockEntityRenderer<PowerBlockEntity> {
    @SuppressWarnings("unused")
    public PowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(PowerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        boolean isActive = blockEntity.isActive();
        if (isActive) {
            poseStack.pushPose();
            BlockPos blockPos = blockEntity.getBlockPos();
            Level level = blockEntity.getLevel();
            if (level != null) {
                float chargeScale = blockEntity.getRenderScale();
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(chargeScale, chargeScale, chargeScale);
                poseStack.translate(-0.5, -0.5, -0.5);

                int lightLevel = getLightLevel(level, blockPos);
                var state = blockEntity.getFakeBlockState();
                var model = blockRenderer.getBlockModel(state);
                var buffer = multiBufferSource.getBuffer(RenderType.translucent());

                blockRenderer.getModelRenderer().tesselateBlock(
                        level,
                        model,
                        state,
                        blockPos,
                        poseStack,
                        buffer,
                        false,
                        RandomSource.create(),
                        42L,
                        lightLevel
                );
            }
            poseStack.popPose();
        }
    }

    private int getLightLevel(Level level, BlockPos blockPos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, blockPos);
        int sLight = level.getBrightness(LightLayer.SKY, blockPos);
        return LightTexture.pack(bLight, sLight);
    }
}
