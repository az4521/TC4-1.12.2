package thaumcraft.client.fx.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;

public class FXGeneric extends Particle {
   boolean loop = false;
   int delay = 0;
   int startParticle = 0;
   int numParticles = 1;
   int particleInc = 1;

   public FXGeneric(World world, double x, double y, double z, double xx, double yy, double zz) {
      super(world, x, y, z, xx, yy, zz);
      this.setSize(0.1F, 0.1F);
      this.canCollide = true;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.motionX = xx;
      this.motionY = yy;
      this.motionZ = zz;
      this.canCollide = false;
   }

   public void setLoop(boolean loop) {
      this.loop = loop;
   }

   public void setScale(float scale) {
      this.particleScale = scale;
   }

   public void setMaxAge(int max, int delay) {
      this.particleMaxAge = max;
      this.particleMaxAge += delay;
      this.delay = delay;
   }

   public void setParticles(int startParticle, int numParticles, int particleInc) {
      this.startParticle = startParticle;
      this.numParticles = numParticles;
      this.particleInc = particleInc;
      this.setParticleTextureIndex(startParticle);
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.loop) {
         this.setParticleTextureIndex(this.startParticle + this.particleAge / this.particleInc % this.numParticles);
      } else {
         float fs = (float)this.particleAge / (float)this.particleMaxAge;
         this.setParticleTextureIndex((int)((float)this.startParticle + Math.min((float)this.numParticles * fs, (float)(this.numParticles - 1))));
      }

   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      if (this.particleAge >= this.delay) {
         float t = this.particleAlpha;
         if (this.particleAge <= 1 || this.particleAge >= this.particleMaxAge - 1) {
            this.particleAlpha = t / 2.0F;
         }

         // Replicate vanilla Particle.renderParticle but with POSITION_TEX_COLOR format
         float u0 = this.particleTextureJitterX;
         float u1 = u0 + 0.0624375F;
         float v0 = this.particleTextureJitterY;
         float v1 = v0 + 0.0624375F;
         if (this.particleTexture != null) {
            u0 = this.particleTexture.getMinU();
            u1 = this.particleTexture.getMaxU();
            v0 = this.particleTexture.getMinV();
            v1 = this.particleTexture.getMaxV();
         }
         float scale = 0.1F * this.particleScale;
         float px = (float)(this.prevPosX + (this.posX - this.prevPosX) * f - interpPosX);
         float py = (float)(this.prevPosY + (this.posY - this.prevPosY) * f - interpPosY);
         float pz = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * f - interpPosZ);
         buffer.pos(px - f1 * scale - f4 * scale, py - f2 * scale, pz - f3 * scale - f5 * scale).tex(u1, v1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex();
         buffer.pos(px - f1 * scale + f4 * scale, py + f2 * scale, pz - f3 * scale + f5 * scale).tex(u1, v0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex();
         buffer.pos(px + f1 * scale + f4 * scale, py + f2 * scale, pz + f3 * scale + f5 * scale).tex(u0, v0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex();
         buffer.pos(px + f1 * scale - f4 * scale, py - f2 * scale, pz + f3 * scale - f5 * scale).tex(u0, v1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex();

         this.particleAlpha = t;
      }
   }
}
