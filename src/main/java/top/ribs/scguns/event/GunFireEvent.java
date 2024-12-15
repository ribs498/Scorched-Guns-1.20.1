package top.ribs.scguns.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * <p>Fired when a player shoots a gun.</p>
 *
 * @author Ocelot
 */
public class GunFireEvent extends PlayerEvent
{
    private final ItemStack stack;

    public GunFireEvent(Player player, ItemStack stack)
    {
        super(player);
        this.stack = stack;
    }

    /**
     * @return The stack the player was holding when firing the gun
     */
    public ItemStack getStack()
    {
        return stack;
    }

    /**
     * @return Whether or not this event was fired on the client side
     */
    public boolean isClient()
    {
        return this.getEntity().getCommandSenderWorld().isClientSide();
    }

    /**
     * <p>Fired when a player is about to shoot a bullet.</p>
     *
     * @author Ocelot
     */
    @Cancelable
    public static class Pre extends GunFireEvent
    {
        public Pre(Player player, ItemStack stack)
        {
            super(player, stack);
        }
    }

    /**
     * <p>Fired after a player has shot a bullet.</p>
     *
     * @author Ocelot
     */
    public static class Post extends GunFireEvent {
        public Post(Player player, ItemStack stack) {
            super(player, stack);
            if (stack.getTag() != null) {
                Item var4 = stack.getItem();
                if (var4 instanceof AnimatedGunItem gunItem) {
                    long id = GeoItem.getId(stack);
                    AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
                    animationController.forceAnimationReset();
                    if (ModSyncedDataKeys.AIMING.getValue(player)) {
                        animationController.tryTriggerAnimation("aim_shoot");
                    } else {
                        animationController.tryTriggerAnimation("shoot");
                    }
                }
            }

        }
        public LivingEntity getShooter() {
            return this.getEntity();
        }
    }
}
