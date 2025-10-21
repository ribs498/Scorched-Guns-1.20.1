package top.ribs.scguns.entity.monster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.ribs.scguns.item.GunItem;

public class ViventrumEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TIMEOUT =
            SynchedEntityData.defineId(ViventrumEntity.class, EntityDataSerializers.INT);

    public ViventrumEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 16D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 2f)
                .add(Attributes.ATTACK_DAMAGE, 4f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.3f)
                .add(Attributes.FLYING_SPEED, 0.3D);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(ATTACK_TIMEOUT, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.isAttacking() && this.getAttackTimeout() > 0) {
                this.setAttackTimeout(this.getAttackTimeout() - 1);
                if (this.getAttackTimeout() == 5) {
                    LivingEntity target = this.getTarget();
                    if (target != null && this.distanceToSqr(target) <= this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth()) {
                        this.doHurtTarget(target);
                    }
                }
                if (this.getAttackTimeout() <= 0) {
                    this.setAttacking(false);
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        ItemStack mainHandItem = this.getMainHandItem();
        boolean hasGun = mainHandItem.getItem() instanceof GunItem;

        if (!hasGun) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
                @Override
                protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
                    if (pDistToEnemySqr <= this.getAttackReachSqr(pEnemy) && this.getTicksUntilNextAttack() <= 0 && !ViventrumEntity.this.isAttacking()) {
                        ViventrumEntity.this.setAttacking(true);
                        this.resetAttackCooldown();
                        this.mob.swing(InteractionHand.MAIN_HAND);
                    }
                }
            });
        }

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(stack);
        if (!this.getItemBySlot(slot).isEmpty()) {
            return false;
        }
        return slot == EquipmentSlot.HEAD;
    }

    @Override
    public boolean canReplaceCurrentItem(ItemStack candidate, ItemStack existing) {
        if (existing.isEmpty()) {
            return Mob.getEquipmentSlotForItem(candidate) == EquipmentSlot.HEAD;
        }
        return false;
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.setAttackTimeout(10);
        }
    }

    public void setAttackTimeout(int timeout) {
        this.entityData.set(ATTACK_TIMEOUT, timeout);
    }

    public int getAttackTimeout() {
        return this.entityData.get(ATTACK_TIMEOUT);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float f = (this.getPose() == Pose.STANDING) ? Math.min(partialTick * 6F, 1f) : 0f;
        this.walkAnimation.update(f, 0.2f);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull net.minecraft.world.level.block.state.BlockState state, @NotNull net.minecraft.core.BlockPos pos) {
        // No fall damage for floating entity
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Attacking", this.isAttacking());
        tag.putInt("AttackTimeout", this.getAttackTimeout());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttacking(tag.getBoolean("Attacking"));
        this.setAttackTimeout(tag.getInt("AttackTimeout"));
    }
}