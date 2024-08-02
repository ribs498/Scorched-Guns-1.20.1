package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class SulfurDustParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SulfurDustParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed,
                                 SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = spriteSet;

        this.lifetime = 100 + this.random.nextInt(50);
        this.gravity = 0.01F;

        float baseScale = 0.5F;
        float scaleVariation = 0.3F;
        this.scale(baseScale + random.nextFloat() * scaleVariation);

        this.xd = xSpeed * 0.01;
        this.yd = ySpeed * 0.01;
        this.zd = zSpeed * 0.01;

        this.rCol = 0.9F;
        this.gCol = 0.9F;
        this.bCol = 0.2F;
        this.alpha = 0.6F + random.nextFloat() * 0.2F;

        this.friction = 0.96F;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        if (this.onGround) {
            this.yd = 0.08; // Small upward movement when on ground
        }

        // Random horizontal movement
        this.xd += (this.random.nextDouble() - 0.5) * 0.01;
        this.zd += (this.random.nextDouble() - 0.5) * 0.01;

        if (this.age >= this.lifetime / 2) {
            this.setAlpha(this.alpha * (1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new SulfurDustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}