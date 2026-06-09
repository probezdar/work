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
                    x + 79, y + 34,
                    176, 0,
                    arrowWidth, 16
            );
        }

        // ── Полоска эфира ────────────────────────────
        int etherHeight = furnace.getEtherScaled(52);
        if (etherHeight > 0) {
            drawTexturedModalRect(
                    x + 8, y + 18 + (52 - etherHeight),
                    200, 52 - etherHeight,
                    12, etherHeight
            );
        }

        // ── Анимация огня если плавит ─────────────────
        if (furnace.isSmelting()) {
            int fireHeight = 14;
            drawTexturedModalRect(
                    x + 56, y + 20,
                    176, 16,
                    14, fireHeight
            );
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        // Заголовок
        String title = "Resonance Furnace";
        fontRenderer.drawString(
                title,
                xSize / 2 - fontRenderer.getStringWidth(title) / 2,
                6, COLOR_TITLE
        );

        // ── Статистика слева ─────────────────────────
        int statX  = 26;
        int startY = 18;
        int lineH  = 9;

        fontRenderer.drawString("Ether", 8, 12, COLOR_LABEL);

        // Эфир
        fontRenderer.drawString("Stored:", statX, startY, COLOR_LABEL);
        fontRenderer.drawString(
                furnace.getEtherStored() + " AE",
                statX + 38, startY, COLOR_ETHER
        );

        // Стоимость
        fontRenderer.drawString("Cost:", statX, startY + lineH, COLOR_LABEL);
        fontRenderer.drawString(
                TileEntityResonanceFurnace.ETHER_PER_SMELT + " AE",
                statX + 38, startY + lineH, COLOR_HOT
        );

        // Статус
        fontRenderer.drawString("Status:", statX, startY + lineH * 2, COLOR_LABEL);
        String status = furnace.isSmelting() ? "Smelting..." : "Idle";
        int statusColor = furnace.isSmelting() ? 0x00AA00 : 0xAAAAAA;
        fontRenderer.drawString(
                status,
                statX + 38, startY + lineH * 2, statusColor
        );

        // Подписи слотов
        fontRenderer.drawString("In",      52, 25, COLOR_LABEL);
        fontRenderer.drawString("Out",    112, 25, COLOR_LABEL);
        fontRenderer.drawString("Bonus",  108, 55, COLOR_LABEL);

        // Подпись инвентаря
        fontRenderer.drawString("Inventory", 8, 74, COLOR_TITLE);
    }
}