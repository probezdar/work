package com.etherforge.mod.tileentity;

import com.etherforge.mod.recipes.EtherWorkbenchRecipeRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class TileEntityEtherWorkbench extends TileEntity implements IInventory {

    public static final int GRID_SIZE     = 4;
    public static final int CATALYST_SLOTS = 4;
    public static final int OUTPUT_SLOT   = 8;
    public static final int TOTAL_SLOTS   = 9;

    private boolean active = false;
    private EnumFacing facing = EnumFacing.NORTH;

    private NonNullList<ItemStack> inventory =
            NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    public boolean isActive()            { return active; }
    public EnumFacing getActiveFacing()  { return facing; }

    public void setActiveData(boolean active, EnumFacing facing) {
        this.active = active;
        this.facing = facing;
        markDirty();
    }

    // ═══════════════════════════════════════════
    //  Крафт
    // ═══════════════════════════════════════════
    public void updateCraftingResult() {
        ItemStack result = EtherWorkbenchRecipeRegistry.findRecipe(this);
        inventory.set(OUTPUT_SLOT, result);
    }

    public void consumeCraftingIngredients() {
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) inventory.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = GRID_SIZE; i < GRID_SIZE + CATALYST_SLOTS; i++) {
            ItemStack catalyst = inventory.get(i);
            if (!catalyst.isEmpty()) {
                catalyst.setItemDamage(catalyst.getItemDamage() + 1);
                if (catalyst.getItemDamage() >= catalyst.getMaxDamage()) {
                    inventory.set(i, ItemStack.EMPTY);
                }
            }
        }
        updateCraftingResult();
        markDirty();
    }

    // ═══════════════════════════════════════════
    //  IInventory
    // ═══════════════════════════════════════════
    @Override public int getSizeInventory() { return TOTAL_SLOTS; }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getStackInSlot(int i) { return inventory.get(i); }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack split;
        if (stack.getCount() <= count) {
            split = stack;
            inventory.set(index, ItemStack.EMPTY);
        } else {
            split = stack.splitStack(count);
        }
        if (index != OUTPUT_SLOT) updateCraftingResult();
        markDirty();
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory.get(index);
        inventory.set(index, ItemStack.EMPTY);
        if (index != OUTPUT_SLOT) updateCraftingResult();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        if (index != OUTPUT_SLOT) updateCraftingResult();
        markDirty();
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer p) { return true; }
    @Override public void openInventory(EntityPlayer p) {}
    @Override public void closeInventory(EntityPlayer p) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index != OUTPUT_SLOT;
    }

    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int v) {}
    @Override public int getFieldCount() { return 0; }

    @Override public void clear() {
        for (int i = 0; i < TOTAL_SLOTS; i++) inventory.set(i, ItemStack.EMPTY);
        updateCraftingResult();
    }

    @Override public String getName() { return "ether_workbench"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    // ═══════════════════════════════════════════
    //  NBT
    // ═══════════════════════════════════════════
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Active", active);
        compound.setInteger("Facing", facing.getIndex());
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!inventory.get(i).isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                inventory.get(i).writeToNBT(tag);
                compound.setTag("Slot" + i, tag);
            }
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        active = compound.getBoolean("Active");
        facing = EnumFacing.getFront(compound.getInteger("Facing"));
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (compound.hasKey("Slot" + i)) {
                inventory.set(i,
                        new ItemStack(compound.getCompoundTag("Slot" + i)));
            }
        }
    }

    @Override public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(
                pos, 3, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
                             net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}