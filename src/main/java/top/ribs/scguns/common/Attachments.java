package top.ribs.scguns.common;

import top.ribs.scguns.item.attachment.impl.Scope;

/**
 * Author: MrCrayfish
 */
public class Attachments
{
    public static final Scope LONG_SCOPE = Scope.builder().aimFovModifier(0.3F).modifiers(GunModifiers.SLOWEST_ADS).build();
    public static final Scope MEDIUM_SCOPE = Scope.builder().aimFovModifier(0.5F).modifiers(GunModifiers.SLOWER_ADS).build();
    public static final Scope REFLEX_SIGHT = Scope.builder().aimFovModifier(0.8F).modifiers(GunModifiers.SLOW_ADS).build();
}
