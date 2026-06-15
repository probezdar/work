package com.etherforge.mod.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class EtherWorkbenchRecipe {

    private final NonNullList<ItemStack> inputs;    // 4 слота сетки
    private final NonNullList<ItemStack> catalysts; // 4 слота катализаторов
    private final ItemStack output;

    public EtherWorkbenchRecipe(ItemStack[] inputs, ItemStack[] catalysts, ItemStack output) {
        this.inputs = NonNullList.withSize(4, ItemStack.EMPTY);
        this.catalysts = NonNullList.withSize(4, ItemStack.EMPTY);
        for (int i = 0; i < 4; i++) {
            if (inputs != null && i < inputs.length && inputs[i] != null) this.inputs.set(i, inputs[i].copy());
            if (catalysts != null && i < catalysts.length && catalysts[i] != null) this.catalysts.set(i, catalysts[i].copy());
        }
        this.output = output.copy();
    }

    public boolean matches(com.etherforge.mod.tileentity.TileEntityEtherWorkbench bench) {
        // Проверяем сетку 2x2
        for (int i = 0; i < 4; i++) {
            ItemStack inSlot = bench.getStackInSlot(i);
            ItemStack req = inputs.get(i);
            if (req.isEmpty() && inSlot.isEmpty()) continue;
            if (req.isEmpty() || inSlot.isEmpty()) return false;
            if (!req.isItemEqual(inSlot) || inSlot.getCount() < req.getCount()) return false;
        }
        // Проверяем катализаторы
        for (int i = 0; i < 4; i++) {
            ItemStack inSlot = bench.getStackInSlot(4 + i);
            ItemStack req = catalysts.get(i);
            if (req.isEmpty() && inSlot.isEmpty()) continue;
            if (req.isEmpty() || inSlot.isEmpty()) return false;
            if (!req.isItemEqual(inSlot)) return false;
        }
        return true;
    }

    public ItemStack getOutput() { return output.copy(); }
}