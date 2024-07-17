package top.ribs.scguns.item;

import net.minecraft.world.item.Item;

public class ScorchedItem extends Item {
    public ScorchedItem(Properties properties) {
        super(properties);
    }
    @Override
    public boolean isFireResistant() {
        return true;
    }
}
