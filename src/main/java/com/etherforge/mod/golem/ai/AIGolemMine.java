// golem/ai/AIGolemMine.java
package com.etherforge.mod.golem.ai;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.golem.GolemCommand;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AIGolemMine extends EntityAIBase {

    private final EntityEtherGolem golem;
    private BlockPos targetBlock = null;
    private int mineTimer = 0;
    private static final int MINE_TICKS = 40; // 2 секунды на блок
    private static final double SPEED   = 1.0;
    private static final double MINE_REACH = 3.0;

    // Блоки которые голем НЕ будет ломать
    private static final List<Block> BLACKLIST = new ArrayList<>();
    static {
        BLACKLIST.add(Blocks.BEDROCK);
        BLACKLIST.add(Blocks.BARRIER);
        BLACKLIST.add(Blocks.COMMAND_BLOCK);
        BLACKLIST.add(Blocks.CHAIN_COMMAND_BLOCK);
        BLACKLIST.add(Blocks.REPEATING_COMMAND_BLOCK);
        BLACKLIST.add(Blocks.AIR);
    }

    public AIGolemMine(EntityEtherGolem golem) {
        this.golem = golem;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (golem.currentTask == null) return false;
        if (golem.currentTask.command != GolemCommand.MINE) return false;

        // Инвентарь полон — задача выполнена
        if (golem.isInventoryFull()) {
            golem.advanceQueue();
            return false;
        }

        // Нет цели — ищем
        return findTargetBlock();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (golem.currentTask == null) return false;
        if (golem.currentTask.command != GolemCommand.MINE) return false;
        if (golem.isInventoryFull()) return false;
        if (targetBlock == null) return findTargetBlock();

        // Проверяем что блок ещё существует
        IBlockState state = golem.world.getBlockState(targetBlock);
        return !state.getBlock().equals(Blocks.AIR);
    }

    @Override
    public void startExecuting() {
        mineTimer = 0;
        moveToTarget();
    }

    @Override
    public void updateTask() {
        if (targetBlock == null) {
            if (!findTargetBlock()) {
                // Нет блоков для добычи в радиусе
                golem.advanceQueue();
                return;
            }
        }

        double distSq = golem.getDistanceSq(
                targetBlock.getX() + 0.5,
                targetBlock.getY() + 0.5,
                targetBlock.getZ() + 0.5);

        if (distSq > MINE_REACH * MINE_REACH) {
            // Идём к блоку
            moveToTarget();
            mineTimer = 0;
        } else {
            // Рядом — ломаем
            golem.getNavigator().clearPath();
            mineTimer++;

            // Визуальный эффект разрушения
            if (mineTimer % 5 == 0) {
                golem.world.sendBlockBreakProgress(
                        golem.getEntityId(),
                        targetBlock,
                        (mineTimer * 9) / MINE_TICKS);
            }

            if (mineTimer >= MINE_TICKS) {
                mineBlock();
                mineTimer = 0;
                targetBlock = null;
            }
        }
    }

    @Override
    public void resetTask() {
        // Сохраняем ссылку ДО обнуления
        BlockPos blockToReset = targetBlock;

        targetBlock = null;
        mineTimer   = 0;
        golem.getNavigator().clearPath();

        // Сбрасываем анимацию разрушения если блок был
        if (blockToReset != null) {
            golem.world.sendBlockBreakProgress(
                    golem.getEntityId(), blockToReset, -1);
        }
    }

    // ═══════════════════════════════════════════
    //  Поиск блока для добычи
    // ═══════════════════════════════════════════
    private boolean findTargetBlock() {
        if (golem.currentTask == null) return false;

        // Если задана конкретная цель
        if (golem.currentTask.target != null) {
            BlockPos target = golem.currentTask.target;
            IBlockState state = golem.world.getBlockState(target);
            if (!state.getBlock().equals(Blocks.AIR)
                    && !BLACKLIST.contains(state.getBlock())) {
                targetBlock = target;
                return true;
            }
            // Цель уже сломана — задача выполнена
            golem.advanceQueue();
            return false;
        }

        // Ищем ближайший блок в радиусе
        int radius = golem.currentTask.radius;
        BlockPos golemPos = golem.getPosition();
        BlockPos closest  = null;
        double minDist    = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos check = golemPos.add(x, y, z);
                    IBlockState state = golem.world.getBlockState(check);

                    if (state.getBlock().equals(Blocks.AIR)) continue;
                    if (BLACKLIST.contains(state.getBlock())) continue;
                    if (!state.getBlock().equals(Blocks.STONE)
                            && !isOre(state)) continue; // только камень и руды

                    double d = golem.getDistanceSq(
                            check.getX() + 0.5,
                            check.getY() + 0.5,
                            check.getZ() + 0.5);
                    if (d < minDist) {
                        minDist  = d;
                        closest  = check;
                    }
                }
            }
        }

        targetBlock = closest;
        return targetBlock != null;
    }

    // ═══════════════════════════════════════════
    //  Сломать блок и забрать дроп
    // ═══════════════════════════════════════════
    private void mineBlock() {
        if (targetBlock == null) return;
        World world = golem.world;
        IBlockState state = world.getBlockState(targetBlock);

        if (state.getBlock().equals(Blocks.AIR)) return;

        // Получаем дроп
        List<ItemStack> drops = state.getBlock().getDrops(
                world, targetBlock, state, 0);

        // Ломаем блок
        world.setBlockToAir(targetBlock);

        // Собираем дроп в инвентарь голема
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            boolean added = golem.addItemToInventory(drop);
            if (!added) {
                // Инвентарь полон — бросаем на землю
                golem.entityDropItem(drop, 0.5f);
            }
        }

        // Сбрасываем прогресс разрушения
        world.sendBlockBreakProgress(golem.getEntityId(), targetBlock, -1);
    }

    private void moveToTarget() {
        if (targetBlock == null) return;
        golem.getNavigator().tryMoveToXYZ(
                targetBlock.getX() + 0.5,
                targetBlock.getY(),
                targetBlock.getZ() + 0.5,
                SPEED);
    }

    // ═══════════════════════════════════════════
    //  Проверка — это руда?
    // ═══════════════════════════════════════════
    private boolean isOre(IBlockState state) {
        String name = state.getBlock().getRegistryName() != null
                ? state.getBlock().getRegistryName().toString()
                : "";
        return name.contains("ore");
    }
}