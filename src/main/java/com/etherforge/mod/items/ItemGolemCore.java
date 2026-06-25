// items/ItemGolemCore.java
package com.etherforge.mod.items;

import com.etherforge.mod.init.ModCreativeTab;
import com.etherforge.mod.util.Reference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemGolemCore extends Item {

    public enum CoreType {
        MECHANICAL, // металл, работа
        MORPHO,     // органика, бой
        ETHEREAL    // эфир, транспорт
    }

    private final CoreType coreType;

    public ItemGolemCore(CoreType type, String name) {
        this.coreType = type;
        setRegistryName(Reference.MOD_ID, name);
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setCreativeTab(ModCreativeTab.INSTANCE);
        setMaxStackSize(1);
    }

    public CoreType getCoreType() { return coreType; }

    @Override
    public void addInformation(ItemStack stack, World world,
                               List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        switch (coreType) {
            case MECHANICAL:
                tooltip.add("§7Тип: §6Механический");
                tooltip.add("§7HP: §a30  §7Урон: §c0");
                tooltip.add("§7Задачи: сбор, добыча, перенос");
                break;
            case MORPHO:
                tooltip.add("§7Тип: §2Морфо");
                tooltip.add("§7HP: §a20  §7Урон: §c10");
                tooltip.add("§7Задачи: бой, сбор растений");
                break;
            case ETHEREAL:
                tooltip.add("§7Тип: §5Эфирный");
                tooltip.add("§7HP: §a∞  §7Урон: §c0");
                tooltip.add("§7Задачи: телепортация предметов");
                break;
        }
    }
}