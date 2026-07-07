// entities/EntityEtherGolem.java
package com.etherforge.mod.entities;

import com.etherforge.mod.golem.GolemCommand;
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public abstract class EntityEtherGolem extends EntityCreature {

    // ═══════════════════════════════════════════
    //  Инвентарь голема — 9 слотов
    // ═══════════════════════════════════════════
    public static final int INVENTORY_SIZE = 9;
    public ItemStack[] golemInventory = new ItemStack[INVENTORY_SIZE];

    // ═══════════════════════════════════════════
    //  Очередь команд
    // ═══════════════════════════════════════════
    public Deque<GolemTaskEntry> commandQueue   = new ArrayDeque<>();
    public List<GolemTaskEntry>  commandHistory = new ArrayList<>(); // для автоповтора
    public GolemTaskEntry        currentTask    = null;
    public boolean               loopTasks      = true; // автоповтор вкл/выкл

    // ═══════════════════════════════════════════
    //  Привязка к дому
    // ═══════════════════════════════════════════
    public BlockPos homePos = BlockPos.ORIGIN;

    // ═══════════════════════════════════════════
    //  Хранение данных задачи
    // ═══════════════════════════════════════════
    public static class GolemTaskEntry {
        public final GolemCommand command;
        public final int          radius;
        public final BlockPos     target;

        public GolemTaskEntry(GolemCommand command,
                              int radius,
                              BlockPos target) {
            this.command = command;
            this.radius  = radius;
            this.target  = target;
        }

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
            GolemCommand cmd    = GolemCommand.valueOf(
                    tag.getString("Command"));
            int      radius = tag.getInteger("Radius");
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
        // Инициализируем инвентарь
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            golemInventory[i] = ItemStack.EMPTY;
        }
    }

    // ═══════════════════════════════════════════
    //  ПКМ по голему
    // ═══════════════════════════════════════════
    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        if (!world.isRemote) {

            // 1) Рука с руной — добавить в очередь
            if (!held.isEmpty()
                    && held.getItem() instanceof ItemRuneCommand) {
                ItemRuneCommand rune = (ItemRuneCommand) held.getItem();
                GolemCommand    cmd  = rune.getCommand();
                int             radius = ItemRuneCommand.getRadius(held);

                BlockPos target = null;
                if (held.hasTagCompound()
                        && held.getTagCompound().hasKey("TargetX")) {
                    NBTTagCompound nbt = held.getTagCompound();
                    target = new BlockPos(
                            nbt.getInteger("TargetX"),
                            nbt.getInteger("TargetY"),
                            nbt.getInteger("TargetZ"));
                }

                GolemTaskEntry entry =
                        new GolemTaskEntry(cmd, radius, target);
                commandQueue.addLast(entry);
                commandHistory.add(entry); // сохраняем для автоповтора

                player.sendMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "§dКоманда добавлена: §f" + cmd.name()
                                        + " §7(очередь: "
                                        + commandQueue.size() + ")"));
                return true;
            }

            // 2) Shift + пустая рука — очистить всё
            if (held.isEmpty() && player.isSneaking()) {
                commandQueue.clear();
                commandHistory.clear();
                currentTask = null;
                player.sendMessage(
                        new net.minecraft.util.text.TextComponentString(
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

    // ═══════════════════════════════════════════
    //  Инвентарь голема
    // ═══════════════════════════════════════════
    public boolean addItemToInventory(ItemStack stack) {
        // Сначала пробуем добить существующий стак
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (golemInventory[i].isEmpty()) continue;
            if (!golemInventory[i].isItemEqual(stack)) continue;

            int canFit = golemInventory[i].getMaxStackSize()
                    - golemInventory[i].getCount();
            if (canFit <= 0) continue;

            int toAdd = Math.min(canFit, stack.getCount());
            golemInventory[i].grow(toAdd);
            stack.shrink(toAdd);
            if (stack.isEmpty()) return true;
        }

        // Ищем пустой слот
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!golemInventory[i].isEmpty()) continue;
            golemInventory[i] = stack.copy();
            stack.setCount(0);
            return true;
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

    public boolean isInventoryEmpty() {
        for (ItemStack s : golemInventory) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    public void clearInventory() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            golemInventory[i] = ItemStack.EMPTY;
        }
    }

    public ItemStack[] getGolemInventory() { return golemInventory; }
    public BlockPos    getHomePos()         { return homePos; }
    public void        setHomePos(BlockPos pos) { homePos = pos; }

    // ═══════════════════════════════════════════
    //  Очередь команд с автоповтором
    // ═══════════════════════════════════════════
    public void advanceQueue() {
        if (!commandQueue.isEmpty()) {
            currentTask = commandQueue.pollFirst();

            // Если автоповтор включён — кладём задачу в конец очереди
            if (loopTasks && currentTask != null) {
                commandQueue.addLast(currentTask);
            }
        } else {
            // Очередь пуста
            if (loopTasks && !commandHistory.isEmpty()) {
                // Восстанавливаем из истории
                for (GolemTaskEntry entry : commandHistory) {
                    commandQueue.addLast(entry);
                }
                currentTask = commandQueue.pollFirst();
                if (currentTask != null) {
                    commandQueue.addLast(currentTask);
                }
            } else {
                currentTask = null;
            }
        }
    }

    public void toggleLoop() {
        loopTasks = !loopTasks;
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

        // Автоповтор
        compound.setBoolean("LoopTasks", loopTasks);

        // Инвентарь
        NBTTagList invList = new NBTTagList();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            NBTTagCompound slotTag = new NBTTagCompound();
            slotTag.setByte("Slot", (byte) i);
            if (!golemInventory[i].isEmpty()) {
                golemInventory[i].writeToNBT(slotTag);
            }
            invList.appendTag(slotTag);
        }
        compound.setTag("GolemInventory", invList);

        // Текущая задача
        if (currentTask != null) {
            compound.setTag("CurrentTask", currentTask.toNBT());
        }

        // Очередь (только уникальные — без дублей от автоповтора)
        NBTTagList queueList = new NBTTagList();
        for (GolemTaskEntry entry : commandHistory) {
            queueList.appendTag(entry.toNBT());
        }
        compound.setTag("CommandHistory", queueList);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        homePos = new BlockPos(
                compound.getInteger("HomeX"),
                compound.getInteger("HomeY"),
                compound.getInteger("HomeZ"));

        loopTasks = !compound.hasKey("LoopTasks")
                || compound.getBoolean("LoopTasks");

        // Инвентарь
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            golemInventory[i] = ItemStack.EMPTY;
        }
        NBTTagList invList = compound.getTagList("GolemInventory", 10);
        for (int i = 0; i < invList.tagCount(); i++) {
            NBTTagCompound slotTag = invList.getCompoundTagAt(i);
            int slot = slotTag.getByte("Slot") & 0xFF;
            if (slot < INVENTORY_SIZE) {
                golemInventory[slot] = new ItemStack(slotTag);
            }
        }

        // История команд
        commandHistory.clear();
        commandQueue.clear();
        NBTTagList histList = compound.getTagList("CommandHistory", 10);
        for (int i = 0; i < histList.tagCount(); i++) {
            GolemTaskEntry entry = GolemTaskEntry.fromNBT(
                    histList.getCompoundTagAt(i));
            commandHistory.add(entry);
            commandQueue.addLast(entry);
        }

        // Текущая задача
        if (compound.hasKey("CurrentTask")) {
            currentTask = GolemTaskEntry.fromNBT(
                    compound.getCompoundTag("CurrentTask"));
        }
    }
}