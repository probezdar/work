package com.etherforge.mod.init;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.blocks.BlockEtherOre;
import com.etherforge.mod.blocks.BlockIgnisOre;
import com.etherforge.mod.blocks.BlockUmbraOre;
import com.etherforge.mod.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModBlocks {

    public static Block ETHER_ORE;
    public static Block ETHER_ORE_IGNIS;
    public static Block ETHER_ORE_UMBRA;
    public static Block ETHER_BLOCK;

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        // Используем кастомные классы с дропами
        ETHER_ORE = new BlockEtherOre()
                .setRegistryName(Reference.MOD_ID, "ether_ore")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_ore")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        ETHER_ORE_IGNIS = new BlockIgnisOre()
                .setRegistryName(Reference.MOD_ID, "ether_ore_ignis")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_ore_ignis")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        ETHER_ORE_UMBRA = new BlockUmbraOre()
                .setRegistryName(Reference.MOD_ID, "ether_ore_umbra")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_ore_umbra")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        // Обычный блок (декор)
        ETHER_BLOCK = new Block(Material.GLASS)
                .setRegistryName(Reference.MOD_ID, "ether_block")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_block")
                .setCreativeTab(ModCreativeTab.INSTANCE)
                .setHardness(3.0f)
                .setResistance(5.0f);

        registry.registerAll(
                ETHER_ORE,
                ETHER_ORE_IGNIS,
                ETHER_ORE_UMBRA,
                ETHER_BLOCK
        );

        EtherForge.LOGGER.info("Блоки зарегистрированы");
    }

    @SubscribeEvent
    public static void onRegisterItemBlocks(RegistryEvent.Register<net.minecraft.item.Item> event) {
        Block[] blocks = {
                ETHER_ORE, ETHER_ORE_IGNIS, ETHER_ORE_UMBRA, ETHER_BLOCK
        };

        IForgeRegistry<net.minecraft.item.Item> registry = event.getRegistry();

        for (Block block : blocks) {
            ItemBlock itemBlock = new ItemBlock(block);
            itemBlock.setRegistryName(block.getRegistryName());
            registry.register(itemBlock);
        }
    }

    public static void register() {}
}