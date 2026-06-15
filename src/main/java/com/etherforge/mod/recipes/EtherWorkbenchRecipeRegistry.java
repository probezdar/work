package com.etherforge.mod.recipes;

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
    }
}