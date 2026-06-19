package com.etherforge.mod.items;

import com.etherforge.mod.blocks.BlockEtherWorkbench;
import com.etherforge.mod.init.ModCreativeTab;
import com.etherforge.mod.util.Reference;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemEtherscope extends Item {

    public ItemEtherscope() {
        setRegistryName(Reference.MOD_ID, "etherscope");
        setUnlocalizedName(Reference.MOD_ID + ".etherscope");
        setCreativeTab(ModCreativeTab.INSTANCE);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world,
                                           BlockPos pos, EnumFacing side,
                                           float hitX, float hitY, float hitZ,
                                           EnumHand hand) {
        if (world.isRemote) return EnumActionResult.PASS;

        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockEtherWorkbench)) {
            return EnumActionResult.PASS;
        }

        // Проверяем не активен ли уже
        BlockPos masterPos = BlockEtherWorkbench.getMasterPos(pos, state);
        net.minecraft.tileentity.TileEntity te = world.getTileEntity(masterPos);
        if (te instanceof com.etherforge.mod.tileentity.TileEntityEtherWorkbench
                && ((com.etherforge.mod.tileentity.TileEntityEtherWorkbench) te).isActive()) {
            player.sendMessage(new TextComponentString(
                    "§5Эфирный Верстак уже активирован."));
            return EnumActionResult.SUCCESS;
        }

        EnumFacing workbenchFacing = player.getHorizontalFacing().getOpposite();
        boolean success = BlockEtherWorkbench.tryActivateFromEtherscope(
                world, pos, workbenchFacing, player);

        return success ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

    private void highlightMissing(World world, BlockPos masterPos,
                                  EnumFacing facing, EntityPlayer player) {
        EnumFacing right = facing.rotateY();
        BlockPos[] positions = {
                masterPos,
                masterPos.offset(right),
                masterPos.offset(facing.getOpposite()),
                masterPos.offset(right).offset(facing.getOpposite())
        };
        String[] names = {"ЛЕВ-ПЕРЕД", "ПРАВ-ПЕРЕД", "ЛЕВ-ЗАД", "ПРАВ-ЗАД"};

        for (int i = 0; i < 4; i++) {
            IBlockState s = world.getBlockState(positions[i]);
            if (!(s.getBlock() instanceof BlockEtherWorkbench)) {
                player.sendMessage(new TextComponentString(
                        "§c  Отсутствует блок: " + names[i] +
                                " (" + positions[i].getX() + "," +
                                positions[i].getY() + "," +
                                positions[i].getZ() + ")"));
            }
        }
    }
}