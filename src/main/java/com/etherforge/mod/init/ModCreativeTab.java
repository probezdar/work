package com.etherforge.mod.init;

import com.etherforge.mod.util.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ModCreativeTab extends CreativeTabs {

    public static final ModCreativeTab INSTANCE = new ModCreativeTab();

    private ModCreativeTab() {
        super(Reference.MOD_ID);
    }

    @Override
    public ItemStack getTabIconItem() {
        // Позже заменим на наш кристалл
        // Пока используем временную заглушку
        return new ItemStack(ModItems.ETHER_CRYSTAL);
    }
}