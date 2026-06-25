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

        // Конденсатор Эфира
//  [S][S][S]
//  [S][C][S]   S = камень, C = Ether Crystal
//  [S][S][S]
        registry.register(new ShapedOreRecipe(
                null,
                new ItemStack(ModBlocks.ETHER_CONDENSER),
                "SSS",
                "SCS",
                "SSS",
                'S', net.minecraft.init.Blocks.STONE,
                'C', ModItems.ETHER_CRYSTAL
        ).setRegistryName(Reference.MOD_ID, "ether_condenser"));

        // Резонансная Печь
//  [C][I][C]
//  [I][F][I]   I = Ignis Crystal, C = Ether Crystal, F = Furnace
//  [C][I][C]
        registry.register(new ShapedOreRecipe(
                null,
                new ItemStack(ModBlocks.RESONANCE_FURNACE),
                "CIC",
                "IFI",
                "CIC",
                'C', ModItems.ETHER_CRYSTAL,
                'I', ModItems.CRYSTAL_IGNIS,
                'F', net.minecraft.init.Blocks.FURNACE
        ).setRegistryName(Reference.MOD_ID, "resonance_furnace"));

        // Ядро Механического Голема
// [V][U][V]
// [U][I][U]   V=Volta, U=Umbra, I=Iron Block
// [V][U][V]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModItems.GOLEM_CORE_MECH),
                "VUV", "UIU", "VUV",
                'V', ModItems.CRYSTAL_VOLTA,
                'U', ModItems.CRYSTAL_UMBRA,
                'I', net.minecraft.init.Blocks.IRON_BLOCK
        ).setRegistryName(Reference.MOD_ID, "golem_core_mech"));

// Руна Idle — простейшая
// [ ][L][ ]
// [L][E][L]   L=Lux, E=Ether Crystal
// [ ][L][ ]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModItems.RUNE_IDLE),
                " L ", "LEL", " L ",
                'L', ModItems.CRYSTAL_LUX,
                'E', ModItems.ETHER_CRYSTAL
        ).setRegistryName(Reference.MOD_ID, "rune_idle"));

// Руна Collect
// [E][L][E]
// [L][A][L]   A=Aqua
// [E][L][E]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModItems.RUNE_COLLECT),
                "ELE", "LAL", "ELE",
                'E', ModItems.ETHER_CRYSTAL,
                'L', ModItems.CRYSTAL_LUX,
                'A', ModItems.CRYSTAL_AQUA
        ).setRegistryName(Reference.MOD_ID, "rune_collect"));

// Руна Return
// [ ][A][ ]
// [A][U][A]   U=Umbra
// [ ][A][ ]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModItems.RUNE_RETURN),
                " A ", "AUA", " A ",
                'A', ModItems.CRYSTAL_AQUA,
                'U', ModItems.CRYSTAL_UMBRA
        ).setRegistryName(Reference.MOD_ID, "rune_return"));

// Руна Mine
// [I][V][I]
// [V][E][V]   I=Iron, V=Volta
// [I][V][I]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModItems.RUNE_MINE),
                "IVI", "VEV", "IVI",
                'I', net.minecraft.init.Items.IRON_INGOT,
                'V', ModItems.CRYSTAL_VOLTA,
                'E', ModItems.ETHER_CRYSTAL
        ).setRegistryName(Reference.MOD_ID, "rune_mine"));

        // Стол Голема
// [U][V][U]
// [V][I][V]   I=Iron Block, V=Volta, U=Umbra
// [I][I][I]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModBlocks.GOLEM_WORKSTATION),
                "UVU",
                "VIV",
                "III",
                'U', ModItems.CRYSTAL_UMBRA,
                'V', ModItems.CRYSTAL_VOLTA,
                'I', net.minecraft.init.Blocks.IRON_BLOCK
        ).setRegistryName(Reference.MOD_ID, "golem_workstation"));

        // Ядро Морфо Голема
// [A][I][A]
// [I][S][I]   A=Aqua, I=Ignis, S=Slime Block
// [A][I][A]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModItems.GOLEM_CORE_MORPHO),
                "AIA", "ISI", "AIA",
                'A', ModItems.CRYSTAL_AQUA,
                'I', ModItems.CRYSTAL_IGNIS,
                'S', net.minecraft.init.Blocks.SLIME_BLOCK
        ).setRegistryName(Reference.MOD_ID, "golem_core_morpho"));

// Ядро Эфирного Голема
// [L][U][L]
// [U][E][U]   L=Lux, U=Umbra, E=Ether Block
// [L][U][L]
        registry.register(new ShapedOreRecipe(null,
                new ItemStack(ModItems.GOLEM_CORE_ETHEREAL),
                "LUL", "UEU", "LUL",
                'L', ModItems.CRYSTAL_LUX,
                'U', ModItems.CRYSTAL_UMBRA,
                'E', ModBlocks.ETHER_BLOCK
        ).setRegistryName(Reference.MOD_ID, "golem_core_ethereal"));
    }
}