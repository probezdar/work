package com.etherforge.mod.tileentity;

import com.etherforge.mod.util.IEtherReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityEtherPipe extends TileEntity implements ITickable {

    private int etherBuffer = 0;
    private int maxBuffer;
    private int throughput;
    private int tier;

    // Таймер для отдачи эфира машинам
    private int distributeTimer = 0;

    public TileEntityEtherPipe() {
        this(20, 100, 1);
    }

    public TileEntityEtherPipe(int throughput, int maxBuffer, int tier) {
        this.throughput = throughput;
        this.maxBuffer  = maxBuffer;
        this.tier       = tier;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        // Раздаём эфир машинам каждые 2 тика
        if (etherBuffer > 0) {
            distributeTimer++;
            if (distributeTimer >= 2) {
                distributeTimer = 0;
                distributeToMachines();
            }
        }
    }

    // ═══════════════════════════════════════════
    //  Принять эфир (вызывается конденсатором)
    // ═══════════════════════════════════════════
    public int receiveEther(int amount) {
        int space    = maxBuffer - etherBuffer;
        int received = Math.min(space, amount);
        etherBuffer += received;
        if (received > 0) markDirty();
        return received;
    }

    // ═══════════════════════════════════════════
    //  Отдать эфир машинам рядом
    // ═══════════════════════════════════════════
    private void distributeToMachines() {
        java.util.List<IEtherReceiver> receivers = new java.util.ArrayList<>();

        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(pos.offset(facing));

            if (te instanceof TileEntityEtherPipe) continue;
            if (te instanceof TileEntityEtherCondenser) continue;

            if (te instanceof IEtherReceiver) {
                IEtherReceiver r = (IEtherReceiver) te;
                if (r.canReceiveEther()) receivers.add(r);
            }
        }

        if (receivers.isEmpty()) return;

        int perReceiver = Math.max(1, Math.min(etherBuffer, throughput) / receivers.size());

        for (IEtherReceiver r : receivers) {
            if (etherBuffer <= 0) break;
            int sent = r.receiveEther(Math.min(perReceiver, etherBuffer), false);
            etherBuffer -= sent;
        }

        if (etherBuffer < 0) etherBuffer = 0;
        markDirty();
    }

    // ═══════════════════════════════════════════
    //  Геттеры
    // ═══════════════════════════════════════════
    public int getEtherBuffer()  { return etherBuffer; }
    public int getMaxBuffer()    { return maxBuffer; }
    public int getThroughput()   { return throughput; }
    public int getMyTier()       { return tier; }

    // ═══════════════════════════════════════════
    //  NBT
    // ═══════════════════════════════════════════
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("EtherBuffer", etherBuffer);
        compound.setInteger("Throughput",  throughput);
        compound.setInteger("MaxBuffer",   maxBuffer);
        compound.setInteger("Tier",        tier);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        etherBuffer = compound.getInteger("EtherBuffer");
        throughput  = compound.getInteger("Throughput");
        maxBuffer   = compound.getInteger("MaxBuffer");
        tier        = compound.getInteger("Tier");
        if (tier <= 0) tier = 1; // защита
        if (maxBuffer <= 0) maxBuffer = 100;
        if (throughput <= 0) throughput = 20;
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