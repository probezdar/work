package com.etherforge.mod.gui;

import com.etherforge.mod.entities.EntityEtherGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class GolemInventoryWrapper implements IInventory {

    private final EntityEtherGolem golem;

    public GolemInventoryWrapper(EntityEtherGolem golem) {
        this.golem = golem;
    }

    @Override public int getSizeInventory() { return 2; }
    @Override public boolean isEmpty() {
        for (ItemStack s : golem.golemInventory)
            if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int i) {
        return golem.golemInventory[i];
    }
    @Override public ItemStack decrStackSize(int i, int count) {
        ItemStack stack = golem.golemInventory[i];
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (stack.getCount() <= count) {
            golem.golemInventory[i] = ItemStack.EMPTY;
            return stack;
        }
        return stack.splitStack(count);
    }
    @Override public ItemStack removeStackFromSlot(int i) {
        ItemStack s = golem.golemInventory[i];
        golem.golemInventory[i] = ItemStack.EMPTY;
        return s;
    }
    @Override public void setInventorySlotContents(int i, ItemStack s) {
        golem.golemInventory[i] = s;
    }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public void markDirty() {}
    @Override public boolean isUsableByPlayer(EntityPlayer p) { return true; }
    @Override public void openInventory(EntityPlayer p) {}
    @Override public void closeInventory(EntityPlayer p) {}
    @Override public boolean isItemValidForSlot(int i, ItemStack s) {
        return true;
    }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int v) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() {
        for (int i = 0; i < 2; i++)
            golem.golemInventory[i] = ItemStack.EMPTY;
    }
    @Override public String getName() { return "golem"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() {
        return new TextComponentString("Golem");
    }
}