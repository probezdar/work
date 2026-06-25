// golem/ai/AIGolemCollect.java
package com.etherforge.mod.golem.ai;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.golem.GolemCommand;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AIGolemCollect extends EntityAIBase {

    private final EntityEtherGolem golem;
    private EntityItem targetItem = null;
    private static final double SPEED = 1.0;
    private static final double PICKUP_RADIUS = 1.5;

    public AIGolemCollect(EntityEtherGolem golem) {
        this.golem = golem;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (golem.currentTask == null) return false;
        if (golem.currentTask.command != GolemCommand.COLLECT) return false;
        if (golem.isInventoryFull()) {
            // Инвентарь полон — переходим к следующей задаче
            golem.advanceQueue();
            return false;
        }
        return findTarget();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (golem.currentTask == null) return false;
        if (golem.currentTask.command != GolemCommand.COLLECT) return false;
        if (golem.isInventoryFull()) return false;
        if (targetItem == null || targetItem.isDead) return findTarget();
        return true;
    }

    @Override
    public void startExecuting() {
        if (targetItem != null) {
            golem.getNavigator().tryMoveToEntityLiving(targetItem, SPEED);
        }
    }

    @Override
    public void updateTask() {
        if (targetItem == null || targetItem.isDead) {
            if (!findTarget()) {
                // Нет предметов — задача выполнена
                golem.advanceQueue();
                return;
            }
        }

        golem.getNavigator().tryMoveToEntityLiving(targetItem, SPEED);

        // Подобрать если рядом
        double dist = golem.getDistanceSq(targetItem);
        if (dist < PICKUP_RADIUS * PICKUP_RADIUS) {
            pickupItem();
        }
    }

    private boolean findTarget() {
        int radius = golem.currentTask != null
                ? golem.currentTask.radius : 8;
        BlockPos pos = golem.getPosition();

        List<EntityItem> items = golem.world.getEntitiesWithinAABB(
                EntityItem.class,
                new AxisAlignedBB(
                        pos.getX() - radius, pos.getY() - radius,
                        pos.getZ() - radius,
                        pos.getX() + radius, pos.getY() + radius,
                        pos.getZ() + radius));

        if (items.isEmpty()) return false;

        // Ближайший предмет
        EntityItem closest = null;
        double minDist = Double.MAX_VALUE;
        for (EntityItem item : items) {
            double d = golem.getDistanceSq(item);
            if (d < minDist) {
                minDist = d;
                closest = item;
            }
        }
        targetItem = closest;
        return targetItem != null;
    }

    private void pickupItem() {
        if (targetItem == null || targetItem.isDead) return;

        ItemStack stack = targetItem.getItem().copy();
        boolean added   = golem.addItemToInventory(stack);

        if (added) {
            targetItem.setDead();
            targetItem = null;
        }
    }

    @Override
    public void resetTask() {
        targetItem = null;
        golem.getNavigator().clearPath();
    }
}