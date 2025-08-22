package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
@OnlyIn(Dist.CLIENT)
public class LaserParticle extends TextureSheetParticle {

   protected LaserParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.setColor(1.0F, 0.0F, 0.0F);
      this.scale(xSpeed > 0 ? (float)xSpeed : 0.75F);
      this.lifetime = 3;
      this.gravity = 0.0F;
   }

   @Override
   public @NotNull ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @Override
   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      }
   }
   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;
      public Provider(SpriteSet sprite) {
         this.sprite = sprite;
      }
      @Override
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         LaserParticle particle = new LaserParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
         particle.pickSprite(this.sprite);
         particle.scale(0.5F);
         return particle;
      }
   }
}