package top.ribs.scguns.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.NetworkGunManager;
import top.ribs.scguns.faction.Faction;
import top.ribs.scguns.faction.GunnerManager;
import top.ribs.scguns.faction.scguns.FactionData;
import top.ribs.scguns.faction.scguns.FactionDataManager;
import top.ribs.scguns.faction.raid.RaidEntity;
import top.ribs.scguns.item.GunItem;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static top.ribs.scguns.faction.GunMobValues.*;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(

                Commands.literal("scguns")
                        .then(Commands.literal("spawnPatrol")
                                .then(Commands.argument("faction", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            List<String> factionConfigs = GunnerManager.getConfigFactions();

                                            for (String factionConfig : factionConfigs) {
                                                String factionName = factionConfig.split("\\|")[0];
                                                builder.suggest(factionName);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("size", IntegerArgumentType.integer(1, 20))
                                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                                        .then(Commands.argument("forceGuns", BoolArgumentType.bool())
                                                                .then(Commands.argument("spawnRadius", IntegerArgumentType.integer(0, 16))
                                                                        .executes(context -> {
                                                                            String factionName = StringArgumentType.getString(context, "faction");
                                                                            int size = IntegerArgumentType.getInteger(context, "size");
                                                                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                            boolean forceGuns = BoolArgumentType.getBool(context, "forceGuns");
                                                                            CommandSourceStack source = context.getSource();
                                                                            int spread = IntegerArgumentType.getInteger(context, "spawnRadius");

                                                                            return executeSpawnPatrol(source, factionName, size, pos, forceGuns, spread);
                                                                        })
                                                                )
                                                                .executes(context -> {
                                                                    String factionName = StringArgumentType.getString(context, "faction");
                                                                    int size = IntegerArgumentType.getInteger(context, "size");
                                                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                    CommandSourceStack source = context.getSource();
                                                                    boolean forceGuns = BoolArgumentType.getBool(context, "forceGuns");

                                                                    return executeSpawnPatrol(source, factionName, size, pos, forceGuns, 10);
                                                                })

                                                        )
                                                        .executes(context -> {
                                                            String factionName = StringArgumentType.getString(context, "faction");
                                                            int size = IntegerArgumentType.getInteger(context, "size");
                                                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                            CommandSourceStack source = context.getSource();

                                                            return executeSpawnPatrol(source, factionName, size, pos, false, 10);
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("simulatePatrol")
                                .then(Commands.argument("faction", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            List<String> factionConfigs = GunnerManager.getConfigFactions();

                                            for (String factionConfig : factionConfigs) {
                                                String factionName = factionConfig.split("\\|")[0];
                                                builder.suggest(factionName);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("size", IntegerArgumentType.integer(1, 20))
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .then(Commands.argument("forceGuns", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    String factionName = StringArgumentType.getString(context, "faction");
                                                                    int size = IntegerArgumentType.getInteger(context, "size");
                                                                    Player player = EntityArgument.getPlayer(context, "player");
                                                                    CommandSourceStack source = context.getSource();
                                                                    boolean forceGuns = BoolArgumentType.getBool(context, "forceGuns");

                                                                    return executeSimulatePatrol(source, factionName, size, player, forceGuns);
                                                                })

                                                        )
                                                        .executes(context -> {
                                                            String factionName = StringArgumentType.getString(context, "faction");
                                                            int size = IntegerArgumentType.getInteger(context, "size");
                                                            Player player = EntityArgument.getPlayer(context, "player");
                                                            CommandSourceStack source = context.getSource();

                                                            return executeSimulatePatrol(source, factionName, size, player, false);
                                                        })
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("startRaid")
                                .then(Commands.argument("faction", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            List<String> factionConfigs = GunnerManager.getConfigFactions();

                                            for (String factionConfig : factionConfigs) {
                                                String factionName = factionConfig.split("\\|")[0];
                                                builder.suggest(factionName);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                    String factionName = StringArgumentType.getString(context, "faction");
                                                    executeStartRaid(source, factionName, pos);

                                                    return 1;
                                                })
                                        )
                                )
                        )



        );
    }

    private static int executeSpawnPatrol(CommandSourceStack source, String factionName, int size, Vec3 pos, boolean forceGuns, int spread) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.nullToEmpty("You do not have permission to execute this command"));
            return 0;
        }

        ServerLevel level = source.getLevel();

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            source.sendFailure(Component.nullToEmpty("Mobs can't spawn in Peaceful!"));
            return 0;
        }

        GunnerManager gunnerManager = GunnerManager.getInstance();
        Faction faction = gunnerManager.getFactionByName(factionName);

        if (faction == null) {
            source.sendFailure(Component.nullToEmpty("Faction '" + factionName + "' does not exist!"));
            return 0;
        }

        for (int i = 0; i < size; i++) {
            Mob mob = getFactionMob(level, faction, pos, forceGuns, spread);
            if (mob == null) {
                source.sendFailure(Component.nullToEmpty("Could not spawn mob for faction " + factionName));
                return 0;
            } else {
                level.addFreshEntity(mob);
            }
        }

        MutableComponent factionLang = Component.translatable("faction.scguns." + factionName);
        source.sendSuccess(() -> Component.nullToEmpty("Spawned " + size + " mobs for faction " + factionLang.getString()), true);
        if (!forceGuns)
            source.sendFailure(Component.nullToEmpty("Please note that by not setting 'forceGuns' to true, the chance of them spawning with Guns is entirely determined by your Config Files!"));
        return 1;
    }

    public static Mob getFactionMob(ServerLevel level, FactionData factionData, Vec3 startPos, boolean forceGuns, int spread) {
        Random random = new Random();

        String mobName = factionData.getMobList().get(random.nextInt(factionData.getMobList().size()));
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobName));

        if (entityType == null) {
            return null;
        }

        Mob mob = (Mob) entityType.create(level);
        if (mob != null) {
            double offsetX = (random.nextDouble() - 0.5) * spread;
            double offsetZ = (random.nextDouble() - 0.5) * spread;
            mob.setPos(startPos.x + offsetX, startPos.y, startPos.z + offsetZ);

            long totalDayTime = mob.level().getDayTime();
            int currentDay = (int) (totalDayTime / 24000L);
            int currentChance = Math.min(initialChance + (currentDay * chanceIncrement), maxChance);

            if (mob.getRandom().nextInt(100) < currentChance || forceGuns) {
                mob.addTag("MobGunner");
            }

            mob.populateDefaultEquipmentSlots(RandomSource.create(), mob.level().getCurrentDifficultyAt(mob.blockPosition()));
            equipArmorWithRandomMaterial(mob, random);

            if (mob.getTags().contains("MobGunner")) {
                Item gun = factionData.getRandomGun(random.nextBoolean());

                boolean elite = (mob.getRandom().nextFloat() < eliteChance && elitesEnabled);
                if (elite) {
                    mob.addTag("EliteGunner");
                    gun = factionData.getEliteGun();
                    if (factionData == FactionDataManager.getTerrorArmadaWave1() || factionData == FactionDataManager.getTerrorArmadaWave2() || factionData == FactionDataManager.getTerrorArmadaWave3()) {
                        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.JACK_O_LANTERN));
                    } else {
                        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.TURTLE_HELMET));
                    }
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 2, false, true));
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, -1, 1, false, false));
                } else if (mob.getRandom().nextFloat() < 0.8F) {
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
                }

                ItemStack modifiedGun = new ItemStack(gun);
                mob.setItemSlot(EquipmentSlot.MAINHAND, modifiedGun);
                if (modifiedGun.getTag() != null && modifiedGun.getItem() instanceof GunItem gunItem) {
                    Gun gunModified = gunItem.getModifiedGun(modifiedGun);
                    modifiedGun.getTag().putInt("AmmoCount", mob.getRandom().nextInt(gunModified.getReloads().getMaxAmmo()));
                }
            } else {
                if (mob instanceof Piglin piglin) {
                    piglin.setItemSlot(EquipmentSlot.MAINHAND, piglin.getRandom().nextFloat() < 0.5f
                            ? new ItemStack(Items.CROSSBOW)
                            : new ItemStack(Items.GOLDEN_SWORD));
                }
            }
        }

        if (mob instanceof AbstractPiglin abstractPiglin) {
            abstractPiglin.setImmuneToZombification(true);
        }

        return mob;
    }

    public static Mob getFactionMob(ServerLevel level, Faction faction, Vec3 startPos, boolean forceGuns, int spread) {
        Random random = new Random();

        String mobName = faction.getMobs().get(random.nextInt(faction.getMobs().size()));
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobName));

        if (entityType == null) {
            return null;
        }

        Mob mob = (Mob) entityType.create(level);
        if (mob != null) {
            double offsetX = (random.nextDouble() - 0.5) * spread;
            double offsetZ = (random.nextDouble() - 0.5) * spread;
            mob.setPos(startPos.x + offsetX, startPos.y, startPos.z + offsetZ);

            long totalDayTime = mob.level().getDayTime();
            int currentDay = (int) (totalDayTime / 24000L);
            int currentChance = Math.min(initialChance + (currentDay * chanceIncrement), maxChance);

            if (mob.getRandom().nextInt(100) < currentChance || forceGuns) {
                mob.addTag("MobGunner");
            }

            mob.populateDefaultEquipmentSlots(RandomSource.create(), mob.level().getCurrentDifficultyAt(mob.getOnPos()));

            equipArmorWithRandomMaterial(mob, random);

            if (mob.getTags().contains("MobGunner")) {
                Item gun = faction.getRandomGun(random.nextBoolean());

                boolean elite = (mob.getRandom().nextFloat() < eliteChance && elitesEnabled);
                if (elite) {
                    mob.addTag("EliteGunner");
                    gun = faction.getEliteGun();
                    //mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.TURTLE_HELMET));
                    mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 2, false, true));
                    mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, -1, 1, false, false));
                }

                ItemStack modifiedGun = new ItemStack(gun);
                mob.setItemSlot(EquipmentSlot.MAINHAND, modifiedGun);
                if (modifiedGun.getTag() != null && modifiedGun.getItem() instanceof GunItem gunItem) {
                    Gun gunModified = gunItem.getModifiedGun(modifiedGun);
                    modifiedGun.getTag().putInt("AmmoCount", mob.getRandom().nextInt(gunModified.getReloads().getMaxAmmo()));
                }
            } else {
                if (mob instanceof Piglin piglin) {
                    piglin.setItemSlot(EquipmentSlot.MAINHAND, piglin.getRandom().nextFloat() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD));
                }
            }
        }

        return mob;
    }

    private static void equipArmorWithRandomMaterial(Mob mob, Random random) {
        Item[] helmetMaterials = { Items.LEATHER_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.CHAINMAIL_HELMET };
        Item[] chestplateMaterials = { Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE };
        Item[] leggingsMaterials = { Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.CHAINMAIL_LEGGINGS };
        Item[] bootsMaterials = { Items.LEATHER_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.CHAINMAIL_BOOTS };

        if (random.nextInt(3) == 0) {
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(helmetMaterials[random.nextInt(helmetMaterials.length)]));
        }
        if (random.nextInt(3) == 0) {
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chestplateMaterials[random.nextInt(chestplateMaterials.length)]));
        }
        if (random.nextInt(3) == 0) {
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(leggingsMaterials[random.nextInt(leggingsMaterials.length)]));
        }
        if (random.nextInt(3) == 0) {
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(bootsMaterials[random.nextInt(bootsMaterials.length)]));
        }
    }

    private static int executeSimulatePatrol(CommandSourceStack source, String factionName, int size, Player player, boolean forceGuns) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.nullToEmpty("You do not have permission to execute this command"));
            return 0;
        }

        ServerLevel level = source.getLevel();

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            source.sendFailure(Component.nullToEmpty("Mobs can't spawn in Peaceful!"));
            return 0;
        }

        GunnerManager gunnerManager = GunnerManager.getInstance();
        Faction faction = gunnerManager.getFactionByName(factionName);

        if (faction == null) {
            source.sendFailure(Component.nullToEmpty("Faction '" + factionName + "' does not exist!"));
            return 0;
        }

        RandomSource random = level.random;
        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable()
                .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                        0,
                        (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));

        spawnPatrol(level, faction, size, player, spawnPos, forceGuns);

        MutableComponent factionLang = Component.translatable("faction.scguns." + factionName);
        source.sendSuccess(() -> Component.nullToEmpty("Spawned a Patrol with " + size + " mobs for faction " + factionLang.getString()), true);
        if (!forceGuns)
            source.sendFailure(Component.nullToEmpty("Please note that by not setting 'forceGuns' to true, the chance of them spawning with Guns is entirely determined by your Config Files!"));
        return 1;
    }

    private static int executeSimulatePhantomSwarm(CommandSourceStack source, int size, Player player) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.nullToEmpty("You do not have permission to execute this command"));
            return 0;
        }

        ServerLevel level = source.getLevel();

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            source.sendFailure(Component.nullToEmpty("Mobs can't spawn in Peaceful!"));
            return 0;
        }

        RandomSource random = level.random;
        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable()
                .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                        0,
                        (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));


        source.sendSuccess(() -> Component.nullToEmpty("Spawned a Phantom Swarm with " + size + " Phantoms!"), true);
        return 1;
    }

    private static int executeStartRaid(CommandSourceStack source, String factionName, Vec3 pos) {
        if (!source.hasPermission(2)) {
            source.sendFailure(Component.nullToEmpty("You do not have permission to execute this command"));
            return 0;
        }

        ServerLevel level = source.getLevel();

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            source.sendFailure(Component.nullToEmpty("Raids, can't be started on Peaceful, dummy."));
            return 0;
        }

        GunnerManager gunnerManager = GunnerManager.getInstance();
        Faction faction = gunnerManager.getFactionByName(factionName);

        if (faction == null) {
            source.sendFailure(Component.nullToEmpty("Faction '" + factionName + "' does not exist!"));
            return 0;
        }

        startRaid(level, faction, pos, true);

        MutableComponent factionLang = Component.translatable("faction.scguns." + factionName);
        source.sendSuccess(() -> Component.nullToEmpty("Started a Raid for the Faction " + factionLang.getString() + ", good luck!"), true);
        return 1;
    }

    public static void startRaid(ServerLevel level, Faction faction, Vec3 pos, boolean forceGun) {
        if (level.getDifficulty().equals(Difficulty.PEACEFUL)) {
            Component message = Component.translatable("broadcast.scguns.raid.peaceful").withStyle(ChatFormatting.WHITE);
            level.getServer().getPlayerList().broadcastSystemMessage(message, true);

            return;
        }

        if (faction == null) {
            ScorchedGuns.LOGGER.atInfo().log("A non existing Faction was trying to be summon for a Raid, but failed.");
            return;
        }

        int x = level.random.nextInt(-50, 50);
        int z = level.random.nextInt(-50, 50);
        RaidEntity.summonRaidEntity(level, faction, pos, true);

        level.playSound(null, BlockPos.containing(pos.add(x, 32, z)), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2).get(), SoundSource.HOSTILE, 1000F, 1);
    }




    public static boolean spawnRaider(ServerLevel level, LivingEntity entity, Player player, BlockPos.MutableBlockPos spawnPos, Vec3 targetPos, boolean forceGuns) {
        RandomSource random = level.random;
        spawnPos.setY(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos).getY());

        return spawnRaidMember(level, spawnPos, targetPos, entity, player, true, random);
    }

    private static boolean spawnRaidMember(ServerLevel level, BlockPos pos, Vec3 targetPos, LivingEntity entity, Player player, boolean forceGuns, RandomSource random) {
        if (!NaturalSpawner.isValidEmptySpawnBlock(level, pos, level.getBlockState(pos), level.getBlockState(pos).getFluidState(), EntityType.PILLAGER)) {
            return false;
        }

        if (!Mob.checkMobSpawnRules(EntityType.SHEEP, level, MobSpawnType.NATURAL, pos, random)) {
            return false;
        }

        entity.setPos(pos.getX(), pos.getY(), pos.getZ());
        entity.addTag("GunnerPatroller");
        if (level.isDay() && entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !entity.getTags().contains("EliteGunner") && entity.getType().is(ModTags.Entities.UNDEAD)) {
            entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        }
        level.addFreshEntity(entity);
        if (entity instanceof PathfinderMob pathfinderMob) {
            pathfinderMob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.2F);
            pathfinderMob.setTarget(player);
        }

        return true;
    }

    public static int spawnPatrol(ServerLevel level, Faction faction, int size, Player player, BlockPos.MutableBlockPos spawnPos, boolean forceGuns) {
        RandomSource random = level.random;

        int spawns = 0;
        while (spawns < size) {
            spawnPos.setY(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos).getY());

            Mob entity = getFactionMob(level, faction, spawnPos.getCenter(), forceGuns, 3);
            if (spawnDayPatrolMember(level, spawnPos, random, entity, player)) {
                spawns++;
            }

            spawnPos.move(random.nextInt(5) - random.nextInt(5), 0, random.nextInt(5) - random.nextInt(5));
        }
        return spawns;
    }

    private static boolean spawnDayPatrolMember(ServerLevel level, BlockPos pos, RandomSource random, Mob entity, Player player) {
        if (!NaturalSpawner.isValidEmptySpawnBlock(level, pos, level.getBlockState(pos), level.getBlockState(pos).getFluidState(), EntityType.PILLAGER)) {
            return false;
        }

        if (!Mob.checkMobSpawnRules(EntityType.SHEEP, level, MobSpawnType.NATURAL, pos, random)) {
            return false;
        }

        entity.setPos(pos.getX(), pos.getY(), pos.getZ());
        entity.addTag("GunnerPatroller");
        level.addFreshEntity(entity);
        if (entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && !entity.getTags().contains("EliteGunner") && entity.getType().is(ModTags.Entities.UNDEAD)) {
            entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        }
        if (entity instanceof PathfinderMob pathfinderMob) {
            pathfinderMob.getNavigation().moveTo(player, 1.2F);
        }
        entity.setTarget(player);

        // Horsemen!
        if (entity.getTags().contains("EliteGunner") && level.random.nextBoolean() && Config.COMMON.gunnerMobs.horsemen.get()) {
            if (entity instanceof Zombie) {
                ZombieHorse zombieHorse = new ZombieHorse(EntityType.ZOMBIE_HORSE, level);
                zombieHorse.setPos(entity.position());
                zombieHorse.addTag("GunnerPatroller");
                level.addFreshEntity(zombieHorse);
                zombieHorse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400));
                entity.startRiding(zombieHorse);
            } else if (entity instanceof AbstractSkeleton) {
                SkeletonHorse skeletonHorse = new SkeletonHorse(EntityType.SKELETON_HORSE, level);
                skeletonHorse.setPos(entity.position());
                skeletonHorse.addTag("GunnerPatroller");
                level.addFreshEntity(skeletonHorse);
                skeletonHorse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400));
                entity.startRiding(skeletonHorse);
            } else {
                Horse horse = new Horse(EntityType.HORSE, level);
                horse.setPos(entity.position());
                horse.addTag("GunnerPatroller");
                level.addFreshEntity(horse);
                entity.startRiding(horse);
            }
        }

        return true;
    }




}