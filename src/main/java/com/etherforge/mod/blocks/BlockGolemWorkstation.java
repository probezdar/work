// blocks/BlockGolemWorkstation.java
package com.etherforge.mod.blocks;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.entities.EntityEtherealGolem;
import com.etherforge.mod.entities.EntityMechGolem;
import com.etherforge.mod.entities.EntityMorphoGolem;
import com.etherforge.mod.items.ItemGolemCore;
import com.etherforge.mod.tileentity.TileEntityGolemWorkstation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockGolemWorkstation extends Block {

    public BlockGolemWorkstation() {
        super(Material.IRON);
        setHardness(3.5f);
        setResistance(8.0f);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityGolemWorkstation();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return true; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos,
                                    IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        ItemStack held = player.getHeldItem(hand);
        TileEntity te  = world.getTileEntity(pos);

        if (!(te instanceof TileEntityGolemWorkstation)) return true;
        TileEntityGolemWorkstation station =
                (TileEntityGolemWorkstation) te;

        // С ядром голема — создать голема
        if (!held.isEmpty() && held.getItem() instanceof ItemGolemCore) {
            ItemGolemCore core = (ItemGolemCore) held.getItem();
            spawnGolem(world, pos, core.getCoreType(), player);
            held.shrink(1);
            return true;
        }

        // Без предмета — показать статус
        player.sendMessage(new TextComponentString(
                "§6=== Стол Голема ==="));
        player.sendMessage(new TextComponentString(
                "§7Големов создано: §f" + station.getGolemCount()));
        player.sendMessage(new TextComponentString(
                "§7Используй Ядро Голема (ПКМ) для создания."));

        return true;
    }

    // ═══════════════════════════════════════════
    //  Спавн голема
    // ═══════════════════════════════════════════
    private void spawnGolem(World world, BlockPos pos,
                            ItemGolemCore.CoreType type,
                            EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityGolemWorkstation)) return;

        TileEntityGolemWorkstation station =
                (TileEntityGolemWorkstation) te;

        // Позиция спавна — рядом со столом
        double spawnX = pos.getX() + 0.5;
        double spawnY = pos.getY() + 1.0;
        double spawnZ = pos.getZ() + 0.5;

        switch (type) {
            case MECHANICAL: {
                EntityMechGolem golem = new EntityMechGolem(world);
                golem.setPosition(spawnX, spawnY, spawnZ);
                golem.setHomePos(pos); // дом = стол голема
                world.spawnEntity(golem);
                station.incrementGolemCount();
                player.sendMessage(new TextComponentString(
                        "§6Механический Голем создан!"));
                break;
            }
            case MORPHO: {
                EntityMorphoGolem golem = new EntityMorphoGolem(world);
                golem.setPosition(spawnX, spawnY, spawnZ);
                golem.setHomePos(pos);
                world.spawnEntity(golem);
                station.incrementGolemCount();
                player.sendMessage(new TextComponentString(
                        "§2Морфо Голем создан!"));
                break;
            }

            case ETHEREAL: {
                EntityEtherealGolem golem = new EntityEtherealGolem(world);
                golem.setPosition(spawnX, spawnY, spawnZ);
                golem.setHomePos(pos);
                world.spawnEntity(golem);
                station.incrementGolemCount();
                player.sendMessage(new TextComponentString(
                        "§5Эфирный Голем создан!"));
                break;
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
    }
}