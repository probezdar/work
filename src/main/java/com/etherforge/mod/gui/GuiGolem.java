// gui/GuiGolem.java
package com.etherforge.mod.gui;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.network.PacketGolemCommand;
import com.etherforge.mod.network.PacketHandler;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class GuiGolem extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/gui/golem.png");

    // Цвета
    private static final int C_TITLE  = 0xDDCCFF;
    private static final int C_LABEL  = 0xAA99CC;
    private static final int C_HP     = 0xFF4444;
    private static final int C_HP_LOW = 0xFF8800;
    private static final int C_TASK   = 0xCC88FF;
    private static final int C_QUEUE  = 0x9977BB;
    private static final int C_NONE   = 0x665577;
    private static final int C_LOOP_ON  = 0x44FF88;
    private static final int C_LOOP_OFF = 0xFF4444;

    private final EntityEtherGolem golem;

    // ID кнопок
    private static final int BTN_CLEAR       = 0;
    private static final int BTN_RETURN_HOME = 1;
    private static final int BTN_TOGGLE_LOOP = 2;

    public GuiGolem(InventoryPlayer playerInv, EntityEtherGolem golem) {
        super(new ContainerGolem(playerInv, golem));
        this.golem = golem;
        this.xSize = 256;
        this.ySize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();
        int gx = (width  - xSize) / 2;
        int gy = (height - ySize) / 2;

        // Кнопка Очистить
        buttonList.add(new GuiButton(
                BTN_CLEAR,
                gx + 8, gy + 108,
                50, 14,
                "Очистить"));

        // Кнопка Домой
        buttonList.add(new GuiButton(
                BTN_RETURN_HOME,
                gx + 62, gy + 108,
                50, 14,
                "Домой"));

        // Кнопка Цикл (toggle)
        buttonList.add(new GuiButton(
                BTN_TOGGLE_LOOP,
                gx + 116, gy + 108,
                50, 14,
                getLoopLabel()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_CLEAR:
                PacketHandler.INSTANCE.sendToServer(
                        new PacketGolemCommand(
                                golem.getEntityId(),
                                PacketGolemCommand.ACTION_CLEAR));
                break;

            case BTN_RETURN_HOME:
                PacketHandler.INSTANCE.sendToServer(
                        new PacketGolemCommand(
                                golem.getEntityId(),
                                PacketGolemCommand.ACTION_RETURN_HOME));
                break;

            case BTN_TOGGLE_LOOP:
                PacketHandler.INSTANCE.sendToServer(
                        new PacketGolemCommand(
                                golem.getEntityId(),
                                PacketGolemCommand.ACTION_TOGGLE_LOOP));
                // Обновляем label кнопки локально
                button.displayString = getLoopLabel();
                break;
        }
    }

    private String getLoopLabel() {
        return golem.loopTasks ? "§aЦикл: ВКЛ" : "§cЦикл: ВЫКЛ";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(TEXTURE);
        int x = (width  - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // HP бар
        drawHpBar(x, y+1);
    }

    private void drawHpBar(int x, int y) {
        int maxHp = (int) golem.getMaxHealth();
        int curHp = (int) golem.getHealth();

        // Фон бара
        drawRect(x + 8, y + 24, x + 176, y + 32, 0xFF3C0A0A);

        if (maxHp > 0) {
            int barWidth = Math.min(168, curHp * 168 / maxHp);
            // Цвет зависит от % HP
            int barColor = curHp > maxHp * 0.5f
                    ? 0xFFDD3333
                    : 0xFFFF6600;
            drawRect(x + 8, y + 25,
                    x + 8 + barWidth, y + 31,
                    barColor);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

        // ── Заголовок ──────────────────────────────
        String title = getTitle();
        fontRenderer.drawString(title,
                xSize / 2 - fontRenderer.getStringWidth(title) / 2,
                6, C_TITLE);

        // ── HP текст ───────────────────────────────
        int maxHp      = (int) golem.getMaxHealth();
        int curHp      = (int) golem.getHealth();
        int displayMax = Math.min(maxHp, 9999);
        fontRenderer.drawString(
                "HP: " + curHp + "/" + displayMax,
                8, 16, C_HP);

        // ── Цикл статус ────────────────────────────
        String loopStr = golem.loopTasks
                ? "§aЦикл: ВКЛ" : "§cЦикл: ВЫКЛ";
        fontRenderer.drawString(loopStr,
                xSize - fontRenderer.getStringWidth(
                        "Цикл: ВЫКЛ") - 8,
                16, 0xFFFFFF);

        // ── Текущая задача ─────────────────────────
        fontRenderer.drawString("Задача:", 80, 40, C_LABEL);
        if (golem.currentTask != null) {
            fontRenderer.drawString(
                    "§d" + golem.currentTask.command.name(),
                    110, 40, C_TASK);
            fontRenderer.drawString(
                    "Радиус: §f" + golem.currentTask.radius,
                    80, 51, C_LABEL);
            if (golem.currentTask.target != null) {
                BlockPos t = golem.currentTask.target;
                fontRenderer.drawString(
                        "§7→ " + t.getX() + " "
                                + t.getY() + " " + t.getZ(),
                        80, 62, C_LABEL);
            }
        } else {
            fontRenderer.drawString("§8Нет задачи", 110, 40, C_NONE);
        }

        // ── Очередь ────────────────────────────────
        int queueSize = golem.commandQueue.size();
        fontRenderer.drawString(
                "Очередь (" + queueSize + "):",
                80, 78, C_LABEL);

        int qy    = 87;
        int shown = 0;
        for (EntityEtherGolem.GolemTaskEntry e : golem.commandQueue) {
            // Пропускаем дубли от автоповтора
            if (shown >= 4) {
                fontRenderer.drawString("§8...", 84, qy, C_NONE);
                break;
            }
            fontRenderer.drawString(
                    "§5• §d" + e.command.name()
                            + " §7r=" + e.radius,
                    84, qy, C_QUEUE);
            qy += 8;
            shown++;
        }

        if (golem.commandQueue.isEmpty()
                && golem.currentTask == null) {
            fontRenderer.drawString("§8Пусто", 84, qy, C_NONE);
        }

        // ── Инвентарь игрока ───────────────────────
        fontRenderer.drawString("Инвентарь", 8, 126, C_LABEL);
    }

    private String getTitle() {
        if (golem instanceof com.etherforge.mod.entities.EntityMechGolem)
            return "§6Механический Голем";
        if (golem instanceof com.etherforge.mod.entities.EntityMorphoGolem)
            return "§2Морфо Голем";
        if (golem instanceof com.etherforge.mod.entities.EntityEtherealGolem)
            return "§5Эфирный Голем";
        return "Голем";
    }

    // Нужен импорт BlockPos для отображения цели
    private static net.minecraft.util.math.BlockPos BlockPos(
            EntityEtherGolem.GolemTaskEntry t) {
        return t.target;
    }
}