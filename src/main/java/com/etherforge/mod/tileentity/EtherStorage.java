package com.etherforge.mod.util;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Хранилище эфирной энергии.
 * Аналог EnergyStorage из RF API, но для нашего эфира.
 */
public class EtherStorage {

    private int etherStored;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;

    public EtherStorage(int capacity) {
        this(capacity, capacity, capacity);
    }

    public EtherStorage(int capacity, int maxReceive, int maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    // =================== Операции ===================

    /**
     * Добавляет эфир в хранилище.
     * @return сколько фактически добавлено
     */
    public int receiveEther(int amount, boolean simulate) {
        int received = Math.min(capacity - etherStored, Math.min(maxReceive, amount));
        if (!simulate) {
            etherStored += received;
        }
        return received;
    }

    /**
     * Извлекает эфир из хранилища.
     * @return сколько фактически извлечено
     */
    public int extractEther(int amount, boolean simulate) {
        int extracted = Math.min(etherStored, Math.min(maxExtract, amount));
        if (!simulate) {
            etherStored -= extracted;
        }
        return extracted;
    }

    // =================== Геттеры ===================

    public int getEtherStored()  { return etherStored; }
    public int getCapacity()     { return capacity; }
    public int getMaxReceive()   { return maxReceive; }
    public int getMaxExtract()   { return maxExtract; }

    public boolean isFull()      { return etherStored >= capacity; }
    public boolean isEmpty()     { return etherStored <= 0; }

    /** Уровень заполнения 0.0 - 1.0 */
    public float getFillFraction() {
        return capacity == 0 ? 0 : (float) etherStored / capacity;
    }

    // =================== NBT ===================

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("EtherStored", etherStored);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        etherStored = nbt.getInteger("EtherStored");
        // Ограничиваем значение на случай изменения capacity
        etherStored = Math.min(etherStored, capacity);
    }

    // Прямая установка (для синхронизации)
    public void setEtherStored(int amount) {
        this.etherStored = Math.min(Math.max(0, amount), capacity);
    }
}