package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityEtherCondenser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerEtherCondenser extends Container {

    private final TileEntityEtherCondenser condenser;

    public ContainerEtherCondenser(InventoryPlayer playerInv,
                                   TileEntityEtherCondenser condenser) {
        this.condenser = condenser;

        // Инвентарь игрока (3 ряда)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                        playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        140 + row * 18
                ));
            }
        }

        // Хотбар игрока
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(
                    playerInv,
                    col,
                    8 + col * 18,
                    198
            ));
        }
    }

    public TileEntityEtherCondenser getCondenser() {
        return condenser;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}