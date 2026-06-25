// golem/ai/AIGolemReturn.java
package com.etherforge.mod.golem.ai;

import com.etherforge.mod.entities.EntityEtherGolem;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public class AIGolemReturn extends EntityAIBase {

    private final EntityEtherGolem golem;
    private static final double SPEED = 1.0;
    private static final double HOME_RADIUS = 2.0;

    public AIGolemReturn(EntityEtherGolem golem) {
        this.golem = golem;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (golem.currentTask == null) return false;
        return golem.currentTask.command ==
                com.etherforge.mod.golem.GolemCommand.RETURN;
    }

    @Override
    public void startExecuting() {
        BlockPos home = golem.getHomePos();
        golem.getNavigator().tryMoveToXYZ(
                home.getX() + 0.5,
                home.getY(),
                home.getZ() + 0.5,
                SPEED);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (golem.currentTask == null) return false;
        if (golem.currentTask.command !=
                com.etherforge.mod.golem.GolemCommand.RETURN) return false;
        return !isAtHome();
    }

    @Override
    public void updateTask() {
        if (isAtHome()) {
            golem.getNavigator().clearPath();
            golem.advanceQueue(); // задача выполнена → следующая
        }
    }

    private boolean isAtHome() {
        return golem.getDistanceSq(
                golem.getHomePos().getX() + 0.5,
                golem.getHomePos().getY(),
                golem.getHomePos().getZ() + 0.5) < HOME_RADIUS * HOME_RADIUS;
    }
}