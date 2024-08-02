package top.ribs.scguns.entity.projectile;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

import javax.annotation.Nullable;
import java.util.*;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;


public class ShulkshotProjectileEntity extends ProjectileEntity {

    private LivingEntity target;
    private int homingDelay;
    private static final int DEFAULT_HOMING_DELAY = 15;

    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.homingDelay = DEFAULT_HOMING_DELAY;
    }

    public ShulkshotProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.homingDelay = DEFAULT_HOMING_DELAY;

        Vec3 shooterDirection = shooter.getLookAngle();
        double offsetDistance = 1.5;
        this.setPos(
                shooter.getX() + shooterDirection.x * offsetDistance,
                shooter.getEyeY() - 0.1 + shooterDirection.y * offsetDistance,
                shooter.getZ() + shooterDirection.z * offsetDistance
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (this.homingDelay > 0) {
                this.homingDelay--;
            } else {
                if (this.target == null || !this.target.isAlive()) {
                    this.findNewTarget();
                }

                if (this.target != null) {
                    this.moveTowardsTarget();
                }
            }
        }

        this.updateHeading();
        if (this.level().isClientSide && this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    private void findNewTarget() {
        List<Mob> potentialTargets = this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16), entity -> entity.isAlive() && !entity.isSpectator());
        if (!potentialTargets.isEmpty()) {
            this.target = potentialTargets.get(this.random.nextInt(potentialTargets.size()));
        }
    }

    private void moveTowardsTarget() {
        Vec3 targetPosition = new Vec3(this.target.getX(), this.target.getY(0.5), this.target.getZ());
        Vec3 direction = targetPosition.subtract(this.position()).normalize();
        double speed = 10.0;
        this.setDeltaMovement(direction.scale(speed));
    }

    public void updateHeading() {
        Vec3 velocity = this.getDeltaMovement();
        double horizontalDistance = velocity.horizontalDistance();
        this.setYRot((float) (Mth.atan2(velocity.x, velocity.z) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(velocity.y, horizontalDistance) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
}