package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class RocketTrailParticle extends TextureSheetParticle {

    protected RocketTrailParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.lifetime = 10;
        this.quadSize *= 1.0F;
        this.yd *= 0.1;
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
    @Override
    public void tick() {
        super.tick();
        this.xd *= 0.9;
        this.zd *= 0.9;
        this.yd = Math.max(this.yd * 0.9, -0.01);
        if (this.age >= this.lifetime) {
            this.remove();
        }
    }
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            RocketTrailParticle particle = new RocketTrailParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}

