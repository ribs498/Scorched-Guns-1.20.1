package top.ribs.scguns.client.particle;

import top.ribs.scguns.particles.TrailData;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class TrailParticle extends BaseAshSmokeParticle
{
    protected TrailParticle(ClientLevel world, double x, double y, double z, float scale, float red, float green, float blue, SpriteSet spriteWithAge)
    {
        super(world, x, y, z, 0.0F, 0.0F, 0.0F, 0.0, 0.0, 0.0, scale, spriteWithAge, 0.2F, 0, 0, false);
        this.lifetime = 4;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.alpha = 1.0F;
    }

    public int getLightColor(float pPartialTick) {
        return 0xF000F0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<TrailData>
    {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(TrailData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            float red = data.isEnchanted() ? 0.611F : 1.0F;
            float green = data.isEnchanted() ? 0.443F : 1.0F;
            float blue = data.isEnchanted() ? 1.0F : 0.0F;
            return new TrailParticle(worldIn, x, y, z, 2.0F, red, green, blue, this.spriteSet);
        }
    }
}
