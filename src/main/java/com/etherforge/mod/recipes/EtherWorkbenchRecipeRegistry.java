package com.etherforge.mod.recipes;

import com.etherforge.mod.init.ModBlocks;
import com.etherforge.mod.init.ModItems;
import com.etherforge.mod.tileentity.TileEntityEtherWorkbench;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EtherWorkbenchRecipeRegistry {

    private static final List<EtherWorkbenchRecipe> RECIPES = new ArrayList<>();

    public static void register(EtherWorkbenchRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static ItemStack findRecipe(TileEntityEtherWorkbench bench) {
        for (EtherWorkbenchRecipe recipe : RECIPES) {
            if (recipe.matches(bench)) return recipe.getOutput();
        }
        return ItemStack.EMPTY;
    }

    public static void init() {
        // Тестовый рецепт: 4 булыжника + 4 Steam Catalyst = 1 алмаз (временно)
        register(new EtherWorkbenchRecipe(
                new ItemStack[] {
                        new ItemStack(Blocks.COBBLESTONE),
                        new ItemStack(Blocks.COBBLESTONE),
                        new ItemStack(Blocks.COBBLESTONE),
                        new ItemStack(Blocks.COBBLESTONE)
                },
                new ItemStack[] {
                        new ItemStack(ModItems.CATALYST_STEAM),
                        new ItemStack(ModItems.CATALYST_STEAM),
                        new ItemStack(ModItems.CATALYST_STEAM),
                        new ItemStack(ModItems.CATALYST_STEAM)
                },
                new ItemStack(Items.DIAMOND)
        ));

        // Пример: 2 железа (по диагонали) + 4 Plasma Catalyst = 1 золото
        register(new EtherWorkbenchRecipe(
                new ItemStack[] {
                        new ItemStack(Items.IRON_INGOT),
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        new ItemStack(Items.IRON_INGOT)
                },
                new ItemStack[] {
                        new ItemStack(ModItems.CATALYST_PLASMA),
                        new ItemStack(ModItems.CATALYST_PLASMA),
                        new ItemStack(ModItems.CATALYST_PLASMA),
                        new ItemStack(ModItems.CATALYST_PLASMA)
                },
                new ItemStack(Items.GOLD_INGOT)
        ));

        // Тир 1: Ether Pipe
// Сетка: [Iron][Crystal][Iron] + [Crystal]    = 4 трубы
//        [Iron][Crystal][Iron]   (только 4 слота 2x2)
// Адаптируем под 2x2:
// [Iron Ingot][Ether Crystal]
// [Ether Crystal][Iron Ingot]
// Катализаторы: 4x Aqua Crystal (поток)
        register(new EtherWorkbenchRecipe(
                new ItemStack[] {
                        new ItemStack(Items.IRON_INGOT),
                        new ItemStack(ModItems.ETHER_CRYSTAL),
                        new ItemStack(ModItems.ETHER_CRYSTAL),
                        new ItemStack(Items.IRON_INGOT)
                },
                new ItemStack[] {
                        new ItemStack(ModItems.CRYSTAL_AQUA),
                        new ItemStack(ModItems.CRYSTAL_AQUA),
                        new ItemStack(ModItems.CRYSTAL_AQUA),
                        new ItemStack(ModItems.CRYSTAL_AQUA)
                },
                new ItemStack(ModBlocks.ETHER_PIPE, 4)
        ));

        // Тир 2: Reinforced Pipe
// [Gold Ingot][Volta Crystal]
// [Volta Crystal][Gold Ingot]
// Катализаторы: 4x Catalyst_Surge (Aqua+Volta)
        register(new EtherWorkbenchRecipe(
                new ItemStack[] {
                        new ItemStack(Items.GOLD_INGOT),
                        new ItemStack(ModItems.CRYSTAL_VOLTA),
                        new ItemStack(ModItems.CRYSTAL_VOLTA),
                        new ItemStack(Items.GOLD_INGOT)
                },
                new ItemStack[] {
                        new ItemStack(ModItems.CATALYST_SURGE),
                        new ItemStack(ModItems.CATALYST_SURGE),
                        new ItemStack(ModItems.CATALYST_SURGE),
                        new ItemStack(ModItems.CATALYST_SURGE)
                },
                new ItemStack(ModBlocks.ETHER_PIPE_REINFORCED, 4)
        ));

// Тир 3: Resonant Pipe
// [Obsidian][Umbra Crystal]
// [Umbra Crystal][Obsidian]
// Катализаторы: 4x Catalyst_Depth (Aqua+Umbra)
        register(new EtherWorkbenchRecipe(
                new ItemStack[] {
                        new ItemStack(Blocks.OBSIDIAN),
                        new ItemStack(ModItems.CRYSTAL_UMBRA),
                        new ItemStack(ModItems.CRYSTAL_UMBRA),
                        new ItemStack(Blocks.OBSIDIAN)
                },
                new ItemStack[] {
                        new ItemStack(ModItems.CATALYST_DEPTH),
                        new ItemStack(ModItems.CATALYST_DEPTH),
                        new ItemStack(ModItems.CATALYST_DEPTH),
                        new ItemStack(ModItems.CATALYST_DEPTH)
                },
                new ItemStack(ModBlocks.ETHER_PIPE_RESONANT, 4)
        ));
    }
}