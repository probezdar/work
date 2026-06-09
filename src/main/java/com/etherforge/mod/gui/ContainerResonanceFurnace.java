package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityResonanceFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class ContainerResonanceFurnace extends Container {

    private final TileEntityResonanceFurnace furnace;

    public ContainerResonanceFurnace(InventoryPlayer playerInv,
                                     TileEntityResonanceFurnace furnace) {
        this.furnace = furnace;

        // Слот входа
        addSlotToContainer(new Slot(furnace,
                TileEntityResonanceFurnace.SLOT_INPUT, 56, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !FurnaceRecipes.instance()
                        .getSmeltingResult(stack).isEmpty();
            }
        });

        // Слот выхода
        addSlotToContainer(new Slot(furnace,
                TileEntityResonanceFurnace.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // Слот побочного продукта
        addSlotToContainer(new Slot(furnace,
                TileEntityResonanceFurnace.SLOT_BYPRODUCT, 116, 55) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // Инвентарь игрока
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

    public TileEntityResonanceFurnace getFurnace() { return furnace; }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return true; }
}