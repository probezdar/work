// golem/ai/AIGolemIdle.java
package com.etherforge.mod.golem.ai;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.golem.GolemCommand;
import net.minecraft.entity.ai.EntityAIBase;

public class AIGolemIdle extends EntityAIBase {

    private final EntityEtherGolem golem;

    public AIGolemIdle(EntityEtherGolem golem) {
        this.golem = golem;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        // IDLE или нет задач
        return golem.currentTask == null
                || golem.currentTask.command == GolemCommand.IDLE;
    }

    @Override
    public void updateTask() {
        // Проверяем появилась ли задача в очереди
        if (golem.currentTask == null && !golem.commandQueue.isEmpty()) {
            golem.advanceQueue();
        }
    }
}