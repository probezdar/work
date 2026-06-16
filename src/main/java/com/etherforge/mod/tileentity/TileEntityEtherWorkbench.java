package com.etherforge.mod.tileentity;

import com.etherforge.mod.blocks.BlockEtherWorkbench;
import com.etherforge.mod.recipes.EtherWorkbenchRecipeRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import static com.etherforge.mod.blocks.BlockEtherWorkbench.canFormAt;
import static com.etherforge.mod.blocks.BlockEtherWorkbench.canFormSilent;

public class TileEntityEtherWorkbench extends TileEntity implements IInventory, ITickable {

    public static final int GRID_SIZE = 4;
    public static final int CATALYST_SLOTS = 4;
    public static final int OUTPUT_SLOT = 8;
    public static final int TOTAL_SLOTS = 9;
    private boolean active = false;
    private EnumFacing facing = EnumFacing.NORTH;
    private boolean needsRestore = false;
    public boolean isActive() { return active; }
    public net.minecraft.util.EnumFacing getActiveFacing() { return facing; }

    @Override
    public void onLoad() {
        // Вызывается после readFromNBT когда чанк загружен
        // Откладываем на следующий тик чтобы мир был готов
        if (!world.isRemote && active) {
            // Планируем восстановление через ITickable
            needsRestore = true;
        }
    }

    private void restoreMultiblock() {
        if (!active || world == null || world.isRemote) return;

        BlockPos masterPos = pos;
        IBlockState state  = world.getBlockState(masterPos);

        if (!(state.getBlock() instanceof BlockEtherWorkbench)) return;

        // Проверяем что все 4 блока на месте
        if (!BlockEtherWorkbench.canFormSilent(world, masterPos, facing)) {
            // Блоки снесли пока мы были оффлайн
            active = false;
            markDirty();
            return;
        }

        BlockEtherWorkbench.formAt_silent(world, masterPos, facing);
    }

    private NonNullList<ItemStack> inventory =
            NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);

    public void setActiveData(boolean active,
                              net.minecraft.util.EnumFacing facing) {
        this.active = active;
        this.facing = facing;
        markDirty();
    }

    // ═══════════════════════════════════════════
    //  Крафт-логика
    // ═══════════════════════════════════════════
    public void updateCraftingResult() {
        ItemStack result = EtherWorkbenchRecipeRegistry.findRecipe(this);
        inventory.set(OUTPUT_SLOT, result);
    }

    public void consumeCraftingIngredients() {
        // Съедаем ингредиенты сетки
        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) inventory.set(i, ItemStack.EMPTY);
            }
        }
        // Тратим прочность катализаторов
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

    @Override public ItemStack getStackInSlot(int index) { return inventory.get(index); }

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
    @Override public boolean isUsableByPlayer(EntityPlayer player) { return true; }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index != OUTPUT_SLOT;
    }

    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
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
        int facingIndex = compound.getInteger("Facing");
        facing = EnumFacing.getFront(facingIndex);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (compound.hasKey("Slot" + i)) {
                inventory.set(i, new ItemStack(compound.getCompoundTag("Slot" + i)));
            }
        }
    }

    // Синхронизация
    @Override public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }
    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos, 3, getUpdateTag());
    }
    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
                             net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }


    @Override
    public void update() {
        if (needsRestore) {
            needsRestore = false;
            restoreMultiblock();
        }
    }
}