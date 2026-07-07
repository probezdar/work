// network/PacketHandler.java
package com.etherforge.mod.network;

import com.etherforge.mod.util.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

    private static int id = 0;

    public static void register() {
        // Клиент → Сервер
        INSTANCE.registerMessage(
                PacketGolemCommand.Handler.class,
                PacketGolemCommand.class,
                id++,
                Side.SERVER
        );
    }
}