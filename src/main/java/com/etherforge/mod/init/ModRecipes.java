package com.etherforge.mod.init;

import com.etherforge.mod.util.Reference;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ModRecipes {

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();

        // ═══════════════════════════════════════════
        //  Блок из кристаллов (9 кристаллов → 1 блок)
        // ═══════════════════════════════════════════
        registry.register(new ShapedOreRecipe(
                null,
                new ItemStack(ModBlocks.ETHER_BLOCK),
                "CCC",
                "CCC",
                "CCC",
                'C', ModItems.ETHER_CRYSTAL
        ).setRegistryName(Reference.MOD_ID, "ether_block_from_crystals"));

        // ═══════════════════════════════════════════
        //  Блок → 9 кристаллов (распаковка)
        // ═══════════════════════════════════════════
        registry.register(new ShapelessOreRecipe(
                null,
                new ItemStack(ModItems.ETHER_CRYSTAL, 9),
                new ItemStack(ModBlocks.ETHER_BLOCK)
        ).setRegistryName(Reference.MOD_ID, "ether_crystal_from_block"));

        // ═══════════════════════════════════════════
        //  Эфироскоп (инструмент для поиска эфира)
        // ═══════════════════════════════════════════
        //   [C][ ][C]
        //   [ ][S][ ]   S = палка, C = кристалл
        //   [ ][S][ ]
        registry.register(new ShapedOreRecipe(
                null,
                new ItemStack(ModItems.ETHERSCOPE),
                "C C",
                " S ",
                " S ",
                'C', ModItems.ETHER_CRYSTAL,
                'S', Items.STICK
        ).setRegistryName(Reference.MOD_ID, "etherscope"));

        // ═══════════════════════════════════════════
        //  Конвертация кристаллов
        //  (базовый → специализированный через смешивание)
        // ═══════════════════════════════════════════

        // 4 Ether Crystal → 1 Ignis Crystal (+ уголь для тепла)
        registry.register(new ShapelessOreRecipe(
                null,
                new ItemStack(ModItems.CRYSTAL_IGNIS),
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                Items.COAL
        ).setRegistryName(Reference.MOD_ID, "crystal_ignis_from_ether"));

        // 4 Ether Crystal → 1 Umbra Crystal (+ чернильный мешок для тьмы)
        registry.register(new ShapelessOreRecipe(
                null,
                new ItemStack(ModItems.CRYSTAL_UMBRA),
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                Items.DYE
        ).setRegistryName(Reference.MOD_ID, "crystal_umbra_from_ether"));

        // 4 Ether Crystal → 1 Aqua Crystal (+ вода через бутылку)
        registry.register(new ShapelessOreRecipe(
                null,
                new ItemStack(ModItems.CRYSTAL_AQUA),
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                Items.WATER_BUCKET
        ).setRegistryName(Reference.MOD_ID, "crystal_aqua_from_ether"));

        // 4 Ether Crystal → 1 Volta Crystal (+ золото для энергии)
        registry.register(new ShapelessOreRecipe(
                null,
                new ItemStack(ModItems.CRYSTAL_VOLTA),
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                Items.GOLD_NUGGET
        ).setRegistryName(Reference.MOD_ID, "crystal_volta_from_ether"));

        // 4 Ether Crystal → 1 Lux Crystal (+ светящийся камень)
        registry.register(new ShapelessOreRecipe(
                null,
                new ItemStack(ModItems.CRYSTAL_LUX),
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                ModItems.ETHER_CRYSTAL,
                Items.GLOWSTONE_DUST
        ).setRegistryName(Reference.MOD_ID, "crystal_lux_from_ether"));
    }
}