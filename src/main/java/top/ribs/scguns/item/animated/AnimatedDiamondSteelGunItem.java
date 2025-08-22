package top.ribs.scguns.item.animated;

import net.minecraft.sounds.SoundEvent;
import top.ribs.scguns.item.animated.AnimatedGunItem;

public class AnimatedDiamondSteelGunItem extends AnimatedGunItem {
    public AnimatedDiamondSteelGunItem(Properties properties, String path, SoundEvent reloadSoundMagOut, SoundEvent reloadSoundMagIn, SoundEvent reloadSoundEnd, SoundEvent boltPullSound, SoundEvent boltReleaseSound) {
        super(properties, path, reloadSoundMagOut, reloadSoundMagIn, reloadSoundEnd, boltPullSound, boltReleaseSound);
    }
    @Override
    public int getEnchantmentValue() {
        return 27;
    }
}
