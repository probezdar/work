package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityEtherCondenser;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiEtherCondenser extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/gui/condenser.png"
    );

    // Цвета
    private static final int COLOR_TITLE   = 0x404040;
    private static final int COLOR_LABEL   = 0x666666;
    private static final int COLOR_ETHER   = 0x7B2FBE;
    private static final int COLOR_RATE    = 0x2E86AB;
    private static final int COLOR_NIGHT   = 0xAA44FF;
    private static final int COLOR_NIGHT_V = 0xFF80FF;

    private final TileEntityEtherCondenser condenser;

    public GuiEtherCondenser(InventoryPlayer playerInv,
                             TileEntityEtherCondenser condenser) {
        super(new ContainerEtherCondenser(playerInv, condenser));
        this.condenser = condenser;
        this.xSize = 176;
        this.ySize = 222;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(TEXTURE);

        int x = (width  - xSize) / 2;
        int y = (height - ySize) / 2;

        // Основной фон
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // Полоска эфира (вертикальная, слева)
        int barHeight = 0;
        if (condenser.getMaxEther() > 0) {
            barHeight = condenser.getEtherStored() * 60
                    / condenser.getMaxEther();
        }
// Ограничиваем
        barHeight = Math.min(barHeight, 60);

        if (barHeight > 0) {
            drawTexturedModalRect(
                    x + 8,                    // X полоски на экране
                    y + 19 + (60 - barHeight),// Y — растём снизу вверх
                    176,                      // U в текстуре
                    60 - barHeight,           // V в текстуре
                    12,                       // ширина
                    barHeight                 // высота
            );
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        // ── Заголовок по центру ──────────────────────
        String title = "Ether Condenser";
        fontRenderer.drawString(
                title,
                xSize / 2 - fontRenderer.getStringWidth(title) / 2,
                6,
                COLOR_TITLE
        );

        // ── Статистика слева (отступ 26px — после полоски) ──
        int statX  = 26; // начало текста по X
        int startY = 18; // начало по Y
        int lineH  = 10; // высота строки

        // Метка полоски
        fontRenderer.drawString("Ether", 6, 6, COLOR_LABEL);

        // Хранилище
        fontRenderer.drawString(
                "Stored:",
                statX, startY,
                COLOR_LABEL
        );
        fontRenderer.drawString(
                condenser.getEtherStored() + " AE",
                statX + 40, startY,
                COLOR_ETHER
        );

        // Максимум
        fontRenderer.drawString(
                "Max:",
                statX, startY + lineH,
                COLOR_LABEL
        );
        fontRenderer.drawString(
                condenser.getMaxEther() + " AE",
                statX + 40, startY + lineH,
                COLOR_ETHER
        );

        // Заполнение
        fontRenderer.drawString(
                "Fill:",
                statX, startY + lineH * 2,
                COLOR_LABEL
        );
        fontRenderer.drawString(
                condenser.getFillPercentage() + "%",
                statX + 40, startY + lineH * 2,
                COLOR_ETHER
        );

        // Скорость
        int rate = condenser.getEffectiveRate();
        boolean isNight = rate > condenser.getEtherPerTick();

        fontRenderer.drawString(
                "Rate:",
                statX, startY + lineH * 3,
                COLOR_LABEL
        );
        fontRenderer.drawString(
                "+" + rate + " AE/s",
                statX + 40, startY + lineH * 3,
                isNight ? COLOR_NIGHT : COLOR_RATE
        );
        // Ночной бонус
        fontRenderer.drawString(
                "Bonus:",
                statX, startY + lineH * 4,
                COLOR_LABEL
        );
        fontRenderer.drawString(
                isNight ? "x2 Night!" : "x1 Day",
                statX + 40, startY + lineH * 4,
                isNight ? COLOR_NIGHT_V : COLOR_LABEL
        );

        // Разделитель
        drawHorizontalLine(25, xSize - 9, startY + lineH * 5 + 4, 0xFFAAAAAA);

        // Подсказка под разделителем
        String hint = "    Place Ether Blocks nearby!";
        fontRenderer.drawString(
                hint,
                xSize / 2 - fontRenderer.getStringWidth(hint) / 2,
                startY + lineH * 5 + 8,
                COLOR_LABEL
        );

        // Инвентарь
        fontRenderer.drawString("Inventory", 8, 128, COLOR_TITLE);
    }
}