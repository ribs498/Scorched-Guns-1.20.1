package top.ribs.scguns.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.blockentity.EnemyTurretBlockEntity;
import top.ribs.scguns.config.MerchantTradeConfig;
import top.ribs.scguns.init.ModBlocks;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class TheMerchantEntity extends PathfinderMob implements Merchant {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(TheMerchantEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DESPAWN_TIMER =
            SynchedEntityData.defineId(TheMerchantEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DAMAGE_COUNT =
            SynchedEntityData.defineId(TheMerchantEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> WALKING_TO_SUMMONER =
            SynchedEntityData.defineId(TheMerchantEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int DEFAULT_DESPAWN_TIME = 6000;
    private static final int MAX_DAMAGE_BEFORE_VANISH = 3;
    private static final Logger LOGGER = LogManager.getLogger(TheMerchantEntity.class);
    private int teleportCooldown = 0;
    private static final int TELEPORT_COOLDOWN_TICKS = 100; // 5 seconds
    private static final double TELEPORT_RANGE = 8.0;
    private long tradesSeed = 0L;
    private boolean tradesInitialized = false;
    private UUID summonerUUID;
    @Nullable
    private Player tradingPlayer;
    private MerchantOffers offers = new MerchantOffers();

    public TheMerchantEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setDespawnTimer(DEFAULT_DESPAWN_TIME);
        this.setDamageCount(0);
        this.initializeTrades();
    }

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public int attackAnimationTimeout = 0;
    private int idleAnimationTimeout = 0;

    private void initializeTrades() {
        if (!tradesInitialized) {
            if (tradesSeed == 0L) {
                tradesSeed = this.random.nextLong();
            }

            Random tradeRandom = new Random(tradesSeed);
            this.offers = MerchantTradeConfig.createRandomizedOffers(tradeRandom);
            tradesInitialized = true;
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!this.level().isClientSide() && this.isAlive()) {
            if (canTradeWith(player)) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    private boolean canTradeWith(Player player) {
        return true;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Override
    @Nullable
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public @NotNull MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(@NotNull MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SPIDER_AMBIENT, SoundSource.NEUTRAL, 1.2F, 0.6F);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                double angle = (i * Math.PI * 2) / 8;
                double x = this.getX() + Math.cos(angle) * 1.5;
                double z = this.getZ() + Math.sin(angle) * 1.5;

                serverLevel.sendParticles(ParticleTypes.WITCH,
                        x, this.getY() + 1.5, z,
                        1, 0.1, 0.1, 0.1, 0.02);
            }

            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    3, 0.3, 0.3, 0.3, 0.01);
        }
    }

    @Override
    public void notifyTradeUpdated(@NotNull ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public @NotNull SoundEvent getNotifyTradeSound() {
        return SoundEvents.SPIDER_STEP;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 120D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.5f)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0f)
                .add(Attributes.ATTACK_KNOCKBACK, 0f)
                .add(Attributes.ATTACK_DAMAGE, 0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        } else {
            handleDespawnTimer();
            handleWalkingToSummoner();

            if (teleportCooldown > 0) {
                teleportCooldown--;
            }
            if (teleportCooldown <= 0 && isSuffocating()) {
                attemptEmergencyTeleport();
            }

            if (!isWalkingToSummoner()) {
                handleLookAtNearbyPlayers();
            }
        }
    }
    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if (!this.level().isClientSide()) {
            boolean isSuffocationDamage = pSource == this.damageSources().inWall() ||
                    pSource == this.damageSources().cramming();

            if (isSuffocationDamage && teleportCooldown <= 0) {
                if (attemptEmergencyTeleport()) {
                    return false;
                }
            }
            if (teleportCooldown <= 0 && this.random.nextFloat() < 0.7f) {
                attemptEmergencyTeleport();
            }

            int damageCount = getDamageCount() + 1;
            setDamageCount(damageCount);
            createDamageEffect();
            if (damageCount >= MAX_DAMAGE_BEFORE_VANISH) {
                despawnWithSpiders();
                return false;
            }
        }

        return super.hurt(pSource, pAmount);
    }
    private boolean isSuffocating() {
        BlockPos pos = this.blockPosition();
        BlockState blockState = this.level().getBlockState(pos);
        BlockState blockStateAbove = this.level().getBlockState(pos.above());

        return !blockState.isAir() || !blockStateAbove.isAir();
    }
    private boolean attemptEmergencyTeleport() {
        Vec3 currentPos = this.position();
        ServerLevel serverLevel = (ServerLevel) this.level();

        for (int attempts = 0; attempts < 16; attempts++) {
            double angle = (attempts * Math.PI * 2) / 16;
            double distance = 3.0 + this.random.nextDouble() * TELEPORT_RANGE;

            double newX = currentPos.x + Math.cos(angle) * distance;
            double newZ = currentPos.z + Math.sin(angle) * distance;

            for (int yOffset = 2; yOffset >= -3; yOffset--) {
                double newY = currentPos.y + yOffset;

                if (isValidTeleportPosition(serverLevel, newX, newY, newZ)) {
                    this.teleportTo(newX, newY, newZ);

                    createTeleportEffect(currentPos);
                    createTeleportEffect(this.position());

                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.2F);

                    teleportCooldown = TELEPORT_COOLDOWN_TICKS;

                    return true;
                }
            }
        }

        return false;
    }
    private boolean isValidTeleportPosition(ServerLevel level, double x, double y, double z) {
        BlockPos spawnPos = new BlockPos((int) x, (int) y, (int) z);
        BlockPos headPos = spawnPos.above();
        if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(headPos).isAir()) {
            return false;
        }

        boolean hasGroundSupport = false;
        for (int checkY = (int) y; checkY >= (int) y - 3; checkY--) {
            BlockPos checkPos = new BlockPos((int) x, checkY, (int) z);
            if (level.getBlockState(checkPos).isSolidRender(level, checkPos)) {
                hasGroundSupport = true;
                break;
            }
        }
        if (level.getBlockState(spawnPos).liquid() ||
                level.getBlockState(spawnPos).is(net.minecraft.tags.BlockTags.FIRE)) {
            return false;
        }

        return hasGroundSupport;
    }
    private void createTeleportEffect(Vec3 position) {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = this.random.nextDouble() * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(ParticleTypes.WITCH,
                        position.x + offsetX,
                        position.y + offsetY,
                        position.z + offsetZ,
                        1, 0.1, 0.1, 0.1, 0.05);
            }
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                double offsetY = this.random.nextDouble() * 1.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        position.x + offsetX,
                        position.y + offsetY + 0.5,
                        position.z + offsetZ,
                        1, 0.0, 0.05, 0.0, 0.02);
            }
        }
    }
    private void handleDespawnTimer() {
        int currentTimer = getDespawnTimer();
        if (currentTimer > 0) {
            setDespawnTimer(currentTimer - 1);
        } else {
            despawnPeacefully();
        }
    }

    private void despawnPeacefully() {
        createSmokeEffect();
        this.discard();
    }

    private void handleWalkingToSummoner() {
        if (isWalkingToSummoner()) {
            Player summoner = getSummoner();
            if (summoner != null) {
                double distance = this.distanceTo(summoner);

                if (distance <= 3.0) {
                    setWalkingToSummoner(false);
                    this.getNavigation().stop();
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.SPIDER_AMBIENT, SoundSource.NEUTRAL, 1.5F, 0.5F);
                } else {
                    this.getNavigation().moveTo(summoner, 1.0);
                }
            } else {
                setWalkingToSummoner(false);
                this.getNavigation().stop();
            }
        }
    }

    private void handleLookAtNearbyPlayers() {
        Player target = getSummoner();

        if (target == null || target.distanceToSqr(this) > 64.0) {
            target = this.level().getNearestPlayer(this, 8.0);
        }

        if (target != null && target.distanceToSqr(this) <= 64.0) {
            Vec3 targetPos = target.getEyePosition();
            Vec3 merchantPos = this.getEyePosition();
            Vec3 lookVector = targetPos.subtract(merchantPos).normalize();
            double yaw = Math.atan2(-lookVector.x, lookVector.z) * (180.0 / Math.PI);
            double pitch = Math.asin(-lookVector.y) * (180.0 / Math.PI);

            float targetYaw = (float) yaw;
            float targetPitch = (float) Mth.clamp(pitch, -20.0, 20.0);

            float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
            float pitchDiff = targetPitch - this.getXRot();

            this.setYRot(this.getYRot() + Mth.clamp(yawDiff, -3.0F, 3.0F));
            this.setXRot(this.getXRot() + Mth.clamp(pitchDiff, -2.0F, 2.0F));

            this.yHeadRot = this.getYRot();
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }

        if (this.isAttacking()) {
            if (attackAnimationTimeout <= 0) {
                attackAnimationTimeout = 12;
                attackAnimationState.start(this.tickCount);
            }
            --attackAnimationTimeout;
        } else {
            attackAnimationState.stop();
        }
    }

    private void createDamageEffect() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 15; i++) {
                double offsetX = (this.random.nextDouble() - 0.5);
                double offsetY = this.random.nextDouble();
                double offsetZ = (this.random.nextDouble() - 0.5);

                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.05, 0.0, 0.01);
            }

            for (int i = 0; i < 8; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                double offsetY = this.random.nextDouble() * 1.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;

                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.05, 0.0, 0.01);
            }

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SPIDER_HURT, SoundSource.NEUTRAL, 1.5F, 0.4F);
        }
    }

    private void despawnWithSpiders() {
        spawnRevengeSpiders();
        spawnRevengeTurrets();
        createDespawnEffect();

        this.discard();
    }
    private void spawnRevengeTurrets() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            int turretCount = 3;

            for (int i = 0; i < turretCount; i++) {
                double angle = (i * Math.PI * 2) / turretCount;
                double distance = 3.0 + this.random.nextDouble() * 2.0;

                double x = this.getX() + Math.cos(angle) * distance;
                double z = this.getZ() + Math.sin(angle) * distance;

                BlockPos spawnPos = findSuitableGroundPosition(serverLevel, x, this.getY(), z);

                if (spawnPos != null) {
                    BlockState turretState = ModBlocks.ENEMY_TURRET.get().defaultBlockState();
                    serverLevel.setBlock(spawnPos, turretState, 3);

                    if (serverLevel.getBlockEntity(spawnPos) instanceof EnemyTurretBlockEntity turretEntity) {

                        turretEntity.setDamageMultiplier(1.25f);
                        turretEntity.setFireRateMultiplier(0.5f);
                    }

                    createTurretSpawnEffect(serverLevel, spawnPos);
                }
            }

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 2.0F, 0.5F);
        }
    }


    private BlockPos findSuitableGroundPosition(ServerLevel level, double x, double y, double z) {
        BlockPos basePos = new BlockPos((int)x, (int)y, (int)z);

        for (int yOffset = -2; yOffset <= 4; yOffset++) {
            BlockPos testPos = basePos.offset(0, yOffset, 0);
            BlockPos groundPos = testPos.below();

            if (level.getBlockState(groundPos).isSolidRender(level, groundPos) &&
                    level.getBlockState(testPos).isAir() &&
                    level.getBlockState(testPos.above()).isAir()) {

                return testPos;
            }
        }
        return null;
    }

    private void createTurretSpawnEffect(ServerLevel serverLevel, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < 15; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
            double offsetY = this.random.nextDouble() * 2.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;

            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, 0.0, 0.1, 0.0, 0.05);
        }

        for (int i = 0; i < 8; i++) {
            double offsetX = (this.random.nextDouble() - 0.5);
            double offsetY = this.random.nextDouble();
            double offsetZ = (this.random.nextDouble() - 0.5);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, 0.0, 0.05, 0.0, 0.02);
        }
    }
    private void spawnRevengeSpiders() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            int spiderCount = 4 + this.random.nextInt(4);

            for (int i = 0; i < spiderCount; i++) {
                double angle = (i * Math.PI * 2) / spiderCount;
                double distance = 2.0 + this.random.nextDouble() * 2.5;

                double x = this.getX() + Math.cos(angle) * distance;
                double z = this.getZ() + Math.sin(angle) * distance;
                double y = this.getY();

                BlockPos spawnPos = new BlockPos((int)x, (int)y, (int)z);
                for (int yOffset = -1; yOffset <= 2; yOffset++) {
                    BlockPos testPos = spawnPos.offset(0, yOffset, 0);
                    if (serverLevel.getBlockState(testPos).isAir() &&
                            serverLevel.getBlockState(testPos.below()).isSolidRender(serverLevel, testPos.below())) {

                        net.minecraft.world.entity.monster.CaveSpider spider =
                                new net.minecraft.world.entity.monster.CaveSpider(
                                        net.minecraft.world.entity.EntityType.CAVE_SPIDER, serverLevel);

                        spider.setPos(testPos.getX() + 0.5, testPos.getY(), testPos.getZ() + 0.5);

                        spider.setTarget(this.level().getNearestPlayer(spider, 16.0));

                        if (serverLevel.addFreshEntity(spider)) {
                            serverLevel.sendParticles(ParticleTypes.SMOKE,
                                    spider.getX(), spider.getY() + 0.5, spider.getZ(),
                                    5, 0.2, 0.2, 0.2, 0.02);
                        }
                        break;
                    }
                }
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.3F);
        }
    }

    private void createDespawnEffect() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 3.0;
                double offsetY = this.random.nextDouble() * 3.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 3.0;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.15, 0.0, 0.04);
            }

            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.5;
                double offsetY = this.random.nextDouble() * 2.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.5;

                serverLevel.sendParticles(ParticleTypes.WITCH,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.1, 0.0, 0.03);
            }

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SPIDER_DEATH, SoundSource.NEUTRAL, 1.8F, 0.3F);
        }
    }

    private void createSmokeEffect() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = this.random.nextDouble() * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.1, 0.0, 0.02);
            }

            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                double offsetY = this.random.nextDouble() * 1.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;

                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.05, 0.0, 0.01);
            }
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                double offsetY = this.random.nextDouble() * 1.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;

                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.05, 0.0, 0.01);
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.2F);
        }
    }

    public void createSpawnEffect() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 40; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.5;
                double offsetY = this.random.nextDouble() * 2.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.5;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.15, 0.0, 0.03);
            }

            for (int i = 0; i < 15; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = this.random.nextDouble() * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(ParticleTypes.WITCH,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 1.0,
                        this.getZ() + offsetZ,
                        1, 0.0, 0.1, 0.0, 0.02);
            }

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 0.8F);
        }
    }

    public void setSummoner(Player player) {
        this.summonerUUID = player.getUUID();
    }

    @Nullable
    public Player getSummoner() {
        if (summonerUUID != null && !this.level().isClientSide()) {
            return Objects.requireNonNull(this.level().getServer()).getPlayerList().getPlayer(summonerUUID);
        }
        return null;
    }

    public boolean isWalkingToSummoner() {
        return this.entityData.get(WALKING_TO_SUMMONER);
    }

    public void setWalkingToSummoner(boolean walking) {
        this.entityData.set(WALKING_TO_SUMMONER, walking);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setDespawnTimer(int timer) {
        this.entityData.set(DESPAWN_TIMER, timer);
    }

    public int getDespawnTimer() {
        return this.entityData.get(DESPAWN_TIMER);
    }

    public void setDamageCount(int count) {
        this.entityData.set(DAMAGE_COUNT, count);
    }

    public int getDamageCount() {
        return this.entityData.get(DAMAGE_COUNT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(DESPAWN_TIMER, DEFAULT_DESPAWN_TIME);
        this.entityData.define(DAMAGE_COUNT, 0);
        this.entityData.define(WALKING_TO_SUMMONER, false);
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if (this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }
        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public float getVoicePitch() {
        return 0.4F;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putLong("TradesSeed", this.tradesSeed);
        compound.putBoolean("TradesInitialized", this.tradesInitialized);

        ListTag offersTag = new ListTag();
        for (MerchantOffer offer : this.offers) {
            CompoundTag offerTag = offer.createTag();
            offersTag.add(offerTag);
        }
        compound.put("Offers", offersTag);

        if (this.summonerUUID != null) {
            compound.putUUID("SummonerUUID", this.summonerUUID);
        }
    }
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("TradesSeed")) {
            this.tradesSeed = compound.getLong("TradesSeed");
        }
        if (compound.contains("TradesInitialized")) {
            this.tradesInitialized = compound.getBoolean("TradesInitialized");
        }
        if (compound.contains("SummonerUUID")) {
            this.summonerUUID = compound.getUUID("SummonerUUID");
        }

        this.offers = new MerchantOffers();
        if (compound.contains("Offers", Tag.TAG_LIST)) {
            ListTag offersTag = compound.getList("Offers", Tag.TAG_COMPOUND);
            for (int i = 0; i < offersTag.size(); i++) {
                try {
                    CompoundTag offerTag = offersTag.getCompound(i);
                    MerchantOffer offer = new MerchantOffer(offerTag);
                    if (!offer.getBaseCostA().isEmpty() && !offer.getResult().isEmpty()) {
                        this.offers.add(offer);
                    } else {
                        LOGGER.warn("Skipping invalid offer at index {}: empty buy or sell item", i);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load merchant offer at index {}: {}", i, e.getMessage());
                }
            }
        }
        if (this.offers.isEmpty() && !this.tradesInitialized) {
            this.initializeTrades();
        }
    }
}