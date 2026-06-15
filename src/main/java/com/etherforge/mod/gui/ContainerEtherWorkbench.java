package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityEtherWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEtherWorkbench extends Container {

    private final TileEntityEtherWorkbench workbench;

    public ContainerEtherWorkbench(InventoryPlayer playerInv,
                                   TileEntityEtherWorkbench workbench) {
        this.workbench = workbench;

        // Сетка 2x2: слоты 0-3
        addSlotToContainer(new Slot(workbench, 0, 76, 36));
        addSlotToContainer(new Slot(workbench, 1, 94, 36));
        addSlotToContainer(new Slot(workbench, 2, 76, 54));
        addSlotToContainer(new Slot(workbench, 3, 94, 54));

        // Катализаторы: слоты 4-7 (слева, сверху, справа, снизу)
        addSlotToContainer(new Slot(workbench, 4, 58, 45));
        addSlotToContainer(new Slot(workbench, 5, 85, 18));
        addSlotToContainer(new Slot(workbench, 6, 112, 45));
        addSlotToContainer(new Slot(workbench, 7, 85, 72));

        // Результат: слот 8
        addSlotToContainer(new Slot(workbench, 8, 148, 45) {
            @Override
            public boolean isItemValid(ItemStack stack) { return false; }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                workbench.consumeCraftingIngredients();
                return super.onTake(player, stack);
            }
        });

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv,
                        col + row * 9 + 9,
                        8 + col * 18, 102 + row * 18));
            }
        }
        // Хотбар
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col,
                    8 + col * 18, 160));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return copy;

        ItemStack stack = slot.getStack();
        copy = stack.copy();

        if (index == 8) { // результат
            if (!mergeItemStack(stack, 9, 45, true)) return ItemStack.EMPTY;
            slot.onSlotChange(stack, copy);
        } else if (index < 8) { // верстак
            if (!mergeItemStack(stack, 9, 45, true)) return ItemStack.EMPTY;
        } else { // инвентарь игрока
            if (!mergeItemStack(stack, 0, 8, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return true; }
}