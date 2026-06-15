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
    public EnumActionResult onItemUseFirst(EntityPlayer player,
                                           World world, BlockPos pos,
                                           EnumFacing facing, float hitX,
                                           float hitY, float hitZ,
                                           EnumHand hand) {
        if (world.isRemote) return EnumActionResult.PASS;

        IBlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof BlockEtherWorkbench)) {
            return EnumActionResult.PASS;
        }

        BlockEtherWorkbench.WorkbenchPart part = state.getValue(BlockEtherWorkbench.PART);
        boolean isActive = state.getValue(BlockEtherWorkbench.ACTIVE);

        if (isActive) {
            player.sendMessage(new TextComponentString(
                    "§5Верстак уже активирован."));
            return EnumActionResult.SUCCESS;
        }

        // Найти мастер-блок
        BlockPos masterPos = BlockEtherWorkbench.getMasterPos(world, pos, state);
        if (masterPos == null) {
            player.sendMessage(new TextComponentString(
                    "§cНе удалось определить мастер-блок."));
            return EnumActionResult.FAIL;
        }

        IBlockState masterState = world.getBlockState(masterPos);
        EnumFacing workbenchFacing = masterState.getValue(BlockEtherWorkbench.FACING);

        // Проверяем полноту мультиблока
        if (BlockEtherWorkbench.isComplete(world, masterPos, workbenchFacing)) {
            // Активируем
            BlockEtherWorkbench.activate(world, masterPos, workbenchFacing);
            player.sendMessage(new TextComponentString(
                    "§dЭфирный Верстак активирован!"));

            // Частицы
            world.spawnParticle(
                    net.minecraft.util.EnumParticleTypes.SPELL_MOB,
                    masterPos.getX() + 1.0,
                    masterPos.getY() + 1.5,
                    masterPos.getZ() + 1.0,
                    0.5, 0.3, 0.9, 20
            );
        } else {
            player.sendMessage(new TextComponentString(
                    "§cМультиблок неполный. Расставь 4 блока верстака квадратом 2x2."));
            // Подсветить какие блоки есть (отладка)
            highlightMissing(world, masterPos, workbenchFacing, player);
        }

        return EnumActionResult.SUCCESS;
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