package top.ribs.scguns.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.SulfurGasCloud;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModParticleTypes;

import java.util.List;

public class SulfurVentBlock extends VentBlock {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int CLOUD_RADIUS = 8;
    private static final int MAX_DUST_PARTICLES_PER_TICK = 10;
    private static final int CLOUD_SPAWN_CHANCE = 95;
    private static final int DUST_SPAWN_CHANCE = 98;
    private static final int EFFECT_INTERVAL = 10;
    public static final int EFFECT_RADIUS = 8;
    public static final int MAX_ACTIVE_VENTS = 1;
    public static final int CHECK_RADIUS = 32;
    public static final int EFFECT_RADIUS_SQUARED = EFFECT_RADIUS * EFFECT_RADIUS;

    public SulfurVentBlock(Properties properties) {
        super(properties, new ResourceLocation(Reference.MOD_ID, "sulfur_vent"));
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!world.isClientSide) {
            if (state.getValue(ACTIVE)) {
                Vec3 center = Vec3.atCenterOf(pos);
                if (SulfurGasCloud.checkAndHandleFireExplosion(world, center, EFFECT_RADIUS)) {
                    shutdownVentTemporarily(world, pos, state);
                    return;
                }
                boolean hasCollectorAbove = hasVentCollectorAbove(world, pos);

                if (state.getValue(VENT_TYPE) == VentType.BASE && !hasCollectorAbove) {
                    // Pass world game time as tick count for optimization
                    int tickCount = (int) (world.getGameTime() % Integer.MAX_VALUE);
                    spawnSulfurCloud(world, pos, random, tickCount);
                    spawnSulfurDust(world, pos, random, tickCount);
                    performEnvironmentalAction(world, pos, center, random);
                }

                applyEffectsToEntities(world, pos);

                world.sendParticles(ModParticleTypes.SULFUR_SMOKE.get(),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1, 0.5, 0.5, 0.5, 0.01);
            } else {
                return;
            }

            world.scheduleTick(pos, this, EFFECT_INTERVAL);
            if (world.getGameTime() % calculateNextTickInterval() == 0) {
                scheduleParticleSpawn(world, pos);
            }
        }
    }

    private void shutdownVentTemporarily(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(ACTIVE, false), 3);
        level.sendBlockUpdated(pos, state, state, 2);
        level.scheduleTick(pos, this, 100);
    }

    private void performEnvironmentalAction(ServerLevel world, BlockPos pos, Vec3 center, RandomSource random) {
        SulfurGasCloud.destroyNatureInArea(world, center, EFFECT_RADIUS, random);

        if (this.config != null && this.config.getPlacement().isEnabled()) {
            if (shouldPlaceBlock(random)) {
                placeLayerBlock(world, pos, random);
            }
        }
    }

    private boolean shouldPlaceBlock(RandomSource random) {
        if (this.config == null) return true;
        return random.nextFloat() < this.config.getPlacement().getPlacementChance();
    }

    private void placeLayerBlock(ServerLevel world, BlockPos pos, RandomSource random) {
        if (this.config == null) return;

        ResourceLocation blockToPlace = this.config.getPlacement().getBlockToPlace();
        if (blockToPlace == null) return;

        Block layerBlock;

        if (blockToPlace.toString().equals("scguns:sulfur_layer")) {
            layerBlock = ModBlocks.SULFUR_LAYER.get();
        } else if (blockToPlace.toString().equals("scguns:niter_layer")) {
            layerBlock = ModBlocks.NITER_LAYER.get();
        } else {
            layerBlock = ForgeRegistries.BLOCKS.getValue(blockToPlace);
        }

        if (layerBlock == null || layerBlock == Blocks.AIR) {
            LOGGER.warn("Could not resolve block: {}", blockToPlace);
            return;
        }

        int radius = this.config.getPlacement().getRadius();

        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = Math.sqrt(random.nextDouble()) * radius;
        int x = (int) Math.round(Math.cos(angle) * distance);
        int z = (int) Math.round(Math.sin(angle) * distance);
        int y = random.nextInt(3) - 1;

        BlockPos randomPos = pos.offset(x, y, z);
        if (canPlaceLayerBlock(world, randomPos, layerBlock)) {
            BlockPos abovePos = randomPos.above();
            BlockState currentState = world.getBlockState(abovePos);
            boolean isWater = currentState.getFluidState().is(FluidTags.WATER);

            if (currentState.isAir() || isWater) {
                BlockState layerState = layerBlock.defaultBlockState();
                if (layerState.hasProperty(SulfurLayerBlock.LAYERS)) {
                    layerState = layerState.setValue(SulfurLayerBlock.LAYERS, 1);
                }
                world.setBlock(abovePos, layerState, 3);
            }
        }
    }

    private boolean canPlaceLayerBlock(ServerLevel world, BlockPos pos, Block layerBlock) {
        BlockState state = world.getBlockState(pos);
        BlockState aboveState = world.getBlockState(pos.above());
        return state.isFaceSturdy(world, pos, Direction.UP)
                && (aboveState.isAir() || aboveState.getFluidState().is(FluidTags.WATER))
                && !(state.getBlock() instanceof SulfurVentBlock)
                && !(aboveState.getBlock() instanceof SulfurVentBlock);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private void spawnSulfurCloud(Level level, BlockPos pos, RandomSource random, int tickCount) {
        if (random.nextInt(100) >= CLOUD_SPAWN_CHANCE) return;

        Vec3 center = Vec3.atCenterOf(pos);
        float intensity = random.nextFloat() * 0.5f + 0.5f;

        // Use the new optimized method with tickCount
        SulfurGasCloud.spawnEnhancedGasCloud(level, center, CLOUD_RADIUS, intensity, random, tickCount);

        if (random.nextFloat() < 0.2) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double y = pos.getY() + 0.5 + random.nextDouble();
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;

            if (level instanceof ServerLevel serverLevel) {
                List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                        new AABB(pos.getX() - 256, pos.getY() - 256, pos.getZ() - 256,
                                pos.getX() + 256, pos.getY() + 256, pos.getZ() + 256));

                for (ServerPlayer player : nearbyPlayers) {
                    serverLevel.sendParticles(player, ParticleTypes.SMOKE,
                            true, x, y, z, 1, 0, 0.05, 0, 0.1);
                }
            }
        }
    }

    private void spawnSulfurDust(Level level, BlockPos pos, RandomSource random, int tickCount) {
        if (random.nextInt(100) >= DUST_SPAWN_CHANCE) return;

        if (level instanceof ServerLevel serverLevel) {
            Vec3 center = Vec3.atCenterOf(pos);
            int particlesToSpawn = random.nextInt(MAX_DUST_PARTICLES_PER_TICK) + 5;

            // Use the new optimized method with tickCount
            SulfurGasCloud.spawnDustParticlesForced(serverLevel, center, CLOUD_RADIUS * 1.2, particlesToSpawn, random, tickCount);
        }
    }

    private void applyEffectsToEntities(ServerLevel world, BlockPos pos) {
        SulfurGasCloud.applyGasEffects(world, pos, EFFECT_RADIUS, 400, 1);
    }

    private void scheduleParticleSpawn(ServerLevel world, BlockPos pos) {
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!isTopOrBaseWithoutTop(state, level, pos) || !state.getValue(ACTIVE)) {
            return;
        }

        if (!shouldShowParticles()) {
            return;
        }

        playAmbientSound(level, pos, random);
        for (int i = 0; i < random.nextInt(2) + 2; ++i) {
            double offsetX = random.nextDouble() * 0.05 - 0.025;
            double offsetY = 0.05 + random.nextDouble() * 0.05;
            double offsetZ = random.nextDouble() * 0.05 - 0.025;
            level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
        }
        for (int i = 0; i < random.nextInt(2) + 2; ++i) {
            double offsetX = random.nextDouble() * 0.2 - 0.1;
            double offsetY = 0.05 + random.nextDouble() * 0.05;
            double offsetZ = random.nextDouble() * 0.2 - 0.1;
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
        }
        for (int i = 0; i < random.nextInt(2) + 1; ++i) {
            double offsetX = random.nextDouble() * 0.05 - 0.025;
            double offsetY = 0.2 + random.nextDouble() * 0.2;
            double offsetZ = random.nextDouble() * 0.05 - 0.025;
            level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0,
                    pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
        }
    }

    @Override
    protected boolean isActive(LevelAccessor level, BlockPos pos) {
        if (this.config == null) {
            reloadConfig();
            if (this.config == null) return false;
        }

        BlockPos basePos = getBasePos(level, pos);
        BlockState belowState = level.getBlockState(basePos.below());

        ResourceLocation configBaseBlock = this.config.getActivation().getBaseBlock();
        net.minecraft.world.level.block.Block baseBlock = ForgeRegistries.BLOCKS.getValue(configBaseBlock);
        if (baseBlock == null || !belowState.is(baseBlock)) {
            return false;
        }

        int activeVentCount = countActiveVentsNearby(level, basePos);
        return activeVentCount < MAX_ACTIVE_VENTS;
    }

    private int countActiveVentsNearby(LevelAccessor level, BlockPos pos) {
        int activeCount = 0;

        for (BlockPos checkPos : BlockPos.betweenClosed(
                pos.offset(-CHECK_RADIUS, -CHECK_RADIUS, -CHECK_RADIUS),
                pos.offset(CHECK_RADIUS, CHECK_RADIUS, CHECK_RADIUS))) {
            if (checkPos.equals(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(checkPos);
            if (state.getBlock() instanceof SulfurVentBlock &&
                    state.getValue(ACTIVE) &&
                    state.getValue(VENT_TYPE) == VentType.BASE) {
                activeCount++;
            }
            if (activeCount >= MAX_ACTIVE_VENTS) {
                return activeCount;
            }
        }

        return activeCount;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        BlockPos basePos = getBasePos(level, pos);
        int activeVentCount = countActiveVentsNearby(level, basePos);

        if (activeVentCount >= MAX_ACTIVE_VENTS && state.getValue(VENT_TYPE) == VentType.BASE) {
            Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.sulfur_vent.too_many_active")
                        .withStyle(ChatFormatting.RED), true);
            }
        }
    }
}