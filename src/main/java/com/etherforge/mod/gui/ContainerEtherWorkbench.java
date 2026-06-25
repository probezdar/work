package com.etherforge.mod.gui;

import com.etherforge.mod.init.ModItems;
import com.etherforge.mod.tileentity.TileEntityEtherWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEtherWorkbench extends Container {

    private final TileEntityEtherWorkbench workbench;

    // Границы слотов для transferStackInSlot
    private static final int SLOT_GRID_START     = 0;  // 0-3   сетка
    private static final int SLOT_CATALYST_START = 4;  // 4-7   катализаторы
    private static final int SLOT_OUTPUT         = 8;  // 8     результат
    private static final int SLOT_INV_START      = 9;  // 9-35  инвентарь
    private static final int SLOT_HOTBAR_START   = 36; // 36-44 хотбар
    private static final int SLOT_TOTAL          = 45;

    public ContainerEtherWorkbench(InventoryPlayer playerInv,
                                   TileEntityEtherWorkbench workbench) {
        this.workbench = workbench;

        // ── Сетка 2x2: слоты 0-3 ────────────────────
        addSlotToContainer(new Slot(workbench, 0, 77, 37));
        addSlotToContainer(new Slot(workbench, 1, 95, 37));
        addSlotToContainer(new Slot(workbench, 2, 77, 55));
        addSlotToContainer(new Slot(workbench, 3, 95, 55));

        // ── Катализаторы: слоты 4-7 ─────────────────
        // Только предметы с isCatalyst()
        for (int i = 4; i < 8; i++) {
            final int slotIndex = i;
            addSlotToContainer(new Slot(workbench, slotIndex,
                    getCatalystX(slotIndex), getCatalystY(slotIndex)) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return isCatalyst(stack);
                }
            });
        }

        // ── Результат: слот 8 ────────────────────────
        addSlotToContainer(new Slot(workbench, SLOT_OUTPUT, 149, 46) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false; // нельзя класть вручную
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                workbench.consumeCraftingIngredients();
                return super.onTake(player, stack);
            }
        });

        // ── Инвентарь игрока: слоты 9-35 ────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv,
                        col + row * 9 + 9,
                        8 + col * 18, 102 + row * 18));
            }
        }

        // ── Хотбар: слоты 36-44 ──────────────────────
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col,
                    8 + col * 18, 160));
        }
    }

    // ═══════════════════════════════════════════
    //  Позиции катализаторов
    // ═══════════════════════════════════════════
    private int getCatalystX(int slot) {
        switch (slot) {
            case 4: return 59;  // лево
            case 5: return 86;  // верх
            case 6: return 113; // право
            case 7: return 86;  // низ
            default: return 0;
        }
    }

    private int getCatalystY(int slot) {
        switch (slot) {
            case 4: return 46;
            case 5: return 19;
            case 6: return 46;
            case 7: return 73;
            default: return 0;
        }
    }

    // ═══════════════════════════════════════════
    //  Проверка катализатора
    // ═══════════════════════════════════════════
    private boolean isCatalyst(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == ModItems.CATALYST_STEAM
                || stack.getItem() == ModItems.CATALYST_PLASMA
                || stack.getItem() == ModItems.CATALYST_DAWN
                || stack.getItem() == ModItems.CATALYST_SURGE
                || stack.getItem() == ModItems.CATALYST_DEPTH
                || stack.getItem() == ModItems.CATALYST_SPARK
                || stack.getItem() == ModItems.CATALYST_ECLIPSE
                || stack.getItem() == ModItems.CATALYST_EMBER;
    }

    // ═══════════════════════════════════════════
    //  Shift + клик
    // ═══════════════════════════════════════════
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return copy;

        ItemStack stack = slot.getStack();
        copy = stack.copy();

        if (index == SLOT_OUTPUT) {
            // Результат → инвентарь (с конца)
            if (!mergeItemStack(stack, SLOT_INV_START, SLOT_TOTAL, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, copy);

        } else if (index >= SLOT_INV_START) {
            // Из инвентаря/хотбара
            if (isCatalyst(stack)) {
                // Катализатор → слоты 4-7
                if (!mergeItemStack(stack,
                        SLOT_CATALYST_START,
                        SLOT_OUTPUT,
                        false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Остальное → сетка 0-3
                if (!mergeItemStack(stack,
                        SLOT_GRID_START,
                        SLOT_CATALYST_START,
                        false)) {
                    return ItemStack.EMPTY;
                }
            }

        } else if (index < SLOT_CATALYST_START) {
            // Из сетки → инвентарь
            if (!mergeItemStack(stack, SLOT_INV_START, SLOT_TOTAL, true)) {
                return ItemStack.EMPTY;
            }

        } else if (index < SLOT_OUTPUT) {
            // Из катализаторов → инвентарь
            if (!mergeItemStack(stack, SLOT_INV_START, SLOT_TOTAL, true)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}