package top.ribs.scguns.faction.raid;

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

public class GunnerRaidSpawner implements CustomSpawner {
    private int nextTick;

    public GunnerRaidSpawner() {
    }

    @Override
    public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        if (!Config.COMMON.gunnerMobs.gunnerMobRaids.get()) {
            return 0;
        }

        if (level.getDifficulty().equals(Difficulty.PEACEFUL)) {
            return 0;
        }

        if (!spawnEnemies || !level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        }

        GunnerRaidData raidData = GunnerRaidData.get(level);
        this.nextTick = raidData.getNextTick();
        RandomSource random = level.random;
        --this.nextTick;
        raidData.setNextTick(this.nextTick);

        if (this.nextTick > 0) {
            return 0;
        }

        int fixedDaysInterval = Config.COMMON.gunnerMobs.raidIntervalDays.get();
        int randomIntervalMin = Config.COMMON.gunnerMobs.randomRaidIntervalMinTicks.get();
        int randomIntervalMax = Config.COMMON.gunnerMobs.randomRaidIntervalMaxTicks.get();

        if (fixedDaysInterval > 0) {
            this.nextTick += fixedDaysInterval * 24000;
        } else {
            this.nextTick += randomIntervalMin + random.nextInt(randomIntervalMax - randomIntervalMin + 1);
        }

        this.nextTick += random.nextInt(12000);
        raidData.setNextTick(this.nextTick);

        long dayTime = level.getDayTime() / 24000L;
        int minimumDays = Config.COMMON.gunnerMobs.minimumDaysForRaids.get();
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

        Holder<Biome> biome = level.getBiome(randomPlayer.getOnPos());
        if (biome.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return 0;
        }

        GunnerManager gunnerManager = GunnerManager.getInstance();
        Faction faction = gunnerManager.getFactionByName(gunnerManager.getRandomFactionName());
        ModCommands.startRaid(level, faction, randomPlayer.position(), true);

        return 1;
    }
}
