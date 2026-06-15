package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityEtherCondenser;
import com.etherforge.mod.tileentity.TileEntityEtherWorkbench;
import com.etherforge.mod.tileentity.TileEntityResonanceFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ModGuiHandler implements IGuiHandler {

    public static final int GUI_CONDENSER = 0;
    public static final int GUI_FURNACE   = 1;  // ← новый
    public static final int GUI_WORKBENCH = 2;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

        if (ID == GUI_CONDENSER && te instanceof TileEntityEtherCondenser) {
            return new ContainerEtherCondenser(
                    player.inventory, (TileEntityEtherCondenser) te
            );
        }
        if (ID == GUI_FURNACE && te instanceof TileEntityResonanceFurnace) {
            return new ContainerResonanceFurnace(
                    player.inventory, (TileEntityResonanceFurnace) te
            );
        }
        if (ID == GUI_WORKBENCH && te instanceof TileEntityEtherWorkbench) {
            return new ContainerEtherWorkbench(player.inventory, (TileEntityEtherWorkbench) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

        if (ID == GUI_CONDENSER && te instanceof TileEntityEtherCondenser) {
            return new GuiEtherCondenser(
                    player.inventory, (TileEntityEtherCondenser) te
            );
        }
        if (ID == GUI_FURNACE && te instanceof TileEntityResonanceFurnace) {
            return new GuiResonanceFurnace(
                    player.inventory, (TileEntityResonanceFurnace) te
            );
        }
        if (ID == GUI_WORKBENCH && te instanceof TileEntityEtherWorkbench) {
            return new GuiEtherWorkbench(player.inventory, (TileEntityEtherWorkbench) te);
        }
        return null;
    }
}