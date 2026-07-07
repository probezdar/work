package com.etherforge.mod.entities;

import com.etherforge.mod.golem.ai.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityMorphoGolem extends EntityEtherGolem {

    public EntityMorphoGolem(World world) {
        super(world);
        setSize(0.7f, 1.2f);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                .setBaseValue(20.0);

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(0.3);

        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
                .setBaseValue(32.0);

        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                .setBaseValue(0.5);

        // Регистрируем ATTACK_DAMAGE перед установкой
        getAttributeMap().registerAttribute(
                        SharedMonsterAttributes.ATTACK_DAMAGE)
                .setBaseValue(10.0);
    }

    @Override
    protected void initEntityAI() {
        // Боевые задачи — выше приоритетом
        tasks.addTask(1, new AIGolemReturn(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1.0, true));
        tasks.addTask(3, new AIGolemCollect(this));
        tasks.addTask(4, new AIGolemTransfer(this));
        tasks.addTask(5, new AIGolemIdle(this));

        // Атакует мобов в радиусе если нет активной задачи
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(
                this,
                net.minecraft.entity.monster.EntityMob.class,
                true));
    }

    // Морфо голем издаёт органические звуки
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SLIME_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SLIME_SQUISH;
    }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    // Атака с эффектом замедления
    @Override
    public boolean attackEntityAsMob(net.minecraft.entity.Entity entity) {
        boolean result = super.attackEntityAsMob(entity);
        if (result && entity instanceof net.minecraft.entity.EntityLivingBase) {
            // Накладываем замедление на 3 секунды
            ((net.minecraft.entity.EntityLivingBase) entity).addPotionEffect(
                    new net.minecraft.potion.PotionEffect(
                            net.minecraft.init.MobEffects.SLOWNESS,
                            60, // 3 секунды
                            1
                    )
            );
        }
        return result;
    }

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