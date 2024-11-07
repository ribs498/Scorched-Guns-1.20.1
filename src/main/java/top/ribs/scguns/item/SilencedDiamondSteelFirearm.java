package top.ribs.scguns.item;

public class SilencedDiamondSteelFirearm extends SilencedFirearm{
    public SilencedDiamondSteelFirearm(Properties properties) {
        super(properties);
    }
    @Override
    public int getEnchantmentValue() {
        return 18;
    }

}
