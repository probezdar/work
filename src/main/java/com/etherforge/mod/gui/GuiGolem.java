package com.etherforge.mod.gui;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.golem.GolemCommand;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class GuiGolem extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/gui/golem.png");

    private static final int COLOR_TITLE  = 0x404040;
    private static final int COLOR_LABEL  = 0x666666;
    private static final int COLOR_HP     = 0xFF4444;
    private static final int COLOR_TASK   = 0xAA44FF;
    private static final int COLOR_QUEUE  = 0x7777AA;
    private static final int COLOR_NONE   = 0x888888;

    private final EntityEtherGolem golem;

    // Кнопки
    private static final int BTN_CLEAR   = 0;
    private static final int BTN_RETURN  = 1;
    private static final int BTN_PAUSE   = 2;

    public GuiGolem(InventoryPlayer playerInv,
                    EntityEtherGolem golem) {
        super(new ContainerGolem(playerInv, golem));
        this.golem  = golem;
        this.xSize  = 176;
        this.ySize  = 200;
    }

    @Override
    public void initGui() {
        super.initGui();

        int x = (width  - xSize) / 2;
        int y = (height - ySize) / 2;

        // Очистить очередь
        buttonList.add(new GuiButton(BTN_CLEAR, x + 8, y + 108,
                50, 16, "Очистить"));

        // Вернуть домой
        buttonList.add(new GuiButton(BTN_RETURN, x + 62, y + 108,
                50, 16, "Домой"));

        // Пауза / Возобновить
        buttonList.add(new GuiButton(BTN_PAUSE, x + 116, y + 108,
                50, 16, "Пауза"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        // Отправляем пакет на сервер
        // (пока через чат-команду для простоты, позже через пакеты)
        if (button.id == BTN_CLEAR) {
            mc.player.sendChatMessage("/golem_clear " + golem.getEntityId());
        } else if (button.id == BTN_RETURN) {
            mc.player.sendChatMessage("/golem_return " + golem.getEntityId());
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(TEXTURE);
        int x = (width  - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        // ── Заголовок ────────────────────────────────
        String title = getGolemTitle();
        fontRenderer.drawString(title,
                xSize / 2 - fontRenderer.getStringWidth(title) / 2,
                6, COLOR_TITLE);

        // ── HP ───────────────────────────────────────
        String hp = "HP: " + (int) golem.getHealth()
                + " / " + (int) golem.getMaxHealth();
        fontRenderer.drawString(hp, 8, 18, COLOR_HP);

        // ── HP Bar ───────────────────────────────────
        int barW = (int) (80 * golem.getHealth() / golem.getMaxHealth());
        drawRect(8, 26, 88, 32, 0xFF330000);       // фон
        drawRect(8, 26, 8 + barW, 32, 0xFFFF4444); // заполнение

        // ── Инвентарь голема ─────────────────────────
        fontRenderer.drawString("Инвентарь:",  8, 46, COLOR_LABEL);
        // слоты отрисовываются автоматически контейнером

        // ── Текущая задача ───────────────────────────
        fontRenderer.drawString("Задача:", 8, 72, COLOR_LABEL);
        String taskStr;
        if (golem.currentTask != null) {
            taskStr = "§d" + golem.currentTask.command.name()
                    + " §7(r=" + golem.currentTask.radius + ")";
        } else {
            taskStr = "Нет";
        }
        fontRenderer.drawString(taskStr, 8, 81, COLOR_TASK);

        // ── Очередь команд ───────────────────────────
        fontRenderer.drawString("Очередь (" +
                golem.commandQueue.size() + "):", 8, 91, COLOR_LABEL);

        int qy = 100;
        int shown = 0;
        for (EntityEtherGolem.GolemTaskEntry entry : golem.commandQueue) {
            if (shown >= 3) { // показываем максимум 3
                fontRenderer.drawString("...", 12, qy, COLOR_NONE);
                break;
            }
            fontRenderer.drawString(
                    "• " + entry.command.name()
                            + " r=" + entry.radius,
                    12, qy, COLOR_QUEUE);
            qy += 9;
            shown++;
        }

        if (golem.commandQueue.isEmpty() && golem.currentTask == null) {
            fontRenderer.drawString("Пусто", 12, qy, COLOR_NONE);
        }

        // ── Инвентарь игрока ─────────────────────────
        fontRenderer.drawString("Инвентарь", 8, 128, COLOR_TITLE);
    }

    private String getGolemTitle() {
        if (golem instanceof com.etherforge.mod.entities.EntityMechGolem) {
            return "Механический Голем";
        } else if (golem instanceof
                com.etherforge.mod.entities.EntityMorphoGolem) {
            return "Морфо Голем";
        } else if (golem instanceof
                com.etherforge.mod.entities.EntityEtherealGolem) {
            return "Эфирный Голем";
        }
        return "Голем";
    }
}