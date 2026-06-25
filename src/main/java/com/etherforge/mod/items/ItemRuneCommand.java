// items/ItemRuneCommand.java
package com.etherforge.mod.items;

import com.etherforge.mod.golem.GolemCommand;
import com.etherforge.mod.init.ModCreativeTab;
import com.etherforge.mod.util.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemRuneCommand extends Item {

    private final GolemCommand command;

    public ItemRuneCommand(GolemCommand command, String name) {
        this.command = command;
        setRegistryName(Reference.MOD_ID, name);
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setCreativeTab(ModCreativeTab.INSTANCE);
        setMaxStackSize(16);
    }

    public GolemCommand getCommand() {
        return command;
    }

    // Показываем команду при наведении
    @Override
    public void addInformation(ItemStack stack, World world,
                               java.util.List<String> tooltip,
                               net.minecraft.client.util.ITooltipFlag flag) {
        tooltip.add("§5Команда: §d" + command.name());

        // Радиус если задан
        if (stack.hasTagCompound()
                && stack.getTagCompound().hasKey("Radius")) {
            int radius = stack.getTagCompound().getInteger("Radius");
            tooltip.add("§7Радиус: §f" + radius + " блоков");
        }

        // Целевой блок если задан (для MINE и TRANSFER)
        if (stack.hasTagCompound()
                && stack.getTagCompound().hasKey("TargetX")) {
            NBTTagCompound nbt = stack.getTagCompound();
            tooltip.add("§7Цель: §f" +
                    nbt.getInteger("TargetX") + " " +
                    nbt.getInteger("TargetY") + " " +
                    nbt.getInteger("TargetZ"));
        }
    }

    // ПКМ по воздуху — установить радиус (по умолчанию 8)
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world,
                                                    EntityPlayer player,
                                                    EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            // Shift+ПКМ — сброс настроек
            stack.setTagCompound(null);
            if (!world.isRemote) {
                player.sendMessage(new TextComponentString(
                        "§7Настройки руны сброшены."));
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    // Установить радиус в NBT
    public static ItemStack setRadius(ItemStack stack, int radius) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("Radius", radius);
        return stack;
    }

    // Установить целевую позицию
    public static ItemStack setTarget(ItemStack stack,
                                      int x, int y, int z) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("TargetX", x);
        stack.getTagCompound().setInteger("TargetY", y);
        stack.getTagCompound().setInteger("TargetZ", z);
        return stack;
    }

    public static int getRadius(ItemStack stack) {
        if (stack.hasTagCompound()
                && stack.getTagCompound().hasKey("Radius")) {
            return stack.getTagCompound().getInteger("Radius");
        }
        return 8; // радиус по умолчанию
    }
}