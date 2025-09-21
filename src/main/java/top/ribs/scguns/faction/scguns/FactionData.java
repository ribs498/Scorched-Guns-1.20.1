package top.ribs.scguns.faction.scguns;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

public class FactionData {
    private String name;
    private int aiDifficulty;
    private List<String> mobList;
    private List<String> closeGuns;
    private List<String> longGuns;
    private List<String> eliteGuns;

    public FactionData(String name, int aiDifficulty, List<String> mobList,
                       List<String> closeGuns, List<String> longGuns, List<String> eliteGuns) {
        this.name = name;
        this.aiDifficulty = aiDifficulty;
        this.mobList = mobList;
        this.closeGuns = closeGuns;
        this.longGuns = longGuns;
        this.eliteGuns = eliteGuns;
    }

    public List<String> getMobList() {
        return mobList;
    }

    public Item getRandomGun(boolean useCloseGun) {
        Random random = new Random();
        List<String> gunList = useCloseGun ? closeGuns : longGuns;
        if (gunList.isEmpty() && !closeGuns.isEmpty()) {
            gunList = closeGuns;
        }
        if (gunList.isEmpty()) {
            return null;
        }
        String gunStr = gunList.get(random.nextInt(gunList.size()));
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(gunStr));
    }

    public Item getEliteGun() {
        if (eliteGuns.isEmpty()) {
            return null;
        }
        String gunStr = eliteGuns.get(0);
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(gunStr));
    }
}