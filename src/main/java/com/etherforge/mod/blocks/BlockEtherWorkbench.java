package com.etherforge.mod.blocks;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.gui.ModGuiHandler;
import com.etherforge.mod.items.ItemEtherscope;
import com.etherforge.mod.tileentity.TileEntityEtherWorkbench;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockEtherWorkbench extends Block {

    public enum WorkbenchPart implements IStringSerializable {
        TL("tl"), TR("tr"), BL("bl"), BR("br");
        private final String name;
        WorkbenchPart(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }

    public static final PropertyEnum<WorkbenchPart> PART =
            PropertyEnum.create("part", WorkbenchPart.class);
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockEtherWorkbench() {
        super(Material.WOOD);
        setHardness(3.5f);
        setResistance(8.0f);
        setHarvestLevel("axe", 1);
        setDefaultState(blockState.getBaseState()
                .withProperty(PART,   WorkbenchPart.TL)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    // ═══════════════════════════════════════════
    //  BlockState — PART(2 бита) + FACING(2 бита)
    // ═══════════════════════════════════════════
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PART, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int part   = state.getValue(PART).ordinal();          // 0-3
        int facing = state.getValue(FACING).getHorizontalIndex(); // 0-3
        return part | (facing << 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        WorkbenchPart part = WorkbenchPart.values()[meta & 3];
        EnumFacing facing  = EnumFacing.getHorizontal((meta >> 2) & 3);
        return getDefaultState()
                .withProperty(PART, part)
                .withProperty(FACING, facing);
    }

    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state)   { return false; }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    // ═══════════════════════════════════════════
    //  TileEntity только у TL
    // ═══════════════════════════════════════════
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(PART) == WorkbenchPart.TL;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        if (state.getValue(PART) == WorkbenchPart.TL) {
            return new TileEntityEtherWorkbench();
        }
        return null;
    }

    // ═══════════════════════════════════════════
    //  Установка — заготовка TL, facing на игрока
    // ═══════════════════════════════════════════
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos,
                                            EnumFacing facing, float hitX,
                                            float hitY, float hitZ, int meta,
                                            EntityLivingBase placer,
                                            EnumHand hand) {
        EnumFacing playerFacing = placer.getHorizontalFacing().getOpposite();
        return getDefaultState()
                .withProperty(PART, WorkbenchPart.TL)
                .withProperty(FACING, playerFacing);
    }

    // ═══════════════════════════════════════════
    //  Клик — открыть GUI если активен
    // ═══════════════════════════════════════════
    @Override
    public boolean onBlockActivated(World world, BlockPos pos,
                                    IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {

        // Эфироскоп обрабатывается отдельно
        Item held = player.getHeldItem(hand).getItem();
        if (held instanceof ItemEtherscope) {
            return false;
        }

        if (!world.isRemote) {
            BlockPos masterPos = getMasterPos(pos, state);
            TileEntity te = world.getTileEntity(masterPos);

            if (te instanceof TileEntityEtherWorkbench
                    && ((TileEntityEtherWorkbench) te).isActive()) {
                player.openGui(EtherForge.instance,
                        ModGuiHandler.GUI_WORKBENCH,
                        world,
                        masterPos.getX(),
                        masterPos.getY(),
                        masterPos.getZ());
            } else {
                player.sendMessage(new TextComponentString(
                        "§5Верстак не активирован. Используй Эфироскоп."));
            }
        }
        return true;
    }

    // ═══════════════════════════════════════════
    //  Мастер-блок (TL) от любой части
    // ═══════════════════════════════════════════
    public static BlockPos getMasterPos(BlockPos pos, IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        WorkbenchPart part = state.getValue(PART);
        EnumFacing right  = facing.rotateY();

        switch (part) {
            case TL: return pos;
            case TR: return pos.offset(right.getOpposite());          // от TR идём влево → TL
            case BL: return pos.offset(facing);                       // от BL идём вперёд → TL
            case BR: return pos.offset(right.getOpposite())
                    .offset(facing);                       // от BR влево+вперёд → TL
            default: return pos;
        }
    }

    // ═══════════════════════════════════════════
    //  Эфироскоп — попытка собрать
    // ═══════════════════════════════════════════
    public static boolean tryActivateFromEtherscope(World world,
                                                    BlockPos clickedPos,
                                                    EnumFacing facing,
                                                    EntityPlayer player) {
        if (world.isRemote) return false;

        EnumFacing right = facing.rotateY();

        // Кликнутый блок может быть любой из 4 частей.
        // Вычисляем возможный masterPos для каждого случая:
        //
        // Если кликнули по TL → master = clickedPos
        // Если кликнули по TR → master = clickedPos - right
        // Если кликнули по BL → master = clickedPos + facing
        // Если кликнули по BR → master = clickedPos - right + facing

        BlockPos[] candidates = {
                clickedPos,                                    // TL
                clickedPos.offset(right.getOpposite()),        // TR
                clickedPos.offset(facing),                     // BL
                clickedPos.offset(right.getOpposite())
                        .offset(facing)                      // BR
        };

        for (BlockPos masterPos : candidates) {
            // Кандидат валиден только если masterPos — это верстак
            if (!isWorkbenchAt(world, masterPos)) continue;

            // Все 4 блока должны быть верстаком
            if (!canFormAt(world, masterPos, facing)) continue;

            // Проверяем что ни один из 4 блоков не занят другим активным верстаком
            if (anyPartBelongsToActiveWorkbench(world, masterPos, facing)) {
                player.sendMessage(new TextComponentString(
                        "§cОдин из блоков уже принадлежит другому верстаку."));
                continue;
            }

            formAt(world, masterPos, facing);
            player.sendMessage(new TextComponentString(
                    "§dЭфирный Верстак собран."));
            world.playSound(null, masterPos,
                    SoundEvent.REGISTRY.getObject(new ResourceLocation(
                            "block.enchantment_table.use")),
                    SoundCategory.BLOCKS, 1.0f, 0.8f);
            return true;
        }

        player.sendMessage(new TextComponentString(
                "§cНужно 4 блока верстака квадратом 2x2 (свободных)."));
        return false;
    }

    // Вспомогательный — проверяем один блок
    private static boolean isWorkbenchAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BlockEtherWorkbench;
    }

    // Проверяем занятость всех 4 блоков будущего верстака
    private static boolean anyPartBelongsToActiveWorkbench(World world,
                                                           BlockPos masterPos,
                                                           EnumFacing facing) {
        EnumFacing right = facing.rotateY();
        BlockPos[] parts = {
                masterPos,
                masterPos.offset(right),
                masterPos.offset(facing.getOpposite()),
                masterPos.offset(right).offset(facing.getOpposite())
        };

        for (BlockPos p : parts) {
            IBlockState s = world.getBlockState(p);
            if (!(s.getBlock() instanceof BlockEtherWorkbench)) continue;

            // Получаем мастера этого блока по его текущему state
            BlockPos existingMaster = getMasterPos(p, s);
            TileEntity te = world.getTileEntity(existingMaster);
            if (te instanceof TileEntityEtherWorkbench
                    && ((TileEntityEtherWorkbench) te).isActive()) {
                return true; // занят
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════
    //  Проверка: все 4 блока — верстак?
    // ═══════════════════════════════════════════
    public static boolean canFormAt(World world, BlockPos masterPos,
                                    EnumFacing facing) {
        EnumFacing right = facing.rotateY();
        BlockPos tr = masterPos.offset(right);
        BlockPos bl = masterPos.offset(facing.getOpposite());
        BlockPos br = masterPos.offset(right).offset(facing.getOpposite());

        return isWorkbench(world, masterPos)
                && isWorkbench(world, tr)
                && isWorkbench(world, bl)
                && isWorkbench(world, br);
    }


    private static boolean isWorkbench(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock()
                instanceof BlockEtherWorkbench;
    }


    // ═══════════════════════════════════════════
    //  Сборка — расставить PART, активировать TE
    // ═══════════════════════════════════════════
    public static void formAt(World world, BlockPos masterPos,
                              EnumFacing facing) {
        EnumFacing right = facing.rotateY();
        BlockPos tl = masterPos;
        BlockPos tr = masterPos.offset(right);
        BlockPos bl = masterPos.offset(facing.getOpposite());
        BlockPos br = masterPos.offset(right).offset(facing.getOpposite());

        BlockEtherWorkbench block =
                (BlockEtherWorkbench) world.getBlockState(masterPos).getBlock();

        setPart(world, tl, block, WorkbenchPart.TL, facing);
        setPart(world, tr, block, WorkbenchPart.TR, facing);
        setPart(world, bl, block, WorkbenchPart.BL, facing);
        setPart(world, br, block, WorkbenchPart.BR, facing);

        removeNonMasterTE(world, tr);
        removeNonMasterTE(world, bl);
        removeNonMasterTE(world, br);

        if (!(world.getTileEntity(tl) instanceof TileEntityEtherWorkbench)) {
            world.setTileEntity(tl, new TileEntityEtherWorkbench());
        }
        TileEntityEtherWorkbench te =
                (TileEntityEtherWorkbench) world.getTileEntity(tl);
        te.setActiveData(true, facing);
    }

    private static void setPart(World world, BlockPos pos,
                                BlockEtherWorkbench block,
                                WorkbenchPart part, EnumFacing facing) {
        world.setBlockState(pos, block.getDefaultState()
                .withProperty(PART, part)
                .withProperty(FACING, facing), 3);
    }

    private static void removeNonMasterTE(World world, BlockPos pos) {
        if (world.getTileEntity(pos) instanceof TileEntityEtherWorkbench) {
            world.removeTileEntity(pos);
        }
    }

    // ═══════════════════════════════════════════
    //  Разрушение — разобрать весь верстак
    // ═══════════════════════════════════════════
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        BlockPos masterPos = getMasterPos(pos, state);
        TileEntity te = world.getTileEntity(masterPos);

        if (te instanceof TileEntityEtherWorkbench
                && ((TileEntityEtherWorkbench) te).isActive()) {

            TileEntityEtherWorkbench bench = (TileEntityEtherWorkbench) te;

            // Выбросить содержимое (кроме результата)
            for (int i = 0; i < bench.getSizeInventory() - 1; i++) {
                ItemStack stack = bench.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    world.spawnEntity(new EntityItem(world,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5, stack));
                }
            }
            bench.setActiveData(false, state.getValue(FACING));
        }
        super.breakBlock(world, pos, state);
    }
}