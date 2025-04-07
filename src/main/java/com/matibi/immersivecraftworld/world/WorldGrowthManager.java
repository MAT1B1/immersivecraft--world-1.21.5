package com.matibi.immersivecraftworld.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Map;

public class WorldGrowthManager {

    private static int TickCounter = 0;

    private static final int SCAN_RADIUS = 30;
    private static final int SCAN_HEIGHT = 10;
    private static final int POSITIONS_PER_PLAYER = 20;

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
        ServerTickEvents.END_WORLD_TICK.register(WorldGrowthManager::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        TickCounter++;
        if (TickCounter % 10 != 0) return;

        Random random = world.getRandom();

        for (ServerPlayerEntity player : world.getPlayers()) {
            BlockPos center = player.getBlockPos();

            for (int i = 0; i < POSITIONS_PER_PLAYER; i++) {
                int dx = random.nextBetween(-SCAN_RADIUS, SCAN_RADIUS + 1);
                int dz = random.nextBetween(-SCAN_RADIUS, SCAN_RADIUS + 1);
                int dy = random.nextBetween(-SCAN_HEIGHT, SCAN_HEIGHT + 1);

                BlockPos groundPos = center.add(dx, dy, dz);
                BlockPos abovePos = groundPos.up();

                BlockState ground = world.getBlockState(groundPos);
                BlockState above = world.getBlockState(abovePos);

                trySpawnGrass(world, ground, above, abovePos);

                Block block = ground.getBlock();

                if (isTreeLog(block)) {
                    trySpreadSapling(world, groundPos, block, random);
                }
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
                        && (targetState.isAir() || PROPAGATE_PLANTS.contains(targetState.getBlock()))
                        && random.nextFloat() < 0.1f) {

                    world.setBlockState(saplingPos, sapling.getDefaultState());
                    return;
                }
            }
        }
    }

    private static boolean isTreeLog(Block block) {
        return LOG_TO_SAPLING.containsKey(block);
    }

}