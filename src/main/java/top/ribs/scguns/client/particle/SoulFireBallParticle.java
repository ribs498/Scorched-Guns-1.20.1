package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulFireBallParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float initialAlpha;
    private final float initialSize;
    private final float gravity;

    protected SoulFireBallParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);

        this.lifetime = 8 + this.random.nextInt(6);

        this.initialSize = 0.3F + this.random.nextFloat() * 0.4F;
        this.quadSize = this.initialSize;
        this.sprites = pSprites;

        this.pickSprite(pSprites);

        this.xd = pXSpeed + (this.random.nextDouble() - 0.5) * 0.1;
        this.yd = pYSpeed + (this.random.nextDouble() - 0.5) * 0.1;
        this.zd = pZSpeed + (this.random.nextDouble() - 0.5) * 0.1;

        this.gravity = -0.00F + this.random.nextFloat() * -0.00F;

        this.hasPhysics = true;
        this.friction = 0.96F;

        this.initialAlpha = 0.9F + this.random.nextFloat() * 0.1F;
        this.alpha = this.initialAlpha;

    }

    @Override
    public void tick() {
        super.tick();

        float ageRatio = (float) this.age / (float) this.lifetime;
        this.setSpriteFromAge(this.sprites);

        this.yd += this.gravity;

        if (ageRatio < 0.3F) {
            this.quadSize = this.initialSize * (1.0F + ageRatio * 0.5F);
        } else if (ageRatio < 0.7F) {
            float midRatio = (ageRatio - 0.3F) / 0.4F;
            this.quadSize = this.initialSize * (1.15F - midRatio * 0.1F);

        } else {
            float fadeRatio = (ageRatio - 0.7F) / 0.3F;
            this.quadSize = this.initialSize * (1.05F - fadeRatio * 0.6F);
            this.alpha = this.initialAlpha * (1.0F - fadeRatio);
        }
        this.quadSize = Math.max(this.quadSize, 0.1F);
        this.alpha = Math.max(this.alpha, 0.0F);
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
            return new SoulFireBallParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprites);
        }
    }
}