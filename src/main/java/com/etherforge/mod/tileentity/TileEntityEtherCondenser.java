package com.etherforge.mod.tileentity;

import com.etherforge.mod.init.ModBlocks;
import com.etherforge.mod.util.IEtherReceiver;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityEtherCondenser extends TileEntity implements ITickable, IEtherReceiver {

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
    private int effectiveRate = 1;
    private int pushTimer = 0;

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

        tickCounter++;
        if (tickCounter >= TICKS_PER_SECOND) {
            tickCounter = 0;

            if (etherStored < MAX_ETHER) {
                int rate = etherPerTick;
                if (!world.isDaytime()) rate *= 2;
                effectiveRate = rate;
                etherStored = Math.min(etherStored + rate, MAX_ETHER);
                markDirty();
            }

            world.notifyBlockUpdate(pos,
                    world.getBlockState(pos),
                    world.getBlockState(pos), 3);
        }

// Толкаем эфир в сеть каждые 2 тика
        pushTimer++;
        if (pushTimer >= 2) {
            pushTimer = 0;
            if (etherStored > 0) {
                pushEtherToNetwork();
            }
        }
    }

    public int getEffectiveRate() {return effectiveRate;}

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

    @Override
    public int receiveEther(int amount, boolean simulate) {
        int space    = MAX_ETHER - etherStored;
        int received = Math.min(space, amount);
        if (!simulate) {
            etherStored += received;
            markDirty();
        }
        return received;
    }

    // ═══════════════════════════════════════════
//  BFS — толкаем эфир по сети труб
// ═══════════════════════════════════════════
    private void pushEtherToNetwork() {
        if (etherStored <= 0) return;

        // BFS обход сети труб
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();

        // Находим все трубы вокруг конденсатора
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos neighborPos = pos.offset(facing);
            TileEntity te = world.getTileEntity(neighborPos);

            if (te instanceof TileEntityEtherPipe) {
                queue.add(neighborPos);
                visited.add(neighborPos);
            }
        }

        if (queue.isEmpty()) return;

        // Определяем тир первой трубы
        TileEntity firstTE = world.getTileEntity(queue.peek());
        if (!(firstTE instanceof TileEntityEtherPipe)) return;
        int networkTier = ((TileEntityEtherPipe) firstTE).getMyTier();

        // Собираем все трубы сети одного тира
        java.util.List<TileEntityEtherPipe> network = new java.util.ArrayList<>();

        while (!queue.isEmpty() && etherStored > 0) {
            BlockPos current = queue.poll();
            TileEntity te = world.getTileEntity(current);

            if (!(te instanceof TileEntityEtherPipe)) continue;
            TileEntityEtherPipe pipe = (TileEntityEtherPipe) te;

            // Только трубы того же тира
            if (pipe.getMyTier() != networkTier) continue;

            network.add(pipe);

            // Добавляем соседей того же тира
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos neighborPos = current.offset(facing);
                if (visited.contains(neighborPos)) continue;
                visited.add(neighborPos);

                TileEntity neighbor = world.getTileEntity(neighborPos);
                if (neighbor instanceof TileEntityEtherPipe) {
                    TileEntityEtherPipe neighborPipe =
                            (TileEntityEtherPipe) neighbor;
                    if (neighborPipe.getMyTier() == networkTier) {
                        queue.add(neighborPos);
                    }
                }
            }
        }

        if (network.isEmpty()) return;

        // Распределяем эфир по трубам сети равномерно
        // Каждая труба получает пропорционально свободному месту
        int totalSpace = 0;
        for (TileEntityEtherPipe pipe : network) {
            totalSpace += (pipe.getMaxBuffer() - pipe.getEtherBuffer());
        }

        if (totalSpace <= 0) return;

        // Сколько можем отдать за этот тик
        int toDistribute = Math.min(etherStored,
                Math.min(TICKS_PER_SECOND * etherPerTick, totalSpace));

        if (toDistribute <= 0) return;

        // Толкаем в каждую трубу пропорционально
        int totalPushed = 0;
        for (TileEntityEtherPipe pipe : network) {
            if (totalPushed >= toDistribute) break;

            int space = pipe.getMaxBuffer() - pipe.getEtherBuffer();
            if (space <= 0) continue;

            // Пропорциональная доля
            int share = Math.max(1,
                    (int) ((float) space / totalSpace * toDistribute));
            share = Math.min(share, toDistribute - totalPushed);
            share = Math.min(share, space);

            int pushed = pipe.receiveEther(share);
            totalPushed += pushed;
        }

        etherStored -= totalPushed;
        if (etherStored < 0) etherStored = 0;
        markDirty();
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
        compound.setInteger("EffectiveRate",effectiveRate);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        etherStored  = compound.getInteger("EtherStored");
        etherPerTick = compound.getInteger("EtherPerTick");
        effectiveRate = compound.getInteger("EffectiveRate");
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