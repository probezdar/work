package com.etherforge.mod.proxy;

import com.etherforge.mod.client.render.RenderEtherealGolem;
import com.etherforge.mod.client.render.RenderMechGolem;
import com.etherforge.mod.client.render.RenderMorphoGolem;
import com.etherforge.mod.entities.EntityEtherealGolem;
import com.etherforge.mod.entities.EntityMechGolem;
import com.etherforge.mod.entities.EntityMorphoGolem;
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
        net.minecraftforge.fml.client.registry.RenderingRegistry
                .registerEntityRenderingHandler(
                        EntityMechGolem.class,
                        RenderMechGolem::new);

        net.minecraftforge.fml.client.registry.RenderingRegistry
                .registerEntityRenderingHandler(
                        EntityMorphoGolem.class,
                        RenderMorphoGolem::new);

        net.minecraftforge.fml.client.registry.RenderingRegistry
                .registerEntityRenderingHandler(
                        EntityEtherealGolem.class,
                        RenderEtherealGolem::new);
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
        registerItemModel(ModItems.CATALYST_STEAM, "catalyst_steam");
        registerItemModel(ModItems.CATALYST_DAWN, "catalyst_dawn");
        registerItemModel(ModItems.CATALYST_DEPTH, "catalyst_depth");
        registerItemModel(ModItems.CATALYST_ECLIPSE, "catalyst_eclipse");
        registerItemModel(ModItems.CATALYST_PLASMA, "catalyst_plasma");
        registerItemModel(ModItems.CATALYST_SURGE, "catalyst_surge");
        registerItemModel(ModItems.CATALYST_SPARK, "catalyst_spark");
        registerItemModel(ModItems.CATALYST_EMBER, "catalyst_ember");
        registerItemModel(ModItems.GOLEM_CORE_MECH,     "golem_core_mech");
        registerItemModel(ModItems.GOLEM_CORE_MORPHO,   "golem_core_morpho");
        registerItemModel(ModItems.GOLEM_CORE_ETHEREAL, "golem_core_ethereal");
        registerItemModel(ModItems.RUNE_IDLE,           "rune_idle");
        registerItemModel(ModItems.RUNE_COLLECT,        "rune_collect");
        registerItemModel(ModItems.RUNE_MINE,           "rune_mine");
        registerItemModel(ModItems.RUNE_RETURN,         "rune_return");
        registerItemModel(ModItems.RUNE_TRANSFER,       "rune_transfer");


        // ── Блоки ───────────────────────────────────────────────
        registerBlockModel(ModBlocks.ETHER_ORE,       "ether_ore");
        registerBlockModel(ModBlocks.ETHER_ORE_IGNIS, "ether_ore_ignis");
        registerBlockModel(ModBlocks.ETHER_ORE_UMBRA, "ether_ore_umbra");
        registerBlockModel(ModBlocks.ETHER_BLOCK,     "ether_block");
        registerBlockModel(ModBlocks.ETHER_CONDENSER, "ether_condenser");
        registerBlockModel(ModBlocks.RESONANCE_FURNACE, "resonance_furnace");
        registerBlockModel(ModBlocks.ETHER_WORKBENCH, "ether_workbench");
        registerBlockModel(ModBlocks.ETHER_PIPE,            "ether_pipe");
        registerBlockModel(ModBlocks.ETHER_PIPE_REINFORCED, "ether_pipe_reinforced");
        registerBlockModel(ModBlocks.ETHER_PIPE_RESONANT,   "ether_pipe_resonant");
        registerBlockModel(ModBlocks.GOLEM_WORKSTATION, "golem_workstation");
        registerBlockModel(ModBlocks.GOLEM_WORKSTATION, "golem_workstation");
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