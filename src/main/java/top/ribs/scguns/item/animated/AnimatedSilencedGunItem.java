package top.ribs.scguns.item.animated;

import net.minecraft.sounds.SoundEvent;

public class AnimatedSilencedGunItem extends AnimatedGunItem{
    public AnimatedSilencedGunItem(Properties properties, String path, SoundEvent reloadSoundMagOut, SoundEvent reloadSoundMagIn, SoundEvent reloadSoundEnd, SoundEvent boltPullSound, SoundEvent boltReleaseSound) {
        super(properties, path, reloadSoundMagOut, reloadSoundMagIn, reloadSoundEnd, boltPullSound, boltReleaseSound);
    }
}
