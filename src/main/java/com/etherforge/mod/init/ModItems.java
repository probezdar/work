package com.etherforge.mod.init;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.util.Reference;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModItems {

    // ═══════════════════════════════
    //  Кристаллы
    // ═══════════════════════════════
    public static Item ETHER_CRYSTAL;        // базовый эфирный кристалл
    public static Item CRYSTAL_IGNIS;        // огонь
    public static Item CRYSTAL_AQUA;         // вода/поток
    public static Item CRYSTAL_VOLTA;        // энергия
    public static Item CRYSTAL_UMBRA;        // тьма/пространство
    public static Item CRYSTAL_LUX;          // свет/скорость

    // ═══════════════════════════════
    //  Инструменты и прочее
    // ═══════════════════════════════
    public static Item ETHERSCOPE;           // эфироскоп

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        ETHER_CRYSTAL = createItem("ether_crystal");
        CRYSTAL_IGNIS = createItem("crystal_ignis");
        CRYSTAL_AQUA  = createItem("crystal_aqua");
        CRYSTAL_VOLTA = createItem("crystal_volta");
        CRYSTAL_UMBRA = createItem("crystal_umbra");
        CRYSTAL_LUX   = createItem("crystal_lux");
        ETHERSCOPE    = createItem("etherscope");

        registry.registerAll(
                ETHER_CRYSTAL,
                CRYSTAL_IGNIS,
                CRYSTAL_AQUA,
                CRYSTAL_VOLTA,
                CRYSTAL_UMBRA,
                CRYSTAL_LUX,
                ETHERSCOPE
        );

        EtherForge.LOGGER.info("Предметы зарегистрированы");
    }

    // ═══════════════════════════════
    //  Вспомогательный метод
    // ═══════════════════════════════
    private static Item createItem(String name) {
        return new Item()
                .setRegistryName(Reference.MOD_ID, name)
                .setUnlocalizedName(Reference.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTab.INSTANCE);
    }

    // Вызывается из главного класса
    public static void register() {
        // Регистрация идёт через @SubscribeEvent выше
    }
}