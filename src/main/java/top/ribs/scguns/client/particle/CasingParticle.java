package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CasingParticle extends TextureSheetParticle {
    CasingParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.gravity = 0.75F;
        this.friction = 0.999F;
        this.hasPhysics = true;
        this.xd *= 0.8F;
        this.yd *= 0.8F;
        this.zd *= 0.8F;
        this.yd = this.random.nextFloat() * 0.225F + 0.22F;
        this.quadSize = 0.35F;
        this.lifetime = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float pScaleFactor) {
        float f = ((float) this.age + pScaleFactor) / (float) this.lifetime;
        return this.quadSize * (1.0F - f * f);
    }

    public void tick() {
        super.tick();
        if (!this.removed) {
            float f = (float) this.age / (float) this.lifetime;
            if (this.random.nextFloat() > f) {
                //this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            CasingParticle casingParticle = new CasingParticle(pLevel, pX, pY, pZ);
            casingParticle.setSpriteFromAge(this.sprite);
            return casingParticle;
        }
    }
}