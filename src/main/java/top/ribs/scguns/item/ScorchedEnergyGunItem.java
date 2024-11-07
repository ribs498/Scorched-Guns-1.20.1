package top.ribs.scguns.item;

public class ScorchedEnergyGunItem extends EnergyGunItem{
    public ScorchedEnergyGunItem(Properties properties, int capacity) {
        super(properties, capacity);
    }
    @Override
    public boolean isFireResistant() {
        return true;
    }
}
