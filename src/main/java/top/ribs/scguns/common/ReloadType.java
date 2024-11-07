package top.ribs.scguns.common;

import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public record ReloadType(ResourceLocation id) {
    /**
     * A reload system that works with ammo items individually
     */
    public static final ReloadType MANUAL = new ReloadType(new ResourceLocation(Reference.MOD_ID, "manual"));

    /**
     * A reload system that works with magazines (stacks of items)
     */
    public static final ReloadType MAG_FED = new ReloadType(new ResourceLocation(Reference.MOD_ID, "mag_fed"));

    /**
     * A reload system which takes 1 item to fill an entire gun
     */
    public static final ReloadType SINGLE_ITEM = new ReloadType(new ResourceLocation(Reference.MOD_ID, "single_item"));

    /**
     * The reload system map.
     */
    private static final Map<ResourceLocation, ReloadType> reloadTypeMap = new HashMap<>();

    static {
        /* Registers the standard reload modes when the class is loaded */
        registerType(MANUAL);
        registerType(MAG_FED);
        registerType(SINGLE_ITEM);
    }

    /**
     * Registers a new reload system. If the id already exists, the reload system will simply be ignored.
     *
     * @param mode the get of the reload system
     */
    public static void registerType(ReloadType mode) {
        reloadTypeMap.putIfAbsent(mode.id(), mode);
    }

    /**
     * Gets the reload system associated the the id. If the reload system does not exist, it will default to
     * one handed.
     *
     * @param id the id of the reload system
     * @return returns an get of the reload system or SEMI_AUTO if it doesn't exist
     */
    public static ReloadType getType(ResourceLocation id) {
        return reloadTypeMap.getOrDefault(id, MANUAL);
    }

    /**
     * Creates a new reload system.
     *
     * @param id the id of the reload system
     */
    public ReloadType {
    }

    /**
     * Gets the id of the reload system
     */
    @Override
    public ResourceLocation id() {
        return this.id;
    }

}
