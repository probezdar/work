package com.etherforge.mod.entities;

import com.etherforge.mod.golem.ai.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityMechGolem extends EntityEtherGolem {

    public EntityMechGolem(World world) {
        super(world);
        setSize(0.6f, 1.8f);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        // MAX_HEALTH, MOVEMENT_SPEED, FOLLOW_RANGE, KNOCKBACK_RESISTANCE
        // — уже зарегистрированы в EntityCreature/EntityLivingBase
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                .setBaseValue(30.0);

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(0.25);

        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
                .setBaseValue(32.0);

        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                .setBaseValue(1.0);

        // ATTACK_DAMAGE — НЕ зарегистрирован в EntityCreature
        // Нужно сначала зарегистрировать, потом устанавливать
        getAttributeMap().registerAttribute(
                        SharedMonsterAttributes.ATTACK_DAMAGE)
                .setBaseValue(0.0);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new AIGolemReturn(this));
        tasks.addTask(2, new AIGolemCollect(this));
        tasks.addTask(3, new AIGolemMine(this));
        tasks.addTask(4, new AIGolemTransfer(this));
        tasks.addTask(5, new AIGolemIdle(this));
    }

    @Override
    public boolean isAIDisabled() { return false; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return null; }

    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Override
    public void onDeath(net.minecraft.util.DamageSource cause) {
        if (!world.isRemote) {
            for (ItemStack stack : golemInventory) {
                if (!stack.isEmpty()) {
                    entityDropItem(stack, 0.5f);
                }
            }
            // Очищаем инвентарь чтобы не дропнуть снова
            for (int i = 0; i < golemInventory.length; i++) {
                golemInventory[i] = ItemStack.EMPTY;
            }
        }
        super.onDeath(cause);
    }

    // Каждый тик — проверяем очередь
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote) {
            if (currentTask == null && !commandQueue.isEmpty()) {
                advanceQueue();
            }
        }
    }
}