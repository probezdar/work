package com.etherforge.mod.golem.ai;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.golem.GolemCommand;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class AIGolemTransfer extends EntityAIBase {

    private final EntityEtherGolem golem;

    private static final double SPEED = 1.0;
    private static final double REACH = 2.5;

    private enum Phase {
        GO_TO_SOURCE,
        TAKE_ITEMS,
        GO_TO_TARGET,
        DEPOSIT_ITEMS
    }

    private Phase phase = Phase.GO_TO_SOURCE;

    public AIGolemTransfer(EntityEtherGolem golem) {
        this.golem = golem;
        setMutexBits(1);
    }

    // ═══════════════════════════════════════════
    //  Условия запуска
    // ═══════════════════════════════════════════
    @Override
    public boolean shouldExecute() {
        if (golem.currentTask == null) return false;
        if (golem.currentTask.command != GolemCommand.TRANSFER) return false;
        return golem.currentTask.target != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (golem.currentTask == null) return false;
        return golem.currentTask.command == GolemCommand.TRANSFER;
    }

    // ═══════════════════════════════════════════
    //  Старт
    // ═══════════════════════════════════════════
    @Override
    public void startExecuting() {
        phase = Phase.GO_TO_SOURCE;
        navigateTo(golem.currentTask.target);
    }

    // ═══════════════════════════════════════════
    //  Тик
    // ═══════════════════════════════════════════
    @Override
    public void updateTask() {
        if (phase == Phase.GO_TO_SOURCE) {
            tickGoToSource();
        } else if (phase == Phase.TAKE_ITEMS) {
            tickTakeItems();
        } else if (phase == Phase.GO_TO_TARGET) {
            tickGoToTarget();
        } else if (phase == Phase.DEPOSIT_ITEMS) {
            tickDepositItems();
        }
    }

    @Override
    public void resetTask() {
        phase = Phase.GO_TO_SOURCE;
        golem.getNavigator().clearPath();
    }

    // ═══════════════════════════════════════════
    //  Фазы
    // ═══════════════════════════════════════════
    private void tickGoToSource() {
        BlockPos source = golem.currentTask.target;
        if (isNearPos(source)) {
            golem.getNavigator().clearPath();
            phase = Phase.TAKE_ITEMS;
        } else {
            navigateTo(source);
        }
    }

    private void tickTakeItems() {
        BlockPos source = golem.currentTask.target;
        TileEntity te   = golem.world.getTileEntity(source);
        if (te instanceof IInventory) {
            takeFromInventory((IInventory) te);
        }
        phase = Phase.GO_TO_TARGET;
        navigateTo(golem.homePos);
    }

    private void tickGoToTarget() {
        if (isNearPos(golem.homePos)) {
            golem.getNavigator().clearPath();
            phase = Phase.DEPOSIT_ITEMS;
        } else {
            navigateTo(golem.homePos);
        }
    }

    private void tickDepositItems() {
        TileEntity te = golem.world.getTileEntity(golem.homePos);
        if (te instanceof IInventory) {
            depositToInventory((IInventory) te);
        } else {
            dropAllItems();
        }
        golem.advanceQueue();
        phase = Phase.GO_TO_SOURCE;
    }

    // ═══════════════════════════════════════════
    //  Взять предметы из контейнера
    // ═══════════════════════════════════════════
    private void takeFromInventory(IInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (golem.isInventoryFull()) break;

            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            ItemStack toTake = stack.copy();
            boolean added    = golem.addItemToInventory(toTake);

            if (added) {
                inv.setInventorySlotContents(i, ItemStack.EMPTY);
            } else {
                int taken = stack.getCount() - toTake.getCount();
                stack.shrink(taken);
                inv.setInventorySlotContents(i, stack);
                break;
            }
        }
    }

    // ═══════════════════════════════════════════
    //  Положить предметы в контейнер
    // ═══════════════════════════════════════════
    private void depositToInventory(IInventory inv) {
        for (int gi = 0; gi < golem.golemInventory.length; gi++) {
            ItemStack golemStack = golem.golemInventory[gi];
            if (golemStack.isEmpty()) continue;

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (golemStack.isEmpty()) break;
                if (!inv.isItemValidForSlot(i, golemStack)) continue;

                ItemStack invStack = inv.getStackInSlot(i);

                if (invStack.isEmpty()) {
                    inv.setInventorySlotContents(i, golemStack.copy());
                    golem.golemInventory[gi] = ItemStack.EMPTY;
                    golemStack = ItemStack.EMPTY;

                } else if (invStack.isItemEqual(golemStack)
                        && invStack.getCount() < invStack.getMaxStackSize()) {
                    int canFit = invStack.getMaxStackSize() - invStack.getCount();
                    int toAdd  = Math.min(canFit, golemStack.getCount());
                    invStack.grow(toAdd);
                    golemStack.shrink(toAdd);
                    inv.setInventorySlotContents(i, invStack);
                    if (golemStack.isEmpty()) {
                        golem.golemInventory[gi] = ItemStack.EMPTY;
                    }
                }
            }

            // Не влезло — на землю
            if (!golem.golemInventory[gi].isEmpty()) {
                golem.entityDropItem(golem.golemInventory[gi], 0.5f);
                golem.golemInventory[gi] = ItemStack.EMPTY;
            }
        }
    }

    // ═══════════════════════════════════════════
    //  Выбросить всё на землю
    // ═══════════════════════════════════════════
    private void dropAllItems() {
        for (int i = 0; i < golem.golemInventory.length; i++) {
            if (!golem.golemInventory[i].isEmpty()) {
                golem.entityDropItem(golem.golemInventory[i], 0.5f);
                golem.golemInventory[i] = ItemStack.EMPTY;
            }
        }
    }

    // ═══════════════════════════════════════════
    //  Навигация
    // ═══════════════════════════════════════════
    private void navigateTo(BlockPos pos) {
        golem.getNavigator().tryMoveToXYZ(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                SPEED);
    }

    private boolean isNearPos(BlockPos pos) {
        double dx = golem.posX - (pos.getX() + 0.5);
        double dy = golem.posY - pos.getY();
        double dz = golem.posZ - (pos.getZ() + 0.5);
        return (dx * dx + dy * dy + dz * dz) < REACH * REACH;
    }
}