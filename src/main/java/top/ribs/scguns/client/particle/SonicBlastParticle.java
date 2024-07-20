package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class SonicBlastParticle extends RisingParticle {
    SonicBlastParticle(ClientLevel p_106800_, double p_106801_, double p_106802_, double p_106803_, double p_106804_, double p_106805_, double p_106806_) {
        super(p_106800_, p_106801_, p_106802_, p_106803_, p_106804_, p_106805_, p_106806_);
        this.lifetime = 10; // Reduced lifetime for quicker dissolution
        this.quadSize = 0.5F; // Initial size (can be adjusted)
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double p_106817_, double p_106818_, double p_106819_) {
        this.setBoundingBox(this.getBoundingBox().move(p_106817_, p_106818_, p_106819_));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float p_106824_) {
        float f = ((float) this.age + p_106824_) / (float) this.lifetime;
        return this.quadSize * (1.0F + f * f * 2.0F); // Scales quicker
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            // Increase transparency over time
            this.setAlpha(1.0F - ((float) this.age / (float) this.lifetime));
        }
    }

    @Override
    public int getLightColor(float p_106821_) {
        float f = ((float) this.age + p_106821_) / (float) this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(p_106821_);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int) (f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SonicBlastParticle ringParticle = new SonicBlastParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            ringParticle.pickSprite(this.sprite);
            ringParticle.setAlpha(0.75F);
            return ringParticle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SmallFlameProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SmallFlameProvider(SpriteSet p_172113_) {
            this.sprite = p_172113_;
        }

        public Particle createParticle(SimpleParticleType p_172124_, ClientLevel p_172125_, double p_172126_, double p_172127_, double p_172128_, double p_172129_, double p_172130_, double p_172131_) {
            SonicBlastParticle greenFlameParticle = new SonicBlastParticle(p_172125_, p_172126_, p_172127_, p_172128_, p_172129_, p_172130_, p_172131_);
            greenFlameParticle.pickSprite(this.sprite);
            greenFlameParticle.scale(0.5F);
            return greenFlameParticle;
        }
    }
}
