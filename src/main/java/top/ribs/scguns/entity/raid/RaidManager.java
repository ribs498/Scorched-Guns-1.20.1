package top.ribs.scguns.entity.raid;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.config.GunnerMobSpawner;
import top.ribs.scguns.config.RaidConfig;
import top.ribs.scguns.entity.ai.GunAttackGoal;
import top.ribs.scguns.entity.player.PlayerGunProgression;
import top.ribs.scguns.item.GunItem;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaidManager {
    private static final UUID BOSS_HEALTH_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID MOUNT_HEALTH_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    private static final long NIGHT_START = 13000;
    private static final long RAID_SPAWN_TIME = 18000;
    private static final int SAVE_INTERVAL = 100;

    private static final Map<ResourceLocation, RaidManager> INSTANCES = new HashMap<>();

    private UUID currentActiveRaidId = null;
    private static final Map<UUID, ActiveRaid> activeRaids = new HashMap<>();
    private boolean needsRestore = true;

    public static RaidManager get(ServerLevel level) {
        ResourceLocation dimension = level.dimension().location();
        return INSTANCES.computeIfAbsent(dimension, k -> new RaidManager());
    }

    public boolean hasActiveRaid() {
        if (currentActiveRaidId == null) return false;
        ActiveRaid raid = activeRaids.get(currentActiveRaidId);
        return raid != null && raid.isActive();
    }

    public static boolean hasActiveRaidInDimension(ServerLevel level) {
        RaidManager manager = get(level);
        return manager.hasActiveRaid();
    }

    @Nullable
    public ActiveRaid getCurrentActiveRaid() {
        if (currentActiveRaidId == null) return null;
        return activeRaids.get(currentActiveRaidId);
    }
    public static void surrenderRaid(ServerLevel level) {
        RaidManager manager = get(level);
        ActiveRaid raid = manager.getCurrentActiveRaid();

        if (raid != null && raid.isActive()) {
            LivingEntity boss = raid.getBoss();
            if (boss != null && boss.isAlive()) {
                boss.discard();
            }

            Entity mount = raid.getMount();
            if (mount != null && mount.isAlive()) {
                mount.discard();
            }

            for (UUID henchmanUUID : raid.getHenchmenUUIDs()) {
                Entity henchman = level.getEntity(henchmanUUID);
                if (henchman != null && henchman.isAlive()) {
                    henchman.discard();
                }
            }

            raid.announceToNearbyPlayers(
                    Component.translatable("raid.scguns.surrendered")
                            .withStyle(ChatFormatting.YELLOW),
                    64
            );

            if (raid.getBossBar() != null) {
                raid.getBossBar().setVisible(false);
                raid.getBossBar().removeAllPlayers();
            }

            raid.setActive(false);
            manager.currentActiveRaidId = null;
            activeRaids.remove(raid.getRaidId());

            RaidSaveData saveData = RaidSaveData.get(level);
            saveData.removeActiveRaid(raid.getRaidId());
        }
    }
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel == serverLevel.getServer().overworld()) {
                RaidManager manager = get(serverLevel);
                if (manager.needsRestore) {
                    manager.restoreRaidsFromSave(serverLevel);
                    manager.needsRestore = false;
                }
            }
        }
    }

    private void restoreRaidsFromSave(ServerLevel level) {
        RaidSaveData saveData = RaidSaveData.get(level);
        Collection<RaidSaveData.ActiveRaidData> savedRaids = saveData.getActiveRaidData();
        saveData.cleanupInvalidRaids(level);

        for (RaidSaveData.ActiveRaidData data : savedRaids) {
            RaidConfig.RaidData config = RaidConfig.getRaidById(data.configRaidId());

            if (config == null) {
                continue;
            }

            ActiveRaid raid = ActiveRaid.restore(data, config, level);
            activeRaids.put(raid.getRaidId(), raid);

            if (currentActiveRaidId == null && raid.isActive()) {
                currentActiveRaidId = raid.getRaidId();
            }

            raid.updateBossBarPlayers();
        }
    }
    public void startRaidFromPlayer(RaidConfig.RaidData config, ServerLevel level, ServerPlayer player) {
        if (hasActiveRaid()) return;

        Vec3 playerPos = player.position();
        Vec3 spawnPos = findRaidSpawnLocation(level, playerPos);

        if (spawnPos != null) {
            startRaid(config, level, spawnPos);
        }
    }
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        RaidManager manager = get(serverLevel);
        manager.tick(serverLevel);
    }

    public void tick(ServerLevel level) {
        tickActiveRaids(level);

        if (!Config.COMMON.raids.raidsEnabled.get()) return;

        checkForNightlyRaidSpawn(level);

        if (level.getGameTime() % SAVE_INTERVAL == 0) {
            saveActiveRaids(level);
            level.getDataStorage().save();
        }
    }

    private void tickActiveRaids(ServerLevel level) {
        Iterator<Map.Entry<UUID, ActiveRaid>> iterator = activeRaids.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveRaid> entry = iterator.next();
            ActiveRaid raid = entry.getValue();

            if (!raid.isActive()) {
                if (raid.getRaidId().equals(currentActiveRaidId)) {
                    currentActiveRaidId = null;
                }

                RaidSaveData saveData = RaidSaveData.get(level);
                saveData.removeActiveRaid(raid.getRaidId());
                iterator.remove();
                continue;
            }

            raid.tick();

            if (raid.shouldSpawnHenchmen()) {
                spawnHenchmen(raid, level);
                raid.resetSpawnTimer();
            }
        }
    }

    private void saveActiveRaids(ServerLevel level) {
        RaidSaveData saveData = RaidSaveData.get(level);
        for (ActiveRaid raid : activeRaids.values()) {
            if (raid.isActive()) {
                saveData.saveActiveRaid(raid);
            }
        }
    }

    private void checkForNightlyRaidSpawn(ServerLevel level) {
        if (level.dimensionType().hasFixedTime()) return;

        ResourceLocation dimension = level.dimension().location();
        long dayTime = level.getDayTime() % 24000;
        long currentDay = level.getDayTime() / 24000;

        RaidSaveData saveData = RaidSaveData.get(level);

        if (dayTime >= NIGHT_START && dayTime < NIGHT_START + 20) {

            if (hasActiveRaid()) {
                return;
            }

            RaidSaveData.ScheduledRaidData scheduled = saveData.getScheduledRaid(dimension);

            if (scheduled != null && scheduled.scheduledDay() < currentDay) {

                saveData.removeScheduledRaid(dimension);
                scheduled = null;
            }

            if (scheduled != null) {

                return;
            }

            if (!Config.COMMON.raids.raidsEnabled.get()) {
                return;
            }

            if (dayTime == NIGHT_START) {
                float raidChance = Config.COMMON.raids.nightlyRaidChance.get().floatValue();
                float roll = level.random.nextFloat();

                if (roll < raidChance) {
                    scheduleRaidForTonight(level, dimension, currentDay, saveData);
                }
            }
        }

        if (dayTime >= RAID_SPAWN_TIME && dayTime < RAID_SPAWN_TIME + 20) {


            if (hasActiveRaid()) {
                return;
            }

            RaidSaveData.ScheduledRaidData scheduled = saveData.getScheduledRaid(dimension);

            if (scheduled == null) {
                return;
            }
            if (scheduled.scheduledDay() != currentDay) {
                saveData.removeScheduledRaid(dimension);
                return;
            }

            ServerPlayer player = level.getServer().getPlayerList().getPlayer(scheduled.targetPlayerUUID());

            if (player == null || player.isRemoved() || player.isSpectator()) {
                saveData.removeScheduledRaid(dimension);
                return;
            }

            Vec3 playerPos = player.position();
            Vec3 spawnPos = findRaidSpawnLocation(level, playerPos);

            if (spawnPos == null) {
                return;
            }

            RaidConfig.RaidData config = RaidConfig.getRaidById(scheduled.raidId());
            if (config == null) {
                saveData.removeScheduledRaid(dimension);
                return;
            }
            startRaid(config, level, spawnPos);
            saveData.removeScheduledRaid(dimension);
        }
    }

    private void scheduleRaidForTonight(ServerLevel level, ResourceLocation dimension, long currentDay, RaidSaveData saveData) {
        if (hasActiveRaid()) {
            return;
        }

        List<ServerPlayer> validPlayers = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && !player.isCreative()) {
                validPlayers.add(player);
            }
        }

        if (validPlayers.isEmpty()) {
            return;
        }

        ServerPlayer targetPlayer = validPlayers.get(level.random.nextInt(validPlayers.size()));
        PlayerGunProgression progression = PlayerGunProgression.get(targetPlayer);
        int raidLevel = progression.getCurrentRaidLevel();

        if (raidLevel == 0) {
            return;
        }

        RaidConfig.RaidData selectedRaid = selectRaidForLevel(raidLevel, level.random);

        if (selectedRaid == null) {
            return;
        }
        saveData.scheduleRaid(dimension, targetPlayer, selectedRaid.raidId(), currentDay);

        targetPlayer.sendSystemMessage(Component.translatable("raid.scguns.warning"));
    }

    @Nullable
    private RaidConfig.RaidData selectRaidForLevel(int playerRaidLevel, net.minecraft.util.RandomSource random) {
        List<RaidConfig.RaidData> availableRaids = RaidConfig.getRaidsForLevel(playerRaidLevel);

        if (availableRaids.isEmpty()) return null;
        if (availableRaids.size() == 1) return availableRaids.get(0);

        List<RaidConfig.RaidData> highestLevelRaids = RaidConfig.getRaidsAtLevel(playerRaidLevel);

        float roll = random.nextFloat();

        if (!highestLevelRaids.isEmpty() && roll < 0.60f) {
            return highestLevelRaids.get(random.nextInt(highestLevelRaids.size()));
        } else {
            return availableRaids.get(random.nextInt(availableRaids.size()));
        }
    }

    @Nullable
    private Vec3 findRaidSpawnLocation(ServerLevel level, Vec3 center) {
        net.minecraft.util.RandomSource random = level.getRandom();

        int playerY = (int)center.y;
        boolean isUnderground = playerY < 50;

        for (int attempt = 0; attempt < 15; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 25 + random.nextDouble() * (35 - 20);

            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;

            BlockPos pos = new BlockPos((int)x, playerY, (int)z);

            BlockPos groundPos;
            if (isUnderground) {
                groundPos = findNearestValidCaveSpawn(level, pos, playerY);
                if (groundPos == null) continue;
            } else {
                groundPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            }

            if (level.getBlockState(groundPos.below()).isSolid() &&
                    level.getBlockState(groundPos).isAir() &&
                    level.getBlockState(groundPos.above()).isAir() &&
                    level.getBlockState(groundPos.above(2)).isAir()) {
                return new Vec3(groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() + 0.5);
            }
        }

        return null;
    }

    @Nullable
    private BlockPos findNearestValidCaveSpawn(ServerLevel level, BlockPos center, int playerY) {
        for (int yOffset = -5; yOffset <= 5; yOffset++) {
            BlockPos checkPos = new BlockPos(center.getX(), playerY + yOffset, center.getZ());

            if (level.getBlockState(checkPos.below()).isSolid() &&
                    level.getBlockState(checkPos).isAir() &&
                    level.getBlockState(checkPos.above()).isAir() &&
                    level.getBlockState(checkPos.above(2)).isAir()) {

                int airCount = 0;
                for (int i = 0; i < 4; i++) {
                    if (level.getBlockState(checkPos.above(i)).isAir()) {
                        airCount++;
                    }
                }

                if (airCount >= 3) {
                    return checkPos;
                }
            }
        }
        return null;
    }

    public void startRaid(RaidConfig.RaidData config, ServerLevel level, Vec3 spawnPos) {
        if (hasActiveRaid()) return;

        ServerPlayer targetPlayer = findNearestPlayer(level, spawnPos);
        long startTime = level.getGameTime();

        Integer raidLevel = config.raidLevel();

        ActiveRaid raid = new ActiveRaid(raidLevel, config, level, spawnPos, startTime);

        if (targetPlayer != null) {
            raid.setTargetPlayer(targetPlayer.getUUID());
        }

        Mob boss = spawnBoss(raid, level, spawnPos);
        if (boss == null) return;

        raid.setBossUUID(boss.getUUID());

        if (targetPlayer != null && boss instanceof PathfinderMob pathfinder) {
            pathfinder.setTarget(targetPlayer);
        }

        if (config.boss().mount() != null) {
            Entity mount = boss.getVehicle();
            if (mount != null) {
                raid.setMountUUID(mount.getUUID());
            }
        }

        raid.setBossConfirmed(true);
        activeRaids.put(raid.getRaidId(), raid);
        currentActiveRaidId = raid.getRaidId();

        String announcement = config.spawnConditions().announcementMessage();
        Component announcementComponent;

        if (announcement.startsWith("translation:")) {
            String translationKey = announcement.substring(12);
            announcementComponent = Component.translatable(translationKey);
        } else {
            announcementComponent = Component.literal(announcement);
        }

        raid.announceToNearbyPlayers(announcementComponent, config.spawnConditions().searchRadius());
        spawnHenchmen(raid, level);
        raid.resetSpawnTimer();

        RaidSaveData saveData = RaidSaveData.get(level);
        saveData.saveActiveRaid(raid);

    }

    @Nullable
    private ServerPlayer findNearestPlayer(ServerLevel level, Vec3 pos) {
        ServerPlayer nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) continue;

            double dist = player.position().distanceTo(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    @Nullable
    private Mob spawnBoss(ActiveRaid raid, ServerLevel level, Vec3 spawnPos) {
        RaidConfig.BossData bossData = raid.getConfig().boss();

        EntityType<?> entityType = bossData.entityType();
        if (!(entityType.create(level) instanceof Mob boss)) return null;

        boss.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        if (bossData.customName() != null) {
            Component nameComponent;
            if (bossData.customName().startsWith("translation:")) {
                String translationKey = bossData.customName().substring(12);
                nameComponent = Component.translatable(translationKey);
            } else {
                nameComponent = Component.literal(bossData.customName());
            }
            boss.setCustomName(nameComponent);
            boss.setCustomNameVisible(true);
        }

        applyHealthConfig(boss, bossData.healthConfig(), BOSS_HEALTH_MODIFIER_UUID);
        applyEffects(boss, bossData.effects());

        if (bossData.weapon() != null) {
            ItemStack weaponStack = createModifiedGun(boss, bossData.weapon().item());

            if (bossData.weapon().nbt() != null) {
                CompoundTag existingTag = weaponStack.getOrCreateTag();
                existingTag.merge(bossData.weapon().nbt());
            }

            boss.setItemSlot(EquipmentSlot.MAINHAND, weaponStack);
            boss.setDropChance(EquipmentSlot.MAINHAND, bossData.weapon().dropChance());
        }

        for (RaidConfig.ArmorEntry armorEntry : bossData.armor()) {
            EquipmentSlot slot = switch (armorEntry.slot()) {
                case "head" -> EquipmentSlot.HEAD;
                case "chest" -> EquipmentSlot.CHEST;
                case "legs" -> EquipmentSlot.LEGS;
                case "feet" -> EquipmentSlot.FEET;
                default -> null;
            };

            if (slot != null) {
                ItemStack armorStack = new ItemStack(armorEntry.item());

                if (armorEntry.nbt() != null) {
                    armorStack.setTag(armorEntry.nbt().copy());
                }

                boss.setItemSlot(slot, armorStack);
                boss.setDropChance(slot, armorEntry.dropChance());
            }
        }

        boss.addTag("RaidBoss");
        boss.addTag("RaidMember_" + raid.getRaidId());
        boss.addTag("MobGunner");
        boss.addTag("AI_" + bossData.aiType().name());
        boss.setPersistenceRequired();

        if (boss instanceof PathfinderMob pathfinderBoss) {
            ItemStack heldItem = boss.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                pathfinderBoss.goalSelector.addGoal(2, new GunAttackGoal<>(
                        pathfinderBoss,
                        heldItem,
                        1.2F,
                        bossData.aiType(),
                        bossData.aiDifficulty()
                ));
            }
            GunnerMobSpawner.extendFollowRange(pathfinderBoss);

            if (boss instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin abstractPiglin) {
                abstractPiglin.setImmuneToZombification(true);

                if (boss instanceof net.minecraft.world.entity.monster.piglin.Piglin piglin) {
                    piglin.setAggressive(true);
                }

                ServerPlayer targetPlayer = findNearestPlayer(level, spawnPos);
                if (targetPlayer != null) {
                    try {
                        var brain = abstractPiglin.getBrain();
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, targetPlayer.getUUID());
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER, true);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, targetPlayer);
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER, targetPlayer);

                        abstractPiglin.setTarget(targetPlayer);
                        abstractPiglin.setLastHurtByMob(targetPlayer);
                    } catch (Exception e) {
                        abstractPiglin.setTarget(targetPlayer);
                    }
                }
            }
        }

        level.addFreshEntity(boss);

        RaidConfig.MountData mountData = bossData.mount();
        if (mountData != null) {
            Mob mount = spawnMount(raid, mountData, level, spawnPos);
            if (mount != null) {
                boss.startRiding(mount, true);
            }
        }

        return boss;
    }
    @Nullable
    private Mob spawnMount(ActiveRaid raid, RaidConfig.MountData mountData, ServerLevel level, Vec3 spawnPos) {
        EntityType<?> mountType = mountData.entityType();
        if (!(mountType.create(level) instanceof Mob mount)) return null;

        mount.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        applyHealthConfig(mount, mountData.healthConfig(), MOUNT_HEALTH_MODIFIER_UUID);
        applyEffects(mount, mountData.effects());

        for (RaidConfig.ArmorEntry armorEntry : mountData.armor()) {
            EquipmentSlot slot = switch (armorEntry.slot()) {
                case "head" -> EquipmentSlot.HEAD;
                case "chest" -> EquipmentSlot.CHEST;
                case "legs" -> EquipmentSlot.LEGS;
                case "feet" -> EquipmentSlot.FEET;
                default -> null;
            };

            if (slot != null) {
                ItemStack armorStack = new ItemStack(armorEntry.item());

                if (armorEntry.nbt() != null) {
                    armorStack.setTag(armorEntry.nbt().copy());
                }

                mount.setItemSlot(slot, armorStack);
                mount.setDropChance(slot, armorEntry.dropChance());
            }
        }

        mount.addTag("RaidMount");
        mount.addTag("RaidMember_" + raid.getRaidId());
        mount.addTag("MobGunner");
        mount.setPersistenceRequired();

        if (!mountData.mountDropsLoot()) {
            mount.addTag("NoLootDrop");
        }

        level.addFreshEntity(mount);

        return mount;
    }

    private void spawnHenchmen(ActiveRaid raid, ServerLevel level) {
        RaidConfig.HenchmenData henchmenData = raid.getConfig().henchmen();
        LivingEntity boss = raid.getBoss();

        if (boss == null || !boss.isAlive()) return;

        Vec3 bossPos = boss.position();

        for (int i = 0; i < henchmenData.spawnAttemptsPerWave(); i++) {
            if (!raid.canSpawnMoreHenchmen()) break;

            RaidConfig.HenchmanType type = henchmenData.selectRandomType(level.getRandom());
            if (type == null) continue;

            Vec3 spawnPos = findHenchmanSpawnPos(level, bossPos, henchmenData.spawnRadius());
            if (spawnPos == null) continue;

            Mob henchman = spawnHenchman(raid, type, level, spawnPos);
            if (henchman != null) {
                raid.addHenchman(henchman.getUUID());
            }
        }
    }

    @Nullable
    private Vec3 findHenchmanSpawnPos(ServerLevel level, Vec3 center, int radius) {
        net.minecraft.util.RandomSource random = level.getRandom();

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * radius;

            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;

            BlockPos pos = new BlockPos((int)x, (int)center.y, (int)z);
            BlockPos groundPos = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);

            if (level.getBlockState(groundPos.below()).isSolid() &&
                    level.getBlockState(groundPos).isAir() &&
                    level.getBlockState(groundPos.above()).isAir()) {
                return new Vec3(groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() + 0.5);
            }
        }

        return null;
    }

    private Mob spawnHenchman(ActiveRaid raid, RaidConfig.HenchmanType type, ServerLevel level, Vec3 spawnPos) {
        EntityType<?> entityType = type.entityType();
        if (!(entityType.create(level) instanceof Mob henchman)) return null;

        henchman.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        applyHealthConfig(henchman, type.healthConfig(), UUID.randomUUID());
        applyEffects(henchman, type.effects());

        henchman.addTag("RaidHenchman");
        henchman.addTag("RaidMember_" + raid.getRaidId());
        henchman.addTag("AI_" + type.aiType().name());
        henchman.setPersistenceRequired();

        if (!type.weapons().isEmpty()) {
            Item weaponItem = type.weapons().get(level.random.nextInt(type.weapons().size()));
            ItemStack weaponStack = createModifiedGun(henchman, weaponItem);
            henchman.setItemSlot(EquipmentSlot.MAINHAND, weaponStack);
            henchman.setDropChance(EquipmentSlot.MAINHAND, 0.05f);
        }

        henchman.addTag("MobGunner");


        for (RaidConfig.ArmorEntry armorEntry : type.armor()) {
            if (level.random.nextFloat() > armorEntry.dropChance()) continue;

            EquipmentSlot slot = switch (armorEntry.slot()) {
                case "head" -> EquipmentSlot.HEAD;
                case "chest" -> EquipmentSlot.CHEST;
                case "legs" -> EquipmentSlot.LEGS;
                case "feet" -> EquipmentSlot.FEET;
                default -> null;
            };

            if (slot != null) {
                ItemStack armorStack = new ItemStack(armorEntry.item());
                if (armorEntry.nbt() != null) {
                    armorStack.setTag(armorEntry.nbt().copy());
                }

                henchman.setItemSlot(slot, armorStack);
                henchman.setDropChance(slot, 0.05f);
            }
        }

        if (henchman instanceof PathfinderMob pathfinderMob) {
            ItemStack heldItem = henchman.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                pathfinderMob.goalSelector.addGoal(2, new GunAttackGoal<>(
                        pathfinderMob,
                        heldItem,
                        1.2F,
                        type.aiType(),
                        type.aiDifficulty()
                ));
            }
            GunnerMobSpawner.extendFollowRange(pathfinderMob);

            ServerPlayer targetPlayer = raid.getTargetPlayer(level);
            if (targetPlayer != null) {
                pathfinderMob.setTarget(targetPlayer);
            }

            if (henchman instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin abstractPiglin) {
                abstractPiglin.setImmuneToZombification(true);

                if (henchman instanceof net.minecraft.world.entity.monster.piglin.Piglin piglin) {
                    piglin.setAggressive(true);
                }

                if (targetPlayer != null) {
                    try {
                        var brain = abstractPiglin.getBrain();
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT, targetPlayer.getUUID());
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.UNIVERSAL_ANGER, true);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, targetPlayer);
                        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.NEAREST_VISIBLE_PLAYER, targetPlayer);

                        abstractPiglin.setTarget(targetPlayer);
                        abstractPiglin.setLastHurtByMob(targetPlayer);
                    } catch (Exception e) {
                        abstractPiglin.setTarget(targetPlayer);
                    }
                }
            }
        }

        level.addFreshEntity(henchman);

        return henchman;
    }

    private ItemStack createModifiedGun(Mob mob, Item gun) {
        ItemStack gunStack = new ItemStack(gun);
        if (gun instanceof GunItem gunItem && gunStack.getTag() != null) {
            Gun gunModified = gunItem.getModifiedGun(gunStack);
            gunStack.getTag().putInt("AmmoCount", mob.getRandom().nextInt(gunModified.getReloads().getMaxAmmo()));
        }
        return gunStack;
    }

    private void applyHealthConfig(Mob mob, RaidConfig.HealthConfig healthConfig, UUID modifierUUID) {
        AttributeInstance healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null) return;

        if (healthConfig.useMultiplier()) {
            healthAttr.addPermanentModifier(new AttributeModifier(
                    modifierUUID,
                    "Raid health multiplier",
                    healthConfig.healthMultiplier() - 1.0,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        } else {
            double currentHealth = healthAttr.getBaseValue();
            healthAttr.addPermanentModifier(new AttributeModifier(
                    modifierUUID,
                    "Raid fixed health",
                    healthConfig.fixedHealth() - currentHealth,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        mob.setHealth(mob.getMaxHealth());
    }

    private void applyEffects(Mob mob, List<RaidConfig.EffectEntry> effects) {
        for (RaidConfig.EffectEntry effectEntry : effects) {
            mob.addEffect(new MobEffectInstance(
                    effectEntry.effect(),
                    effectEntry.duration(),
                    effectEntry.amplifier(),
                    effectEntry.ambient(),
                    effectEntry.visible()
            ));
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob)) return;

        RaidManager manager = get(level);

        for (ActiveRaid raid : manager.activeRaids.values()) {
            if (mob.getUUID().equals(raid.getBossUUID())) {
                RaidConfig.BossData bossData = raid.getConfig().boss();

                if (bossData.specialLootTable() != null) {
                    manager.dropSpecialLoot(mob, bossData.specialLootTable(), level);
                }

                raid.onBossDefeated();
                break;
            }

            if (raid.getHenchmenUUIDs().contains(mob.getUUID())) {
                raid.removeHenchman(mob.getUUID());
                break;
            }
        }
    }

    private void dropSpecialLoot(Mob boss, ResourceLocation lootTableLocation, ServerLevel level) {
        net.minecraft.world.level.storage.loot.LootTable lootTable = level.getServer()
                .getLootData()
                .getLootTable(lootTableLocation);

        net.minecraft.world.level.storage.loot.LootParams.Builder builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, boss)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, boss.position())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,
                        boss.getLastDamageSource() != null ? boss.getLastDamageSource() : level.damageSources().generic());

        if (boss.getKillCredit() instanceof net.minecraft.world.entity.player.Player player) {
            builder.withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.KILLER_ENTITY, player)
                    .withLuck(player.getLuck());
        }

        net.minecraft.world.level.storage.loot.LootParams params = builder.create(
                net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);

        lootTable.getRandomItems(params).forEach(itemStack -> {
            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level, boss.getX(), boss.getY(), boss.getZ(), itemStack);
            level.addFreshEntity(itemEntity);
        });
    }

    public Collection<ActiveRaid> getActiveRaids() {
        return activeRaids.values();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        RaidManager manager = get(overworld);

        manager.saveActiveRaids(overworld);
        RaidSaveData.get(overworld);
        overworld.getDataStorage().save();

        INSTANCES.clear();
    }
}