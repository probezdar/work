package com.etherforge.mod.blocks;

import com.etherforge.mod.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockEtherOre extends Block {

    public BlockEtherOre() {
        super(Material.ROCK);
        setHardness(3.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 2); // алмазная кирка
    }

    // Что дропается при разбивке
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.ETHER_CRYSTAL;
    }

    // Количество дропа
    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    // Бонус от fortune
    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0) {
            return 1 + random.nextInt(fortune + 1);
        }
        return quantityDropped(random);
    }

    // Опыт при разбивке
    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world,
                          BlockPos pos, int fortune) {
        return 2 + new Random().nextInt(4); // 2-5 опыта
    }
}