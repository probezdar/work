package com.etherforge.mod.blocks;

import com.etherforge.mod.gui.ModGuiHandler;
import com.etherforge.mod.tileentity.TileEntityEtherCondenser;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEtherCondenser extends Block {

    public BlockEtherCondenser() {
        super(Material.IRON);
        setHardness(3.5f);
        setResistance(8.0f);
        setHarvestLevel("pickaxe", 1);
    }

    // ✅ Не полный куб — соседние блоки рендерят свои грани
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    // ✅ Не полный куб
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    // ✅ Но рендерим как обычный блок (не стекло)
    @Override
    public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
        return net.minecraft.util.EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityEtherCondenser();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX,
                                    float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(
                    com.etherforge.mod.EtherForge.instance,
                    ModGuiHandler.GUI_CONDENSER,
                    world,
                    pos.getX(), pos.getY(), pos.getZ()
            );
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
    }
}