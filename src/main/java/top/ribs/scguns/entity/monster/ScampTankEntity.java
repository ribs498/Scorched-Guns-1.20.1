package top.ribs.scguns.entity.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.entity.projectile.BrassBoltEntity;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModSounds;
import top.ribs.scguns.init.ModTags;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class ScampTankEntity extends Monster implements RangedAttackMob {

    private static final EntityDataAccessor<Integer> MAIN_TURRET_FLASH_TIMER = SynchedEntityData.defineId(ScampTankEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MACHINE_GUN_FLASH_TIMER = SynchedEntityData.defineId(ScampTankEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_CHARGING = SynchedEntityData.defineId(ScampTankEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_IN_SECOND_PHASE = SynchedEntityData.defineId(ScampTankEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_IN_THIRD_PHASE = SynchedEntityData.defineId(ScampTankEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean hasTriggeredWeaponDestruction = false;

    private int mainCannonCooldown = 0;
    private int machineGunCooldown = 0;

    private static final int MAIN_CANNON_COOLDOWN_TICKS = 40;
    private static final int MACHINE_GUN_COOLDOWN_TICKS = 5;

    private static final double MAIN_CANNON_RANGE = 35.0;
    private static final double MACHINE_GUN_RANGE = 12.0;

    private static final double PREFERRED_COMBAT_RANGE = 12.0;
    private static final double MIN_COMBAT_RANGE = 4.0;
    private static final double DETECTION_RANGE = 50.0;

    private int beaconSpawnCooldown = 0;
    private static final int BEACON_SPAWN_COOLDOWN = 60;
    private static final int MAX_SKY_CARRIERS_IN_AREA = 4;
    private static final double SKY_CARRIER_CHECK_RADIUS = 40.0;

    private Vec3 chargeDirection = Vec3.ZERO;
    private int chargeCooldown = 0;
    private int chargeWarmupTicks = 0;
    private int chargeActiveTicks = 0;
    private static final int CHARGE_WARMUP_DURATION = 20;
    private static final int CHARGE_DURATION = 35;
    private static final int CHARGE_COOLDOWN_DURATION = 60;
    private static final double CHARGE_SPEED = 1.5;
    private static final double CHARGE_DAMAGE = 8.0;
    private static final double CHARGE_RANGE = 45.0;
    private int postChargeRotationTicks = 0;
    private static final int POST_CHARGE_ROTATION_DURATION = 30;

    private boolean isRegenerating = false;
    private int regenerationTicks = 0;
    private static final int REGENERATION_DURATION = 60;
    private static final float REGENERATION_TARGET_HEALTH = 700.0F;
    private static final float REGENERATION_RATE = 2.0F;

   private boolean hasTriggeredThirdPhase = false;
    private boolean isRegeneratingThirdPhase = false;
    private int regenerationTicksThirdPhase = 0;
    private static final int THIRD_PHASE_REGENERATION_DURATION = 60;
    private static final float THIRD_PHASE_REGENERATION_TARGET_HEALTH = 350.0F;
    private static final float THIRD_PHASE_REGENERATION_RATE = 2.5F;

    private int scamplerSpawnCooldown = 0;
    private static final int SCAMPLER_SPAWN_COOLDOWN = 30;
    private static final int MAX_SCAMPLERS_IN_AREA = 10;
    private static final double SCAMPLER_CHECK_RADIUS = 30.0;

    private int thirdPhaseBeaconCooldown = 0;
    private static final int THIRD_PHASE_BEACON_COOLDOWN = 60;
    private static final float THIRD_PHASE_BEACON_CHANCE = 0.65f;
    private int repositionCooldown = 0;

    private int terrainDestructionCooldown = 0;
    private static final int TERRAIN_DESTRUCTION_COOLDOWN_TICKS = 10;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private int avoidanceTimer = 0;
    private int noLineOfSightTimer = 0;
    private static final int NO_LOS_THRESHOLD = 60;
    private boolean isAggressivelyRepositioning = false;
    private Vec3 lastKnownTargetPosition = null;
    private int frustratedShotAttempts = 0;

    public ScampTankEntity(EntityType<? extends ScampTankEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setMaxUpStep(2.0F);
        this.bossEvent.setVisible(true);
        this.xpReward = XP_REWARD_BOSS;
        this.setPersistenceRequired();
    }
    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
    public boolean isInSecondPhase() {
        return this.entityData.get(IS_IN_SECOND_PHASE);
    }

    public void setInSecondPhase(boolean inSecondPhase) {
        this.entityData.set(IS_IN_SECOND_PHASE, inSecondPhase);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000D)
                .add(Attributes.FOLLOW_RANGE, 35D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.8f)
                .add(Attributes.ARMOR, 12f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 3f);
    }
    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!this.level().isClientSide) {
            this.bossEvent.removeAllPlayers();
        }
    }
    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance pPotionEffect) {
        MobEffect effect = pPotionEffect.getEffect();

        if (effect == MobEffects.POISON ||
                effect == MobEffects.WITHER ||
                effect == MobEffects.HUNGER ||
                effect == MobEffects.REGENERATION ||
                effect == MobEffects.SATURATION ||
                effect == MobEffects.CONFUSION ||
                effect == MobEffects.BLINDNESS ||
                effect == MobEffects.WEAKNESS ||
                effect == MobEffects.MOVEMENT_SLOWDOWN ||
                effect == MobEffects.DIG_SLOWDOWN ||
                effect == MobEffects.HARM ||
                effect == ModEffects.SULFUR_POISONING.get() ||
                effect == MobEffects.HEAL) {
            return false;
        }

        return super.canBeAffected(pPotionEffect);
    }
    @Override
    public void die(@NotNull DamageSource pCause) {
        super.die(pCause);

        if (!this.level().isClientSide) {
            SupplyScampEntity supplyScamp = new SupplyScampEntity(ModEntities.SUPPLY_SCAMP.get(), this.level());
            supplyScamp.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            supplyScamp.setPersistenceRequired();
            this.level().addFreshEntity(supplyScamp);

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1.0F, 1.2F);

            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 20; i++) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            supplyScamp.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                            supplyScamp.getY() + 1.0 + this.random.nextDouble(),
                            supplyScamp.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                            1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }
    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(name != null ? name : this.getDisplayName());
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MAIN_TURRET_FLASH_TIMER, 0);
        this.entityData.define(MACHINE_GUN_FLASH_TIMER, 0);
        this.entityData.define(IS_CHARGING, false);
        this.entityData.define(IS_IN_SECOND_PHASE, false);
        this.entityData.define(IS_IN_THIRD_PHASE, false);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TankChargeGoal());
        this.goalSelector.addGoal(2, new TankChaseGoal());
        this.goalSelector.addGoal(3, new TankLookGoal());

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new ExtendedRangeTargetGoal());
    }
    public boolean isInThirdPhase() {
        return this.entityData.get(IS_IN_THIRD_PHASE);
    }

    public void setInThirdPhase(boolean inThirdPhase) {
        this.entityData.set(IS_IN_THIRD_PHASE, inThirdPhase);
    }

    private void triggerWeaponDestruction() {
        if (!this.level().isClientSide && !hasTriggeredWeaponDestruction) {
            hasTriggeredWeaponDestruction = true;
            setInSecondPhase(true);

            this.setHealth(REGENERATION_TARGET_HEALTH);

            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, REGENERATION_DURATION, 1));

            this.isRegenerating = true;
            this.regenerationTicks = REGENERATION_DURATION;

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0F, 0.8F);

            if (this.level() instanceof ServerLevel serverLevel) {
                double turretX = this.getX();
                double turretY = this.getY() + this.getBbHeight() * 0.8;
                double turretZ = this.getZ();

                for (int i = 0; i < 30; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * 4.0;
                    double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
                    double offsetZ = (this.random.nextDouble() - 0.5) * 4.0;

                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                            turretX + offsetX, turretY + offsetY, turretZ + offsetZ,
                            1, 0.0, 0.0, 0.0, 0.0);
                }

                double machineGunX = this.getX() + Math.cos(Math.toRadians(this.getYRot() + 90)) * 2.0;
                double machineGunY = this.getY() + this.getBbHeight() * 0.6;
                double machineGunZ = this.getZ() + Math.sin(Math.toRadians(this.getYRot() + 90)) * 2.0;

                for (int i = 0; i < 20; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                    double offsetY = (this.random.nextDouble() - 0.5);
                    double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                            machineGunX + offsetX, machineGunY + offsetY, machineGunZ + offsetZ,
                            1, 0.0, 0.0, 0.0, 0.0);
                }

                for (int i = 0; i < 50; i++) {
                    double smokeX = turretX + (this.random.nextDouble() - 0.5) * 6.0;
                    double smokeY = turretY + this.random.nextDouble() * 3.0;
                    double smokeZ = turretZ + (this.random.nextDouble() - 0.5) * 6.0;

                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                            smokeX, smokeY, smokeZ,
                            1, 0.0, 0.1, 0.0, 0.02);
                }
            }
        }
    }
    private void triggerThirdPhase() {
        if (!this.level().isClientSide && !hasTriggeredThirdPhase) {
            hasTriggeredThirdPhase = true;
            setInThirdPhase(true);

            this.setHealth(THIRD_PHASE_REGENERATION_TARGET_HEALTH);

            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, THIRD_PHASE_REGENERATION_DURATION, 2));

            this.isRegeneratingThirdPhase = true;
            this.regenerationTicksThirdPhase = THIRD_PHASE_REGENERATION_DURATION;

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.6F);

            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 50; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * 6.0;
                    double offsetY = (this.random.nextDouble() - 0.5) * 4.0;
                    double offsetZ = (this.random.nextDouble() - 0.5) * 6.0;

                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX() + offsetX, this.getY() + 2.0 + offsetY, this.getZ() + offsetZ,
                            1, 0.0, 0.0, 0.0, 0.0);
                }

                for (int i = 0; i < 40; i++) {
                    double smokeX = this.getX() + (this.random.nextDouble() - 0.5) * 8.0;
                    double smokeY = this.getY() + this.random.nextDouble() * 4.0;
                    double smokeZ = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0;

                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                            smokeX, smokeY, smokeZ,
                            1, 0.0, 0.15, 0.0, 0.03);
                }
            }
        }
    }
    private int countNearbyScamplers() {
        if (this.level().isClientSide) return 0;

        AABB searchArea = new AABB(
                this.getX() - SCAMPLER_CHECK_RADIUS, this.getY() - 10, this.getZ() - SCAMPLER_CHECK_RADIUS,
                this.getX() + SCAMPLER_CHECK_RADIUS, this.getY() + 10, this.getZ() + SCAMPLER_CHECK_RADIUS
        );

        List<ScamplerEntity> scamplers = this.level().getEntitiesOfClass(ScamplerEntity.class, searchArea);
        return scamplers.size();
    }
    private void spawnScampler() {
        if (this.level().isClientSide) return;

        if (countNearbyScamplers() >= MAX_SCAMPLERS_IN_AREA) {
            return;
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 3.0 + this.random.nextDouble() * 4.0;

            double spawnX = this.getX() + Math.cos(angle) * distance;
            double spawnZ = this.getZ() + Math.sin(angle) * distance;
            double spawnY = this.getY();

            BlockPos spawnPos = new BlockPos((int)spawnX, (int)spawnY, (int)spawnZ);
            for (int y = 0; y < 5; y++) {
                BlockPos checkPos = spawnPos.below(y);
                if (!this.level().getBlockState(checkPos).isAir()) {
                    spawnY = checkPos.getY() + 1;
                    break;
                }
            }

            BlockPos finalSpawnPos = new BlockPos((int)spawnX, (int)spawnY, (int)spawnZ);
            if (this.level().getBlockState(finalSpawnPos).isAir() &&
                    this.level().getBlockState(finalSpawnPos.above()).isAir()) {

                ScamplerEntity scampler = new ScamplerEntity(ModEntities.SCAMPLER.get(), this.level());
                scampler.moveTo(spawnX, spawnY, spawnZ, this.random.nextFloat() * 360F, 0.0F);

                if (this.getTarget() != null) {
                    scampler.setTarget(this.getTarget());
                }

                this.level().addFreshEntity(scampler);

                if (this.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 15; i++) {
                        serverLevel.sendParticles(ParticleTypes.POOF,
                                spawnX, spawnY + 0.5, spawnZ,
                                1, 0.3, 0.3, 0.3, 0.1);
                    }
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            spawnX, spawnY + 0.5, spawnZ,
                            5, 0.2, 0.2, 0.2, 0.05);
                }
                this.level().playSound(null, spawnX, spawnY, spawnZ,
                        SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 0.8F, 1.5F);

                break;
            }
        }
    }
    private void checkAndDestroyTerrain() {
        if (this.level().isClientSide || terrainDestructionCooldown > 0) return;

        AABB destructionBox = this.getBoundingBox().inflate(0.5, 0.2, 0.5);

        BlockPos minPos = new BlockPos(
                (int)Math.floor(destructionBox.minX),
                (int)Math.floor(destructionBox.minY),
                (int)Math.floor(destructionBox.minZ)
        );
        BlockPos maxPos = new BlockPos(
                (int)Math.ceil(destructionBox.maxX),
                (int)Math.ceil(destructionBox.maxY + 1),
                (int)Math.ceil(destructionBox.maxZ)
        );

        boolean destroyedAny = false;

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = this.level().getBlockState(pos);

            if (canDestroyBlock(state, pos)) {
                destroyBlock(pos, state, false);
                destroyedAny = true;
            }
        }

        if (destroyedAny) {
            terrainDestructionCooldown = TERRAIN_DESTRUCTION_COOLDOWN_TICKS;
        }
    }
    private void chargeDestroyTerrain() {
        if (this.level().isClientSide) return;

        Vec3 chargeDir = this.chargeDirection.normalize();
        double checkDistance = 3.0;

        for (double d = 0; d <= checkDistance; d += 0.5) {
            Vec3 checkPos = this.position().add(chargeDir.scale(d));
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 2; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = new BlockPos(
                                (int)(checkPos.x + x),
                                (int)(checkPos.y + y),
                                (int)(checkPos.z + z)
                        );

                        BlockState state = this.level().getBlockState(pos);
                        if (canDestroyBlock(state, pos)) {
                            destroyBlock(pos, state, true);
                        }
                    }
                }
            }
        }
    }
    private boolean canDestroyBlock(BlockState state, BlockPos pos) {
        if (state.isAir()) return false;

        return state.is(ModTags.Blocks.TANK_BREAKABLE);
    }
    private void destroyBlock(BlockPos pos, BlockState state, boolean isCharging) {
        if (this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
                if (this.random.nextFloat() < (isCharging ? 0.1f : 0.2f)) {
                    Block.dropResources(state, serverLevel, pos, null, this, ItemStack.EMPTY);
                }
            }

            this.level().destroyBlock(pos, false);

            serverLevel.sendParticles(
                    isCharging ? ParticleTypes.EXPLOSION : ParticleTypes.CLOUD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    isCharging ? 3 : 5,
                    0.25, 0.25, 0.25,
                    0.1
            );

            this.level().playSound(null, pos,
                    isCharging ? SoundEvents.STONE_BREAK : SoundEvents.WOOD_BREAK,
                    SoundSource.BLOCKS,
                    isCharging ? 1.5F : 1.0F,
                    isCharging ? 0.7F : 0.9F
            );

            if (isCharging) {
                for (int i = 0; i < 10; i++) {
                    serverLevel.sendParticles(
                            ParticleTypes.ITEM_SNOWBALL,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            1,
                            this.random.nextGaussian() * 0.3,
                            this.random.nextDouble() * 0.3 + 0.2,
                            this.random.nextGaussian() * 0.3,
                            0.15
                    );
                }
            }
        }
    }
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (terrainDestructionCooldown > 0) {
                terrainDestructionCooldown--;
            }

            Vec3 movement = this.getDeltaMovement();
            double speed = movement.horizontalDistance();

            if (speed > 0.1) {
                checkAndDestroyTerrain();
            }
            updateFlashTimers();
            updateAttackCooldowns();
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (isInThirdPhase()) {
                this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
                triggerThirdPhase();
                handleThirdPhase();
            } else if (isInSecondPhase()) {
                this.bossEvent.setColor(BossEvent.BossBarColor.RED);
                triggerWeaponDestruction();
                handleChargingPhase();

                if (this.getHealth() / this.getMaxHealth() <= 0.25f && !isRegenerating) {
                    setInThirdPhase(true);
                }

                if (isRegenerating && regenerationTicks > 0) {
                    float currentHealth = this.getHealth();
                    if (currentHealth < REGENERATION_TARGET_HEALTH) {
                        this.setHealth(Math.min(currentHealth + REGENERATION_RATE, REGENERATION_TARGET_HEALTH));
                    }
                    regenerationTicks--;
                    if (regenerationTicks <= 0) {
                        isRegenerating = false;
                        this.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                    }
                }
                if (beaconSpawnCooldown > 0) {
                    beaconSpawnCooldown--;
                }
            } else {
                this.bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
                if (this.getHealth() / this.getMaxHealth() <= 0.25f) {
                    setInSecondPhase(true);
                } else {
                    handleCombat();
                }
            }

            for (ServerPlayer player : Objects.requireNonNull(this.level().getServer()).getPlayerList().getPlayers()) {
                double distance = this.distanceToSqr(player);
                if (distance < DETECTION_RANGE * DETECTION_RANGE && this.isAlive()) {
                    this.bossEvent.addPlayer(player);
                } else {
                    this.bossEvent.removePlayer(player);
                }
            }

            if (repositionCooldown > 0) {
                repositionCooldown--;
            }
            if (chargeCooldown > 0) {
                chargeCooldown--;
            }
            if (scamplerSpawnCooldown > 0) {
                scamplerSpawnCooldown--;
            }
            if (thirdPhaseBeaconCooldown > 0) {
                thirdPhaseBeaconCooldown--;
            }
        }

        if (this.level().isClientSide && this.isAlive()) {
            addMovementParticles();
            if (isCharging()) {
                addChargingParticles();
            }
            if (isInThirdPhase()) {
                addThirdPhaseParticles();
            }
        }
    }
    private void handleThirdPhase() {
        if (isRegeneratingThirdPhase && regenerationTicksThirdPhase > 0) {
            float currentHealth = this.getHealth();
            if (currentHealth < THIRD_PHASE_REGENERATION_TARGET_HEALTH) {
                this.setHealth(Math.min(currentHealth + THIRD_PHASE_REGENERATION_RATE, THIRD_PHASE_REGENERATION_TARGET_HEALTH));
            }
            regenerationTicksThirdPhase--;
            if (regenerationTicksThirdPhase <= 0) {
                isRegeneratingThirdPhase = false;
                this.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
        }
        if (scamplerSpawnCooldown <= 0) {
            spawnScampler();
            scamplerSpawnCooldown = SCAMPLER_SPAWN_COOLDOWN;
        }
        if (thirdPhaseBeaconCooldown <= 0) {
            if (this.random.nextFloat() < THIRD_PHASE_BEACON_CHANCE) {
                spawnThirdPhaseBeacon();
                thirdPhaseBeaconCooldown = THIRD_PHASE_BEACON_COOLDOWN;
            } else {
                thirdPhaseBeaconCooldown = 60; // 3 seconds
            }
        }
        if (this.getNavigation().isDone() && this.random.nextInt(100) < 3) {
            double wanderX = this.getX() + (this.random.nextDouble() - 0.5) * 16.0;
            double wanderZ = this.getZ() + (this.random.nextDouble() - 0.5) * 16.0;
            this.getNavigation().moveTo(wanderX, this.getY(), wanderZ, 0.4);
        }
    }
    private void spawnThirdPhaseBeacon() {
        if (this.level().isClientSide) return;

        if (countNearbySkyCatriers() >= MAX_SKY_CARRIERS_IN_AREA) {
            return;
        }

        LivingEntity target = this.getTarget();
        Vec3 targetDirection;

        if (target != null) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double length = Math.sqrt(dx * dx + dz * dz);

            double angle = Math.atan2(dz, dx) + (this.random.nextDouble() - 0.5) * Math.PI * 0.3;
            double distance = 8.0 + this.random.nextDouble() * 12.0;

            targetDirection = new Vec3(
                    Math.cos(angle) * distance,
                    0,
                    Math.sin(angle) * distance
            );
        } else {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 6.0 + this.random.nextDouble() * 14.0;
            targetDirection = new Vec3(
                    Math.cos(angle) * distance,
                    0,
                    Math.sin(angle) * distance
            );
        }

        BeaconProjectileEntity beaconProjectile = new BeaconProjectileEntity(ModEntities.BEACON_PROJECTILE.get(), this.level(), this);

        double launchX = this.getX();
        double launchY = this.getY() + this.getBbHeight() + 1.0;
        double launchZ = this.getZ();

        beaconProjectile.setPos(launchX, launchY, launchZ);

        Vec3 landingPos = this.position().add(targetDirection);
        beaconProjectile.setLandingTarget(landingPos.x, landingPos.z);

        double dx = landingPos.x - launchX;
        double dz = landingPos.z - launchZ;
        double distance = Math.sqrt(dx * dx + dz * dz);

        double launchVelocity = 1.4;
        double launchAngle = Math.PI / 5;

        Vec3 launchVector = new Vec3(
                (dx / distance) * launchVelocity * Math.cos(launchAngle),
                launchVelocity * Math.sin(launchAngle),
                (dz / distance) * launchVelocity * Math.cos(launchAngle)
        );

        beaconProjectile.setDeltaMovement(launchVector);
        this.level().addFreshEntity(beaconProjectile);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 1.2F, 0.6F); // Lower pitch

        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        launchX, launchY, launchZ,
                        1, 0.2, 0.1, 0.2, 0.08);
            }
            for (int i = 0; i < 5; i++) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        launchX, launchY, launchZ,
                        1, 0.3, 0.1, 0.3, 0.1);
            }
        }
    }
    private void addThirdPhaseParticles() {
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + this.random.nextGaussian() * 3.0,
                    this.getY() + 1.0 + this.random.nextDouble() * 2.0,
                    this.getZ() + this.random.nextGaussian() * 3.0,
                    this.random.nextGaussian() * 0.02,
                    0.05,
                    this.random.nextGaussian() * 0.02);
        }

        if (this.random.nextInt(5) == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    this.getX() + this.random.nextGaussian() * 2.5,
                    this.getY() + 2.0,
                    this.getZ() + this.random.nextGaussian() * 2.5,
                    0.0, 0.08, 0.0);
        }
        if (this.random.nextInt(8) == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + this.random.nextGaussian() * 2.0,
                    this.getY() + 1.0 + this.random.nextDouble(),
                    this.getZ() + this.random.nextGaussian() * 2.0,
                    this.random.nextGaussian() * 0.1,
                    this.random.nextDouble() * 0.1,
                    this.random.nextGaussian() * 0.1);
        }
    }
    private void handleChargingPhase() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        float targetPostChargeYaw = 0.0f;
        if (chargeWarmupTicks > 0) {
            chargeWarmupTicks--;
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.1, 1.0, 0.1)); // Slow down dramatically

            if (chargeWarmupTicks == 0) {
                chargeActiveTicks = CHARGE_DURATION;
                this.entityData.set(IS_CHARGING, true);
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);
                chargeDirection = length > 0 ? new Vec3(dx / length, 0, dz / length) : Vec3.ZERO;

                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 0.7F);
            }
        } else if (chargeActiveTicks > 0) {
            chargeActiveTicks--;
            executeCharge();

            if (chargeActiveTicks == 0) {
                this.entityData.set(IS_CHARGING, false);
                chargeCooldown = CHARGE_COOLDOWN_DURATION;
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
                spawnSupplyBeacons();
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                postChargeRotationTicks = POST_CHARGE_ROTATION_DURATION;
            }
        } else if (postChargeRotationTicks > 0) {
            postChargeRotationTicks--;

            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            targetPostChargeYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI) - 90.0);

            float currentYaw = this.getYRot();
            float newYaw = lerpAngle(currentYaw, targetPostChargeYaw, 0.5f);

            this.setYRot(newYaw);
            this.yBodyRot = newYaw;
            this.setYHeadRot(newYaw);

            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            this.getNavigation().stop();

            if (postChargeRotationTicks == 0) {
                this.setYRot(targetPostChargeYaw);
                this.yBodyRot = targetPostChargeYaw;
                this.setYHeadRot(targetPostChargeYaw);
            }
        }
    }
    private float lerpAngle(float current, float target, float factor) {
        float difference = target - current;
        while (difference > 180.0f) difference -= 360.0f;
        while (difference < -180.0f) difference += 360.0f;
        return current + difference * factor;
    }
    private void executeCharge() {
        Vec3 movement = chargeDirection.scale(CHARGE_SPEED);
        this.setDeltaMovement(movement.x, this.getDeltaMovement().y, movement.z);
        chargeDestroyTerrain();
        AABB hitBox = this.getBoundingBox().expandTowards(movement).inflate(0.5);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                entity -> entity != this && entity.isAlive() && !entity.isSpectator());

        for (LivingEntity entity : entities) {
            if (entity.hurt(this.damageSources().mobAttack(this), (float) CHARGE_DAMAGE)) {
                double dx = entity.getX() - this.getX();
                double dz = entity.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > 0) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            (dx / distance) * 1.5, 0.3, (dz / distance) * 1.5));
                }

                this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.MUD_HIT, SoundSource.HOSTILE, 1.5F, 0.8F);
            }
        }
        BlockPos frontPos = new BlockPos(
                (int)(this.getX() + chargeDirection.x * 2.5),
                (int)(this.getY()),
                (int)(this.getZ() + chargeDirection.z * 2.5)
        );

        BlockState blockState = this.level().getBlockState(frontPos);
        if (!blockState.isAir() && blockState.blocksMotion()) {
            chargeActiveTicks = 0;
            this.entityData.set(IS_CHARGING, false);
            chargeCooldown = CHARGE_COOLDOWN_DURATION / 3;
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 25; i++) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            frontPos.getX() + 0.5, frontPos.getY() + 1.0, frontPos.getZ() + 0.5,
                            1, 0.5, 0.5, 0.5, 0.1);
                }
            }
            this.level().playSound(null, frontPos.getX(), frontPos.getY(), frontPos.getZ(),
                    SoundEvents.ANVIL_FALL, SoundSource.HOSTILE, 2.0F, 0.6F);
        }
    }

    private void addChargingParticles() {
        if (this.random.nextInt(2) == 0) {
            double backX = this.getX() - Math.cos(Math.toRadians(this.getYRot())) * 2.5;
            double backZ = this.getZ() - Math.sin(Math.toRadians(this.getYRot())) * 2.5;

            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    backX + this.random.nextGaussian() * 0.3,
                    this.getY() + 2.0 + this.random.nextDouble() * 0.5,
                    backZ + this.random.nextGaussian() * 0.3,
                    this.random.nextGaussian() * 0.05,
                    0.1,
                    this.random.nextGaussian() * 0.05);
        }
        if (this.random.nextInt(1) == 0) {
            addIntenseTreadParticles();
        }
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + this.random.nextGaussian() * 2.0,
                    this.getY() + 1.0 + this.random.nextDouble(),
                    this.getZ() + this.random.nextGaussian() * 2.0,
                    this.random.nextGaussian() * 0.2,
                    this.random.nextDouble() * 0.2,
                    this.random.nextGaussian() * 0.2);
        }
    }

    private void addIntenseTreadParticles() {
        for (int side = 0; side < 2; side++) {
            double sideOffset = (side == 0 ? -1.5 : 1.5);
            double treadX = this.getX() + sideOffset * Math.cos(Math.toRadians(this.getYRot() + 90));
            double treadZ = this.getZ() + sideOffset * Math.sin(Math.toRadians(this.getYRot() + 90));

            for (int i = 0; i < 4; i++) {
                this.level().addParticle(ParticleTypes.POOF,
                        treadX + this.random.nextGaussian() * 0.5,
                        this.getY() + 0.1,
                        treadZ + this.random.nextGaussian() * 0.5,
                        this.random.nextGaussian() * 0.3,
                        this.random.nextDouble() * 0.2,
                        this.random.nextGaussian() * 0.3);
            }
        }
    }
    private class TankChargeGoal extends Goal {
        public TankChargeGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!ScampTankEntity.this.isInSecondPhase() || ScampTankEntity.this.isInThirdPhase()) return false;
            if (ScampTankEntity.this.chargeCooldown > 0) return false;
            if (ScampTankEntity.this.chargeWarmupTicks > 0 || ScampTankEntity.this.chargeActiveTicks > 0) return false;
            if (ScampTankEntity.this.postChargeRotationTicks > 0) return false;

            LivingEntity target = ScampTankEntity.this.getTarget();
            if (target == null || !target.isAlive()) return false;

            double distance = ScampTankEntity.this.distanceTo(target);
            return distance <= CHARGE_RANGE && ScampTankEntity.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return ScampTankEntity.this.chargeWarmupTicks > 0 ||
                    ScampTankEntity.this.chargeActiveTicks > 0 ||
                    ScampTankEntity.this.postChargeRotationTicks > 0;
        }

        @Override
        public void start() {
            LivingEntity target = ScampTankEntity.this.getTarget();
            if (target == null) return;

            Vec3 targetVelocity = target.getDeltaMovement();
            double predictionTime = 1.25;

            double predictedX = target.getX() + (targetVelocity.x * predictionTime * 20);
            double predictedZ = target.getZ() + (targetVelocity.z * predictionTime * 20);

            double dx = predictedX - ScampTankEntity.this.getX();
            double dz = predictedZ - ScampTankEntity.this.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                ScampTankEntity.this.chargeDirection = new Vec3(dx / distance, 0, dz / distance);

                float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI) - 90.0);
                ScampTankEntity.this.setYRot(yaw);
                ScampTankEntity.this.yBodyRot = yaw;

                ScampTankEntity.this.chargeWarmupTicks = CHARGE_WARMUP_DURATION;

                ScampTankEntity.this.level().playSound(null,
                        ScampTankEntity.this.getX(), ScampTankEntity.this.getY(), ScampTankEntity.this.getZ(),
                        SoundEvents.PISTON_EXTEND, SoundSource.HOSTILE, 2.0F, 0.5F);
            }
        }

        @Override
        public void tick() {
            ScampTankEntity.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            ScampTankEntity.this.entityData.set(IS_CHARGING, false);
        }
    }
    private boolean hasCleanLineOfSight(LivingEntity target) {
        if (target == null) return false;

        double[][] checkPoints = {
                {0, this.getBbHeight() * 0.8, 0},
                {0, this.getBbHeight() * 0.6, 0},
                {0, this.getBbHeight() * 1.2, 0}
        };

        for (double[] point : checkPoints) {
            Vec3 tankPos = new Vec3(this.getX() + point[0], this.getY() + point[1], this.getZ() + point[2]);
            Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());

            if (this.level().clip(new ClipContext(
                    tankPos,
                    targetPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this
            )).getType() == HitResult.Type.MISS) {
                return true;
            }
        }

        return false;
    }
    private void handleCombat() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            noLineOfSightTimer = 0;
            return;
        }

        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            this.setTarget(null);
            return;
        }

        boolean hasLOS = hasCleanLineOfSight(target);
        double distanceToTarget = this.distanceToSqr(target);

        if (!hasLOS) {
            noLineOfSightTimer++;
            lastKnownTargetPosition = target.position();

            if (noLineOfSightTimer >= NO_LOS_THRESHOLD && !isAggressivelyRepositioning) {
                initiateAggressiveRepositioning();
            }

            if (noLineOfSightTimer < 20 && lastKnownTargetPosition != null) {
                attemptPredictiveShot(target);
            }
        } else {
            noLineOfSightTimer = 0;
            isAggressivelyRepositioning = false;
            frustratedShotAttempts = 0;
            if (distanceToTarget <= MACHINE_GUN_RANGE * MACHINE_GUN_RANGE && machineGunCooldown <= 0) {
                fireMachineGun(target);
                machineGunCooldown = MACHINE_GUN_COOLDOWN_TICKS;
            }
            else if (distanceToTarget > MACHINE_GUN_RANGE * MACHINE_GUN_RANGE &&
                    distanceToTarget <= MAIN_CANNON_RANGE * MAIN_CANNON_RANGE &&
                    mainCannonCooldown <= 0) {
                fireMainCannon(target);
                mainCannonCooldown = MAIN_CANNON_COOLDOWN_TICKS;
            }
        }
    }
    private void initiateAggressiveRepositioning() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        isAggressivelyRepositioning = true;
        frustratedShotAttempts++;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.5F, 0.5F);

        if (frustratedShotAttempts >= 3 && !isInSecondPhase()) {
            fireSuppressionBarrage(lastKnownTargetPosition);
            frustratedShotAttempts = 0;
        }
    }
    private void attemptPredictiveShot(LivingEntity target) {
        if (mainCannonCooldown > 0 || lastKnownTargetPosition == null) return;
        Vec3 targetVelocity = target.getDeltaMovement();
        Vec3 predictedPos = lastKnownTargetPosition.add(
                targetVelocity.x * 20,
                targetVelocity.y * 10,
                targetVelocity.z * 20
        );
        double turretHeight = this.getBbHeight() * 1.2;
        double spawnX = this.getX();
        double spawnY = this.getY() + turretHeight;
        double spawnZ = this.getZ();

        ScampRocketEntity rocket = new ScampRocketEntity(ModEntities.SCAMP_ROCKET.get(), this.level(), this);
        rocket.setPos(spawnX, spawnY, spawnZ);
        rocket.setDamage(5.0);
        rocket.setExplosionRadius(4.0f);

        double dx = predictedPos.x - spawnX;
        double dy = predictedPos.y - spawnY;
        double dz = predictedPos.z - spawnZ;

        rocket.shoot(dx, dy, dz, 2.8f, 2.0f);
        this.level().addFreshEntity(rocket);

        mainCannonCooldown = MAIN_CANNON_COOLDOWN_TICKS / 2;
    }

    private void fireSuppressionBarrage(Vec3 targetArea) {
        if (this.level().isClientSide) return;

        for (int i = 0; i < 5; i++) {
            ScampRocketEntity rocket = new ScampRocketEntity(ModEntities.SCAMP_ROCKET.get(), this.level(), this);

            double spawnX = this.getX();
            double spawnY = this.getY() + this.getBbHeight() * 1.2;
            double spawnZ = this.getZ();

            rocket.setPos(spawnX, spawnY, spawnZ);
            rocket.setDamage(4.0);
            rocket.setExplosionRadius(3.0f);

            double spreadX = (this.random.nextDouble() - 0.5) * 8.0;
            double spreadZ = (this.random.nextDouble() - 0.5) * 8.0;

            double dx = targetArea.x + spreadX - spawnX;
            double dy = targetArea.y - spawnY;
            double dz = targetArea.z + spreadZ - spawnZ;

            rocket.shoot(dx, dy, dz, 2.5f, 3.0f);
            this.level().addFreshEntity(rocket);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 2.0F, 0.6F);

        mainCannonCooldown = MAIN_CANNON_COOLDOWN_TICKS * 2;
    }
    private void updateFlashTimers() {
        int mainTimer = this.entityData.get(MAIN_TURRET_FLASH_TIMER);
        if (mainTimer > 0) {
            this.entityData.set(MAIN_TURRET_FLASH_TIMER, mainTimer - 1);
        }

        int machineTimer = this.entityData.get(MACHINE_GUN_FLASH_TIMER);
        if (machineTimer > 0) {
            this.entityData.set(MACHINE_GUN_FLASH_TIMER, machineTimer - 1);
        }
    }

    private void updateAttackCooldowns() {
        if (mainCannonCooldown > 0) {
            mainCannonCooldown--;
        }
        if (machineGunCooldown > 0) {
            machineGunCooldown--;
        }
    }

    public boolean isCharging() {
        return this.entityData.get(IS_CHARGING);
    }


    private void addMovementParticles() {
        Vec3 deltaMovement = this.getDeltaMovement();
        double speed = deltaMovement.horizontalDistance();

        if (speed > 0.02) {
            float intensity = (float) Math.min(speed * 2.0, 1.0);

            if (this.random.nextInt(2) == 0) {
                addTreadDustParticles(intensity);
            }

            if (this.random.nextInt(3) == 0) {
                addExhaustSmoke(intensity);
            }

            if (speed > 0.1 && this.random.nextInt(4) == 0) {
                addGroundImpactParticles(intensity);
            }
            if (speed > 0.03 && this.random.nextInt(8) == 0) {
                addMechanicalSparks();
            }
        }
    }

    private void addTreadDustParticles(float intensity) {
        double leftX = this.getX() - 1.5 * Math.cos(Math.toRadians(this.getYRot() + 90));
        double leftZ = this.getZ() - 1.5 * Math.sin(Math.toRadians(this.getYRot() + 90));

        for (int i = 0; i < (int)(3 * intensity); i++) {
            double offsetX = this.random.nextGaussian() * 0.3;
            double offsetZ = this.random.nextGaussian() * 0.3;

            this.level().addParticle(ParticleTypes.POOF,
                    leftX + offsetX, this.getY() + 0.1, leftZ + offsetZ,
                    this.random.nextGaussian() * 0.1,
                    this.random.nextDouble() * 0.1,
                    this.random.nextGaussian() * 0.1);
        }

        double rightX = this.getX() + 1.5 * Math.cos(Math.toRadians(this.getYRot() + 90));
        double rightZ = this.getZ() + 1.5 * Math.sin(Math.toRadians(this.getYRot() + 90));

        for (int i = 0; i < (int)(3 * intensity); i++) {
            double offsetX = this.random.nextGaussian() * 0.3;
            double offsetZ = this.random.nextGaussian() * 0.3;

            this.level().addParticle(ParticleTypes.POOF,
                    rightX + offsetX, this.getY() + 0.1, rightZ + offsetZ,
                    this.random.nextGaussian() * 0.1,
                    this.random.nextDouble() * 0.1,
                    this.random.nextGaussian() * 0.1);
        }
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.CLOUD,
                    this.getX() + this.random.nextGaussian() * 2.0,
                    this.getY() + 0.2,
                    this.getZ() + this.random.nextGaussian() * 2.0,
                    0.0, 0.05, 0.0);
        }
    }

    private void addExhaustSmoke(float intensity) {
        double backX = this.getX() - 2.0 * Math.cos(Math.toRadians(this.getYRot()));
        double backZ = this.getZ() - 2.0 * Math.sin(Math.toRadians(this.getYRot()));

        for (int i = 0; i < (int)(2 * intensity); i++) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    backX + this.random.nextGaussian() * 0.5,
                    this.getY() + 2.0 + this.random.nextDouble() * 0.5,
                    backZ + this.random.nextGaussian() * 0.5,
                    this.random.nextGaussian() * 0.02,
                    0.05 + this.random.nextDouble() * 0.03,
                    this.random.nextGaussian() * 0.02);
        }
        if (this.random.nextInt(5) == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    backX, this.getY() + 2.2, backZ,
                    0.0, 0.08, 0.0);
        }
    }

    private void addGroundImpactParticles(float intensity) {
        for (int i = 0; i < (int)(4 * intensity); i++) {
            double offsetX = this.random.nextGaussian() * 1.5;
            double offsetZ = this.random.nextGaussian() * 1.5;

            this.level().addParticle(ParticleTypes.CLOUD,
                    this.getX() + offsetX,
                    this.getY(),
                    this.getZ() + offsetZ,
                    this.random.nextGaussian() * 0.1,
                    this.random.nextDouble() * 0.2,
                    this.random.nextGaussian() * 0.1);
        }
        if (this.random.nextInt(6) == 0) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX() + this.random.nextGaussian() * 2.0,
                    this.getY() + 0.1,
                    this.getZ() + this.random.nextGaussian() * 2.0,
                    0.0, 0.03, 0.0);
        }
    }

    private void addMechanicalSparks() {
        double sparkX = this.getX() + this.random.nextGaussian() * 2.0;
        double sparkZ = this.getZ() + this.random.nextGaussian() * 2.0;
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    sparkX, this.getY() + 1.0, sparkZ,
                    this.random.nextGaussian() * 0.1,
                    this.random.nextDouble() * 0.15,
                    this.random.nextGaussian() * 0.1);
        }
    }
    private void fireMainCannon(LivingEntity target) {
        double turretHeight = this.getBbHeight() * 1.2;
        double turretForwardOffset = 2.0;

        double spawnX = this.getX() + Math.cos(Math.toRadians(this.getYRot() + 90)) * turretForwardOffset;
        double spawnY = this.getY() + turretHeight;
        double spawnZ = this.getZ() + Math.sin(Math.toRadians(this.getYRot() + 90)) * turretForwardOffset;

        ScampRocketEntity rocket = new ScampRocketEntity(ModEntities.SCAMP_ROCKET.get(), this.level(), this);
        rocket.setPos(spawnX, spawnY, spawnZ);
        rocket.setDamage(5.0);
        rocket.setExplosionRadius(3.5f);

        double dx = target.getX() + (target.getDeltaMovement().x * 10) - spawnX;
        double dy = target.getEyeY() - spawnY;
        double dz = target.getZ() + (target.getDeltaMovement().z * 10) - spawnZ;

        rocket.shoot(dx, dy, dz, 2.6f, 1.0f);
        this.level().addFreshEntity(rocket);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2F, 0.7F);
        this.triggerMainTurretFlash();
    }

    private void fireMachineGun(LivingEntity target) {
        double machineGunHeight = this.getBbHeight() * 0.8;
        double machineGunOffset = 1.0;

        double spawnX = this.getX() + Math.cos(Math.toRadians(this.getYRot() + 90)) * machineGunOffset;
        double spawnY = this.getY() + machineGunHeight;
        double spawnZ = this.getZ() + Math.sin(Math.toRadians(this.getYRot() + 90)) * machineGunOffset;

        BrassBoltEntity bolt = new BrassBoltEntity(this.level(), this);
        bolt.setPos(spawnX, spawnY, spawnZ);

        double dx = target.getX() - spawnX;
        double dy = target.getEyeY() - spawnY;
        double dz = target.getZ() - spawnZ;

        bolt.shoot(dx, dy, dz, 3.0f, 1.5f);
        this.level().addFreshEntity(bolt);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.BRUISER_SILENCED_FIRE.get(), SoundSource.HOSTILE, 0.8F, 1.2F);
        this.triggerMachineGunFlash();
    }
    private int countNearbySkyCatriers() {
        if (this.level().isClientSide) return 0;

        AABB searchArea = new AABB(
                this.getX() - SKY_CARRIER_CHECK_RADIUS, this.getY() - 20, this.getZ() - SKY_CARRIER_CHECK_RADIUS,
                this.getX() + SKY_CARRIER_CHECK_RADIUS, this.getY() + 40, this.getZ() + SKY_CARRIER_CHECK_RADIUS
        );

        List<SkyCarrierEntity> skyCarriers = this.level().getEntitiesOfClass(SkyCarrierEntity.class, searchArea);
        return skyCarriers.size();
    }
    private void shootBeaconProjectile() {
        if (this.level().isClientSide) return;

        LivingEntity target = this.getTarget();
        Vec3 targetDirection;

        if (target != null) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double length = Math.sqrt(dx * dx + dz * dz);

            double angle = Math.atan2(dz, dx) + (this.random.nextDouble() - 0.5) * Math.PI * 0.5; //
            double distance = 15.0 + this.random.nextDouble() * 10.0;

            targetDirection = new Vec3(
                    Math.cos(angle) * distance,
                    0,
                    Math.sin(angle) * distance
            );
        } else {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double distance = 10.0 + this.random.nextDouble() * 20.0;
            targetDirection = new Vec3(
                    Math.cos(angle) * distance,
                    0,
                    Math.sin(angle) * distance
            );
        }

        BeaconProjectileEntity beaconProjectile = new BeaconProjectileEntity(ModEntities.BEACON_PROJECTILE.get(), this.level(), this);
        double launchX = this.getX();
        double launchY = this.getY() + this.getBbHeight() + 1.0;
        double launchZ = this.getZ();

        beaconProjectile.setPos(launchX, launchY, launchZ);

        Vec3 landingPos = this.position().add(targetDirection);
        beaconProjectile.setLandingTarget(landingPos.x, landingPos.z);

        double dx = landingPos.x - launchX;
        double dz = landingPos.z - launchZ;
        double distance = Math.sqrt(dx * dx + dz * dz);

        double launchVelocity = 1.2;
        double launchAngle = Math.PI / 6; // 30 degrees

        Vec3 launchVector = new Vec3(
                (dx / distance) * launchVelocity * Math.cos(launchAngle),
                launchVelocity * Math.sin(launchAngle),
                (dz / distance) * launchVelocity * Math.cos(launchAngle)
        );

        beaconProjectile.setDeltaMovement(launchVector);
        this.level().addFreshEntity(beaconProjectile);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 1.5F, 0.8F);

        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 10; i++) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        launchX, launchY, launchZ,
                        1, 0.3, 0.1, 0.3, 0.1);
            }
        }
    }

    private boolean shouldSpawnBeacon() {

        if (beaconSpawnCooldown > 0) {
            return false;
        }

        if (countNearbySkyCatriers() >= MAX_SKY_CARRIERS_IN_AREA) {
            return false;
        }
        return this.random.nextFloat() < 0.3f;
    }

    private void spawnSupplyBeacons() {
        if (this.level().isClientSide) return;

        if (!shouldSpawnBeacon()) {
            return;
        }

        shootBeaconProjectile();

        beaconSpawnCooldown = BEACON_SPAWN_COOLDOWN;
    }
    private class TankChaseGoal extends Goal {
        private int pathUpdateTimer = 0;
        private int repositionTimer = 0;
        private double targetX, targetZ;
        private boolean hasDestination = false;
        private Vec3 lastTargetPos = Vec3.ZERO;
        private int failedPathAttempts = 0;

        public TankChaseGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (ScampTankEntity.this.isInThirdPhase()) {
                return true;
            }
            if (ScampTankEntity.this.isInSecondPhase() ||
                    ScampTankEntity.this.postChargeRotationTicks > 0) return false;

            LivingEntity target = ScampTankEntity.this.getTarget();
            if (target == null || !target.isAlive()) return false;

            double distance = ScampTankEntity.this.distanceTo(target);

            return distance > MIN_COMBAT_RANGE ||
                    ScampTankEntity.this.avoidanceTimer > 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (ScampTankEntity.this.isInThirdPhase()) {
                return hasDestination && !ScampTankEntity.this.getNavigation().isDone();
            }

            if (ScampTankEntity.this.isInSecondPhase() ||
                    ScampTankEntity.this.postChargeRotationTicks > 0) return false;

            LivingEntity target = ScampTankEntity.this.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            pathUpdateTimer = 0;
            failedPathAttempts = 0;
            calculateDestination();
        }

        @Override
        public void tick() {
            LivingEntity target = ScampTankEntity.this.getTarget();
            if (target == null) return;

            pathUpdateTimer++;

            if (ScampTankEntity.this.isAggressivelyRepositioning) {
                if (pathUpdateTimer % 10 == 0) {
                    calculateAggressiveDestination(target);
                }
            } else {
                if (pathUpdateTimer >= 40) {
                    pathUpdateTimer = 0;
                    calculateDestination();
                }
            }
            if (hasDestination) {
                double distToDestination = Math.sqrt(
                        Math.pow(ScampTankEntity.this.getX() - targetX, 2) +
                                Math.pow(ScampTankEntity.this.getZ() - targetZ, 2)
                );

                if (distToDestination < 3.0) {
                    hasDestination = false;
                    if (ScampTankEntity.this.isAggressivelyRepositioning) {
                        if (ScampTankEntity.this.hasCleanLineOfSight(target)) {
                            ScampTankEntity.this.isAggressivelyRepositioning = false;
                        }
                    }
                }
            }
        }
        private void calculateDestination() {
            LivingEntity target = ScampTankEntity.this.getTarget();
            if (target == null) return;

            double distance = ScampTankEntity.this.distanceTo(target);

            if (distance > PREFERRED_COMBAT_RANGE + 3.0) {
                double dx = target.getX() - ScampTankEntity.this.getX();
                double dz = target.getZ() - ScampTankEntity.this.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);

                if (length > 0) {
                    double targetDistance = PREFERRED_COMBAT_RANGE;
                    targetX = target.getX() - (dx / length) * targetDistance;
                    targetZ = target.getZ() - (dz / length) * targetDistance;

                    ScampTankEntity.this.getNavigation().moveTo(targetX, target.getY(), targetZ, 0.8);
                    hasDestination = true;
                }
            } else if (distance < MIN_COMBAT_RANGE) {
                double dx = ScampTankEntity.this.getX() - target.getX();
                double dz = ScampTankEntity.this.getZ() - target.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);

                if (length > 0) {
                    targetX = ScampTankEntity.this.getX() + (dx / length) * 8.0;
                    targetZ = ScampTankEntity.this.getZ() + (dz / length) * 8.0;

                    ScampTankEntity.this.getNavigation().moveTo(targetX, target.getY(), targetZ, 0.6);
                    hasDestination = true;
                }
            }
        }
        private void calculateAggressiveDestination(LivingEntity target) {
            double angle = Math.atan2(
                    target.getZ() - ScampTankEntity.this.getZ(),
                    target.getX() - ScampTankEntity.this.getX()
            );
            double[] angleOffsets = {Math.PI/3, -Math.PI/3, Math.PI/2, -Math.PI/2, Math.PI*2/3, -Math.PI*2/3};

            for (double offset : angleOffsets) {
                double testAngle = angle + offset;
                double testDistance = PREFERRED_COMBAT_RANGE;

                targetX = target.getX() + Math.cos(testAngle) * testDistance;
                targetZ = target.getZ() + Math.sin(testAngle) * testDistance;

                BlockPos testPos = new BlockPos((int)targetX, (int)ScampTankEntity.this.getY(), (int)targetZ);
                if (ScampTankEntity.this.level().getBlockState(testPos).isAir()) {
                    ScampTankEntity.this.getNavigation().moveTo(targetX, target.getY(), targetZ, 1.2); // Fast movement
                    hasDestination = true;
                    break;
                }
            }
        }
        @Override
        public void stop() {
            ScampTankEntity.this.getNavigation().stop();
            hasDestination = false;
            failedPathAttempts = 0;
        }
    }

    private class TankLookGoal extends Goal {
        private float targetYaw = 0;
        private static final float MAX_ROTATION_SPEED = 4.0f;

        public TankLookGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return ScampTankEntity.this.getTarget() != null &&
                    !ScampTankEntity.this.isInSecondPhase() &&
                    ScampTankEntity.this.postChargeRotationTicks <= 0 &&
                    ScampTankEntity.this.avoidanceTimer <= 0;
        }

        @Override
        public void tick() {
            LivingEntity target = ScampTankEntity.this.getTarget();
            if (target == null) return;

            double dx = target.getX() - ScampTankEntity.this.getX();
            double dz = target.getZ() - ScampTankEntity.this.getZ();
            targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI) - 90.0);

            float currentYaw = ScampTankEntity.this.getYRot();
            float yawDiff = targetYaw - currentYaw;

            while (yawDiff > 180.0f) yawDiff -= 360.0f;
            while (yawDiff < -180.0f) yawDiff += 360.0f;

            float rotation = Math.signum(yawDiff) * Math.min(Math.abs(yawDiff), MAX_ROTATION_SPEED);
            float newYaw = currentYaw + rotation;

            if (Math.abs(yawDiff) > 1.0f) {
                ScampTankEntity.this.setYRot(newYaw);
                ScampTankEntity.this.yBodyRot = newYaw;
                ScampTankEntity.this.setYHeadRot(newYaw);
            }
        }
    }

    private class ExtendedRangeTargetGoal extends Goal {
        private int targetSearchDelay = 0;

        public ExtendedRangeTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (--this.targetSearchDelay <= 0) {
                this.targetSearchDelay = 20;
                Player target = findNearestPlayer();
                if (target != null) {
                    ScampTankEntity.this.setTarget(target);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            Player target = findNearestPlayer();
            ScampTankEntity.this.setTarget(target);
        }

        private Player findNearestPlayer() {
            AABB searchBox = ScampTankEntity.this.getBoundingBox().inflate(DETECTION_RANGE);
            List<Player> players = ScampTankEntity.this.level().getEntitiesOfClass(Player.class, searchBox);

            Player closest = null;
            double closestDistance = DETECTION_RANGE * DETECTION_RANGE;

            for (Player player : players) {
                if (player.isCreative() || player.isSpectator()) continue;

                double distance = ScampTankEntity.this.distanceToSqr(player);
                if (distance < closestDistance) {
                    closest = player;
                    closestDistance = distance;
                }
            }

            return closest;
        }
    }

    public void triggerMainTurretFlash() {
        this.entityData.set(MAIN_TURRET_FLASH_TIMER, 4);
    }

    public void triggerMachineGunFlash() {
        this.entityData.set(MACHINE_GUN_FLASH_TIMER, 4);
    }

    public boolean isMainTurretFlashVisible() {
        return this.entityData.get(MAIN_TURRET_FLASH_TIMER) > 0;
    }

    public boolean isMachineGunFlashVisible() {
        return this.entityData.get(MACHINE_GUN_FLASH_TIMER) > 0;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}