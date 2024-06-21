package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HealingGlintParticle extends TextureSheetParticle {
   HealingGlintParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      float f = this.random.nextFloat() * 0.1F + 0.2F;
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.setSize(0.02F, 0.02F);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.5F;
      this.xd *= 0.02F;
      this.yd *= 0.02F;
      this.zd *= 0.02F;
      this.lifetime = (int)(10.0D / (Math.random() * 0.8D + 0.2D));
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public int getLightColor(float pPartialTick) {
      return 240;
   }

   public void move(double pX, double pY, double pZ) {
      this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
      this.setLocationFromBoundingbox();
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.99D;
         this.yd *= 0.99D;
         this.zd *= 0.99D;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         HealingGlintParticle glint = new HealingGlintParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         glint.setColor(1.0F, 1.0F, 1.0F);
         glint.pickSprite(this.sprite);
         glint.setLifetime(10 + pLevel.getRandom().nextInt(5));
         return glint;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class GhostProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public GhostProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         HealingGlintParticle glint = new HealingGlintParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         glint.setColor(1.0F, 1.0F, 1.0F);
         glint.pickSprite(this.sprite);
         glint.setLifetime(5 + pLevel.getRandom().nextInt(5));
         return glint;
      }
   }
}