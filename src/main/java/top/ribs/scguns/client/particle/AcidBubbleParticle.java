package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class AcidBubbleParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected AcidBubbleParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed,
                                 SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = spriteSet;

        this.lifetime = 20 + this.random.nextInt(10);
        this.gravity = -0.02F;

        float baseScale = 0.7F;
        float scaleVariation = 0.1F;
        this.scale(baseScale + random.nextFloat() * scaleVariation);

        this.xd = xSpeed + (random.nextDouble() - 0.5) * 0.02;
        this.yd = ySpeed + 0.02 + random.nextDouble() * 0.01;
        this.zd = zSpeed + (random.nextDouble() - 0.5) * 0.02;

        this.rCol = 0.56F;
        this.gCol = 0.74F;
        this.bCol = 0.20F;
        this.alpha = 0.8F;

        this.friction = 0.98F;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        this.xd += (this.random.nextDouble() - 0.5) * 0.005;
        this.zd += (this.random.nextDouble() - 0.5) * 0.005;

        if (this.age >= this.lifetime - 6) {
            this.setAlpha(0);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {

        @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                           double x, double y, double z,
                                           double xSpeed, double ySpeed, double zSpeed) {
                return new AcidBubbleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            }
        }
}