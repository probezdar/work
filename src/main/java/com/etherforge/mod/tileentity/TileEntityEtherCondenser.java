package com.etherforge.mod.tileentity;

import com.etherforge.mod.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityEtherCondenser extends TileEntity implements ITickable {

    // ═══════════════════════════════════════════
    //  Константы
    // ═══════════════════════════════════════════
    public static final int MAX_ETHER          = 10000;
    public static final int TICKS_PER_SECOND   = 20;
    public static final int SCAN_RADIUS        = 3; // радиус сканирования

    // ═══════════════════════════════════════════
    //  Данные
    // ═══════════════════════════════════════════
    private int etherStored   = 0;
    private int tickCounter   = 0;
    private int etherPerTick  = 1;   // текущая скорость (обновляется каждые 5 сек)
    private int scanTimer     = 0;   // таймер сканирования окружения

    // ═══════════════════════════════════════════
    //  Tick
    // ═══════════════════════════════════════════
    @Override
    public void update() {
        if (world.isRemote) return;

        // Сканируем окружение каждые 5 секунд
        scanTimer++;
        if (scanTimer >= TICKS_PER_SECOND * 5) {
            scanTimer = 0;
            etherPerTick = calculateEtherRate();
        }

        // Накапливаем эфир каждую секунду
        tickCounter++;
        if (tickCounter >= TICKS_PER_SECOND) {
            tickCounter = 0;

            if (etherStored < MAX_ETHER) {
                // Ночной множитель
                int rate = etherPerTick;
                if (!world.isDaytime()) {
                    rate *= 2;
                }
                etherStored = Math.min(etherStored + rate, MAX_ETHER);
                markDirty();

                // Синхронизируем с клиентом
                world.notifyBlockUpdate(pos,
                        world.getBlockState(pos),
                        world.getBlockState(pos),
                        3
                );
            }
        }
    }

    // ═══════════════════════════════════════════
    //  Расчёт скорости накопления
    // ═══════════════════════════════════════════
    private int calculateEtherRate() {
        int rate = 1; // базовая скорость

        // Сканируем блоки в радиусе
        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos scanPos = pos.add(x, y, z);
                    Block block = world.getBlockState(scanPos).getBlock();

                    if (block == ModBlocks.ETHER_BLOCK) {
                        rate += 5;
                    } else if (block == ModBlocks.ETHER_ORE) {
                        rate += 2;
                    } else if (block == ModBlocks.ETHER_ORE_IGNIS) {
                        rate += 3;
                    } else if (block == ModBlocks.ETHER_ORE_UMBRA) {
                        rate += 3;
                    }
                }
            }
        }

        return Math.min(rate, 100); // максимум 100/сек
    }

    // ═══════════════════════════════════════════
    //  Геттеры / Сеттеры
    // ═══════════════════════════════════════════
    public int getEtherStored()    { return etherStored; }
    public int getMaxEther()       { return MAX_ETHER; }
    public int getEtherPerTick()   { return etherPerTick; }

    public void setEtherStored(int amount) {
        this.etherStored = Math.max(0, Math.min(amount, MAX_ETHER));
    }

    public int extractEther(int amount) {
        int extracted = Math.min(etherStored, amount);
        etherStored -= extracted;
        markDirty();
        return extracted;
    }

    public int insertEther(int amount) {
        int space = MAX_ETHER - etherStored;
        int inserted = Math.min(space, amount);
        etherStored += inserted;
        markDirty();
        return inserted;
    }

    public int getFillPercentage() {
        return (int) ((etherStored / (float) MAX_ETHER) * 100);
    }

    // ═══════════════════════════════════════════
    //  NBT
    // ═══════════════════════════════════════════
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("EtherStored",  etherStored);
        compound.setInteger("EtherPerTick", etherPerTick);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        etherStored  = compound.getInteger("EtherStored");
        etherPerTick = compound.getInteger("EtherPerTick");
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
                pos, 1, getUpdateTag()
        );
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
                             net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}