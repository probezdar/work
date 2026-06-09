package com.etherforge.mod.blocks;

import com.etherforge.mod.gui.ModGuiHandler;
import com.etherforge.mod.tileentity.TileEntityResonanceFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockResonanceFurnace extends Block {

    public BlockResonanceFurnace() {
        super(Material.ROCK);
        setHardness(3.5f);
        setResistance(8.0f);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return true; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityResonanceFurnace();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX,
                                    float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(
                    com.etherforge.mod.EtherForge.instance,
                    ModGuiHandler.GUI_FURNACE,
                    world,
                    pos.getX(), pos.getY(), pos.getZ()
            );
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // Выбрасываем предметы из инвентаря при разрушении
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityResonanceFurnace) {
            TileEntityResonanceFurnace furnace = (TileEntityResonanceFurnace) te;
            for (ItemStack stack : furnace.getInventory()) {
                if (!stack.isEmpty()) {
                    spawnAsEntity(world, pos, stack);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }
}