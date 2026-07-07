// network/PacketGolemCommand.java
package com.etherforge.mod.network;

import com.etherforge.mod.entities.EntityEtherGolem;
import com.etherforge.mod.golem.GolemCommand;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGolemCommand implements IMessage {

    // ═══════════════════════════════════════════
    //  Типы действий
    // ═══════════════════════════════════════════
    public static final int ACTION_CLEAR      = 0;
    public static final int ACTION_RETURN_HOME = 1;
    public static final int ACTION_TOGGLE_LOOP = 2;

    private int entityId;
    private int action;

    // Обязательный пустой конструктор
    public PacketGolemCommand() {}

    public PacketGolemCommand(int entityId, int action) {
        this.entityId = entityId;
        this.action   = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        action   = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(action);
    }

    // ═══════════════════════════════════════════
    //  Обработчик на сервере
    // ═══════════════════════════════════════════
    public static class Handler
            implements IMessageHandler<PacketGolemCommand, IMessage> {

        @Override
        public IMessage onMessage(PacketGolemCommand msg,
                                  MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            // Выполняем в главном потоке сервера
            player.getServerWorld().addScheduledTask(() -> {
                Entity entity = player.world.getEntityByID(msg.entityId);
                if (!(entity instanceof EntityEtherGolem)) return;

                EntityEtherGolem golem = (EntityEtherGolem) entity;

                switch (msg.action) {
                    case ACTION_CLEAR:
                        golem.commandQueue.clear();
                        golem.commandHistory.clear();
                        golem.currentTask = null;
                        break;

                    case ACTION_RETURN_HOME:
                        // Вставляем RETURN в начало очереди
                        golem.commandQueue.addFirst(
                                new EntityEtherGolem.GolemTaskEntry(
                                        GolemCommand.RETURN, 8, null));
                        // В историю не добавляем — это разовая команда
                        break;

                    case ACTION_TOGGLE_LOOP:
                        golem.toggleLoop();
                        break;
                }
            });

            return null;
        }
    }
}