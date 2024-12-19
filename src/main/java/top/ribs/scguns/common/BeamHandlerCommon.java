package top.ribs.scguns.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.init.ModTags;

import java.util.*;

import static top.ribs.scguns.common.network.ServerPlayHandler.rayTraceEntities;

public class BeamHandlerCommon {
    public static class BeamMiningManager {
        private static final Map<UUID, Integer> playerBreakingIds = new HashMap<>();
        private static final Map<BlockPos, MiningProgress> miningProgress = new HashMap<>();
        private static int nextBreakerId = 1;
        private static final long RESET_TIMEOUT = 1000;
        private static final double GLASS_PENETRATION_DAMAGE_REDUCTION = 0.15;

        private static class MiningProgress {
            float progress;
            long lastUpdate;
            UUID minerId;
            int breakerId;
            boolean isActive;
            int lastStage;

            public MiningProgress(UUID minerId) {
                this.progress = 0f;
                this.lastUpdate = System.currentTimeMillis();
                this.minerId = minerId;
                this.breakerId = playerBreakingIds.computeIfAbsent(minerId, k -> nextBreakerId++);
                this.isActive = true;
                this.lastStage = -1;
            }
        }

        public static class ExtendedBlockHitResult extends BlockHitResult {
            private double damageMultiplier = 1.0;
            private List<BlockHitResult> glassPenetrations = new ArrayList<>();

            public ExtendedBlockHitResult(Vec3 location, Direction direction, BlockPos blockPos, boolean insideBlock) {
                super(location, direction, blockPos, insideBlock);
            }

            public static ExtendedBlockHitResult fromBlockHitResult(BlockHitResult original) {
                return new ExtendedBlockHitResult(original.getLocation(), original.getDirection(),
                        original.getBlockPos(), original.isInside());
            }

            public void setDamageMultiplier(double multiplier) {
                this.damageMultiplier = multiplier;
            }

            public double getDamageMultiplier() {
                return damageMultiplier;
            }

            public void setGlassPenetrations(List<BlockHitResult> penetrations) {
                this.glassPenetrations = penetrations;
            }

            public List<BlockHitResult> getGlassPenetrations() {
                return glassPenetrations;
            }
        }

        public static class ExtendedEntityHitResult extends EntityHitResult {
            private double damageMultiplier = 1.0;

            public ExtendedEntityHitResult(Entity entity, Vec3 location) {
                super(entity, location);
            }

            public void setDamageMultiplier(double multiplier) {
                this.damageMultiplier = multiplier;
            }

            public double getDamageMultiplier() {
                return damageMultiplier;
            }

            public static ExtendedEntityHitResult fromEntityHitResult(EntityHitResult original) {
                return new ExtendedEntityHitResult(original.getEntity(), original.getLocation());
            }
        }

        public static HitResult getBeamHitResult(Level world, Vec3 startVec, Vec3 endVec, Entity shooter, double maxDistance) {
            Vec3 currentPos = startVec;
            Vec3 direction = endVec.subtract(startVec).normalize();
            List<BlockHitResult> glassPenetrations = new ArrayList<>();
            double distanceTraveled = 0;
            double remainingDamageMultiplier = 1.0;

            while (distanceTraveled < maxDistance) {
                Vec3 nextEndVec = currentPos.add(direction.scale(maxDistance - distanceTraveled));
                BlockHitResult blockHit = world.clip(new ClipContext(currentPos, nextEndVec,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));

                if (blockHit.getType() == HitResult.Type.MISS) {
                    EntityHitResult entityHit = rayTraceEntities(world, shooter, currentPos, nextEndVec);
                    if (entityHit != null) {
                        ExtendedEntityHitResult extendedEntityHit = ExtendedEntityHitResult.fromEntityHitResult(entityHit);
                        extendedEntityHit.setDamageMultiplier(remainingDamageMultiplier);
                        return extendedEntityHit;
                    }
                    return blockHit;
                }

                BlockState hitState = world.getBlockState(blockHit.getBlockPos());
                if (isGlassBlock(hitState)) {
                    glassPenetrations.add(blockHit);
                    remainingDamageMultiplier *= (1.0 - GLASS_PENETRATION_DAMAGE_REDUCTION);
                    currentPos = blockHit.getLocation().add(direction.scale(0.01));
                    distanceTraveled = currentPos.subtract(startVec).length();
                    continue;
                }

                EntityHitResult entityHit = rayTraceEntities(world, shooter, currentPos, blockHit.getLocation());
                if (entityHit != null) {
                    ExtendedEntityHitResult extendedEntityHit = ExtendedEntityHitResult.fromEntityHitResult(entityHit);
                    extendedEntityHit.setDamageMultiplier(remainingDamageMultiplier);
                    return extendedEntityHit;
                }

                ExtendedBlockHitResult extendedBlockHit = ExtendedBlockHitResult.fromBlockHitResult(blockHit);
                extendedBlockHit.setDamageMultiplier(remainingDamageMultiplier);
                extendedBlockHit.setGlassPenetrations(glassPenetrations);
                return extendedBlockHit;
            }

            return BlockHitResult.miss(endVec, Direction.UP, new BlockPos(
                    Mth.floor(endVec.x),
                    Mth.floor(endVec.y),
                    Mth.floor(endVec.z)
            ));
        }

        private static boolean isGlassBlock(BlockState state) {
            return state.is(BlockTags.create(new ResourceLocation("forge", "glass"))) ||
                    state.is(Blocks.GLASS) ||
                    state.is(Blocks.GLASS_PANE) ||
                    state.is(Blocks.TINTED_GLASS);
        }
        public static void updateBlockMining(Level world, BlockPos pos, ServerPlayer player, Gun modifiedGun) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || isGlassBlock(state)) {
                return;
            }
            if (handleFragileBlock(world, pos, state, modifiedGun)) {
                return;
            }

            if (!Config.COMMON.gameplay.griefing.enableBeamMining.get() || !modifiedGun.getGeneral().canMine()) {
                return;
            }

            handleBeamMining(world, pos, state, player, modifiedGun);
        }

        private static boolean handleFragileBlock(Level world, BlockPos pos, BlockState state, Gun modifiedGun) {
            if (!Config.COMMON.gameplay.griefing.enableGlassBreaking.get() || !state.is(ModTags.Blocks.FRAGILE)) {
                return false;
            }

            float destroySpeed = state.getDestroySpeed(world, pos);
            if (destroySpeed < 0) {
                return false;
            }

            float baseChance = Config.COMMON.gameplay.griefing.fragileBaseBreakChance.get().floatValue();
            float beamModifier = modifiedGun.getGeneral().getFireMode() == FireMode.BEAM ? 2.0f : 1.5f;
            float chance = (baseChance * beamModifier) / (destroySpeed + 1);

            if (world.random.nextFloat() < chance) {
                world.destroyBlock(pos, Config.COMMON.gameplay.griefing.fragileBlockDrops.get());
                return true;
            }

            return false;
        }

        private static void handleBeamMining(Level world, BlockPos pos, BlockState state, ServerPlayer player, Gun modifiedGun) {
            float hardness = state.getDestroySpeed(world, pos);
            if (hardness < 0) {
                return;
            }

            MiningProgress progress = miningProgress.computeIfAbsent(pos, k -> new MiningProgress(player.getUUID()));

            if (!progress.minerId.equals(player.getUUID())) {
                return;
            }

            progress.isActive = true;
            progress.lastUpdate = System.currentTimeMillis();

            float miningSpeed = modifiedGun.getGeneral().getMiningSpeed();
            float progressIncrement = miningSpeed / (hardness * 10);
            progress.progress += progressIncrement;

            int newStage = Math.min((int) (progress.progress * 10.0F), 9);

            if (newStage != progress.lastStage) {
                progress.lastStage = newStage;
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.destroyBlockProgress(progress.breakerId, pos, newStage);
                }
            }

            if (progress.progress >= 1.0F) {
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.destroyBlockProgress(progress.breakerId, pos, -1);
                }
                miningProgress.remove(pos);

                if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                    world.removeBlock(pos, false);
                } else {
                    BlockState blockState = world.getBlockState(pos);
                    BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(pos) : null;
                    Block.dropResources(blockState, world, pos, blockEntity, player, player.getMainHandItem());
                    world.removeBlock(pos, false);
                    world.levelEvent(2001, pos, Block.getId(blockState));
                }
            }
        }
        public static void tickMiningProgress(Level world) {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<BlockPos, MiningProgress>> iterator = miningProgress.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockPos, MiningProgress> entry = iterator.next();
                MiningProgress progress = entry.getValue();
                if (!progress.isActive && (currentTime - progress.lastUpdate) >= RESET_TIMEOUT) {
                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.destroyBlockProgress(progress.breakerId, entry.getKey(), -1);
                    }
                    iterator.remove();
                } else if (progress.isActive || (currentTime - progress.lastUpdate) < 50) {
                    progress.isActive = false;
                }
            }
        }
    }
}