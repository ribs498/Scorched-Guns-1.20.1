package top.ribs.scguns.common;

import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: ribs
 */
public class FireMode
{
    /**
     * A fire mode that shoots once per trigger press
     */
    public static final FireMode SEMI_AUTO = new FireMode(new ResourceLocation(Reference.MOD_ID, "semi_automatic"));

    /**
     * A fire mode that shoots as long as the trigger is held down
     */
    public static final FireMode AUTOMATIC = new FireMode(new ResourceLocation(Reference.MOD_ID, "automatic"));


    /**
     * A fire mode that shoots once per cooldown
     */
    public static final FireMode PULSE = new FireMode(new ResourceLocation(Reference.MOD_ID, "pulse"));


    /**
     * The fire mode map.
     */
    private static final Map<ResourceLocation, FireMode> fireModeMap = new HashMap<>();

    static
    {
        /* Registers the standard fire modes when the class is loaded */
        registerType(SEMI_AUTO);
        registerType(AUTOMATIC);
        registerType(PULSE);
    }

    /**
     * Registers a new fire mode. If the id already exists, the fire mode will simply be ignored.
     *
     * @param mode the get of the fire mode
     */
    public static void registerType(FireMode mode)
    {
        fireModeMap.putIfAbsent(mode.getId(), mode);
    }

    /**
     * Gets the fire mode associated the the id. If the fire mode does not exist, it will default to
     * one handed.
     *
     * @param id the id of the fire mode
     * @return returns an get of the fire mode or SEMI_AUTO if it doesn't exist
     */
    public static FireMode getType(ResourceLocation id)
    {
        return fireModeMap.getOrDefault(id, SEMI_AUTO);
    }

    private final ResourceLocation id;

    /**
     * Creates a new fire mode.
     *
     * @param id the id of the fire mode
     *
     */
    public FireMode(ResourceLocation id)
    {
        this.id = id;
    }

    /**
     * Gets the id of the fire mode
     */
    public ResourceLocation getId()
    {
        return this.id;
    }

}
