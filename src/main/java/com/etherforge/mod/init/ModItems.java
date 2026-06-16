package com.etherforge.mod.init;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.items.ItemEtherscope;
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
    public static Item ETHERSCOPE;

    // ═══════════════════════════════════════════
//  Кристаллы-катализаторы (сплавы)
// ═══════════════════════════════════════════
    public static Item CATALYST_STEAM;       // Ignis + Aqua
    public static Item CATALYST_PLASMA;      // Ignis + Volta
    public static Item CATALYST_DAWN;        // Ignis + Lux
    public static Item CATALYST_SURGE;       // Aqua + Volta
    public static Item CATALYST_DEPTH;       // Aqua + Umbra
    public static Item CATALYST_SPARK;       // Volta + Lux
    public static Item CATALYST_ECLIPSE;     // Umbra + Lux
    public static Item CATALYST_EMBER;       // Ignis + Umbra

    // ═══════════════════════════════
    //  Инструменты и прочее
    // ═══════════════════════════════

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        ETHER_CRYSTAL = createItem("ether_crystal");
        CRYSTAL_IGNIS = createItem("crystal_ignis");
        CRYSTAL_AQUA  = createItem("crystal_aqua");
        CRYSTAL_VOLTA = createItem("crystal_volta");
        CRYSTAL_UMBRA = createItem("crystal_umbra");
        CRYSTAL_LUX   = createItem("crystal_lux");
        ETHERSCOPE = new com.etherforge.mod.items.ItemEtherscope();


        CATALYST_STEAM  = createCatalyst("catalyst_steam");
        CATALYST_PLASMA = createCatalyst("catalyst_plasma");
        CATALYST_DAWN   = createCatalyst("catalyst_dawn");
        CATALYST_SURGE  = createCatalyst("catalyst_surge");
        CATALYST_DEPTH  = createCatalyst("catalyst_depth");
        CATALYST_SPARK  = createCatalyst("catalyst_spark");
        CATALYST_ECLIPSE= createCatalyst("catalyst_eclipse");
        CATALYST_EMBER  = createCatalyst("catalyst_ember");

        registry.registerAll(
                ETHER_CRYSTAL,
                CRYSTAL_IGNIS,
                CRYSTAL_AQUA,
                CRYSTAL_VOLTA,
                CRYSTAL_UMBRA,
                CRYSTAL_LUX,
                CATALYST_STEAM, CATALYST_PLASMA, CATALYST_DAWN, CATALYST_SURGE,
                CATALYST_DEPTH, CATALYST_SPARK, CATALYST_ECLIPSE, CATALYST_EMBER
        );
        registry.register(ETHERSCOPE);

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

    private static Item createCatalyst(String name) {
        return new Item()
                .setRegistryName(Reference.MOD_ID, name)
                .setUnlocalizedName(Reference.MOD_ID + "." + name)
                .setCreativeTab(ModCreativeTab.INSTANCE)
                .setMaxDamage(16)   // 64 использования
                .setMaxStackSize(1)
                .setNoRepair();
    }

    // Вызывается из главного класса
    public static void register() {
        // Регистрация идёт через @SubscribeEvent выше
    }
}
