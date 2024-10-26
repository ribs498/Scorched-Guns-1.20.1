package top.ribs.scguns.common.network;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.client.handler.BeamHandler;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.common.*;
import top.ribs.scguns.common.container.AttachmentContainer;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.interfaces.IProjectileFactory;
import top.ribs.scguns.item.AirGunItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.SilencedFirearm;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ServerPlayHandler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity ->
            (entity.getSoundSource() == SoundSource.HOSTILE || entity.getType() == EntityType.PIGLIN || entity.getType() == EntityType.ZOMBIFIED_PIGLIN || entity.getType() == EntityType.ENDERMAN) &&
                    !Config.COMMON.aggroMobs.exemptEntities.get().contains(EntityType.getKey(entity.getType()).toString());
    private static final Map<UUID, BeamHandler.BeamInfo> activeBeams = new HashMap<UUID, BeamHandler.BeamInfo>();
    private static final Predicate<LivingEntity> FLEEING_ENTITIES = entity ->
            Config.COMMON.fleeingMobs.fleeingEntities.get().contains(EntityType.getKey(entity.getType()).toString());

    /**
     * Fires the weapon the player is currently holding.
     * This is only intended for use on the logical server.
     *
     * @param player the player for who's weapon to fire
     */
    public static void handleShoot(C2SMessageShoot message, ServerPlayer player) {
        if (player.isSpectator())
            return;

        if (player.getUseItem().getItem() == Items.SHIELD)
            return;

        Level world = player.level();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() instanceof GunItem item && (Gun.hasAmmo(heldItem) || player.isCreative())) {
            Gun modifiedGun = item.getModifiedGun(heldItem);
            if (modifiedGun != null) {
                if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                    return;
                player.setYRot(Mth.wrapDegrees(message.getRotationYaw()));
                player.setXRot(Mth.clamp(message.getRotationPitch(), -90F, 90F));
                ShootTracker tracker = ShootTracker.getShootTracker(player);
                if (tracker.hasCooldown(item) && tracker.getRemaining(item) > Config.SERVER.cooldownThreshold.get()) {
                    ScorchedGuns.LOGGER.warn(player.getName().getContents() + "(" + player.getUUID() + ") tried to fire before cooldown finished or server is lagging? Remaining milliseconds: " + tracker.getRemaining(item));
                    return;
                }
                tracker.putCooldown(heldItem, item, modifiedGun);

                if(ModSyncedDataKeys.RELOADING.getValue(player))
                {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                }
                if (!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F) {
                    SpreadTracker.get(player).update(player, item);
                }

                FireMode fireMode = modifiedGun.getGeneral().getFireMode();
                if (fireMode.equals(FireMode.BEAM)) {
                    handleBeamWeapon(player, heldItem, modifiedGun);
                } else {
                    int count = modifiedGun.getGeneral().getProjectileAmount();
                    Gun.Projectile projectileProps = modifiedGun.getProjectile();
                    ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];
                    for (int i = 0; i < count; i++) {
                        IProjectileFactory factory = ProjectileManager.getInstance().getFactory(ForgeRegistries.ITEMS.getKey(projectileProps.getItem()));
                        ProjectileEntity projectileEntity = factory.create(world, player, heldItem, item, modifiedGun);
                        projectileEntity.setWeapon(heldItem);
                        projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
                        world.addFreshEntity(projectileEntity);
                        spawnedProjectiles[i] = projectileEntity;
                        projectileEntity.tick();
                    }
                    if (!projectileProps.isVisible()) {
                        double spawnX = player.getX();
                        double spawnY = player.getY() + 1.0;
                        double spawnZ = player.getZ();
                        double radius = Config.COMMON.network.projectileTrackingRange.get();
                        ParticleOptions data = GunEnchantmentHelper.getParticle(heldItem);
                        S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, player.getId(), data);
                        PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), spawnX, spawnY, spawnZ, radius), messageBulletTrail);
                    }
                }

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));

                double x = player.getX();
                double y = player.getY() + 0.5;
                double z = player.getZ();
                double aggroRadius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.COMMON.aggroMobs.unsilencedRange.get());
                double fleeRadius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.COMMON.fleeingMobs.unsilencedRange.get());

                // Check if the weapon is silenced
                boolean isSilenced = heldItem.getItem() instanceof SilencedFirearm || heldItem.getItem() instanceof AirGunItem || GunModifierHelper.isSilencedFire(heldItem);

                AABB aggroBox = new AABB(x - aggroRadius, y - aggroRadius, z - aggroRadius, x + aggroRadius, y + aggroRadius, z + aggroRadius);
                AABB fleeBox = new AABB(x - fleeRadius, y - fleeRadius, z - fleeRadius, x + fleeRadius, y + fleeRadius, z + fleeRadius);

                double dx, dy, dz;

                // Aggro hostile entities only if the weapon is not silenced
                if (Config.COMMON.aggroMobs.enabled.get() && !isSilenced) {
                    List<LivingEntity> allEntities = world.getEntitiesOfClass(LivingEntity.class, aggroBox);
                    List<LivingEntity> hostileEntities = allEntities.stream().filter(HOSTILE_ENTITIES).toList();
                    for (LivingEntity entity : hostileEntities) {
                        dx = x - entity.getX();
                        dy = y - entity.getY();
                        dz = z - entity.getZ();
                        double distanceSquared = dx * dx + dy * dy + dz * dz;
                        if (distanceSquared <= aggroRadius) {
                            if (entity instanceof ZombifiedPiglin zombifiedPiglin) {
                                zombifiedPiglin.setPersistentAngerTarget(player.getUUID());
                                zombifiedPiglin.setRemainingPersistentAngerTime(400 + world.random.nextInt(400));
                            } else if (entity instanceof Piglin piglin) {
                                piglin.setTarget(player);
                                piglin.setAggressive(true);
                            } else if (entity instanceof EnderMan enderman) {
                                enderman.setTarget(player);
                            } else {
                                entity.setLastHurtByMob(player);
                            }
                        }
                    }
                }
                if (Config.COMMON.fleeingMobs.enabled.get() && !isSilenced) {
                    List<LivingEntity> allEntities = world.getEntitiesOfClass(LivingEntity.class, fleeBox);
                    List<LivingEntity> fleeingEntities = allEntities.stream().filter(FLEEING_ENTITIES).toList();
                    for (LivingEntity entity : fleeingEntities) {
                        dx = x - entity.getX();
                        dy = y - entity.getY();
                        dz = z - entity.getZ();
                        double distanceSquared = dx * dx + dy * dy + dz * dz;
                        if (distanceSquared <= fleeRadius) {
                            if (entity instanceof Mob mob) {
                                mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
                                mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
                                mob.getNavigation().stop();
                                mob.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
                                double speedMultiplier = (entity.getType() == EntityType.VILLAGER) ? 1.3 : 1.0;
                                PanicGoal panicGoal = new PanicGoal((PathfinderMob) mob, speedMultiplier);
                                mob.goalSelector.addGoal(1, panicGoal);
                                panicGoal.start();
                                mob.goalSelector.removeGoal(panicGoal);
                            }
                        }
                    }
                }

                ResourceLocation fireSound = getFireSound(heldItem, modifiedGun);
                if (fireSound != null) {
                    double posX = player.getX();
                    double posY = player.getY() + player.getEyeHeight();
                    double posZ = player.getZ();
                    float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                    float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
                    boolean muzzle = modifiedGun.getDisplay().getFlash() != null;
                    S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, player.getId(), muzzle, false);
                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), posX, posY, posZ, radius), messageSound);
                }

                if (!player.isCreative()) {
                    CompoundTag tag = heldItem.getOrCreateTag();
                    if (!tag.getBoolean("IgnoreAmmo")) {
                        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RECLAIMED.get(), heldItem);
                        if (level == 0 || player.level().random.nextInt(4 - Mth.clamp(level, 1, 2)) != 0) {
                            tag.putInt("AmmoCount", Math.max(0, tag.getInt("AmmoCount") - 1));
                        }
                    }
                }
                player.awardStat(Stats.ITEM_USED.get(item));
            }
        } else {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
        }
    }
    private static void handleBeamWeapon(ServerPlayer player, ItemStack heldItem, Gun modifiedGun) {
        UUID playerId = player.getUUID();
        Level world = player.level();

        // Always get the player's position
        Vec3 playerPos = player.position();

        // Static offset for the beam's origin (relative to player body, tweak as necessary)
        // Adjust the offset to shift it down and flip on the X-axis
        Vec3 beamOriginOffset = new Vec3(-0.1, player.getEyeHeight() - 0.1, 0.0);  // Adjust this as necessary (X = right/left, Y = up/down, Z = forward/back)

        // Rotate the offset based on the player's current yaw and pitch
        Vec3 beamOrigin = rotateOffset(player, beamOriginOffset);

        // Get the direction the player is looking (view direction)
        Vec3 lookVec = player.getLookAngle(); // The direction where the player is looking

        // Maximum distance the beam can travel
        double maxDistance = 50.0;

        // End position of the beam (where it will hit)
        Vec3 endPos = beamOrigin.add(lookVec.scale(maxDistance));

        // Perform the raytrace (ray hits an entity or block)
        HitResult hitResult = world.clip(new ClipContext(beamOrigin, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 hitPos = hitResult.getLocation();

        // Store and update the beam info
        long currentTime = System.currentTimeMillis();
        BeamHandler.BeamInfo beamInfo = activeBeams.computeIfAbsent(playerId, k -> new BeamHandler.BeamInfo(beamOrigin, hitPos, currentTime));
        beamInfo.startPos = beamOrigin;
        beamInfo.endPos = hitPos;

        // Consume ammo every second for continuous beam fire
        if (currentTime - beamInfo.startTime >= 1000) {
            consumeAmmo(player, heldItem, modifiedGun);
            beamInfo.startTime = currentTime;
        }

        // Update the beam and send updates to nearby players
        BeamHandler.updateBeam(playerId, beamOrigin, hitPos);
        PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageBeamUpdate(playerId, beamOrigin, hitPos));

        // Handle any effects caused by the beam (damage to entities, interaction with blocks)
        handleBeamEffects(player, hitResult, modifiedGun);

        // Play sound for firing the beam
        playBeamSound(player, heldItem, modifiedGun, beamOrigin);
    }

    private static Vec3 rotateOffset(ServerPlayer player, Vec3 offset) {
        // Get player's yaw (horizontal rotation) and pitch (vertical rotation)
        float yaw = player.getYRot() * ((float) Math.PI / 180F);  // Convert to radians
        float pitch = player.getXRot() * ((float) Math.PI / 180F);  // Convert to radians

        // Apply yaw (horizontal rotation) to the offset
        double x = offset.x * Math.cos(yaw) - offset.z * Math.sin(yaw);
        double z = offset.x * Math.sin(yaw) + offset.z * Math.cos(yaw);

        // Apply pitch (vertical rotation) to the offset
        double y = offset.y - (pitch != 0.0 ? offset.z * Math.sin(pitch) : 0);

        // Return the rotated offset
        return player.position().add(x, y, z);
    }


    private static void handleBeamEffects(ServerPlayer player, HitResult hitResult, Gun modifiedGun) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity hitEntity = ((EntityHitResult) hitResult).getEntity();
            // Apply damage or effects to the hit entity
            // For example: hitEntity.hurt(DamageSource.playerAttack(player), modifiedGun.getDamage());
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitBlock = ((BlockHitResult) hitResult).getBlockPos();
            // Handle any block interactions or effects
        }
    }

    private static void playBeamSound(ServerPlayer player, ItemStack heldItem, Gun modifiedGun, Vec3 muzzlePos) {
        ResourceLocation fireSound = getFireSound(heldItem, modifiedGun);
        if (fireSound != null) {
            double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
            S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS,
                    (float) muzzlePos.x, (float) muzzlePos.y, (float) muzzlePos.z,
                    GunModifierHelper.getFireSoundVolume(heldItem),
                    0.9F + player.level().random.nextFloat() * 0.2F,
                    player.getId(), false, true);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), muzzlePos.x, muzzlePos.y, muzzlePos.z, radius), messageSound);
        }
    }

    private static void consumeAmmo(ServerPlayer player, ItemStack heldItem, Gun modifiedGun) {
        if (!player.isCreative()) {
            CompoundTag tag = heldItem.getOrCreateTag();
            if (!tag.getBoolean("IgnoreAmmo")) {
                int currentAmmo = tag.getInt("AmmoCount");
                tag.putInt("AmmoCount", Math.max(0, currentAmmo - 1));
            }
        }
    }

    public static void handlePreFireSound(C2SMessagePreFireSound message, ServerPlayer player) {
        Level world = player.level();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.getItem() instanceof GunItem item && (Gun.hasAmmo(heldItem) || player.isCreative()))
        {
            Gun modifiedGun = item.getModifiedGun(heldItem);
            ResourceLocation fireSound = getPreFireSound(heldItem, modifiedGun);
            if(fireSound != null)
            {
                double posX = player.getX();
                double posY = player.getY() + player.getEyeHeight();
                double posZ = player.getZ();
                float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
                S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, player.getId(), false, false);
                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), posX, posY, posZ, radius), messageSound);
            }
        }
    }
    private static ResourceLocation getFireSound(ItemStack stack, Gun modifiedGun)
    {
        ResourceLocation fireSound = null;
        if(GunModifierHelper.isSilencedFire(stack))
        {
            fireSound = modifiedGun.getSounds().getSilencedFire();
        }
        else if(stack.isEnchanted())
        {
            fireSound = modifiedGun.getSounds().getEnchantedFire();
        }
        if(fireSound != null)
        {
            return fireSound;
        }
        return modifiedGun.getSounds().getFire();
    }

    private static ResourceLocation getPreFireSound(ItemStack stack, Gun modifiedGun)
    {
        return modifiedGun.getSounds().getPreFire();
    }

    /**
     * @param player
     */
    public static void handleUnload(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = stack.getTag();
            if (gun.getReloads().getReloadType() != ReloadType.SINGLE_ITEM) {
                if (tag != null && tag.contains("AmmoCount", Tag.TAG_INT)) {
                    int count = tag.getInt("AmmoCount");
                    tag.putInt("AmmoCount", 0);

                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(gun.getProjectile().getItem());
                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    if (item == null) {
                        return;
                    }

                    int maxStackSize = item.getMaxStackSize();
                    int stacks = count / maxStackSize;
                    for (int i = 0; i < stacks; i++) {
                        spawnAmmo(player, new ItemStack(item, maxStackSize));
                    }

                    int remaining = count % maxStackSize;
                    if (remaining > 0) {
                        spawnAmmo(player, new ItemStack(item, remaining));
                    }
                }
            }
        }
    }
    /**
     * @param player
     */
    public static void handleExtraAmmo(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("AmmoCount", Tag.TAG_INT)) {
                int currentAmmo = tag.getInt("AmmoCount");
                int modifiedCapacity = GunModifierHelper.getModifiedAmmoCapacity(stack, gun);

                if (currentAmmo > modifiedCapacity) {
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(gun.getProjectile().getItem());
                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    if (item == null) {
                        return;
                    }
                    int residue = currentAmmo - modifiedCapacity;
                    tag.putInt("AmmoCount", modifiedCapacity);
                    // ScorchedGuns.LOGGER.atInfo().log("Returning " + residue + " excess ammo to player.");
                    spawnAmmo(player, new ItemStack(item, residue));
                }
            }
        }
    }
    /**
     * @param player
     * @param stack
     */
    private static void spawnAmmo(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (stack.getCount() > 0) {
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    /**
     * @param player
     */
    public static void handleAttachments(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, player1) -> new AttachmentContainer(windowId, playerInventory, heldItem), Component.translatable("container.scguns.attachments")));
        }
    }
}