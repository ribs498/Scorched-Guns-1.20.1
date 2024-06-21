package top.ribs.scguns.item.attachment;

import top.ribs.scguns.item.BarrelItem;
import top.ribs.scguns.item.attachment.impl.Barrel;

/**
 * An interface to turn an any item into a barrel attachment. This is useful if your item extends a
 * custom item class otherwise {@link BarrelItem} can be used instead of
 * this interface.
 * <p>
 * Author: Ocelot, MrCrayfish
 */
public interface IBarrel extends IAttachment<Barrel>
{
    /**
     * @return The type of this attachment
     */
    @Override
    default Type getType()
    {
        return Type.BARREL;
    }
}
