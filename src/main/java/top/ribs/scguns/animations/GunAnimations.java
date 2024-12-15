package top.ribs.scguns.animations;

import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class GunAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation CARBINE_IDLE = RawAnimation.begin().thenLoop("carbine_idle");
    public static final RawAnimation SHOOT;
    public static final RawAnimation SHOOT1;
    public static final RawAnimation CARBINE_SHOOT;
    public static final RawAnimation AIM_SHOOT;
    public static final RawAnimation AIM_SHOOT1;
    public static final RawAnimation CARBINE_AIM_SHOOT;
    public static final RawAnimation RELOAD;
    public static final RawAnimation CARBINE_RELOAD;
    public static final RawAnimation RELOAD_ALT;
    public static final RawAnimation RELOAD_START;
    public static final RawAnimation CARBINE_RELOAD_START;
    public static final RawAnimation RELOAD_LOOP;
    public static final RawAnimation CARBINE_RELOAD_LOOP;
    public static final RawAnimation RELOAD_STOP;
    public static final RawAnimation CARBINE_RELOAD_STOP;
    public static final RawAnimation INSPECT;
    public static final RawAnimation CARBINE_INSPECT;
    public static final RawAnimation DRAW;
    public static final RawAnimation CARBINE_DRAW;
    public static final RawAnimation JAM;

    public GunAnimations() {}

    static {
        INSPECT = RawAnimation.begin().then("inspect", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_INSPECT = RawAnimation.begin().then("carbine_inspect", Animation.LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        SHOOT = RawAnimation.begin().then("shoot", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        SHOOT1 = RawAnimation.begin().then("shoot1", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_SHOOT = RawAnimation.begin().then("carbine_shoot", Animation.LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        AIM_SHOOT = RawAnimation.begin().then("aim_shoot", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        AIM_SHOOT1 = RawAnimation.begin().then("aim_shoot1", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_AIM_SHOOT = RawAnimation.begin().then("carbine_aim_shoot", Animation.LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        RELOAD = RawAnimation.begin().then("reload", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_RELOAD = RawAnimation.begin().then("carbine_reload", Animation.LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        RELOAD_ALT = RawAnimation.begin().then("reload_alt", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        RELOAD_START = RawAnimation.begin().then("reload_start", Animation.LoopType.PLAY_ONCE).thenLoop("reload_loop");
        CARBINE_RELOAD_START = RawAnimation.begin().then("carbine_reload_start", Animation.LoopType.PLAY_ONCE).thenLoop("carbine_reload_loop");
        RELOAD_LOOP = RawAnimation.begin().then("reload_loop", Animation.LoopType.LOOP);
        CARBINE_RELOAD_LOOP = RawAnimation.begin().then("carbine_reload_loop", Animation.LoopType.LOOP);
        RELOAD_STOP = RawAnimation.begin().then("reload_stop", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_RELOAD_STOP = RawAnimation.begin().then("carbine_reload_stop", Animation.LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        DRAW = RawAnimation.begin().then("draw", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
        CARBINE_DRAW = RawAnimation.begin().then("carbine_draw", Animation.LoopType.PLAY_ONCE).thenLoop("carbine_idle");
        JAM = RawAnimation.begin().then("jam", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    }
}