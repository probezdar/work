package com.etherforge.mod;

import com.etherforge.mod.init.ModBlocks;
import com.etherforge.mod.init.ModItems;
import com.etherforge.mod.proxy.CommonProxy;
import com.etherforge.mod.util.Reference;
import com.etherforge.mod.world.gen.ModWorldGen;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION
)
public class EtherForge {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @Instance
    public static EtherForge instance;

    @SidedProxy(
            clientSide = Reference.CLIENT_PROXY,
            serverSide = Reference.COMMON_PROXY
    )
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("EtherForge - PreInit начат");
        proxy.preInit(); // ← возвращаем
        LOGGER.info("EtherForge - PreInit завершён");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("EtherForge - Init начат");
        proxy.init();
        ModWorldGen.register();
        registerSmeltingRecipes();
        LOGGER.info("EtherForge - Init завершён");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("EtherForge - PostInit завершён");
    }

    private void registerSmeltingRecipes() {
        // Ether Ore → Ether Crystal
        net.minecraft.item.crafting.FurnaceRecipes.instance().addSmeltingRecipe(
                new net.minecraft.item.ItemStack(ModBlocks.ETHER_ORE),
                new net.minecraft.item.ItemStack(ModItems.ETHER_CRYSTAL),
                0.7f // опыт
        );

        // Ignis Ore → Ignis Crystal
        net.minecraft.item.crafting.FurnaceRecipes.instance().addSmeltingRecipe(
                new net.minecraft.item.ItemStack(ModBlocks.ETHER_ORE_IGNIS),
                new net.minecraft.item.ItemStack(ModItems.CRYSTAL_IGNIS),
                0.9f
        );

        // Umbra Ore → Umbra Crystal
        net.minecraft.item.crafting.FurnaceRecipes.instance().addSmeltingRecipe(
                new net.minecraft.item.ItemStack(ModBlocks.ETHER_ORE_UMBRA),
                new net.minecraft.item.ItemStack(ModItems.CRYSTAL_UMBRA),
                1.2f
        );
    }
}