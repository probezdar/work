package com.etherforge.mod.gui;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.golem.GolemCommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public class ContainerGolem extends Container {

    public final EntityEtherGolem golem;

    // Слоты 0-1 — инвентарь голема
    // Слоты 2-28 — инвентарь игрока (3 ряда)
    // Слоты 29-37 — хотбар

    public ContainerGolem(InventoryPlayer playerInv,
                          EntityEtherGolem golem) {
        this.golem = golem;

        // Инвентарь голема — 2 слота
        IInventory golemInv = new GolemInventoryWrapper(golem);
        addSlotToContainer(new Slot(golemInv, 0, 62, 35));
        addSlotToContainer(new Slot(golemInv, 1, 80, 35));

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv,
                        col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }

        // Хотбар
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col,
                    8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return copy;

        ItemStack stack = slot.getStack();
        copy = stack.copy();

        if (index < 2) {
            // Из голема → в инвентарь
            if (!mergeItemStack(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Из инвентаря → в голема
            if (!mergeItemStack(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        // Закрыть GUI если голем далеко или мёртв
        return !golem.isDead
                && golem.getDistance(player) < 8.0;
    }
}