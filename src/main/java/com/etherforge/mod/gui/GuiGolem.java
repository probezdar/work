package com.etherforge.mod.gui;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiGolem extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/gui/golem.png");

    // Цвета
    private static final int C_TITLE = 0xDDCCFF;
    private static final int C_LABEL = 0xAA99CC;
    private static final int C_HP    = 0xFF4444;
    private static final int C_TASK  = 0xCC88FF;
    private static final int C_QUEUE = 0x9977BB;
    private static final int C_NONE  = 0x665577;

    private final EntityEtherGolem golem;

    // Кнопки
    private static final int BTN_CLEAR  = 0;
    private static final int BTN_RETURN = 1;
    private static final int BTN_PAUSE  = 2;

    public GuiGolem(InventoryPlayer playerInv, EntityEtherGolem golem) {
        super(new ContainerGolem(playerInv, golem));
        this.golem = golem;
        this.xSize = 176;
        this.ySize = 222; // высота как у кондесатора
    }

    @Override
    public void initGui() {
        super.initGui();
        int gx = (width  - xSize) / 2;
        int gy = (height - ySize) / 2;

        buttonList.add(new GuiButton(BTN_CLEAR,
                gx + 8,   gy + 108, 50, 14, "Очистить"));
        buttonList.add(new GuiButton(BTN_RETURN,
                gx + 62,  gy + 108, 50, 14, "Домой"));
        buttonList.add(new GuiButton(BTN_PAUSE,
                gx + 116, gy + 108, 50, 14, "Пауза"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        // TODO: заменить на пакеты
        if (button.id == BTN_CLEAR) {
            golem.commandQueue.clear();
            golem.currentTask = null;
        }

        if (button.id == BTN_RETURN) {
            golem.commandQueue.addFirst(
                    new EntityEtherGolem.GolemTaskEntry(
                            com.etherforge.mod.golem.GolemCommand.RETURN,
                            8, null));
        }

        // BTN_PAUSE — пока не реализован
        if (button.id == BTN_PAUSE) {
            // TODO: флаг паузы в EntityEtherGolem
        }
    }

    private net.minecraft.world.World world() {
        return mc.world;
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

        // ── Заголовок ─────────────────────────────────────
        String title = getTitle();
        fontRenderer.drawString(title,
                xSize / 2 - fontRenderer.getStringWidth(title) / 2,
                6, C_TITLE);

        // ── HP ────────────────────────────────────────────
        int maxHp = (int) golem.getMaxHealth();
        int curHp = (int) golem.getHealth();
        // Ограничиваем для отображения
        int displayMax = Math.min(maxHp, 9999);
        String hpStr = "HP: " + curHp + " / " + displayMax;
        fontRenderer.drawString(hpStr, 8, 18, C_HP);

        // HP Bar
        int barTotal = 80;
        // Заменить расчёт barFill:
        int barFill = 0;
        if (maxHp > 0) {
            // Используем float чтобы избежать потери точности
            barFill = (int) Math.min(barTotal,
                    (float) curHp / maxHp * barTotal);
        }
        drawRect(8,  28, 8 + barTotal, 33, 0xFF220000); // фон
        drawRect(8,  28, 8 + barFill,  33, 0xFFDD3333); // заполнение
        drawRect(8,  28, 8 + barTotal, 28, 0xFF550000); // верх
        drawRect(8,  32, 8 + barTotal, 33, 0xFF550000); // низ

        // ── Текущая задача ────────────────────────────────
        fontRenderer.drawString("Задача:", 20, 62, C_LABEL);
        if (golem.currentTask != null) {
            fontRenderer.drawString(
                    golem.currentTask.command.name()
                            + "  r=" + golem.currentTask.radius,
                    50, 62, C_TASK);
        } else {
            fontRenderer.drawString("Нет", 50, 62, C_NONE);
        }

        // ── Очередь ───────────────────────────────────────
        fontRenderer.drawString(
                "Очередь (" + golem.commandQueue.size() + "):",
                20, 80, C_LABEL);

        int qy   = 89;
        int shown = 0;
        for (EntityEtherGolem.GolemTaskEntry e : golem.commandQueue) {
            if (shown >= 3) {
                fontRenderer.drawString("...", 28, qy, C_NONE);
                break;
            }
            fontRenderer.drawString(
                    "• " + e.command.name() + " r=" + e.radius,
                    28, qy, C_QUEUE);
            qy   += 9;
            shown++;
        }

        if (golem.commandQueue.isEmpty() && golem.currentTask == null) {
            fontRenderer.drawString("Пусто", 28, qy, C_NONE);
        }

    }

    private String getTitle() {
        if (golem instanceof com.etherforge.mod.entities.EntityMechGolem)
            return "Mechanical Golem";
        if (golem instanceof com.etherforge.mod.entities.EntityMorphoGolem)
            return "Morpho Golem";
        if (golem instanceof com.etherforge.mod.entities.EntityEtherealGolem)
            return "Ether Golem";
        return "Golem";
    }
}