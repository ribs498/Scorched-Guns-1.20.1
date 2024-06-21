package top.ribs.scguns.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import top.ribs.scguns.Config;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModSounds;

public class StunRingingSound extends AbstractTickableSoundInstance
{
    public StunRingingSound()
    {
        super(ModSounds.ENTITY_STUN_GRENADE_RING.get(), SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.looping = true;
        this.attenuation = Attenuation.NONE;
        this.tick();
    }

    @Override
    public void tick()
    {
        Player player = Minecraft.getInstance().player;
        if(player != null && player.isAlive())
        {
            MobEffectInstance effect = player.getEffect(ModEffects.DEAFENED.get());
            if(effect != null)
            {
                this.x = (float) player.getX();
                this.y = (float) player.getY();
                this.z = (float) player.getZ();
                float percent = Math.min((effect.getDuration() / (float) Config.SERVER.soundFadeThreshold.get()), 1);
                this.volume = (float) (percent * Config.SERVER.ringVolume.get());
                return;
            }
        }

        //Stops playing the sound
        this.stop();
    }
}