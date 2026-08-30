package dev.matthiesen.poke_power.common.client.screen;

import dev.matthiesen.matthiesen_core.common.utility.EnergyUtilities;
import dev.matthiesen.matthiesen_core.common.utility.ui.screen.AbstractSimpleScreen;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.menu.PowerBlockMenu;
import dev.matthiesen.poke_power.common.network.InsertPokemonPayload;
import dev.matthiesen.poke_power.common.network.RemovePokemonPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public final class PowerBlockScreen extends AbstractSimpleScreen<PowerBlockMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PokePowerCommon.MOD_ID, "textures/gui/power_generator.png");
    private static final ResourceLocation ENERGY_BAR_TEXTURE =
            new ResourceLocation(PokePowerCommon.MOD_ID, "textures/gui/power_generator_energy_bar.png");

    // Energy bar position within the texture (local to leftPos/topPos)
    private static final int ENERGY_X = 154;
    private static final int ENERGY_Y = 22;
    private static final int ENERGY_W = 12; // interior width (bar itself, inside the 14px frame)
    private static final int ENERGY_H = 50; // interior height

    public PowerBlockScreen(PowerBlockMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected int getBgWidth() {
        return 176;
    }

    @Override
    protected int getBgHeight() {
        return 186;
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        renderEnergyBar(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        // Section headers drawn in screen-local coords (relative to leftPos/topPos)
        graphics.drawString(font,
                Component.translatable("container.poke_power.generator_slots"),
                PowerBlockMenu.GEN_SLOT_X0 + 2, 11, 0x404040, false);
        graphics.drawString(font,
                Component.translatable("container.poke_power.party_slots"),
                PowerBlockMenu.PARTY_SLOT_X0 + 2, 49, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Slot hovered = this.hoveredSlot;
            if (hovered != null) {
                int slotIdx = hovered.index;
                BlockPos pos = getMenu().getBlockPos();
                if (pos != null) {
                    var net = PokePowerCommon.INSTANCE.getNetworkingManager();

                    if (slotIdx >= PowerBlockMenu.GEN_SLOT_START
                            && slotIdx < PowerBlockMenu.GEN_SLOT_START + PowerBlockMenu.GEN_SLOT_COUNT) {
                        // Click on a generator slot → request removal back to party
                        if (!hovered.getItem().isEmpty()) {
                            net.sendToServer(new RemovePokemonPayload(pos, slotIdx - PowerBlockMenu.GEN_SLOT_START));
                            return true;
                        }
                    } else if (slotIdx >= PowerBlockMenu.PARTY_SLOT_START
                            && slotIdx < PowerBlockMenu.PARTY_SLOT_START + PowerBlockMenu.PARTY_SLOT_COUNT) {
                        // Click on a party slot → request insertion into generator
                        if (!hovered.getItem().isEmpty()) {
                            net.sendToServer(new InsertPokemonPayload(pos, slotIdx - PowerBlockMenu.PARTY_SLOT_START));
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private boolean isMouseOverEnergyBar(int mouseX, int mouseY) {
        int barLeft = leftPos + ENERGY_X;
        int barTop  = topPos  + ENERGY_Y;
        return mouseX >= barLeft && mouseX < barLeft + ENERGY_W + 2
            && mouseY >= barTop  && mouseY < barTop  + ENERGY_H + 2;
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (isMouseOverEnergyBar(mouseX, mouseY)) {
            long energy   = getMenu().getSyncedEnergy();
            long capacity = getMenu().getSyncedCapacity();
            int  pct      = capacity > 0 ? (int) (energy * 100 / capacity) : 0;
            ChatFormatting pctColor = pct < 25 ? ChatFormatting.RED : (pct < 75 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("tooltip.poke_power.energy_bar.title").withStyle(style -> style.withBold(true).withColor(ChatFormatting.AQUA)),

                    Component.translatable("tooltip.poke_power.energy_bar.capacity")
                            .withStyle(style -> style.withBold(true).withColor(pctColor))
                            .append(Component.translatable("tooltip.poke_power.energy_bar.value", EnergyUtilities.toParsedString(capacity))
                                    .withStyle(style -> style.withBold(false).withColor(ChatFormatting.YELLOW))
                            ),

                    Component.translatable("tooltip.poke_power.energy_bar.stored")
                            .withStyle(style -> style.withBold(true).withColor(pctColor))
                            .append(Component.translatable("tooltip.poke_power.energy_bar.value", EnergyUtilities.toParsedString(energy))
                                    .withStyle(style -> style.withBold(false).withColor(ChatFormatting.YELLOW))
                            )
            ), mouseX, mouseY);
        }
    }

    private void renderEnergyBar(GuiGraphics graphics) {
        long energy   = getMenu().getSyncedEnergy();
        long capacity = getMenu().getSyncedCapacity();
        if (capacity <= 0) return;

        int filledH = (int) (ENERGY_H * energy / capacity);
        if (filledH <= 0) return;

        int barX      = leftPos + ENERGY_X + 1;
        int barYBottom = topPos + ENERGY_Y + 1 + ENERGY_H;
        int barYTop    = barYBottom - filledH;
        // Fill from the bottom up (like a fuel gauge)
        graphics.blit(ENERGY_BAR_TEXTURE, barX, barYTop, 0, ENERGY_H - filledH, ENERGY_W, filledH, ENERGY_W, ENERGY_H);
    }

}
