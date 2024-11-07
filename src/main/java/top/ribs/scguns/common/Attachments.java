package top.ribs.scguns.common;

import top.ribs.scguns.item.attachment.impl.Scope;

/**
 * Author: MrCrayfish
 */
public class Attachments
{
    public static final Scope LONG_SCOPE = Scope.builder().aimFovModifier(0.15F).modifiers(GunModifiers.SLOWER_ADS).build();
    public static final Scope MEDIUM_SCOPE = Scope.builder().aimFovModifier(0.25F).modifiers(GunModifiers.SLOWER_ADS).build();
    public static final Scope REFLEX_SIGHT = Scope.builder().aimFovModifier(1.3F).modifiers(GunModifiers.SLOW_ADS).build();
    public static final Scope LASER_SIGHT = Scope.builder().aimFovModifier(1.1F).modifiers(GunModifiers.NORMAL_ADS).build();
}
