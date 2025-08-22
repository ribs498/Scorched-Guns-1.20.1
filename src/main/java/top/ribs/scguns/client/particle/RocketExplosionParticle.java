package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RocketExplosionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float initialAlpha;
    private final float initialRed;
    private final float initialGreen;
    private final float initialBlue;

    protected RocketExplosionParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pSizeMultiplier, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, 0.0, 0.0, 0.0);

        this.lifetime = 13 + this.random.nextInt(4);

        float baseSize = 3.0F;
        this.quadSize = baseSize * (float)Math.max(1.0, pSizeMultiplier);
        this.sprites = pSprites;

        this.pickSprite(pSprites);

        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.hasPhysics = false;

        this.initialAlpha = 1.0F;
        this.alpha = this.initialAlpha;

        this.initialRed = 3.0F;
        this.initialGreen = 3.0F;
        this.initialBlue = 3.0F;

        this.rCol = this.initialRed;
        this.gCol = this.initialGreen;
        this.bCol = this.initialBlue;
    }

    @Override
    public void tick() {
        super.tick();

        float ageRatio = (float) this.age / (float) this.lifetime;
        this.setSpriteFromAge(this.sprites);

//        if (ageRatio > 0.7F) {
//            this.alpha = this.initialAlpha * (1.0F - (ageRatio - 0.7F) / 0.3F);
//        }

        this.quadSize *= 1.08F;

        if (ageRatio > 0.5F) {
            float fadeRatio = (ageRatio - 0.5F) / 0.5F;

            float brightnessMultiplier = 1.0F - (fadeRatio);

            this.rCol = this.initialRed * brightnessMultiplier;
            this.gCol = this.initialGreen * brightnessMultiplier;
            this.bCol = this.initialBlue * brightnessMultiplier;

            this.rCol = Math.max(this.rCol, 1F);
            this.gCol = Math.max(this.gCol, 1F);
            this.bCol = Math.max(this.bCol, 1F);
        }
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites) {
            this.sprites = pSprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new RocketExplosionParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
        }
    }
}