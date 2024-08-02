package top.ribs.scguns.init;

import net.minecraftforge.fml.ModList;

public class ModCompat {
    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded("create");
    }
    public static boolean isFarmersDelightLoaded() {
        return ModList.get().isLoaded("farmersdelight");
    }
    public static boolean isIELoaded() {
        return ModList.get().isLoaded("immersiveengineering");
    }
    public static boolean isMekanismLoaded() {
        return ModList.get().isLoaded("mekanism");
    }
}