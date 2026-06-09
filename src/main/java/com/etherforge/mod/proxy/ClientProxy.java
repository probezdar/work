package com.etherforge.mod.proxy;

import com.etherforge.mod.init.ModBlocks;
import com.etherforge.mod.init.ModItems;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {

    }

    @Override
    public void init() {}

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        // ── Предметы ────────────────────────────────────────────
        registerItemModel(ModItems.ETHER_CRYSTAL,  "ether_crystal");
        registerItemModel(ModItems.CRYSTAL_IGNIS,  "crystal_ignis");
        registerItemModel(ModItems.CRYSTAL_AQUA,   "crystal_aqua");
        registerItemModel(ModItems.CRYSTAL_VOLTA,  "crystal_volta");
        registerItemModel(ModItems.CRYSTAL_UMBRA,  "crystal_umbra");
        registerItemModel(ModItems.CRYSTAL_LUX,    "crystal_lux");
        registerItemModel(ModItems.ETHERSCOPE,     "etherscope");

        // ── Блоки ───────────────────────────────────────────────
        registerBlockModel(ModBlocks.ETHER_ORE,       "ether_ore");
        registerBlockModel(ModBlocks.ETHER_ORE_IGNIS, "ether_ore_ignis");
        registerBlockModel(ModBlocks.ETHER_ORE_UMBRA, "ether_ore_umbra");
        registerBlockModel(ModBlocks.ETHER_BLOCK,     "ether_block");
    }

    // Явно указываем имя без автоматического getRegistryName()
    private static void registerItemModel(Item item, String name) {
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory")
        );
    }

    private static void registerBlockModel(net.minecraft.block.Block block, String name) {
        Item item = Item.getItemFromBlock(block);
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory")
        );
    }
}