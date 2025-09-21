package top.ribs.scguns.faction.raid;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.faction.Faction;
import top.ribs.scguns.faction.GunnerManager;
import top.ribs.scguns.init.ModCommands;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModParticleTypes;
import top.ribs.scguns.init.ModTags;

import java.util.HashSet;
import java.util.List;


public class RaidEntity extends Entity {
    private final ServerBossEvent bossBar;
    private final HashSet<LivingEntity> activeMobs = new HashSet<>();
    private final HashSet<LivingEntity> spawnedMobs = new HashSet<>();
    private final HashSet<Player> activePlayers = new HashSet<>();
    private Faction faction;
    private boolean forceGuns = true;
    private int totalWaves = 3;
    private int maxCooldown = 200;
    private int maxBreakTime = 100;

    private boolean inWave = false;
    private boolean justFinishedWave = false;
    private boolean spawningWave = false;
    private boolean isSpawningMobs = false;
    private int currentWave = 0;
    private boolean isFinished = false;
    private int cooldown = maxCooldown;
    private int breakTime = maxBreakTime;
    private int spawnInterval = 60;
    private int spawnCooldown = spawnInterval;
    private boolean victory = false;
    private boolean defeat = false;
    private int despawnTicks = MAX_DESPAWN_TICKS;
    private boolean result = true;
    private int resultPrize = 1;

    private static final int MAX_ACTIVE_MOBS = 10;
    private static int TOTAL_WAVE_MOBS = 20;
    private static final int MAX_DESPAWN_TICKS = 600;

    private static final int ACTIVE_RADIUS = 64;

    public RaidEntity(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);

        GunnerManager gunnerManager = GunnerManager.getInstance();
        this.faction = gunnerManager.getFactionByName(gunnerManager.getRandomFactionName());

        MutableComponent factionLang = Component.translatable("faction.scguns." + this.faction.getName());
        this.bossBar = new ServerBossEvent(
                Component.translatable(factionLang.getString() + " active raid!"),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossBar.setVisible(true);
    }

    public RaidEntity(EntityType<? extends Entity> type, Level level, Faction faction, boolean forceGuns, int waves, int waveSize) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);

        this.faction = faction;
        this.forceGuns = forceGuns;
        this.totalWaves = waves;

        MutableComponent factionLang = Component.translatable("faction.jeg." + faction.getName());
        MutableComponent raidLang = Component.translatable("raid.jeg");
        this.bossBar = new ServerBossEvent(
                Component.translatable(factionLang.getString() + " " + raidLang.getString()),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossBar.setVisible(true);
    }

    private static ItemStack getRandomHorseArmor(RandomSource random) {
        Item[] horseArmors = {
                Items.IRON_HORSE_ARMOR,
                Items.GOLDEN_HORSE_ARMOR,
                Items.DIAMOND_HORSE_ARMOR
        };
        return new ItemStack(horseArmors[random.nextInt(horseArmors.length)]);
    }

    public void spawnMobs(ServerLevel level, Vec3 startPos, boolean forceGuns, int spread) {
        BlockPos.MutableBlockPos spawnPos = this.blockPosition().mutable()
                .move((12 + random.nextInt(12)) * (random.nextBoolean() ? -1 : 1),
                        0,
                        (12 + random.nextInt(12)) * (random.nextBoolean() ? -1 : 1));

        if (level.hasChunksAt(spawnPos.getX() - 10, spawnPos.getZ() - 10, spawnPos.getX() + 10, spawnPos.getZ() + 10)) {
            if (activeMobs.size() < MAX_ACTIVE_MOBS && spawnedMobs.size() < TOTAL_WAVE_MOBS) {
                int mobsToSpawn = this.random.nextInt(2, 3);
                for (int i = 0; i < mobsToSpawn; i++) {
                    if (activeMobs.size() >= MAX_ACTIVE_MOBS) {
                        break;
                    }
                    LivingEntity mob = ModCommands.getFactionMob(level, faction, startPos, forceGuns, spread);
                    if (mob != null) {
                        if (ModCommands.spawnRaider(level, mob, null, spawnPos, this.position(), true)) {
                            activeMobs.add(mob);
                            spawnedMobs.add(mob);
                            if (!this.activePlayers.isEmpty() && mob instanceof PathfinderMob pathfinderMob) {
                                pathfinderMob.setTarget(this.activePlayers.stream().findAny().get());
                            }

                            // Horsemen!
                            if (mob.getTags().contains("EliteGunner") && level.random.nextBoolean() && Config.COMMON.gunnerMobs.horsemen.get()) {
                                if (mob instanceof Zombie) {
                                    ZombieHorse zombieHorse = new ZombieHorse(EntityType.ZOMBIE_HORSE, this.level());
                                    zombieHorse.setPos(mob.position());
                                    zombieHorse.addTag("GunnerPatroller");

                                    if (level.random.nextInt(3) == 0) {
                                        ItemStack randomHorseArmor = getRandomHorseArmor(level.random);
                                        zombieHorse.setItemSlot(EquipmentSlot.CHEST, randomHorseArmor);
                                    }

                                    this.level().addFreshEntity(zombieHorse);
                                    zombieHorse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400));
                                    mob.startRiding(zombieHorse);
                                } else if (mob instanceof AbstractSkeleton) {
                                    SkeletonHorse skeletonHorse = new SkeletonHorse(EntityType.SKELETON_HORSE, this.level());
                                    skeletonHorse.setPos(mob.position());
                                    skeletonHorse.addTag("GunnerPatroller");

                                    if (level.random.nextInt(3) == 0) {
                                        ItemStack randomHorseArmor = getRandomHorseArmor(level.random);
                                        skeletonHorse.setItemSlot(EquipmentSlot.CHEST, randomHorseArmor);
                                    }

                                    this.level().addFreshEntity(skeletonHorse);
                                    skeletonHorse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400));
                                    mob.startRiding(skeletonHorse);
                                } else {
                                    Horse horse = new Horse(EntityType.HORSE, this.level());
                                    horse.setPos(mob.position());
                                    horse.addTag("GunnerPatroller");

                                    if (level.random.nextInt(3) == 0) {
                                        ItemStack randomHorseArmor = getRandomHorseArmor(level.random);
                                        horse.setItemSlot(EquipmentSlot.CHEST, randomHorseArmor);
                                    }

                                    this.level().addFreshEntity(horse);
                                    mob.startRiding(horse);
                                }
                            }

                            // Reinforcements!
                            if (Config.COMMON.gunnerMobs.explosiveMobs.get()) {
                                if (this.level().random.nextFloat() < 0.2) {
                                    if (this.level().random.nextBoolean()) {
                                        Creeper annoyingBoy = new Creeper(EntityType.CREEPER, this.level());
                                        annoyingBoy.setPos(mob.position());
                                        activeMobs.add(annoyingBoy);
                                        spawnedMobs.add(annoyingBoy);
                                        annoyingBoy.addTag("GunnerPatroller");
                                        this.level().addFreshEntity(annoyingBoy);
                                        if (!this.activePlayers.isEmpty()) {
                                            annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                        }
                                    } else {
                                        if (this.level().random.nextBoolean()) {
                                            Phantom annoyingBoy = new Phantom(EntityType.PHANTOM, this.level());
                                            annoyingBoy.setPos(mob.position());
                                            activeMobs.add(annoyingBoy);
                                            spawnedMobs.add(annoyingBoy);
                                            annoyingBoy.addTag("GunnerPatroller");
                                            this.level().addFreshEntity(annoyingBoy);

                                            if (!this.activePlayers.isEmpty()) {
                                                annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                            }
                                        } else {
                                            Silverfish annoyingBoy = new Silverfish(EntityType.SILVERFISH, this.level());
                                            annoyingBoy.setPos(mob.position());
                                            annoyingBoy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
                                            activeMobs.add(annoyingBoy);
                                            spawnedMobs.add(annoyingBoy);
                                            annoyingBoy.addTag("GunnerPatroller");
                                            this.level().addFreshEntity(annoyingBoy);

                                            if (!this.activePlayers.isEmpty()) {
                                                annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                            }
                                        }
                                    }
                                }
                            }
                            if (Config.COMMON.gunnerMobs.raidSupportMobs.get()) {
                                if (this.level().random.nextFloat() < 0.3) {
                                    if (mob.getType().is(ModTags.Entities.UNDEAD)) {
                                        Phantom annoyingBoy = new Phantom(EntityType.PHANTOM, this.level());
                                        annoyingBoy.setPos(mob.position().add(0, 10, 0));
                                        activeMobs.add(annoyingBoy);
                                        spawnedMobs.add(annoyingBoy);
                                        annoyingBoy.addTag("GunnerPatroller");
                                        if (level.isDay() && annoyingBoy.getType().is(ModTags.Entities.UNDEAD)) {
                                            annoyingBoy.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0, false, true));
                                            annoyingBoy.extinguishFire();
                                        }
                                        this.level().addFreshEntity(annoyingBoy);
                                        if (!this.activePlayers.isEmpty()) {
                                            annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                        }
                                    }
                                }
                                if (this.level().random.nextFloat() < 0.2) {
                                    if (mob instanceof AbstractIllager) {
                                        if (this.level().random.nextBoolean()) {
                                            Witch annoyingBoy = new Witch(EntityType.WITCH, this.level());
                                            annoyingBoy.setPos(mob.position());
                                            activeMobs.add(annoyingBoy);
                                            spawnedMobs.add(annoyingBoy);
                                            annoyingBoy.addTag("GunnerPatroller");
                                            this.level().addFreshEntity(annoyingBoy);
                                            if (!this.activePlayers.isEmpty()) {
                                                annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                            }
                                        } else {
                                            Evoker annoyingBoy = new Evoker(EntityType.EVOKER, this.level());
                                            annoyingBoy.setPos(mob.position());
                                            activeMobs.add(annoyingBoy);
                                            spawnedMobs.add(annoyingBoy);
                                            annoyingBoy.addTag("GunnerPatroller");
                                            this.level().addFreshEntity(annoyingBoy);
                                            if (!this.activePlayers.isEmpty()) {
                                                annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeMobs.size() >= MAX_ACTIVE_MOBS || spawnedMobs.size() >= TOTAL_WAVE_MOBS) {
            this.isSpawningMobs = false;
        }
    }

    public void waveIterator() {
        if (this.currentWave < this.totalWaves) {
            for (Player player : this.activePlayers) {
                this.bossBar.addPlayer((ServerPlayer) player);
            }

            //startWave((ServerLevel) this.level(), faction, this.position(), true, 10, 5);
            this.inWave = true;
            this.isSpawningMobs = true;
            this.currentWave++;
            this.playHorn();
        }
    }

    public void playHorn () {
        int x = this.level().random.nextInt(-25, 25);
        int z = this.level().random.nextInt(-25, 25);
        this.level().playSound(null, BlockPos.containing(this.position().add(x, 32, z)), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).get(), SoundSource.HOSTILE, 1000F, 1);
    }

    public void playCelebrationHorn () {
        int x = this.level().random.nextInt(-25, 25);
        int z = this.level().random.nextInt(-25, 25);
        SoundEvent sound;
        if (this.level().random.nextBoolean()) {
            sound = SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).get();
        } else {
            sound = SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1).get();
        }
        this.level().playSound(null, BlockPos.containing(this.position().add(x, 32, z)), sound, SoundSource.HOSTILE, 1000F, 1);
    }

    public boolean isWaveComplete() {
        if (this.spawningWave) {
            return false;
        }

        return activeMobs.isEmpty() && spawnedMobs.size() >= TOTAL_WAVE_MOBS;
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean isInWave() {
        return this.inWave;
    }

    public int getCurrentWave() {
        return this.currentWave;
    }

    public HashSet<LivingEntity> getActiveMobs() {
        return activeMobs;
    }

    public HashSet<Player> getActivePlayers() {
        return activePlayers;
    }

    private void updateBossBar() {
        if (this.isFinished) {
            this.bossBar.setVisible(false);
            this.bossBar.removeAllPlayers();
            return;
        }

        int remainingMobs = TOTAL_WAVE_MOBS - spawnedMobs.size();

        float progress = (float) remainingMobs / TOTAL_WAVE_MOBS;
        this.bossBar.setProgress(progress);
        MutableComponent factionLang = Component.translatable("faction.jeg." + faction.getName());
        MutableComponent raidLang = Component.translatable("raid.jeg");
        MutableComponent waveLang = Component.translatable("raid.jeg.wave");
        this.bossBar.setName(Component.translatable(factionLang.getString() + " " + raidLang.getString() + " | " + waveLang.getString() + " : " + this.currentWave + "/" + this.totalWaves));
    }

    private void updatePlayers() {
        AABB playerBox = this.getBoundingBox();
        AABB inflatedBox = playerBox.inflate(ACTIVE_RADIUS, ACTIVE_RADIUS, ACTIVE_RADIUS);
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, inflatedBox);

        for (Player player : nearbyPlayers) {
            if (!activePlayers.contains(player)) {
                activePlayers.add(player);
                System.out.println("Player entered: " + player.getName().getString());
            }
        }

        activePlayers.removeIf(player -> !nearbyPlayers.contains(player) || player.isDeadOrDying());
    }

    private void waveReward(Player player, ServerLevel serverLevel) {
        ResourceLocation lootTableID;
        lootTableID = new ResourceLocation(Reference.MOD_ID, "factions/raids/generic_raid_wave_reward");
        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableID);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.CHEST);

        // Roll the loot table 1 time
        for (int i = 0; i < 1; i++) {
            List<ItemStack> loot = lootTable.getRandomItems(lootParams);

            for (ItemStack itemStack : loot) {
                if (!player.getInventory().add(itemStack)) {
                    player.drop(itemStack, false);
                }
            }
        }
    }

    private void victoryReward(Player player, ServerLevel serverLevel) {
        ResourceLocation lootTableID;
        lootTableID = new ResourceLocation(Reference.MOD_ID, "factions/raids/generic_raid_victory_reward");
        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableID);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.CHEST);

        // Roll the loot table 1 time
        for (int i = 0; i < 1; i++) {
            List<ItemStack> loot = lootTable.getRandomItems(lootParams);

            for (ItemStack itemStack : loot) {
                if (!player.getInventory().add(itemStack)) {
                    player.drop(itemStack, false);
                }
            }
        }

        ResourceLocation lootTableID2;
        lootTableID2 = new ResourceLocation(Reference.MOD_ID, "factions/raids/" + this.faction.getName() + "/victory_reward");
        LootTable lootTable2 = serverLevel.getServer().getLootData().getLootTable(lootTableID2);
        LootParams lootParams2 = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.CHEST);

        // Roll the loot table 1 time
        for (int i = 0; i < 1; i++) {
            List<ItemStack> loot = lootTable2.getRandomItems(lootParams2);

            for (ItemStack itemStack : loot) {
                if (!player.getInventory().add(itemStack)) {
                    player.drop(itemStack, false);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (!this.isFinished) {
                summonParticleRing();
            }
        }

        if (!this.level().isClientSide) {
            activeMobs.removeIf(mob -> mob.isRemoved() || !mob.isAlive() || mob.isDeadOrDying());
            updateBossBar();
            updatePlayers();

            /*JustEnoughGuns.LOGGER.atInfo().log("isFinished: " + isFinished);
            JustEnoughGuns.LOGGER.atInfo().log("isSpawningMobs: " + isSpawningMobs);
            JustEnoughGuns.LOGGER.atInfo().log("inWave: " + inWave);
            JustEnoughGuns.LOGGER.atInfo().log("spawningWave: " + spawningWave);
            JustEnoughGuns.LOGGER.atInfo().log("activeMobs: " + activeMobs.size());
            JustEnoughGuns.LOGGER.atInfo().log("spawnedMobs: " + spawnedMobs.size());*/

            if (this.getActivePlayers().isEmpty() || this.level().getDifficulty().equals(Difficulty.PEACEFUL)) {
                this.isFinished = true;
                this.defeat = true;
            }

            if (this.currentWave >= this.totalWaves && this.isWaveComplete()) {
                this.isFinished = true;
            }

            if (this.breakTime > 0 && this.currentWave != 0) {

            }

            if (this.justFinishedWave) {
                for (Player player : this.activePlayers) {
                    Component message = Component.translatable("broadcast.scguns.raid.next_wave");
                    player.sendSystemMessage(message);
                    this.waveReward(player, (ServerLevel) player.level());
                }
                this.justFinishedWave = false;
            }

            if (!this.isFinished) {
                if (this.isSpawningMobs) {
                    this.spawnCooldown--;
                    if (this.spawnCooldown <= 0) {
                        spawnMobs((ServerLevel) this.level(), this.position(), this.forceGuns, 10);
                        this.spawnCooldown = spawnInterval;
                    }
                }

                if (!this.inWave) {
                    if (this.spawningWave) {
                        float progress = 1.0f - ((float) this.cooldown / this.maxCooldown);
                        this.bossBar.setProgress(progress);

                        if (this.currentWave != 0 && this.breakTime == this.maxBreakTime) {
                            this.justFinishedWave = true;
                        }

                        this.breakTime--;
                        if (this.breakTime <= 0) {
                            this.cooldown--;
                            if (this.cooldown <= 0) {
                                waveIterator();
                                this.inWave = true;
                                this.spawningWave = false;
                            }
                        }
                    } else {
                        if (this.currentWave < this.totalWaves) {
                            this.spawningWave = true;
                        }
                        this.cooldown = this.maxCooldown;
                        this.breakTime = this.maxBreakTime;
                    }
                } else {
                    if (activeMobs.size() < MAX_ACTIVE_MOBS && spawnedMobs.size() < TOTAL_WAVE_MOBS) {
                        this.isSpawningMobs = true;
                    }
                }

                if (getActiveMobs().size() < 10 && spawnedMobs.size() > TOTAL_WAVE_MOBS - (TOTAL_WAVE_MOBS / 5)) {
                    for (LivingEntity entity : getActiveMobs()) {
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 180, 0, false, false));
                    }
                }
            }

            if (this.isWaveComplete()) {
                if (this.currentWave >= this.totalWaves) {
                    this.victory = true;
                }
                this.inWave = false;
                this.cooldown = this.maxCooldown;
                this.breakTime = this.maxBreakTime;
                this.spawnedMobs.clear();
                this.activeMobs.clear();
            }
            if (this.isFinished) {
                if (this.resultPrize > 0) {
                    if (this.victory) {
                        Component message = Component.translatable("broadcast.jeg.raid.victory", Component.translatable("faction.jeg." + this.faction.getName())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD);
                        ((ServerLevel) this.level()).getServer().getPlayerList().broadcastSystemMessage(message, false);
                        for (Player player : this.activePlayers) {
                            this.victoryReward(player, (ServerLevel) player.level());
                        }
                        this.resultPrize--;
                    }
                    if (this.defeat) {
                        for (LivingEntity entity : getActiveMobs()) {
                            entity.removeEffect(MobEffects.GLOWING);
                        }
                        Component message2 = Component.translatable("broadcast.jeg.raid.no_players", Component.translatable("faction.jeg." + this.faction.getName())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
                        ((ServerLevel) this.level()).getServer().getPlayerList().broadcastSystemMessage(message2, false);
                        Component message = Component.translatable("broadcast.jeg.raid.defeat", Component.translatable("faction.jeg." + this.faction.getName())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
                        ((ServerLevel) this.level()).getServer().getPlayerList().broadcastSystemMessage(message, false);
                        this.resultPrize--;
                    }
                }
                if (this.victory) {
                    if (this.tickCount % 5 == 0) {
                        //FireworkRocketEntity firework = new FireworkRocketEntity(this.level(), getFireworkStack(this.random.nextBoolean(), false, this.random.nextInt(0, 3), 3), this.getX() + this.random.nextInt(-64, 64), this.getY() + this.random.nextInt(0, 3 ), this.getZ() + this.random.nextInt(-64, 64), false);
                        //this.level().addFreshEntity(firework);
                    }
                }
                if (this.defeat) {
                    if (this.tickCount % 80 == 0) {
                        this.playCelebrationHorn();
                    }
                }

                ((ServerLevel) this.level()).setWeatherParameters(12000, 0, false, false);

                this.despawnTicks--;
                if (this.despawnTicks < 0) {
                    this.bossBar.setVisible(false);
                    this.bossBar.removeAllPlayers();
                    this.discard();
                }
            }
        }

    }

    @Override
    public void onRemovedFromWorld() {
        if (!this.victory && !this.defeat && this.level() instanceof ServerLevel serverLevel) {
            Component message2 = Component.translatable("broadcast.jeg.raid.no_players", Component.translatable("faction.jeg." + this.faction.getName())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message2, false);
            Component message = Component.translatable("broadcast.jeg.raid.defeat", Component.translatable("faction.jeg." + this.faction.getName())).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    public static void summonRaidEntity(ServerLevel level, Faction faction, Vec3 startPos, boolean forceGuns) {
        RaidEntity raidEntity = new RaidEntity(ModEntities.RAID_ENTITY.get(), level);
        raidEntity.setPos(startPos);
        raidEntity.faction = faction;
        if (raidEntity.level().random.nextFloat() < 0.3) {
            level.setWeatherParameters(0, 12000, true, true);
        }
        level.addFreshEntity(raidEntity);
        Component message = Component.translatable("broadcast.jeg.raid", Component.translatable("faction.jeg." + raidEntity.faction.getName()), BlockPos.containing(startPos)).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
        level.getServer().getPlayerList().broadcastSystemMessage(message, false);
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    private void summonParticleRing() {
        for (int i = 0; i < 360; i += 1) {
            double angle = Math.toRadians(i);
            double xOffset = Math.cos(angle) * ACTIVE_RADIUS;
            double zOffset = Math.sin(angle) * ACTIVE_RADIUS;
            this.level().addParticle(ModParticleTypes.LASER.get(),
                    this.getX() + xOffset, this.getY() + 5, this.getZ() + zOffset, 0.0, 0.0, 0.0);
        }
    }
}