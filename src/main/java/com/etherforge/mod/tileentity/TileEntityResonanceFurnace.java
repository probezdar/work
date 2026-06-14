package com.etherforge.mod.tileentity;

import com.etherforge.mod.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.Random;

public class TileEntityResonanceFurnace extends TileEntity
        implements ITickable, IInventory {

    // ═══════════════════════════════════════════
    //  Константы
    // ═══════════════════════════════════════════
    public static final int MAX_ETHER       = 5000;
    public static final int ETHER_PER_SMELT = 100;
    public static final int SMELT_TIME      = 200;

    public static final int SLOT_INPUT      = 0;
    public static final int SLOT_OUTPUT     = 1;
    public static final int SLOT_BYPRODUCT  = 2;
    public static final int SIZE            = 3;

    // ID полей для синхронизации
    public static final int FIELD_ETHER     = 0;
    public static final int FIELD_SMELT     = 1;
    public static final int FIELD_COUNT     = 2;

    // ═══════════════════════════════════════════
    //  Данные
    // ═══════════════════════════════════════════
    private NonNullList<ItemStack> inventory =
            NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private int etherStored = 0;
    private int smeltTime   = 0;
    private int totalSmelt  = SMELT_TIME;

    private final Random random = new Random();

    // ═══════════════════════════════════════════
    //  Tick
    // ═══════════════════════════════════════════
    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        boolean dirty = false;

        // Автоматически тянем эфир из соседних конденсаторов
        if (etherStored < MAX_ETHER) {
            dirty = pullEtherFromNeighbors() || dirty;
        }

        if (canSmelt()) {
            if (etherStored >= ETHER_PER_SMELT) {
                smeltTime++;
                dirty = true;

                if (smeltTime >= totalSmelt) {
                    smeltTime = 0;
                    etherStored -= ETHER_PER_SMELT;
                    doSmelt();
                }
            } else {
                // Нет эфира — прогресс откатываем
                if (smeltTime > 0) {
                    smeltTime = Math.max(0, smeltTime - 2);
                    dirty = true;
                }
            }
        } else {
            if (smeltTime > 0) {
                smeltTime = 0;
                dirty = true;
            }
        }

        if (dirty) {
            markDirty();
        }
    }

    // ═══════════════════════════════════════════
//  Забираем эфир у соседних конденсаторов
// ═══════════════════════════════════════════
    private boolean pullEtherFromNeighbors() {
        boolean got = false;

        // Проверяем все 6 сторон
        for (net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.VALUES) {
            net.minecraft.util.math.BlockPos neighborPos = pos.offset(facing);
            net.minecraft.tileentity.TileEntity te =
                    world.getTileEntity(neighborPos);

            if (te instanceof TileEntityEtherCondenser) {
                TileEntityEtherCondenser condenser =
                        (TileEntityEtherCondenser) te;

                int need      = MAX_ETHER - etherStored;
                int perTick   = 50; // сколько берём за тик максимум
                int toExtract = Math.min(need, perTick);

                if (toExtract > 0 && condenser.getEtherStored() > 0) {
                    int extracted = condenser.extractEther(toExtract);
                    etherStored += extracted;
                    if (extracted > 0) got = true;
                }
            }
        }

        return got;
    }

    // ═══════════════════════════════════════════
    //  Логика плавки
    // ═══════════════════════════════════════════
    private boolean canSmelt() {
        if (inventory.get(SLOT_INPUT).isEmpty()) return false;

        ItemStack result = FurnaceRecipes.instance()
                .getSmeltingResult(inventory.get(SLOT_INPUT));
        if (result.isEmpty()) return false;

        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) return true;
        if (!output.isItemEqual(result)) return false;

        return output.getCount() + result.getCount()
                <= output.getMaxStackSize();
    }

    private void doSmelt() {
        ItemStack input  = inventory.get(SLOT_INPUT);
        ItemStack result = FurnaceRecipes.instance()
                .getSmeltingResult(input).copy();
        ItemStack output = inventory.get(SLOT_OUTPUT);

        if (output.isEmpty()) {
            inventory.set(SLOT_OUTPUT, result);
        } else if (output.isItemEqual(result)) {
            output.grow(result.getCount());
        }

        input.shrink(1);
        if (input.isEmpty()) {
            inventory.set(SLOT_INPUT, ItemStack.EMPTY);
        }

        giveByproduct();
        markDirty();
    }

    private void giveByproduct() {
        if (random.nextFloat() > 0.30f) return;

        net.minecraft.item.Item[] crystals = {
                ModItems.ETHER_CRYSTAL,
                ModItems.CRYSTAL_IGNIS,
                ModItems.CRYSTAL_AQUA,
                ModItems.CRYSTAL_VOLTA,
                ModItems.CRYSTAL_UMBRA,
                ModItems.CRYSTAL_LUX
        };

        net.minecraft.item.Item byproduct =
                crystals[random.nextInt(crystals.length)];
        ItemStack byproductStack = new ItemStack(byproduct, 1);
        ItemStack current = inventory.get(SLOT_BYPRODUCT);

        if (current.isEmpty()) {
            inventory.set(SLOT_BYPRODUCT, byproductStack);
        } else if (current.isItemEqual(byproductStack)
                && current.getCount() < current.getMaxStackSize()) {
            current.grow(1);
        }
    }

    // ═══════════════════════════════════════════
    //  Эфир
    // ═══════════════════════════════════════════
    public int getEtherStored() { return etherStored; }
    public int getMaxEther()    { return MAX_ETHER; }

    public void setEtherStored(int amount) {
        this.etherStored = Math.max(0, Math.min(amount, MAX_ETHER));
    }

    public int insertEther(int amount) {
        int space    = MAX_ETHER - etherStored;
        int inserted = Math.min(space, amount);
        etherStored += inserted;
        markDirty();
        return inserted;
    }

    // ═══════════════════════════════════════════
    //  Прогресс для GUI
    // ═══════════════════════════════════════════
    public int getSmeltTime()  { return smeltTime; }
    public int getTotalSmelt() { return totalSmelt; }

    public int getSmeltProgressScaled(int scale) {
        if (totalSmelt == 0) return 0;
        return smeltTime * scale / totalSmelt;
    }

    public int getEtherScaled(int scale) {
        if (MAX_ETHER == 0) return 0;
        return etherStored * scale / MAX_ETHER;
    }

    public boolean isSmelting() {
        return smeltTime > 0;
    }

    // ═══════════════════════════════════════════
    //  IInventory
    // ═══════════════════════════════════════════
    @Override
    public int getSizeInventory() { return SIZE; }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (stack.getCount() <= count) {
            inventory.set(index, ItemStack.EMPTY);
            markDirty();
            return stack;
        }
        ItemStack split = stack.splitStack(count);
        markDirty();
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory.get(index);
        inventory.set(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) { return true; }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_INPUT) {
            return !FurnaceRecipes.instance()
                    .getSmeltingResult(stack).isEmpty();
        }
        return false;
    }

    // ═══════════════════════════════════════════
    //  Поля синхронизации — ВАЖНО для GUI
    // ═══════════════════════════════════════════
    @Override
    public int getField(int id) {
        switch (id) {
            case FIELD_ETHER: return etherStored;
            case FIELD_SMELT: return smeltTime;
            default:          return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case FIELD_ETHER: etherStored = value; break;
            case FIELD_SMELT: smeltTime   = value; break;
        }
    }

    @Override
    public int getFieldCount() { return FIELD_COUNT; }

    @Override
    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            inventory.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public String getName()         { return "resonance_furnace"; }

    @Override
    public boolean hasCustomName()  { return false; }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    public NonNullList<ItemStack> getInventory() { return inventory; }

    // ═══════════════════════════════════════════
    //  NBT
    // ═══════════════════════════════════════════
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("EtherStored", etherStored);
        compound.setInteger("SmeltTime",   smeltTime);

        for (int i = 0; i < SIZE; i++) {
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
        etherStored = compound.getInteger("EtherStored");
        smeltTime   = compound.getInteger("SmeltTime");

        for (int i = 0; i < SIZE; i++) {
            if (compound.hasKey("Slot" + i)) {
                inventory.set(i, new ItemStack(
                        compound.getCompoundTag("Slot" + i)));
            }
        }
    }

    // ═══════════════════════════════════════════
    //  Синхронизация
    // ═══════════════════════════════════════════
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(
                pos, 2, getUpdateTag()
        );
    }

    @Override
    public void onDataPacket(
            net.minecraft.network.NetworkManager net,
            net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}