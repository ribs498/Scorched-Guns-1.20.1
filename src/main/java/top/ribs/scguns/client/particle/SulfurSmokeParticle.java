package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class SulfurSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SulfurSmokeParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed,
                                  SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = spriteSet;
        float baseScale = 8.0F;
        float scaleVariation = 4.0F;
        this.scale(baseScale + random.nextFloat() * scaleVariation);
        this.setSize(0.5F, 0.5F);
        this.lifetime = 100 + this.random.nextInt(150);
        this.gravity = 0.001F;
        this.xd = xSpeed * 0.1;
        this.yd = ySpeed * 0.1;
        this.zd = zSpeed * 0.1;
        this.setSpriteFromAge(spriteSet);
        this.rCol = 0.9F;
        this.gCol = 0.9F;
        this.bCol = 0.2F;
        this.alpha = 0.6F + random.nextFloat() * 0.2F;
        this.friction = 0.96F;
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.yd += 0.0001D;
        this.xd += (this.random.nextDouble() - 0.5) * 0.0001;
        this.zd += (this.random.nextDouble() - 0.5) * 0.0001;

        if (this.age >= this.lifetime / 2) {
            this.setAlpha(this.alpha * (1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime));
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

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new SulfurSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }

    public static class SulfurSmokeParticleOptions implements ParticleOptions {
        private final int lifetime;

        public SulfurSmokeParticleOptions(int lifetime) {
            this.lifetime = lifetime;
        }

        public int getLifetime() {
            return lifetime;
        }

        @Override
        public ParticleType<?> getType() {
            return null;
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {

        }

        @Override
        public String writeToString() {
            return null;
        }
    }
}