package com.etherforge.mod.blocks;

import com.etherforge.mod.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockUmbraOre extends Block {

    public BlockUmbraOre() {
        super(Material.ROCK);
        setHardness(4.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 2);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.CRYSTAL_UMBRA;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0) {
            return 1 + random.nextInt(fortune + 1);
        }
        return quantityDropped(random);
    }

    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world,
                          BlockPos pos, int fortune) {
        return 4 + new Random().nextInt(6); // 4-9 опыта
    }
}