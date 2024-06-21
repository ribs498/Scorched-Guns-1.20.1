package top.ribs.scguns.item.attachment;

import top.ribs.scguns.item.UnderBarrelItem;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;

/**
 * An interface to turn an any item into a under barrel attachment. This is useful if your item
 * extends a custom item class otherwise {@link UnderBarrelItem} can be
 * used instead of this interface.
 * <p>
 * Author: MrCrayfish
 */
public interface IUnderBarrel extends IAttachment<UnderBarrel>
{
    /**
     * @return The type of this attachment
     */
    @Override
    default Type getType()
    {
        return Type.UNDER_BARREL;
    }
}
