package top.ribs.scguns.faction;

import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Random;

public class Faction {
    private final String name;
    private final int aiLevel;
    private final List<String> mobs;
    private final List<Item> closeGuns;
    private final List<Item> longGuns;
    private final List<Item> eliteGuns;

    public Faction(String name, int aiLevel, List<String> mobs, List<Item> closeGuns, List<Item> longGuns, List<Item> eliteGuns) {
        this.name = name;
        this.aiLevel = aiLevel;
        this.mobs = mobs;
        this.closeGuns = closeGuns;
        this.longGuns = longGuns;
        this.eliteGuns = eliteGuns;
    }

    public String getName() {
        return name;
    }

    public int getAiLevel() {
        return aiLevel;
    }

    public List<String> getMobs() {
        return mobs;
    }

    public Item getRandomGun(boolean isCloseRange) {
        List<Item> pool = isCloseRange ? closeGuns : longGuns;
        return pool.get(new Random().nextInt(pool.size()));
    }

    public Item getEliteGun() {
        List<Item> pool = eliteGuns;
        return pool.get(new Random().nextInt(pool.size()));
    }
}
