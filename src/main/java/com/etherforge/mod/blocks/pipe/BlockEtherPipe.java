// blocks/pipe/BlockEtherPipe.java
package com.etherforge.mod.blocks.pipe;

import com.etherforge.mod.tileentity.TileEntityEtherCondenser;
import com.etherforge.mod.tileentity.TileEntityEtherPipe;
import com.etherforge.mod.util.IEtherReceiver;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEtherPipe extends Block {

    // ═══════════════════════════════════════════
    //  6 свойств подключения
    // ═══════════════════════════════════════════
    public static final PropertyBool NORTH =
            PropertyBool.create("north");
    public static final PropertyBool SOUTH =
            PropertyBool.create("south");
    public static final PropertyBool EAST  =
            PropertyBool.create("east");
    public static final PropertyBool WEST  =
            PropertyBool.create("west");
    public static final PropertyBool UP    =
            PropertyBool.create("up");
    public static final PropertyBool DOWN  =
            PropertyBool.create("down");

    // ═══════════════════════════════════════════
    //  AABB — центральный куб + рукава
    // ═══════════════════════════════════════════
    // Центр трубы 6/16 до 10/16
    private static final float PIPE_MIN = 6f / 16f;
    private static final float PIPE_MAX = 10f / 16f;

    private static final AxisAlignedBB AABB_CENTER =
            new AxisAlignedBB(PIPE_MIN, PIPE_MIN, PIPE_MIN,
                    PIPE_MAX, PIPE_MAX, PIPE_MAX);

    private static final AxisAlignedBB AABB_NORTH =
            new AxisAlignedBB(PIPE_MIN, PIPE_MIN, 0,
                    PIPE_MAX, PIPE_MAX, PIPE_MIN);
    private static final AxisAlignedBB AABB_SOUTH =
            new AxisAlignedBB(PIPE_MIN, PIPE_MIN, PIPE_MAX,
                    PIPE_MAX, PIPE_MAX, 1);
    private static final AxisAlignedBB AABB_WEST =
            new AxisAlignedBB(0, PIPE_MIN, PIPE_MIN,
                    PIPE_MIN, PIPE_MAX, PIPE_MAX);
    private static final AxisAlignedBB AABB_EAST =
            new AxisAlignedBB(PIPE_MAX, PIPE_MIN, PIPE_MIN,
                    1, PIPE_MAX, PIPE_MAX);
    private static final AxisAlignedBB AABB_DOWN =
            new AxisAlignedBB(PIPE_MIN, 0, PIPE_MIN,
                    PIPE_MAX, PIPE_MIN, PIPE_MAX);
    private static final AxisAlignedBB AABB_UP =
            new AxisAlignedBB(PIPE_MIN, PIPE_MAX, PIPE_MIN,
                    PIPE_MAX, 1, PIPE_MAX);

    // ═══════════════════════════════════════════
    //  Параметры тира
    // ═══════════════════════════════════════════
    protected final int throughput;
    protected final int maxBuffer;

    public BlockEtherPipe(int throughput, int maxBuffer) {
        super(Material.IRON);
        this.throughput = throughput;
        this.maxBuffer  = maxBuffer;
        setHardness(1.5f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 1);

        setDefaultState(blockState.getBaseState()
                .withProperty(NORTH, false)
                .withProperty(SOUTH, false)
                .withProperty(EAST,  false)
                .withProperty(WEST,  false)
                .withProperty(UP,    false)
                .withProperty(DOWN,  false));
    }

    // ═══════════════════════════════════════════
    //  BlockState
    // ═══════════════════════════════════════════
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this,
                NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        // Мы используем multipart/getActualState,
        // meta не хранит ничего значимого
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos,
                                IBlockState state,
                                net.minecraft.entity.EntityLivingBase placer,
                                net.minecraft.item.ItemStack stack) {

        // Принудительно обновить renderRange для себя и соседей
        world.markBlockRangeForRenderUpdate(
                pos.add(-1, -1, -1),
                pos.add(1, 1, 1)
        );

        // Обновить всех соседей (они видят новое соединение с нами)
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos n = pos.offset(facing);
            IBlockState ns = world.getBlockState(n);
            world.notifyBlockUpdate(n, ns, ns, 3);
        }
    }

    // Реальное состояние берём из мира
    @Override
    public IBlockState getActualState(IBlockState state,
                                      IBlockAccess world,
                                      BlockPos pos) {
        return state
                .withProperty(NORTH, canConnect(world, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, canConnect(world, pos, EnumFacing.SOUTH))
                .withProperty(EAST,  canConnect(world, pos, EnumFacing.EAST))
                .withProperty(WEST,  canConnect(world, pos, EnumFacing.WEST))
                .withProperty(UP,    canConnect(world, pos, EnumFacing.UP))
                .withProperty(DOWN,  canConnect(world, pos, EnumFacing.DOWN));
    }

    // ═══════════════════════════════════════════
    //  Условие подключения
    // ═══════════════════════════════════════════
    private boolean canConnect(IBlockAccess world, BlockPos pos,
                               EnumFacing facing) {
        BlockPos neighborPos = pos.offset(facing);
        TileEntity te = world.getTileEntity(neighborPos);

        // Подключаемся к трубам
        if (te instanceof TileEntityEtherPipe) return true;

        // Подключаемся к конденсатору (источник)
        if (te instanceof TileEntityEtherCondenser) return true;

        // Подключаемся к любой машине с IEtherReceiver
        if (te instanceof IEtherReceiver) return true;

        return false;
    }

    // ═══════════════════════════════════════════
    //  Хитбоксы
    // ═══════════════════════════════════════════
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state,
                                        IBlockAccess source,
                                        BlockPos pos) {
        // Возвращаем полный куб для collision
        return FULL_BLOCK_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world,
                                      BlockPos pos,
                                      AxisAlignedBB entityBox,
                                      java.util.List<AxisAlignedBB> collidingBoxes,
                                      net.minecraft.entity.Entity entity,
                                      boolean isActualState) {
        IBlockState actual = world.getBlockState(pos)
                .getActualState(world, pos);

        addCollisionBox(AABB_CENTER, entityBox, collidingBoxes, pos);
        if (actual.getValue(NORTH)) addCollisionBox(AABB_NORTH, entityBox, collidingBoxes, pos);
        if (actual.getValue(SOUTH)) addCollisionBox(AABB_SOUTH, entityBox, collidingBoxes, pos);
        if (actual.getValue(EAST))  addCollisionBox(AABB_EAST,  entityBox, collidingBoxes, pos);
        if (actual.getValue(WEST))  addCollisionBox(AABB_WEST,  entityBox, collidingBoxes, pos);
        if (actual.getValue(UP))    addCollisionBox(AABB_UP,    entityBox, collidingBoxes, pos);
        if (actual.getValue(DOWN))  addCollisionBox(AABB_DOWN,  entityBox, collidingBoxes, pos);
    }

    private void addCollisionBox(AxisAlignedBB aabb,
                                 AxisAlignedBB entityBox,
                                 java.util.List<AxisAlignedBB> list,
                                 BlockPos pos) {
        AxisAlignedBB offset = aabb.offset(pos);
        if (entityBox.intersects(offset)) list.add(offset);
    }

    // ═══════════════════════════════════════════
    //  Рендер
    // ═══════════════════════════════════════════
    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
        return net.minecraft.util.EnumBlockRenderType.MODEL;
    }

    // ═══════════════════════════════════════════
    //  Обновление соседей
    // ═══════════════════════════════════════════
    @Override
    public void neighborChanged(IBlockState state, World world,
                                BlockPos pos, Block block,
                                BlockPos fromPos) {
        if (!world.isRemote) {
            // Принудительно пересчитываем actual state
            IBlockState newState = state.getActualState(world, pos);
            world.notifyBlockUpdate(pos, state, newState, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        updateNeighborConnections(world, pos);
    }


    private void updateNeighborConnections(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos neighborPos = pos.offset(facing);
            IBlockState neighborState = world.getBlockState(neighborPos);
            world.notifyBlockUpdate(neighborPos,
                    neighborState, neighborState, 3);
        }
    }

    // ═══════════════════════════════════════════
    //  TileEntity
    // ═══════════════════════════════════════════
    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityEtherPipe(throughput, maxBuffer);
    }
}