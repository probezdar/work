package com.etherforge.mod.init;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.blocks.*;
import com.etherforge.mod.blocks.pipe.BlockEtherPipe;
import com.etherforge.mod.blocks.pipe.BlockEtherPipeReinforced;
import com.etherforge.mod.blocks.pipe.BlockEtherPipeResonant;
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
    public static Block ETHER_CONDENSER;
    public static Block RESONANCE_FURNACE;
    public static Block ETHER_WORKBENCH;
    public static Block ETHER_PIPE;
    public static Block ETHER_PIPE_REINFORCED;
    public static Block ETHER_PIPE_RESONANT;

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

        ETHER_CONDENSER = new com.etherforge.mod.blocks.BlockEtherCondenser()
                .setRegistryName(Reference.MOD_ID, "ether_condenser")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_condenser")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        RESONANCE_FURNACE = new BlockResonanceFurnace()
                .setRegistryName(Reference.MOD_ID, "resonance_furnace")
                .setUnlocalizedName(Reference.MOD_ID + ".resonance_furnace")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        ETHER_WORKBENCH = new BlockEtherWorkbench()
                .setRegistryName(Reference.MOD_ID, "ether_workbench")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_workbench")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        ETHER_PIPE = new BlockEtherPipe(20, 100,1)
                .setRegistryName(Reference.MOD_ID, "ether_pipe")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_pipe")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        ETHER_PIPE_REINFORCED = new BlockEtherPipeReinforced()
                .setRegistryName(Reference.MOD_ID, "ether_pipe_reinforced")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_pipe_reinforced")
                .setCreativeTab(ModCreativeTab.INSTANCE);

        ETHER_PIPE_RESONANT = new BlockEtherPipeResonant()
                .setRegistryName(Reference.MOD_ID, "ether_pipe_resonant")
                .setUnlocalizedName(Reference.MOD_ID + ".ether_pipe_resonant")
                .setCreativeTab(ModCreativeTab.INSTANCE);



        registry.registerAll(
                ETHER_ORE,
                ETHER_ORE_IGNIS,
                ETHER_ORE_UMBRA,
                ETHER_BLOCK,
                ETHER_CONDENSER,
                RESONANCE_FURNACE,
                ETHER_WORKBENCH,
                ETHER_PIPE,ETHER_PIPE_REINFORCED,ETHER_PIPE_RESONANT
        );

        EtherForge.LOGGER.info("Блоки зарегистрированы");
    }

    @SubscribeEvent
    public static void onRegisterItemBlocks(RegistryEvent.Register<net.minecraft.item.Item> event) {
        Block[] blocks = {
                ETHER_ORE, ETHER_ORE_IGNIS, ETHER_ORE_UMBRA, ETHER_BLOCK, ETHER_CONDENSER,
                RESONANCE_FURNACE, ETHER_WORKBENCH, ETHER_PIPE, ETHER_PIPE_REINFORCED, ETHER_PIPE_RESONANT

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