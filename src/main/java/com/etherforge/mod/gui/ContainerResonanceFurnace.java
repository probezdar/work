package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityResonanceFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class ContainerResonanceFurnace extends Container {

    private final TileEntityResonanceFurnace furnace;

    private int lastEther = -1;
    private int lastSmelt = -1;

    // Границы слотов
    private static final int SLOT_INPUT     = 0;
    private static final int SLOT_OUTPUT    = 1;
    private static final int SLOT_BYPRODUCT = 2;
    private static final int INV_START      = 3;   // начало инвентаря игрока
    private static final int INV_END        = 30;  // конец (3 ряда * 9 = 27 слотов, 3+27=30)
    private static final int HOTBAR_START   = 30;
    private static final int HOTBAR_END     = 39;  // 30 + 9

    public ContainerResonanceFurnace(InventoryPlayer playerInv,
                                     TileEntityResonanceFurnace furnace) {
        this.furnace = furnace;

        // Слот входа
        addSlotToContainer(new Slot(furnace,
                TileEntityResonanceFurnace.SLOT_INPUT, 57, 31) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !FurnaceRecipes.instance()
                        .getSmeltingResult(stack).isEmpty();
            }
        });

        // Слот выхода
        addSlotToContainer(new Slot(furnace,
                TileEntityResonanceFurnace.SLOT_OUTPUT, 129, 21) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // Слот побочного продукта
        addSlotToContainer(new Slot(furnace,
                TileEntityResonanceFurnace.SLOT_BYPRODUCT, 129, 38) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // Инвентарь игрока (3 ряда по 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                        playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }

        // Хотбар
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(
                    playerInv, col,
                    8 + col * 18, 142
            ));
        }
    }

    // ═══════════════════════════════════════════
    //  Shift+Click — перемещение предметов
    // ═══════════════════════════════════════════
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) {
            return copy;
        }

        ItemStack stack = slot.getStack();
        copy = stack.copy();

        if (index == SLOT_OUTPUT || index == SLOT_BYPRODUCT) {
            // Из выходных слотов → в инвентарь игрока
            if (!mergeItemStack(stack, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, copy);

        } else if (index >= INV_START) {
            // Из инвентаря → пробуем положить во входной слот
            ItemStack smeltResult = FurnaceRecipes.instance()
                    .getSmeltingResult(stack);

            if (!smeltResult.isEmpty()) {
                // Предмет плавится — кладём во входной слот
                if (!mergeItemStack(stack, SLOT_INPUT, SLOT_INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Не плавится — перемещаем между инвентарём и хотбаром
                if (index < HOTBAR_START) {
                    if (!mergeItemStack(stack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(stack, INV_START, INV_START + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

        } else if (index == SLOT_INPUT) {
            // Из входного слота → в инвентарь
            if (!mergeItemStack(stack, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : listeners) {
            if (lastEther != furnace.getEtherStored()) {
                listener.sendWindowProperty(this,
                        TileEntityResonanceFurnace.FIELD_ETHER,
                        furnace.getEtherStored());
            }
            if (lastSmelt != furnace.getSmeltTime()) {
                listener.sendWindowProperty(this,
                        TileEntityResonanceFurnace.FIELD_SMELT,
                        furnace.getSmeltTime());
            }
        }

        lastEther = furnace.getEtherStored();
        lastSmelt = furnace.getSmeltTime();
    }

    @Override
    public void updateProgressBar(int id, int value) {
        furnace.setField(id, value);
    }

    public TileEntityResonanceFurnace getFurnace() { return furnace; }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return true; }
}