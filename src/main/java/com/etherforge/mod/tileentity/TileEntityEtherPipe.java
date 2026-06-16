// tileentity/TileEntityEtherPipe.java
package com.etherforge.mod.tileentity;

import com.etherforge.mod.util.IEtherReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TileEntityEtherPipe extends TileEntity implements ITickable {

    // ═══════════════════════════════════════════
    //  Буфер трубы
    // ═══════════════════════════════════════════
    private int etherBuffer   = 0;
    private int maxBuffer     = 100; // базовый тир
    private int throughput    = 20;  // AE/tick — базовый тир

    private int tickCounter   = 0;
    private static final int TICK_RATE = 2; // работаем каждые 2 тика

    // ═══════════════════════════════════════════
    //  Конструктор для тиров
    // ═══════════════════════════════════════════
    public TileEntityEtherPipe() {
        this(20, 100);
    }

    public TileEntityEtherPipe(int throughput, int maxBuffer) {
        this.throughput = throughput;
        this.maxBuffer  = maxBuffer;
    }

    // ═══════════════════════════════════════════
    //  Tick
    // ═══════════════════════════════════════════
    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        tickCounter++;
        if (tickCounter < TICK_RATE) return;
        tickCounter = 0;

        // 1. Тянем эфир из источников (конденсаторов) рядом
        pullFromSources();

        // 2. Распределяем буфер по получателям
        if (etherBuffer > 0) {
            distributeEther();
        }
    }

    // ═══════════════════════════════════════════
    //  Тянем из конденсаторов и других труб
    // ═══════════════════════════════════════════
    private void pullFromSources() {
        if (etherBuffer >= maxBuffer) return;

        int canAccept = maxBuffer - etherBuffer;

        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos neighborPos = pos.offset(facing);
            TileEntity te = world.getTileEntity(neighborPos);

            // Тянем из конденсатора
            if (te instanceof TileEntityEtherCondenser) {
                TileEntityEtherCondenser condenser =
                        (TileEntityEtherCondenser) te;

                int toExtract = Math.min(canAccept, throughput);
                if (toExtract > 0 && condenser.getEtherStored() > 0) {
                    int extracted = condenser.extractEther(toExtract);
                    etherBuffer += extracted;
                    canAccept   -= extracted;
                    if (canAccept <= 0) break;
                }
            }

            // Тянем из другой трубы если у неё больше буфер
            // (выравниваем уровень между трубами)
            if (te instanceof TileEntityEtherPipe) {
                TileEntityEtherPipe otherPipe = (TileEntityEtherPipe) te;
                if (otherPipe.etherBuffer > etherBuffer + 1) {
                    int diff    = (otherPipe.etherBuffer - etherBuffer) / 2;
                    int toTake  = Math.min(diff, throughput);
                    toTake      = Math.min(toTake, canAccept);
                    if (toTake > 0) {
                        otherPipe.etherBuffer -= toTake;
                        etherBuffer           += toTake;
                        canAccept             -= toTake;
                        otherPipe.markDirty();
                        if (canAccept <= 0) break;
                    }
                }
            }
        }

        if (etherBuffer > 0) markDirty();
    }

    // ═══════════════════════════════════════════
    //  Распределяем эфир поровну между получателями
    // ═══════════════════════════════════════════
    private void distributeEther() {
        // Собираем всех получателей (машины с IEtherReceiver)
        List<IEtherReceiver> receivers = new ArrayList<>();

        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos neighborPos = pos.offset(facing);
            TileEntity te = world.getTileEntity(neighborPos);

            // Машины-получатели (не трубы и не конденсаторы-источники)
            if (te instanceof IEtherReceiver
                    && !(te instanceof TileEntityEtherPipe)
                    && !(te instanceof TileEntityEtherCondenser)) {

                IEtherReceiver receiver = (IEtherReceiver) te;
                if (receiver.canReceiveEther()) {
                    receivers.add(receiver);
                }
            }
        }

        if (receivers.isEmpty()) return;

        // Делим поровну
        int totalToSend  = Math.min(etherBuffer, throughput);
        int perReceiver  = totalToSend / receivers.size();
        if (perReceiver <= 0) perReceiver = 1;

        for (IEtherReceiver receiver : receivers) {
            if (etherBuffer <= 0) break;

            int toSend  = Math.min(perReceiver, etherBuffer);
            int accepted = receiver.receiveEther(toSend, false);
            etherBuffer -= accepted;
        }

        markDirty();
    }

    // ═══════════════════════════════════════════
    //  Геттеры
    // ═══════════════════════════════════════════
    public int getEtherBuffer()  { return etherBuffer; }
    public int getMaxBuffer()    { return maxBuffer; }
    public int getThroughput()   { return throughput; }

    // ═══════════════════════════════════════════
    //  NBT
    // ═══════════════════════════════════════════
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("EtherBuffer", etherBuffer);
        compound.setInteger("Throughput",  throughput);
        compound.setInteger("MaxBuffer",   maxBuffer);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        etherBuffer = compound.getInteger("EtherBuffer");
        throughput  = compound.getInteger("Throughput");
        maxBuffer   = compound.getInteger("MaxBuffer");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(
                pos, 4, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
                             net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}