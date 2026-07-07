// gui/ContainerGolem.java
package com.etherforge.mod.gui;

import com.etherforge.mod.entities.EntityEtherGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGolem extends Container {

    private final EntityEtherGolem        golem;
    private final GolemInventoryWrapper   golemInv;

    // Позиции слотов голема (3x3 сетка)
    // Начало x=8, y=38
    private static final int GOLEM_SLOT_X = 8;
    private static final int GOLEM_SLOT_Y = 38;

    public ContainerGolem(InventoryPlayer playerInv,
                          EntityEtherGolem golem) {
        this.golem   = golem;
        this.golemInv = new GolemInventoryWrapper(golem);

        // ── Слоты голема (3x3) ──────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlotToContainer(new Slot(
                        golemInv,
                        row * 3 + col,
                        GOLEM_SLOT_X + col * 20,
                        GOLEM_SLOT_Y + row * 20));
            }
        }

        // ── Инвентарь игрока (3 строки) ─────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                        playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        130 + row * 18));
            }
        }

        // ── Хотбар ──────────────────────────────────
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(
                    playerInv,
                    col,
                    8 + col * 18,
                    188));
        }
    }

    // ── Shift+клик ──────────────────────────────────
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) return result;

        ItemStack stack    = slot.getStack();
        result = stack.copy();

        // Из слотов голема → в инвентарь игрока
        if (index < 9) {
            if (!mergeItemStack(stack, 9, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        }
        // Из инвентаря игрока → в слоты голема
        else {
            if (!mergeItemStack(stack, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        return result;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(golem) < 64.0;
    }
}