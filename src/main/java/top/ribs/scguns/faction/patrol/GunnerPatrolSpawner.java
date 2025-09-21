package top.ribs.scguns.faction.patrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import top.ribs.scguns.Config;
import top.ribs.scguns.faction.Faction;
import top.ribs.scguns.faction.GunnerManager;
import top.ribs.scguns.init.ModCommands;

public class GunnerPatrolSpawner implements CustomSpawner {
    //private int nextTick = 24000 * 5;
    private int nextTick;
    
    public GunnerPatrolSpawner() {
    }

    @Override
    public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        if (!Config.COMMON.gunnerMobs.gunnerMobPatrols.get()) {
            return 0;
        }

        if (level.getDifficulty().equals(Difficulty.PEACEFUL)) {
            return 0;
        }

        if (!spawnEnemies || !level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        }

        RandomSource random = level.random;
        --this.nextTick;

        if (this.nextTick > 0) {
            return 0;
        }

        int fixedDaysInterval = Config.COMMON.gunnerMobs.patrolIntervalDays.get();
        int randomIntervalMin = Config.COMMON.gunnerMobs.randomIntervalMinTicks.get();
        int randomIntervalMax = Config.COMMON.gunnerMobs.randomIntervalMaxTicks.get();

        if (fixedDaysInterval > 0) {
            this.nextTick += fixedDaysInterval * 24000;
        } else {
            this.nextTick += randomIntervalMin + random.nextInt(randomIntervalMax - randomIntervalMin + 1);
        }

        // Adds a chance for the Patrol to spawn in either Daytime or Nighttime
        this.nextTick += random.nextInt(12000);

        long dayTime = level.getDayTime() / 24000L;

        int minimumDays = Config.COMMON.gunnerMobs.minimumDaysForPatrols.get();
        if (dayTime < minimumDays) {
            return 0;
        }

        int playerCount = level.players().size();
        if (playerCount < 1) {
            return 0;
        }

        Player randomPlayer = level.players().get(random.nextInt(playerCount));
        if (randomPlayer.isSpectator() || level.isCloseToVillage(randomPlayer.blockPosition(), 2)) {
            return 0;
        }

        BlockPos.MutableBlockPos spawnPos = randomPlayer.blockPosition().mutable()
            .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                  0,
                  (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));

        if (!level.hasChunksAt(spawnPos.getX() - 10, spawnPos.getZ() - 10, spawnPos.getX() + 10, spawnPos.getZ() + 10)) {
            return 0;
        }

        Holder<Biome> biome = level.getBiome(spawnPos);
        if (biome.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return 0;
        }

        GunnerManager gunnerManager = GunnerManager.getInstance();
        Faction faction = gunnerManager.getFactionByName(gunnerManager.getRandomFactionName());

        if (faction == null) {
            return 0;
        }

        int patrolCount = 2 + random.nextInt(5);
        return ModCommands.spawnPatrol(level, faction, patrolCount, randomPlayer, spawnPos, false);
    }
}