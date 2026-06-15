package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityResonanceFurnace;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiResonanceFurnace extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/gui/resonance_furnace.png"
    );

    private static final int COLOR_TITLE  = 0x404040;
    private static final int COLOR_LABEL  = 0x666666;
    private static final int COLOR_ETHER  = 0x7B2FBE;
    private static final int COLOR_HOT    = 0xFF4400;

    private final TileEntityResonanceFurnace furnace;

    public GuiResonanceFurnace(InventoryPlayer playerInv,
                               TileEntityResonanceFurnace furnace) {
        super(new ContainerResonanceFurnace(playerInv, furnace));
        this.furnace = furnace;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(TEXTURE);

        int x = (width  - xSize) / 2;
        int y = (height - ySize) / 2;

        // Основной фон
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // ── Стрелка прогресса ────────────────────────
        int arrowWidth = furnace.getSmeltProgressScaled(24);
        if (arrowWidth > 0) {
            drawTexturedModalRect(
                    x + 78, y + 31,
                    176, 0,
                    arrowWidth+9, 16
            );
        }

        // ── Полоска эфира ────────────────────────────
        int etherHeight = furnace.getEtherScaled(52);
        if (etherHeight > 0) {
            drawTexturedModalRect(
                    x + 8, y + 18 + (52 - etherHeight),
                    208, 55 - etherHeight,
                    12, etherHeight
            );
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        // ── Заголовок ────────────────────────────────
        String title = "Resonance Furnace";
        fontRenderer.drawString(
                title,
                xSize / 2 - fontRenderer.getStringWidth(title) / 2,
                6,
                COLOR_TITLE
        );

        // ── Статистика — левый блок ───────────────────
        // Начинаем ПОСЛЕ полоски эфира (полоска x=7..20)
        // Статистика начинается с x=26
        int statX  = 26;
        int startY = 18;
        int lineH  = 10;

        // Метка полоски
        fontRenderer.drawString(
                "Ether",
                6, 8,
                COLOR_LABEL
        );

        // Stored
        fontRenderer.drawString("Stored:", statX, startY+2, COLOR_LABEL);
        fontRenderer.drawString(
                furnace.getEtherStored() + " AE",
                statX + 42, startY+2,
                COLOR_ETHER
        );

        // Cost
        fontRenderer.drawString(
                "Cost:", statX, startY+20 + lineH*2, COLOR_LABEL
        );
        fontRenderer.drawString(
                TileEntityResonanceFurnace.ETHER_PER_SMELT + " AE",
                statX+30, startY+20 + lineH*2,
                COLOR_HOT
        );

        // Лунный множитель
        String moonText;
        int moonColor;

        if (furnace.isFullMoon()) {
            moonText  = "Full Moon! x2";
            moonColor = 0xAA44FF;
        } else if (furnace.getMoonMultiplier() > 1.0f) {
            // Форматируем float до 1 знака после запятой
            moonText  = "Night x" + String.format("%.1f", furnace.getMoonMultiplier());
            moonColor = 0x8866CC;
        } else {
            moonText  = "Day x1.0";
            moonColor = COLOR_LABEL;
        }

        fontRenderer.drawString("Moon:", statX+65, startY + 20 + lineH * 2, COLOR_LABEL);
        fontRenderer.drawString(moonText, statX + 95, startY + 20 + lineH * 2, moonColor);

        // ── Инвентарь ─────────────────────────────────
        fontRenderer.drawString(
                "Inventory",
                8, 72,
                COLOR_TITLE
        );
    }
}