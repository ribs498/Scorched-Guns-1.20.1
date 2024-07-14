package top.ribs.scguns.entity.weapon;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ProjectileManager;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.interfaces.IProjectileFactory;
import top.ribs.scguns.interfaces.IWeapon;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBulletTrail;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.concurrent.ThreadLocalRandom;

public class ScGunsWeapon implements IWeapon {
    private final ItemStack gunStack;
    private SoundEvent fireSound;
    private SoundEvent loadSound;
    private final Gun gun;

    public ScGunsWeapon(ItemStack stack) {
        if (stack.getItem() instanceof GunItem g) {
            this.gunStack = stack;
            this.gun = g.getModifiedGun(stack);
        } else {
            this.gunStack = ModItems.SCRAPPER.get().getDefaultInstance();
            this.gun = ModItems.SCRAPPER.get().getGun();
        }
    }

    @Override
    public Item getWeapon() {
        return gunStack.getItem();
    }

    @Override
    public double getMoveSpeedAmp() {
        return 0.4;
    }

    @Override
    public int getAttackCooldown() {
        return GunModifierHelper.getModifiedRate(gunStack, GunEnchantmentHelper.getRate(gunStack, gun));
    }

    public int getAdjustedAttackCooldown(double modifier) {
        return (int) (getAttackCooldown() * modifier);
    }

    @Override
    public int getWeaponLoadTime() {
        return 0;
    }

    @Override
    public float getProjectileSpeed() {
        return (float) GunModifierHelper.getModifiedProjectileSpeed(
                gunStack,
                gun.getProjectile().getSpeed() * GunEnchantmentHelper.getProjectileSpeedModifier(gunStack)
        );
    }

    public float getAdjustedProjectileSpeed(double modifier) {
        return getProjectileSpeed() * (float) modifier;
    }

    @Override
    public SoundEvent getShootSound() {
        if (fireSound == null)
            fireSound = ForgeRegistries.SOUND_EVENTS.getValue(
                    GunModifierHelper.isSilencedFire(gunStack) ? gun.getSounds().getSilencedFire() :
                            gunStack.isEnchanted() ? gun.getSounds().getEnchantedFire() :
                                    gun.getSounds().getFire()
            );
        return fireSound;
    }

    @Override
    public SoundEvent getLoadSound() {
        if (loadSound == null)
            loadSound = ForgeRegistries.SOUND_EVENTS.getValue(gun.getSounds().getReload());
        return loadSound;
    }

    @Override
    public void performRangedAttackIWeapon(Mob shooter, double x, double y, double z, float projectileSpeed) {
        final Level level = shooter.level();
        if (level.isClientSide()) return;
        int count = gun.getGeneral().getProjectileAmount();
        Gun.Projectile projectileProps = gun.getProjectile();
        ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];
        for (int i = 0; i < count; ++i) {
            ResourceLocation projectileItemLocation = ForgeRegistries.ITEMS.getKey(projectileProps.getItem());
            IProjectileFactory factory = ProjectileManager.getInstance().getFactory(projectileItemLocation);
            ProjectileEntity projectileEntity = factory.create(level, shooter, gunStack, (GunItem) gunStack.getItem(), gun);
            projectileEntity.setWeapon(gunStack);
            projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(gunStack));
            final Vec3 startPos = shooter.getEyePosition();
            final float gunSpread = GunModifierHelper.getModifiedSpread(gunStack, gun.getGeneral().getSpread()) * .5F;
            final Vec3 track = new Vec3(x, y, z).subtract(startPos).normalize().add(
                    ThreadLocalRandom.current().nextFloat() * gunSpread / 100.0,
                    ThreadLocalRandom.current().nextFloat() * gunSpread / 100.0,
                    ThreadLocalRandom.current().nextFloat() * gunSpread / 100.0
            );
            projectileEntity.setPos(startPos.add(track));
            projectileEntity.setDeltaMovement(track.scale(projectileSpeed));
            level.addFreshEntity(projectileEntity);
            spawnedProjectiles[i] = projectileEntity;
            projectileEntity.tick();
        }
        consumeAmmoInGun();
        if (!projectileProps.isVisible()) {
            int radius = (int) shooter.getX();
            int y1 = (int) (shooter.getY() + 1.0);
            int z1 = (int) shooter.getZ();
            double r = Config.COMMON.network.projectileTrackingRange.get();
            ParticleOptions data = GunEnchantmentHelper.getParticle(gunStack);

            S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), data);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(level, radius, y1, z1, r),
                    messageBulletTrail
            );
        }
    }

    @Override
    public boolean isLoaded() {
        return Gun.hasAmmo(gunStack);
    }

    @Override
    public void setLoaded(int ammo) {
        CompoundTag tag = gunStack.getOrCreateTag();
        if (tag.getBoolean("IgnoreAmmo")) return;
        tag.putInt("AmmoCount", ammo);
    }

    public void consumeAmmoInGun() {
        CompoundTag tag = gunStack.getOrCreateTag();
        if (!tag.getBoolean("IgnoreAmmo")) {
            int level = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.RECLAIMED.get(), gunStack);
            if (level == 0 || ThreadLocalRandom.current().nextInt(4 - Mth.clamp(level, 1, 2)) != 0) {
                tag.putInt("AmmoCount", Math.max(0, tag.getInt("AmmoCount") - 1));
            }
        }
    }
}

