// entities/EntityEtherGolem.java
package com.etherforge.mod.entities;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.golem.GolemCommand;
import com.etherforge.mod.gui.ModGuiHandler;
import com.etherforge.mod.items.ItemRuneCommand;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class EntityEtherGolem extends EntityCreature {

    // ═══════════════════════════════════════════
    //  Инвентарь голема — 2 слота
    // ═══════════════════════════════════════════
    public ItemStack[] golemInventory = new ItemStack[]{
            ItemStack.EMPTY,
            ItemStack.EMPTY
    };

    // ═══════════════════════════════════════════
    //  Очередь команд
    // ═══════════════════════════════════════════
    public Deque<GolemTaskEntry> commandQueue = new ArrayDeque<>();
    public GolemTaskEntry currentTask = null;

    // ═══════════════════════════════════════════
    //  Привязка к дому
    // ═══════════════════════════════════════════
    public BlockPos homePos = BlockPos.ORIGIN;

    // ═══════════════════════════════════════════
    //  Хранение данных задачи
    // ═══════════════════════════════════════════
    public static class GolemTaskEntry {
        public final GolemCommand command;
        public final int radius;
        public final BlockPos target; // для MINE и TRANSFER

        public GolemTaskEntry(GolemCommand command, int radius,
                              BlockPos target) {
            this.command = command;
            this.radius  = radius;
            this.target  = target;
        }

        // Сохранение в NBT
        public NBTTagCompound toNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Command", command.name());
            tag.setInteger("Radius",  radius);
            if (target != null) {
                tag.setInteger("TX", target.getX());
                tag.setInteger("TY", target.getY());
                tag.setInteger("TZ", target.getZ());
            }
            return tag;
        }

        public static GolemTaskEntry fromNBT(NBTTagCompound tag) {
            GolemCommand cmd = GolemCommand.valueOf(
                    tag.getString("Command"));
            int radius = tag.getInteger("Radius");
            BlockPos target = null;
            if (tag.hasKey("TX")) {
                target = new BlockPos(
                        tag.getInteger("TX"),
                        tag.getInteger("TY"),
                        tag.getInteger("TZ"));
            }
            return new GolemTaskEntry(cmd, radius, target);
        }
    }

    public EntityEtherGolem(World world) {
        super(world);
        setSize(0.6f, 1.8f);
    }

    // ═══════════════════════════════════════════
    //  ПКМ по голему
    // ═══════════════════════════════════════════
    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        if (!world.isRemote) {
            // 1) Рука с руной — добавить в очередь
            if (!held.isEmpty() && held.getItem() instanceof ItemRuneCommand) {
                ItemRuneCommand rune = (ItemRuneCommand) held.getItem();
                GolemCommand cmd     = rune.getCommand();
                int radius           = ItemRuneCommand.getRadius(held);

                BlockPos target = null;
                if (held.hasTagCompound()
                        && held.getTagCompound().hasKey("TargetX")) {
                    NBTTagCompound nbt = held.getTagCompound();
                    target = new BlockPos(
                            nbt.getInteger("TargetX"),
                            nbt.getInteger("TargetY"),
                            nbt.getInteger("TargetZ"));
                }

                commandQueue.addLast(new GolemTaskEntry(cmd, radius, target));
                player.sendMessage(new net.minecraft.util.text.TextComponentString(
                        "§dКоманда добавлена: §f" + cmd.name()
                                + " §7(очередь: " + commandQueue.size() + ")"));
                return true;
            }

            // 2) Shift + пустая рука — очистить очередь
            if (held.isEmpty() && player.isSneaking()) {
                commandQueue.clear();
                currentTask = null;
                player.sendMessage(new net.minecraft.util.text.TextComponentString(
                        "§7Очередь очищена."));
                return true;
            }

            // 3) Пустая рука — открыть GUI
            if (held.isEmpty()) {
                player.openGui(
                        com.etherforge.mod.EtherForge.instance,
                        com.etherforge.mod.gui.ModGuiHandler.GUI_GOLEM,
                        world,
                        getEntityId(), 0, 0);
                return true;
            }
        }
        return super.processInteract(player, hand);
    }


    protected void showStatus(EntityPlayer player) {
        player.sendMessage(new net.minecraft.util.text.TextComponentString(
                "§6=== Голем ==="));
        player.sendMessage(new net.minecraft.util.text.TextComponentString(
                "§7HP: §a" + (int) getHealth() + "/"
                        + (int) getMaxHealth()));

        if (currentTask != null) {
            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                    "§7Текущая задача: §d" + currentTask.command.name()));
        } else {
            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                    "§7Текущая задача: §8Нет"));
        }

        player.sendMessage(new net.minecraft.util.text.TextComponentString(
                "§7Очередь: §f" + commandQueue.size() + " команд"));

        // Инвентарь
        for (int i = 0; i < golemInventory.length; i++) {
            ItemStack s = golemInventory[i];
            String name = s.isEmpty() ? "§8пусто" :
                    s.getDisplayName() + " x" + s.getCount();
            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                    "§7Слот " + (i + 1) + ": " + name));
        }
    }

    // ═══════════════════════════════════════════
    //  Инвентарь голема
    // ═══════════════════════════════════════════
    public boolean addItemToInventory(ItemStack stack) {
        for (int i = 0; i < golemInventory.length; i++) {
            if (golemInventory[i].isEmpty()) {
                golemInventory[i] = stack.copy();
                stack.setCount(0);
                return true;
            }
            // Можно добить в существующий стак
            if (golemInventory[i].isItemEqual(stack)
                    && golemInventory[i].getCount()
                    < golemInventory[i].getMaxStackSize()) {
                int canFit = golemInventory[i].getMaxStackSize()
                        - golemInventory[i].getCount();
                int toAdd  = Math.min(canFit, stack.getCount());
                golemInventory[i].grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return true;
            }
        }
        return false; // инвентарь полон
    }

    public boolean isInventoryFull() {
        for (ItemStack s : golemInventory) {
            if (s.isEmpty()) return false;
            if (s.getCount() < s.getMaxStackSize()) return false;
        }
        return true;
    }

    public ItemStack[] getGolemInventory() { return golemInventory; }
    public BlockPos getHomePos()           { return homePos; }
    public void setHomePos(BlockPos pos)   { homePos = pos; }

    // ═══════════════════════════════════════════
    //  Следующая задача из очереди
    // ═══════════════════════════════════════════
    public void advanceQueue() {
        currentTask = commandQueue.isEmpty()
                ? null
                : commandQueue.pollFirst();
    }

    // ═══════════════════════════════════════════
    //  NBT
    // ═══════════════════════════════════════════
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        // Дом
        compound.setInteger("HomeX", homePos.getX());
        compound.setInteger("HomeY", homePos.getY());
        compound.setInteger("HomeZ", homePos.getZ());

        // Инвентарь
        for (int i = 0; i < golemInventory.length; i++) {
            if (!golemInventory[i].isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                golemInventory[i].writeToNBT(tag);
                compound.setTag("GolemSlot" + i, tag);
            }
        }

        // Текущая задача
        if (currentTask != null) {
            compound.setTag("CurrentTask", currentTask.toNBT());
        }

        // Очередь
        NBTTagList queueList = new NBTTagList();
        for (GolemTaskEntry entry : commandQueue) {
            queueList.appendTag(entry.toNBT());
        }
        compound.setTag("CommandQueue", queueList);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        homePos = new BlockPos(
                compound.getInteger("HomeX"),
                compound.getInteger("HomeY"),
                compound.getInteger("HomeZ"));

        for (int i = 0; i < golemInventory.length; i++) {
            if (compound.hasKey("GolemSlot" + i)) {
                golemInventory[i] = new ItemStack(
                        compound.getCompoundTag("GolemSlot" + i));
            }
        }

        if (compound.hasKey("CurrentTask")) {
            currentTask = GolemTaskEntry.fromNBT(
                    compound.getCompoundTag("CurrentTask"));
        }

        commandQueue.clear();
        NBTTagList queueList = compound.getTagList("CommandQueue", 10);
        for (int i = 0; i < queueList.tagCount(); i++) {
            commandQueue.addLast(
                    GolemTaskEntry.fromNBT(queueList.getCompoundTagAt(i)));
        }
    }
}