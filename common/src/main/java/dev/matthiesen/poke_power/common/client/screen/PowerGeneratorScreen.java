package dev.matthiesen.poke_power.common.client.screen;

import dev.matthiesen.matthiesen_core.common.utility.ui.screen.AbstractSimpleScreen;
import dev.matthiesen.poke_power.common.PokePowerCommon;
import dev.matthiesen.poke_power.common.menu.PowerGeneratorMenu;
import dev.matthiesen.poke_power.common.network.InsertPokemonPayload;
import dev.matthiesen.poke_power.common.network.RemovePokemonPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class PowerGeneratorScreen extends AbstractSimpleScreen<PowerGeneratorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PokePowerCommon.MOD_ID, "textures/gui/power_generator.png");

    // Energy bar position within the texture (local to leftPos/topPos)
    private static final int ENERGY_X = 154;
    private static final int ENERGY_Y = 22;
    private static final int ENERGY_W = 12; // interior width (bar itself, inside the 14px frame)
    private static final int ENERGY_H = 50; // interior height

    public PowerGeneratorScreen(PowerGeneratorMenu menu, Inventory inv, Component title) {
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
                PowerGeneratorMenu.GEN_SLOT_X0, 8, 0x404040, false);
        graphics.drawString(font,
                Component.translatable("container.poke_power.party_slots"),
                PowerGeneratorMenu.PARTY_SLOT_X0, 44, 0x404040, false);
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

                    if (slotIdx >= PowerGeneratorMenu.GEN_SLOT_START
                            && slotIdx < PowerGeneratorMenu.GEN_SLOT_START + PowerGeneratorMenu.GEN_SLOT_COUNT) {
                        // Click on a generator slot → request removal back to party
                        if (!hovered.getItem().isEmpty()) {
                            net.sendToServer(new RemovePokemonPayload(pos, slotIdx - PowerGeneratorMenu.GEN_SLOT_START));
                            return true;
                        }
                    } else if (slotIdx >= PowerGeneratorMenu.PARTY_SLOT_START
                            && slotIdx < PowerGeneratorMenu.PARTY_SLOT_START + PowerGeneratorMenu.PARTY_SLOT_COUNT) {
                        // Click on a party slot → request insertion into generator
                        if (!hovered.getItem().isEmpty()) {
                            net.sendToServer(new InsertPokemonPayload(pos, slotIdx - PowerGeneratorMenu.PARTY_SLOT_START));
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

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
        graphics.fill(barX, barYTop, barX + ENERGY_W, barYBottom, 0xFF00BFFF);
    }

}
