package top.ribs.scguns.compat;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;

public class CompatManager {
    public static final boolean SCULK_HORDE_LOADED = modLoaded("sculkhorde");


    public static boolean modLoaded(String modID) {
        ModFileInfo mod = FMLLoader.getLoadingModList().getModFileById(modID);
        return mod != null;
    }


}
