package com.etherforge.mod.world.gen;

import com.etherforge.mod.EtherForge;
import com.etherforge.mod.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

public class ModWorldGen implements IWorldGenerator {

    // ═══════════════════════════════════════════════
    //  Параметры генерации
    // ═══════════════════════════════════════════════

    // Ether Ore — обычный мир, средние глубины
    private static final int ETHER_ORE_VEIN_SIZE    = 6;  // размер жилы
    private static final int ETHER_ORE_ATTEMPTS      = 4;  // попыток на чанк
    private static final int ETHER_ORE_MIN_Y         = 10; // мин. высота
    private static final int ETHER_ORE_MAX_Y         = 50; // макс. высота

    // Ignis Ore — глубоко, ближе к лаве
    private static final int IGNIS_ORE_VEIN_SIZE     = 4;
    private static final int IGNIS_ORE_ATTEMPTS      = 2;
    private static final int IGNIS_ORE_MIN_Y         = 5;
    private static final int IGNIS_ORE_MAX_Y         = 25;

    // Umbra Ore — очень глубоко, редкая
    private static final int UMBRA_ORE_VEIN_SIZE     = 3;
    private static final int UMBRA_ORE_ATTEMPTS      = 1;
    private static final int UMBRA_ORE_MIN_Y         = 5;
    private static final int UMBRA_ORE_MAX_Y         = 15;

    // ═══════════════════════════════════════════════
    //  Регистрация генератора
    // ═══════════════════════════════════════════════


    public static void register() {
        GameRegistry.registerWorldGenerator(new ModWorldGen(), 0);
    }

    // ═══════════════════════════════════════════════
    //  Основной метод генерации
    // ═══════════════════════════════════════════════

    @Override
    public void generate(Random random, int chunkX, int chunkZ,
                         World world, IChunkGenerator chunkGenerator,
                         IChunkProvider chunkProvider) {

        switch (world.provider.getDimension()) {
            case 0:
                generateOverworld(random, chunkX, chunkZ, world);
                break;
        }
    }

    // ═══════════════════════════════════════════════
    //  Генерация в обычном мире
    // ═══════════════════════════════════════════════

    private void generateOverworld(Random random, int chunkX, int chunkZ, World world) {
        generateOre(
                ModBlocks.ETHER_ORE.getDefaultState(),
                world, random, chunkX, chunkZ,
                ETHER_ORE_VEIN_SIZE,
                ETHER_ORE_ATTEMPTS,
                ETHER_ORE_MIN_Y,
                ETHER_ORE_MAX_Y
        );

        generateOre(
                ModBlocks.ETHER_ORE_IGNIS.getDefaultState(),
                world, random, chunkX, chunkZ,
                IGNIS_ORE_VEIN_SIZE,
                IGNIS_ORE_ATTEMPTS,
                IGNIS_ORE_MIN_Y,
                IGNIS_ORE_MAX_Y
        );

        generateOre(
                ModBlocks.ETHER_ORE_UMBRA.getDefaultState(),
                world, random, chunkX, chunkZ,
                UMBRA_ORE_VEIN_SIZE,
                UMBRA_ORE_ATTEMPTS,
                UMBRA_ORE_MIN_Y,
                UMBRA_ORE_MAX_Y
        );
    }

    // ═══════════════════════════════════════════════
    //  Вспомогательный метод генерации жилы
    // ═══════════════════════════════════════════════

    private void generateOre(IBlockState oreState, World world, Random random,
                             int chunkX, int chunkZ,
                             int veinSize, int attempts,
                             int minY, int maxY) {

        WorldGenMinable generator = new WorldGenMinable(oreState, veinSize);

        for (int i = 0; i < attempts; i++) {
            // Случайная позиция внутри чанка
            int x = chunkX * 16 + random.nextInt(16);
            int y = minY + random.nextInt(maxY - minY);
            int z = chunkZ * 16 + random.nextInt(16);

            generator.generate(world, random, new BlockPos(x, y, z));
        }
    }


}