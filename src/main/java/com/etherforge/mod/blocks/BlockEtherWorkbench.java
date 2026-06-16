package com.etherforge.mod.blocks;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.gui.ModGuiHandler;
import com.etherforge.mod.tileentity.TileEntityEtherWorkbench;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEtherWorkbench extends Block {

    // Часть мультиблока
    public enum WorkbenchPart implements IStringSerializable {
        TL("tl"), TR("tr"), BL("bl"), BR("br");
        private final String name;
        WorkbenchPart(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }

    public static final PropertyEnum<WorkbenchPart> PART =
            PropertyEnum.create("part", WorkbenchPart.class);
    public static final PropertyBool ACTIVE =
            PropertyBool.create("active");
    public static final PropertyDirection FACING =
            PropertyDirection.create("facing",
                    EnumFacing.Plane.HORIZONTAL);

    public BlockEtherWorkbench() {
        super(Material.WOOD);
        setHardness(3.5f);
        setResistance(8.0f);
        setHarvestLevel("axe", 1);
        setDefaultState(blockState.getBaseState()
                .withProperty(PART,   WorkbenchPart.TL)
                .withProperty(ACTIVE, false)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    // ═══════════════════════════════════════════
    //  BlockState
    // ═══════════════════════════════════════════
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, PART, ACTIVE, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        // meta 0-3 = часть, bit 2 = активен, bit 3 = facing(2 бита)
        WorkbenchPart part = WorkbenchPart.values()[meta & 3];
        return getDefaultState().withProperty(PART, part)
                .withProperty(ACTIVE, false)
                .withProperty(FACING, EnumFacing.NORTH);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(PART).ordinal();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
        return net.minecraft.util.EnumBlockRenderType.MODEL;
    }

    // ═══════════════════════════════════════════
    //  TileEntity только у TL (мастер)
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
    //  Установка блока
    // ═══════════════════════════════════════════
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos,
                                            EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        EnumFacing playerFacing = placer.getHorizontalFacing().getOpposite();
        return getDefaultState()
                .withProperty(PART,   WorkbenchPart.TL)
                .withProperty(ACTIVE, false)
                .withProperty(FACING, playerFacing);
    }

    // ═══════════════════════════════════════════
    //  Клик — открыть GUI только если активен
    // ═══════════════════════════════════════════
    @Override
    public boolean onBlockActivated(World world, BlockPos pos,
                                    IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {

        // Эфироскоп обрабатывается в ItemEtherscope
        if (!world.isRemote) {
            if (state.getValue(ACTIVE)) {
                // Найти мастер-блок
                BlockPos masterPos = getMasterPos(world, pos, state);
                if (masterPos != null) {
                    player.openGui(EtherForge.instance,
                            ModGuiHandler.GUI_WORKBENCH,
                            world,
                            masterPos.getX(),
                            masterPos.getY(),
                            masterPos.getZ());
                }
            } else {
                player.sendMessage(new net.minecraft.util.text.TextComponentString(
                        "§5Верстак не активирован. Используй Эфироскоп."));
            }
        }
        return true;
    }

    // ═══════════════════════════════════════════
    //  Найти мастер-блок (TL) от любой части
    // ═══════════════════════════════════════════
    public static BlockPos getMasterPos(World world, BlockPos pos,
                                        IBlockState state) {
        EnumFacing facing  = state.getValue(FACING);
        WorkbenchPart part = state.getValue(PART);
        EnumFacing right   = facing.rotateY();

        // Смещения от каждой части до TL
        switch (part) {
            case TL: return pos;
            case TR: return pos.offset(right.getOpposite());
            case BL: return pos.offset(facing.getOpposite());
            case BR: return pos.offset(right.getOpposite())
                    .offset(facing.getOpposite());
            default: return null;
        }
    }

    // ═══════════════════════════════════════════
    //  Проверка полного мультиблока
    // ═══════════════════════════════════════════
    public static boolean isComplete(World world, BlockPos masterPos,
                                     EnumFacing facing) {
        EnumFacing right = facing.rotateY();
        BlockPos tr = masterPos.offset(right);
        BlockPos bl = masterPos.offset(facing.getOpposite());
        BlockPos br = masterPos.offset(right).offset(facing.getOpposite());

        return isWorkbenchPart(world, masterPos, WorkbenchPart.TL, facing)
                && isWorkbenchPart(world, tr,         WorkbenchPart.TR, facing)
                && isWorkbenchPart(world, bl,         WorkbenchPart.BL, facing)
                && isWorkbenchPart(world, br,         WorkbenchPart.BR, facing);
    }

    public static boolean tryActivateFromEtherscope(World world,
                                                    BlockPos clickedPos,
                                                    EnumFacing facing,
                                                    EntityPlayer player) {
        if (world.isRemote) return false;

        EnumFacing right = facing.rotateY();

        /*
         * Относительно masterPos:
         *
         * TL = masterPos
         * TR = masterPos + right
         * BL = masterPos + facing.opposite
         * BR = masterPos + right + facing.opposite
         *
         * Но игрок может кликнуть по любому из 4 блоков.
         * Поэтому пробуем 4 возможных masterPos.
         */

        BlockPos[] possibleMasters = new BlockPos[] {
                clickedPos,                                            // кликнули по TL
                clickedPos.offset(right.getOpposite()),                // кликнули по TR
                clickedPos.offset(facing),                             // кликнули по BL
                clickedPos.offset(right.getOpposite()).offset(facing)  // кликнули по BR
        };

        for (BlockPos masterPos : possibleMasters) {
            if (canFormAt(world, masterPos, facing)) {
                formAt(world, masterPos, facing);

                player.sendMessage(new net.minecraft.util.text.TextComponentString(
                        "§dЭфирный Верстак собран."
                ));

                world.playSound(
                        null,
                        masterPos,
                        net.minecraft.util.SoundEvent.REGISTRY.getObject(
                                new net.minecraft.util.ResourceLocation(
                                        "block.enchantment_table.use"
                                )
                        ),
                        net.minecraft.util.SoundCategory.BLOCKS,
                        1.0f,
                        0.8f
                );

                return true;
            }
        }

        player.sendMessage(new net.minecraft.util.text.TextComponentString(
                "§cМультиблок неполный. Нужно поставить 4 блока верстака квадратом 2x2."
        ));

        player.sendMessage(new net.minecraft.util.text.TextComponentString(
                "§7Подсказка: смотри на конструкцию с той стороны, где должен быть перед верстака, и нажми эфироскопом."
        ));

        return false;
    }

    private static boolean canFormAt(World world,
                                     BlockPos masterPos,
                                     EnumFacing facing) {
        EnumFacing right = facing.rotateY();

        BlockPos tl = masterPos;
        BlockPos tr = masterPos.offset(right);
        BlockPos bl = masterPos.offset(facing.getOpposite());
        BlockPos br = masterPos.offset(right).offset(facing.getOpposite());

        return isAnyWorkbench(world, tl)
                && isAnyWorkbench(world, tr)
                && isAnyWorkbench(world, bl)
                && isAnyWorkbench(world, br);
    }

    private static boolean isAnyWorkbench(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockEtherWorkbench;
    }

    private static void formAt(World world,
                               BlockPos masterPos,
                               EnumFacing facing) {
        EnumFacing right = facing.rotateY();

        BlockPos tl = masterPos;
        BlockPos tr = masterPos.offset(right);
        BlockPos bl = masterPos.offset(facing.getOpposite());
        BlockPos br = masterPos.offset(right).offset(facing.getOpposite());

        BlockEtherWorkbench block = (BlockEtherWorkbench) world.getBlockState(masterPos).getBlock();

        world.setBlockState(tl, block.getDefaultState()
                .withProperty(PART, WorkbenchPart.TL)
                .withProperty(FACING, facing)
                .withProperty(ACTIVE, true), 3);

        world.setBlockState(tr, block.getDefaultState()
                .withProperty(PART, WorkbenchPart.TR)
                .withProperty(FACING, facing)
                .withProperty(ACTIVE, true), 3);

        world.setBlockState(bl, block.getDefaultState()
                .withProperty(PART, WorkbenchPart.BL)
                .withProperty(FACING, facing)
                .withProperty(ACTIVE, true), 3);

        world.setBlockState(br, block.getDefaultState()
                .withProperty(PART, WorkbenchPart.BR)
                .withProperty(FACING, facing)
                .withProperty(ACTIVE, true), 3);

        // На всякий случай гарантируем TileEntity у мастер-блока
        if (!(world.getTileEntity(tl) instanceof TileEntityEtherWorkbench)) {
            world.setTileEntity(tl, new TileEntityEtherWorkbench());
        }

        world.notifyBlockUpdate(tl, world.getBlockState(tl), world.getBlockState(tl), 3);
        world.notifyBlockUpdate(tr, world.getBlockState(tr), world.getBlockState(tr), 3);
        world.notifyBlockUpdate(bl, world.getBlockState(bl), world.getBlockState(bl), 3);
        world.notifyBlockUpdate(br, world.getBlockState(br), world.getBlockState(br), 3);
    }

    private static boolean isWorkbenchPart(World world, BlockPos pos,
                                           WorkbenchPart part,
                                           EnumFacing facing) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockEtherWorkbench
                && state.getValue(PART)   == part
                && state.getValue(FACING) == facing;
    }

    // ═══════════════════════════════════════════
    //  Активация всех 4 блоков
    // ═══════════════════════════════════════════
    public static void activate(World world, BlockPos masterPos,
                                EnumFacing facing) {
        EnumFacing right = facing.rotateY();
        BlockPos tr = masterPos.offset(right);
        BlockPos bl = masterPos.offset(facing.getOpposite());
        BlockPos br = masterPos.offset(right).offset(facing.getOpposite());

        setActive(world, masterPos, true);
        setActive(world, tr,        true);
        setActive(world, bl,        true);
        setActive(world, br,        true);

        world.playSound(null, masterPos,
                net.minecraft.util.SoundEvent.REGISTRY.getObject(
                        new net.minecraft.util.ResourceLocation(
                                "block.enchantment_table.use")),
                net.minecraft.util.SoundCategory.BLOCKS,
                1.0f, 0.8f);
    }

    // ═══════════════════════════════════════════
    //  Деактивация всех 4 блоков
    // ═══════════════════════════════════════════
    public static void deactivate(World world, BlockPos masterPos,
                                  EnumFacing facing) {
        EnumFacing right = facing.rotateY();
        BlockPos tr = masterPos.offset(right);
        BlockPos bl = masterPos.offset(facing.getOpposite());
        BlockPos br = masterPos.offset(right).offset(facing.getOpposite());

        setActive(world, masterPos, false);
        setActive(world, tr,        false);
        setActive(world, bl,        false);
        setActive(world, br,        false);
    }

    private static void setActive(World world, BlockPos pos, boolean active) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockEtherWorkbench) {
            world.setBlockState(pos,
                    state.withProperty(ACTIVE, active), 3);
        }
    }

    // ═══════════════════════════════════════════
    //  При разрушении — деактивировать всё
    // ═══════════════════════════════════════════
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        BlockPos masterPos = getMasterPos(world, pos, state);
        EnumFacing facing  = state.getValue(FACING);

        if (masterPos != null && state.getValue(ACTIVE)) {
            // Выбросить содержимое
            TileEntity te = world.getTileEntity(masterPos);
            if (te instanceof TileEntityEtherWorkbench) {
                TileEntityEtherWorkbench bench =
                        (TileEntityEtherWorkbench) te;
                for (int i = 0; i < bench.getSizeInventory() - 1; i++) {
                    ItemStack stack = bench.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        world.spawnEntity(new EntityItem(world,
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5, stack));
                    }
                }
            }
            deactivate(world, masterPos, facing);
        }
        super.breakBlock(world, pos, state);
    }
}