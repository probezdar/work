package com.etherforge.mod.entities;

import com.etherforge.mod.golem.GolemCommand;
import com.etherforge.mod.golem.ai.*;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityEtherealGolem extends EntityEtherGolem {

    // Эфирный голем телепортирует предметы
    // не ходит — летает/левитирует
    private int teleportTimer = 0;
    private static final int TELEPORT_INTERVAL = 40; // каждые 2 секунды

    public EntityEtherealGolem(World world) {
        super(world);
        setSize(0.7f, 1.2f);
        setNoGravity(true); // левитирует
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                .setBaseValue(Float.MAX_VALUE / 2);

        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(0.4);

        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
                .setBaseValue(64.0);

        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                .setBaseValue(1.0);

        // Регистрируем ATTACK_DAMAGE
        getAttributeMap().registerAttribute(
                        SharedMonsterAttributes.ATTACK_DAMAGE)
                .setBaseValue(0.0);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new AIGolemReturn(this));
        tasks.addTask(2, new AIGolemCollect(this));
        tasks.addTask(3, new AIGolemTransfer(this));
        tasks.addTask(4, new AIGolemIdle(this));
    }

    // ═══════════════════════════════════════════
    //  Бессмертие — не умирает от урона
    // ═══════════════════════════════════════════
    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true; // иммунитет ко всему
    }

    // Без звуков
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return null; }

    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    // ═══════════════════════════════════════════
    //  Основная способность — телепортация предметов
    //  Если команда COLLECT — телепортирует предметы
    //  прямо в инвентарь без ходьбы
    // ═══════════════════════════════════════════
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;

        // Берём следующую задачу
        if (currentTask == null && !commandQueue.isEmpty()) {
            advanceQueue();
        }

        // Особое поведение эфирного голема
        if (currentTask != null) {
            teleportTimer++;
            if (teleportTimer >= TELEPORT_INTERVAL) {
                teleportTimer = 0;
                executeTeleportTask();
            }
        }

        // Плавное левитирование
        motionY = Math.sin(ticksExisted * 0.05) * 0.02;
    }

    // ═══════════════════════════════════════════
    //  Телепортация предметов (без ходьбы)
    // ═══════════════════════════════════════════
    private void executeTeleportTask() {
        if (currentTask == null) return;

        if (currentTask.command == GolemCommand.COLLECT) {
            teleportCollect();
        } else if (currentTask.command == GolemCommand.TRANSFER) {
            teleportTransfer();
        } else if (currentTask.command == GolemCommand.RETURN) {
            // Мгновенный возврат
            setPosition(
                    homePos.getX() + 0.5,
                    homePos.getY() + 1.0,
                    homePos.getZ() + 0.5);
            advanceQueue();
        }
    }

    private void teleportCollect() {
        if (isInventoryFull()) {
            advanceQueue();
            return;
        }

        int radius = currentTask.radius;
        BlockPos center = getPosition();

        // Телепортируем предметы прямо в инвентарь
        List<EntityItem> items = world.getEntitiesWithinAABB(
                EntityItem.class,
                new AxisAlignedBB(
                        center.getX() - radius,
                        center.getY() - radius,
                        center.getZ() - radius,
                        center.getX() + radius,
                        center.getY() + radius,
                        center.getZ() + radius
                )
        );

        if (items.isEmpty()) {
            advanceQueue();
            return;
        }

        for (EntityItem entityItem : items) {
            if (isInventoryFull()) break;
            if (entityItem.isDead) continue;

            ItemStack stack = entityItem.getItem().copy();

            // Визуальный эффект телепортации
            spawnEtherParticles(entityItem.posX,
                    entityItem.posY, entityItem.posZ);

            boolean added = addItemToInventory(stack);
            if (added) {
                entityItem.setDead();
            }
        }
    }

    private void teleportTransfer() {
        if (currentTask.target == null) {
            advanceQueue();
            return;
        }

        BlockPos source = currentTask.target;
        net.minecraft.tileentity.TileEntity sourceTE =
                world.getTileEntity(source);
        net.minecraft.tileentity.TileEntity targetTE =
                world.getTileEntity(homePos);

        if (!(sourceTE instanceof net.minecraft.inventory.IInventory)
                || !(targetTE instanceof net.minecraft.inventory.IInventory)) {
            advanceQueue();
            return;
        }

        net.minecraft.inventory.IInventory srcInv =
                (net.minecraft.inventory.IInventory) sourceTE;
        net.minecraft.inventory.IInventory dstInv =
                (net.minecraft.inventory.IInventory) targetTE;

        // Прямая телепортация предметов из source в target
        teleportBetweenInventories(srcInv, dstInv);

        spawnEtherParticles(
                source.getX() + 0.5,
                source.getY() + 0.5,
                source.getZ() + 0.5);

        advanceQueue();
    }

    private void teleportBetweenInventories(
            net.minecraft.inventory.IInventory src,
            net.minecraft.inventory.IInventory dst) {

        for (int i = 0; i < src.getSizeInventory(); i++) {
            ItemStack stack = src.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            // Пробуем положить в dst
            for (int j = 0; j < dst.getSizeInventory(); j++) {
                if (stack.isEmpty()) break;
                if (!dst.isItemValidForSlot(j, stack)) continue;

                ItemStack dstStack = dst.getStackInSlot(j);

                if (dstStack.isEmpty()) {
                    dst.setInventorySlotContents(j, stack.copy());
                    src.setInventorySlotContents(i, ItemStack.EMPTY);
                    stack = ItemStack.EMPTY;

                } else if (dstStack.isItemEqual(stack)
                        && dstStack.getCount() < dstStack.getMaxStackSize()) {
                    int canFit = dstStack.getMaxStackSize()
                            - dstStack.getCount();
                    int toMove = Math.min(canFit, stack.getCount());
                    dstStack.grow(toMove);
                    stack.shrink(toMove);
                    dst.setInventorySlotContents(j, dstStack);
                    if (stack.isEmpty()) {
                        src.setInventorySlotContents(i, ItemStack.EMPTY);
                    } else {
                        src.setInventorySlotContents(i, stack);
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    //  Частицы при телепортации
    // ═══════════════════════════════════════════
    private void spawnEtherParticles(double x, double y, double z) {
        if (world.isRemote) return;
        net.minecraft.network.play.server.SPacketParticles packet =
                new net.minecraft.network.play.server.SPacketParticles(
                        net.minecraft.util.EnumParticleTypes.PORTAL,
                        false,
                        (float) x, (float) y, (float) z,
                        0.3f, 0.3f, 0.3f,
                        0.1f, 8,
                        new int[0]);

        for (Object obj : world.playerEntities) {
            if (obj instanceof net.minecraft.entity.player.EntityPlayerMP) {
                ((net.minecraft.entity.player.EntityPlayerMP) obj)
                        .connection.sendPacket(packet);
            }
        }
    }
}