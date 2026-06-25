// tileentity/TileEntityGolemWorkstation.java
package com.etherforge.mod.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityGolemWorkstation extends TileEntity {

    private int golemCount = 0;

    public int getGolemCount()    { return golemCount; }

    public void incrementGolemCount() {
        golemCount++;
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("GolemCount", golemCount);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        golemCount = compound.getInteger("GolemCount");
    }
}