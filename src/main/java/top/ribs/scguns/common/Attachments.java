package top.ribs.scguns.common;

import top.ribs.scguns.item.attachment.impl.Scope;

/**
 * Author: MrCrayfish
 */
public class Attachments
{
    public static final Scope LONG_SCOPE = Scope.builder()
            .aimFovModifier(0.15F)
            .modifiers(
                    GunModifiers.LONG_SCOPE_SENSITIVITY_COMPENSATION,
                    GunModifiers.SLOWER_ADS,
                    GunModifiers.LONG_SCOPE_RECOIL_REDUCTION
            )
            .build();

    public static final Scope MEDIUM_SCOPE = Scope.builder()
            .aimFovModifier(0.25F)
            .modifiers(
                    GunModifiers.MEDIUM_SCOPE_SENSITIVITY_COMPENSATION,
                    GunModifiers.SLOWER_ADS,
                    GunModifiers.MEDIUM_SCOPE_RECOIL_REDUCTION
            )
            .build();

    public static final Scope REFLEX_SIGHT = Scope.builder()
            .aimFovModifier(1.3F)
            .modifiers(
                    GunModifiers.SLOW_ADS,
                    GunModifiers.REFLEX_SIGHT_CRIT_BONUS,
                    GunModifiers.REFLEX_SIGHT_ADS_BONUS
            )
            .build();

    public static final Scope LASER_SIGHT = Scope.builder()
            .aimFovModifier(1.1F)
            .modifiers(
                    GunModifiers.NORMAL_ADS,
                    GunModifiers.LASER_SIGHT_SPREAD_REDUCTION,
                    GunModifiers.LASER_SIGHT_ADS_BONUS
            )
            .build();
}