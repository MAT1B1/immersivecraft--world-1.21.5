package com.matibi.immersivecraftworld.world;

import com.matibi.immersivecraftworld.ImmersiveCraftWorld;
import com.matibi.immersivecraftworld.util.ChunkTracker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

import java.util.*;

public class WorldGrowthManager {

    private static int TickCounter = 0;

    private static final List<Block> PROPAGATE_PLANTS = List.of(
            Blocks.SHORT_GRASS,
            Blocks.POPPY,
            Blocks.DANDELION,
            Blocks.CORNFLOWER,
            Blocks.AZURE_BLUET,
            Blocks.ALLIUM,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.FERN,
            Blocks.BUSH,
            Blocks.FIREFLY_BUSH,
            Blocks.WILDFLOWERS
    );

    private static final Map<Block, Block> LOG_TO_SAPLING = Map.of(
            Blocks.OAK_LOG, Blocks.OAK_SAPLING,
            Blocks.BIRCH_LOG, Blocks.BIRCH_SAPLING,
            Blocks.SPRUCE_LOG, Blocks.SPRUCE_SAPLING,
            Blocks.JUNGLE_LOG, Blocks.JUNGLE_SAPLING,
            Blocks.ACACIA_LOG, Blocks.ACACIA_SAPLING,
            Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_SAPLING,
            Blocks.CHERRY_LOG, Blocks.CHERRY_SAPLING
    );


    public static void register() {
        ImmersiveCraftWorld.LOGGER.info("Registering the growth manager from " + ImmersiveCraftWorld.MOD_ID);
        ServerTickEvents.END_WORLD_TICK.register(WorldGrowthManager::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        TickCounter++;
        if (TickCounter % 10 != 0) return;

        Random random = world.getRandom();

        Set<ChunkPos> loadedChunks = ChunkTracker.getLoadedChunks();
        List<ChunkPos> chunkList = new ArrayList<>(loadedChunks);
        Collections.shuffle(chunkList);
        int maxChunksPerTick = 10;
        int processed = 0;

        for (ChunkPos chunkPos : chunkList) {
            if (processed++ >= maxChunksPerTick) break;

            BlockPos.Mutable groundPos = new BlockPos.Mutable();
            BlockPos.Mutable abovePos = new BlockPos.Mutable();

            int x = chunkPos.getStartX() + random.nextInt(16);
            int z = chunkPos.getStartZ() + random.nextInt(16);
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z) - 1;

            groundPos.set(x, y, z);
            abovePos.set(x, y + 1, z);

            BlockState ground = world.getBlockState(groundPos);
            BlockState above = world.getBlockState(abovePos);

            trySpawnGrass(world, ground, above, abovePos);

            SeasonState state = SeasonState.get(world);

            switch (SeasonManager.seasonName(state.getSeason())) {
                case "Winter" -> tryApplyIce(world, ground, groundPos,abovePos);
                case "Summer" -> tryDehydrateGrass(world, groundPos);
                case "Spring" -> trySpawnFlowers(world, abovePos, ground, above, random);
                case "Autumn" -> {}
            }

            Block block = ground.getBlock();

            if (isTreeLog(block)) {
                trySpreadSapling(world, groundPos, block, random);
            }
        }
    }

    private static void trySpawnGrass(ServerWorld world, BlockState ground, BlockState above,
                                      BlockPos abovePos) {
        if (!ground.isOf(Blocks.GRASS_BLOCK)) return;
        if (!world.isSkyVisible(abovePos)) return;

        if (above.isOf(Blocks.SHORT_GRASS)) {
            BlockPos top = abovePos.up();
            if (world.getBlockState(top).isAir()) {
                world.setBlockState(abovePos, Blocks.TALL_GRASS.getDefaultState()
                        .with(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
                world.setBlockState(top, Blocks.TALL_GRASS.getDefaultState()
                        .with(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
            }
            return;
        }

        if (!above.isAir()) return;

        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos neighborPos = abovePos.offset(dir);
            BlockState neighbor = world.getBlockState(neighborPos);

            if (PROPAGATE_PLANTS.contains(neighbor.getBlock())) {
                world.setBlockState(abovePos, neighbor.getBlock().getDefaultState());
                return;
            }
        }
        world.setBlockState(abovePos, Blocks.SHORT_GRASS.getDefaultState());
    }

    private static void trySpreadSapling(ServerWorld world, BlockPos origin, Block logBlock, Random random) {
        Block sapling = LOG_TO_SAPLING.get(logBlock);

        for (int i = 0; i < 10; i++) {
            int dx = random.nextBetween(-10, 11);
            int dz = random.nextBetween(-10, 11);
            BlockPos basePos = origin.add(dx, 0, dz);
            BlockPos surfacePos = world.getTopPosition(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, basePos).down();

            for (int dy = -1; dy <= 1; dy++) {
                BlockPos ground = surfacePos.up(dy);
                BlockPos saplingPos = ground.up();

                BlockState groundState = world.getBlockState(ground);
                BlockState targetState = world.getBlockState(saplingPos);

                if (groundState.isOf(Blocks.GRASS_BLOCK)
                        && (targetState.isAir() || PROPAGATE_PLANTS.contains(targetState.getBlock()))) {

                    world.setBlockState(saplingPos, sapling.getDefaultState());
                    return;
                }
            }
        }
    }

    private static void tryApplyIce(ServerWorld world, BlockState ground, BlockPos groundPos, BlockPos abovePos) {
        BlockState above = world.getBlockState(abovePos);
        if (world.isSkyVisible(abovePos) && above.isAir() && ground.isOf(Blocks.WATER)) {
            world.setBlockState(groundPos, Blocks.ICE.getDefaultState());
        }
    }

    private static void tryDehydrateGrass(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isOf(Blocks.SHORT_GRASS)) {
            world.setBlockState(pos, Blocks.SHORT_DRY_GRASS.getDefaultState());
        } else if (state.isOf(Blocks.TALL_GRASS)) {
            world.setBlockState(pos, Blocks.TALL_DRY_GRASS.getDefaultState());
        }
    }

    private static void trySpawnFlowers(ServerWorld world, BlockPos abovePos, BlockState ground, BlockState above, Random random) {
        if (!ground.isOf(Blocks.GRASS_BLOCK)) return;
        if (!above.isAir() && !PROPAGATE_PLANTS.contains(above.getBlock()) && !above.isOf(Blocks.SHORT_GRASS)) return;
        if (!world.isSkyVisible(abovePos)) return;

        List<Block> flowers = PROPAGATE_PLANTS.stream()
                .filter(b -> b != Blocks.SHORT_GRASS && b != Blocks.FERN)
                .toList();

        if (flowers.isEmpty()) return;

        Block flower = flowers.get(random.nextInt(flowers.size()));
        world.setBlockState(abovePos, flower.getDefaultState());
    }


    private static boolean isTreeLog(Block block) {
        return LOG_TO_SAPLING.containsKey(block);
    }

}