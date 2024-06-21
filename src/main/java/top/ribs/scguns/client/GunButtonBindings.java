package top.ribs.scguns.client;

import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.input.Buttons;

/**
 * Author: MrCrayfish
 */
public class GunButtonBindings
{
    public static final ButtonBinding SHOOT = new ButtonBinding(Buttons.RIGHT_TRIGGER, "scguns.button.shoot", "button.categories.scguns", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding AIM = new ButtonBinding(Buttons.LEFT_TRIGGER, "scguns.button.aim", "button.categories.scguns", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding RELOAD = new ButtonBinding(Buttons.X, "scguns.button.reload", "button.categories.scguns", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding OPEN_ATTACHMENTS = new ButtonBinding(Buttons.B, "scguns.button.attachments", "button.categories.scguns", GunConflictContext.IN_GAME_HOLDING_WEAPON);
    public static final ButtonBinding STEADY_AIM = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "scguns.button.steadyAim", "button.categories.scguns", GunConflictContext.IN_GAME_HOLDING_WEAPON);

    public static void register()
    {
        BindingRegistry.getInstance().register(SHOOT);
        BindingRegistry.getInstance().register(AIM);
        BindingRegistry.getInstance().register(RELOAD);
        BindingRegistry.getInstance().register(OPEN_ATTACHMENTS);
        BindingRegistry.getInstance().register(STEADY_AIM);

    }
}
