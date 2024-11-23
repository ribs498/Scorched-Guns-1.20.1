package top.ribs.scguns.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;
import java.util.List;
import java.util.Random;

public class SulfurVentBlock extends Block {
    public static final EnumProperty<SulfurVentType> VENT_TYPE = EnumProperty.create("vent_type", SulfurVentType.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_MIDDLE_TOP = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final int CLOUD_RADIUS = 8;
    private static final int CLOUD_HEIGHT = 4;
    private static final int MAX_CLOUD_PARTICLES_PER_TICK = 15;
    private static final int MAX_DUST_PARTICLES_PER_TICK = 10;
    private static final int CLOUD_SPAWN_CHANCE = 95;
    private static final int DUST_SPAWN_CHANCE = 98;
    private static final int BASE_TICK_INTERVAL = 20;
    private static final int TICK_WIGGLE_ROOM = 10;
    private static final int EFFECT_INTERVAL = 10;
    public static final int EFFECT_RADIUS = 8;
    public static final int MAX_ACTIVE_VENTS = 1;
    public static final int CHECK_RADIUS = 32;
    public static final int EFFECT_RADIUS_SQUARED = EFFECT_RADIUS * EFFECT_RADIUS;
    private static final int HELMET_DAMAGE_INTERVAL = 50;
    public static final IntegerProperty VENT_POWER = IntegerProperty.create("vent_power", 1, 5);
    public static final int MAX_VENT_POWER = 5;
    private final Random random = new Random();

    public SulfurVentBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VENT_TYPE, SulfurVentType.BASE)
                .setValue(ACTIVE, false)
                .setValue(VENT_POWER, 1));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VENT_TYPE, ACTIVE, VENT_POWER);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean isActive = isActive(level, pos);
        int ventPower = calculateVentPower(level, pos);
        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                .setValue(ACTIVE, isActive)
                .setValue(VENT_POWER, ventPower);
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (level instanceof Level) {
            boolean isActive = isActive(level, pos);
            int ventPower = calculateVentPower((Level) level, pos);
            return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()))
                    .setValue(ACTIVE, isActive)
                    .setValue(VENT_POWER, ventPower);
        }
        return this.updateState(level.getBlockState(pos.below()), level.getBlockState(pos.above()));
    }
    private int calculateVentPower(LevelAccessor level, BlockPos pos) {
        BlockPos basePos = getBasePos(level, pos);
        int power = 1;
        BlockPos checkPos = basePos.above();

        while (level.getBlockState(checkPos).getBlock() instanceof SulfurVentBlock && power < MAX_VENT_POWER) {
            power++;
            checkPos = checkPos.above();
        }

        return power;
    }

    private BlockState updateState(BlockState belowState, BlockState aboveState) {
        if (belowState.getBlock() instanceof SulfurVentBlock) {
            if (aboveState.getBlock() instanceof SulfurVentBlock) {
                return this.defaultBlockState().setValue(VENT_TYPE, SulfurVentType.MIDDLE);
            } else {
                return this.defaultBlockState().setValue(VENT_TYPE, SulfurVentType.TOP);
            }
        } else {
            return this.defaultBlockState().setValue(VENT_TYPE, SulfurVentType.BASE);
        }
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(VENT_TYPE) == SulfurVentType.BASE ? SHAPE_BASE : SHAPE_MIDDLE_TOP;
    }



    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (block instanceof PistonBaseBlock) {
            level.destroyBlock(pos, true);
        }
        if (isFireSource(block.defaultBlockState()) && isInCloudArea(level, fromPos, pos)) {
            triggerExplosion(level, pos);
            shutdownVentTemporarily(level, pos, state);
            return;
        }

        boolean isActive = isActive(level, pos);
        level.setBlock(pos, state.setValue(ACTIVE, isActive), 3);
    }
    private boolean isInCloudArea(Level level, BlockPos firePos, BlockPos ventPos) {
        double distanceSquared = firePos.distSqr(ventPos);
        return distanceSquared <= EFFECT_RADIUS_SQUARED;
    }

    private void triggerExplosion(Level level, BlockPos pos) {
        RandomSource random = level.random;
        for (int i = 0; i < 6; i++) {
            double xOffset = (random.nextDouble() - 0.5) * 2.0 * EFFECT_RADIUS;
            double yOffset = (random.nextDouble() - 0.5) * 2.0 * EFFECT_RADIUS;
            double zOffset = (random.nextDouble() - 0.5) * 2.0 * EFFECT_RADIUS;
            BlockPos explosionPos = pos.offset((int) xOffset, (int) yOffset, (int) zOffset);

            level.explode(null, explosionPos.getX(), explosionPos.getY(), explosionPos.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
        }
    }

    private void extinguishFire(Level world, BlockPos ventPos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(ventPos.offset(-EFFECT_RADIUS, -1, -EFFECT_RADIUS), ventPos.offset(EFFECT_RADIUS, 1, EFFECT_RADIUS))) {
            BlockState blockState = world.getBlockState(checkPos);
            if (blockState.is(Blocks.FIRE) || blockState.is(Blocks.SOUL_FIRE)) {
                world.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);
            } else if (blockState.is(Blocks.CAMPFIRE) || blockState.is(Blocks.SOUL_CAMPFIRE)) {
                world.setBlock(checkPos, blockState.setValue(BlockStateProperties.LIT, false), 3);
            }
        }
    }
    private void shutdownVentTemporarily(Level level, BlockPos pos, BlockState state) {

        level.setBlock(pos, state.setValue(ACTIVE, false), 3);
        level.sendBlockUpdated(pos, state, state, 2);
        level.scheduleTick(pos, this, 100);
    }
    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        boolean isTop = state.getValue(VENT_TYPE) == SulfurVentType.TOP;
        boolean isBaseWithoutTop = state.getValue(VENT_TYPE) == SulfurVentType.BASE && !isVentAbove(level, pos);
        if (state.getValue(ACTIVE) && state.getValue(VENT_TYPE) == SulfurVentType.BASE) {
            spawnSulfurCloud(level, pos, random);
            spawnSulfurDust(level, pos, random);
        }

        if ((isTop || isBaseWithoutTop) && state.getValue(ACTIVE)) {
            if (random.nextInt(20) == 0) {
                level.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                double offsetX = random.nextDouble() * 0.05 - 0.025;
                double offsetY = 0.05 + random.nextDouble() * 0.05;
                double offsetZ = random.nextDouble() * 0.05 - 0.025;
                level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
            }

            for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                double offsetX = random.nextDouble() * 0.2 - 0.1;
                double offsetY = 0.05 + random.nextDouble() * 0.05;
                double offsetZ = random.nextDouble() * 0.2 - 0.1;
                level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
            }
            for (int i = 0; i < random.nextInt(2) + 1; ++i) {
                double offsetX = random.nextDouble() * 0.05 - 0.025;
                double offsetY = 0.2 + random.nextDouble() * 0.2;
                double offsetZ = random.nextDouble() * 0.05 - 0.025;
                level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, offsetX, offsetY, offsetZ);
            }
        }
    }
    private void spawnSulfurCloud(Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) >= CLOUD_SPAWN_CHANCE) return;

        int particlesToSpawn = random.nextInt(MAX_CLOUD_PARTICLES_PER_TICK) + 20;

        for (int i = 0; i < particlesToSpawn; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = Math.sqrt(random.nextDouble()) * (CLOUD_RADIUS);
            double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;
            double y = pos.getY() + random.nextDouble() * CLOUD_HEIGHT;
            double speed = 0.002 + random.nextDouble() * 0.005;
            double xSpeed = (random.nextDouble() - 0.5) * speed;
            double ySpeed = random.nextDouble() * speed * 0.5;
            double zSpeed = (random.nextDouble() - 0.5) * speed;
            level.addParticle(ModParticleTypes.SULFUR_SMOKE.get(), true, x, y, z, xSpeed, ySpeed, zSpeed);
        }
        if (random.nextFloat() < 0.2) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double y = pos.getY() + 0.5 + random.nextDouble();
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.SMALL_FLAME, true, x, y, z, 0, 0.05, 0);
        }
    }

    private void spawnSulfurDust(Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) >= DUST_SPAWN_CHANCE) return;

        int particlesToSpawn = random.nextInt(MAX_DUST_PARTICLES_PER_TICK) + 5;

        for (int i = 0; i < particlesToSpawn; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = Math.sqrt(random.nextDouble()) * (CLOUD_RADIUS * 1.2);
            double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;
            double y = pos.getY() + 0.1 + random.nextDouble() * 0.3;

            double speed = 0.001 + random.nextDouble() * 0.002;
            double xSpeed = (random.nextDouble() - 0.5) * speed;
            double ySpeed = random.nextDouble() * speed * 0.5;
            double zSpeed = (random.nextDouble() - 0.5) * speed;

            level.addParticle(ModParticleTypes.SULFUR_DUST.get(), x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!world.isClientSide) {
            if (state.getValue(ACTIVE)) {
                if (isFireInCloudArea(world, pos)) {
                    triggerExplosion(world, pos);
                    extinguishFire(world, pos);
                    shutdownVentTemporarily(world, pos, state);
                    return;
                }

                applyEffectsToEntities(world, pos);
                world.sendParticles(ModParticleTypes.SULFUR_SMOKE.get(),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1, 0.5, 0.5, 0.5, 0.01);
            } else {
                return;
            }

            world.scheduleTick(pos, this, EFFECT_INTERVAL);
            if (world.getGameTime() % (BASE_TICK_INTERVAL + random.nextInt(TICK_WIGGLE_ROOM)) == 0) {
                scheduleParticleSpawn(world, pos);
            }
        }
    }
    private boolean isFireInCloudArea(Level world, BlockPos ventPos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(ventPos.offset(-EFFECT_RADIUS, -1, -EFFECT_RADIUS), ventPos.offset(EFFECT_RADIUS, 1, EFFECT_RADIUS))) {
            BlockState blockState = world.getBlockState(checkPos);
            if (isFireSource(blockState)) {
                return true;
            }
        }
        return false;
    }
    private boolean isFireSource(BlockState blockState) {
        return blockState.is(Blocks.FIRE) ||
                blockState.is(Blocks.SOUL_FIRE) ||
                (blockState.is(Blocks.CAMPFIRE) && blockState.getValue(BlockStateProperties.LIT)) ||
                (blockState.is(Blocks.SOUL_CAMPFIRE) && blockState.getValue(BlockStateProperties.LIT));
    }

    private void applyEffectsToEntities(ServerLevel world, BlockPos pos) {
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos).inflate(EFFECT_INTERVAL),
                entity -> entity.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= EFFECT_RADIUS_SQUARED);

        for (LivingEntity entity : entities) {
            if (entity instanceof Player player) {
                if (player.isCreative() || player.isSpectator()) {
                    continue;
                }
            }
            ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.is(ModTags.Items.GAS_MASK)) {
                int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, helmet);
                if (entity.getPersistentData().getLong("LastHelmetDamageTick") + HELMET_DAMAGE_INTERVAL <= entity.tickCount) {
                    entity.getPersistentData().putLong("LastHelmetDamageTick", entity.tickCount);

                    if (shouldDamageItem(unbreakingLevel, entity.getRandom())) {
                        helmet.hurtAndBreak(1, entity, (e) -> e.broadcastBreakEvent(EquipmentSlot.HEAD));
                    }
                }

                continue;
            }
            // Apply effects if the entity is not wearing a gas mask
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 3));
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            entity.hurt(entity.damageSources().magic(), 2.0F);
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }
    }


    private boolean shouldDamageItem(int unbreakingLevel, RandomSource random) {
        if (unbreakingLevel > 0) {
            int chance = 1 + unbreakingLevel;
            return random.nextInt(chance) == 0;
        }
        return true;
    }
    private void scheduleParticleSpawn(ServerLevel world, BlockPos pos) {
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
    }
    private int calculateNextTickInterval() {
        return BASE_TICK_INTERVAL + random.nextInt(TICK_WIGGLE_ROOM);
    }
    private boolean isVentAbove(Level level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return level.getBlockState(abovePos).getBlock() instanceof SulfurVentBlock;
    }

    private boolean isActive(LevelAccessor level, BlockPos pos) {
        BlockPos basePos = getBasePos(level, pos);
        BlockState belowState = level.getBlockState(basePos.below());
        if (!belowState.is(Blocks.MAGMA_BLOCK)) {
            return false;
        }
        int activeVentCount = countActiveVentsNearby(level, basePos);
        return activeVentCount < MAX_ACTIVE_VENTS;
    }

    private int countActiveVentsNearby(LevelAccessor level, BlockPos pos) {
        int activeCount = 0;

        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-CHECK_RADIUS, -CHECK_RADIUS, -CHECK_RADIUS), pos.offset(CHECK_RADIUS, CHECK_RADIUS, CHECK_RADIUS))) {
            if (checkPos.equals(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(checkPos);
            if (state.getBlock() instanceof SulfurVentBlock && state.getValue(ACTIVE) && state.getValue(VENT_TYPE) == SulfurVentType.BASE) {
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
        BlockPos basePos = getBasePos(level, pos);
        boolean isActive = isActive(level, basePos);
        int activeVentCount = countActiveVentsNearby(level, basePos);

        // Only update vent power if needed
        updateVentPower(level, pos);

        // Avoid updating the state if it's already correct
        if (state.getValue(ACTIVE) != isActive) {
            level.setBlock(pos, state.setValue(ACTIVE, isActive), 3);
        }

        // Schedule tick only if necessary
        if (activeVentCount >= MAX_ACTIVE_VENTS && state.getValue(VENT_TYPE) == SulfurVentType.BASE) {
            Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 5, false);
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.sulfur_vent.too_many_active").withStyle(ChatFormatting.RED), true);
            }
        }

        level.scheduleTick(pos, this, calculateNextTickInterval());
    }

    private BlockPos getBasePos(LevelAccessor level, BlockPos pos) {
        while (level.getBlockState(pos.below()).getBlock() instanceof SulfurVentBlock) {
            if (pos.getY() <= 0) { // Ensure it doesn't go below the world
                break;
            }
            pos = pos.below();
        }
        return pos;
    }
    private void updateVentPower(Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        int currentPower = currentState.getValue(VENT_POWER);
        int newPower = calculateVentPower(level, pos);

        if (currentPower != newPower) {
            // Update only if power changes
            level.setBlock(pos, currentState.setValue(VENT_POWER, newPower), 3);
        }
    }


    public enum SulfurVentType implements StringRepresentable {
        BASE("base"),
        MIDDLE("middle"),
        TOP("top");

        private final String name;

        SulfurVentType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}