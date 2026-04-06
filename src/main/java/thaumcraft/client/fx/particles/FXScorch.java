package thaumcraft.client.fx.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class FXScorch extends Particle {
   public boolean pvp = true;
   public boolean mobs = true;
   public boolean animals = true;
   private double px;
   private double py;
   private double pz;
   private float transferParticleScale;
   Entity partDestEnt;
   public boolean lance = false;

   public FXScorch(World world, double x, double y, double z, Vec3d v, float spread, boolean lance) {
      super(world, x, y, z, 0.0F, 0.0F, 0.0F);
      this.posX = x;
      this.posY = y;
      this.posZ = z;
      this.lance = lance;
      this.px = x + v.x * (double)100.0F;
      this.py = y + v.y * (double)100.0F;
      this.pz = z + v.z * (double)100.0F;
      if (!lance) {
         this.px += (this.rand.nextFloat() - this.rand.nextFloat()) * spread;
         this.py += (this.rand.nextFloat() - this.rand.nextFloat()) * spread;
         this.pz += (this.rand.nextFloat() - this.rand.nextFloat()) * spread;
      } else {
         this.px += (double)(this.rand.nextFloat() - this.rand.nextFloat()) * (double)0.5F;
         this.py += (double)(this.rand.nextFloat() - this.rand.nextFloat()) * (double)0.5F;
         this.pz += (double)(this.rand.nextFloat() - this.rand.nextFloat()) * (double)0.5F;
      }

      this.transferParticleScale = this.particleScale = this.rand.nextFloat() * 0.5F + 2.0F;
      if (!lance) {
         this.transferParticleScale = this.particleScale = this.rand.nextFloat() + 3.0F;
      }

      this.particleMaxAge = 50;
      this.setSize(0.1F, 0.1F);
      this.setParticleTextureIndex(151);
      this.canCollide = true;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.particleAlpha = 0.66F;
   }

   /** Apply two successive offsets, setting prevPos after the first one. Used by UtilsFX.scorchFx. */
   public void applySpawnOffset(double dx, double dy, double dz) {
      this.posX += dx;
      this.posY += dy;
      this.posZ += dz;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.posX += dx;
      this.posY += dy;
      this.posZ += dz;
   }

   public int getBrightnessForRender(float par1) {
      return 210;
   }

   public float getBrightness(float par1) {
      return 1.0F;
   }

   public void onUpdate() {
      double dx = this.px - this.posX;
      double dy = this.py - this.posY;
      double dz = this.pz - this.posZ;
      double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
      this.motionX = dx / (distance * (double)1.25F);
      this.motionY = dy / (distance * (double)1.25F);
      this.motionZ = dz / (distance * (double)1.25F);
      this.motionX *= (float)(this.particleMaxAge - this.particleAge) / (float)this.particleMaxAge;
      this.motionY *= (float)(this.particleMaxAge - this.particleAge) / (float)this.particleMaxAge;
      this.motionZ *= (float)(this.particleMaxAge - this.particleAge) / (float)this.particleMaxAge;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.motionX += this.rand.nextFloat() * 0.07F - 0.035F;
      this.motionY += this.rand.nextFloat() * 0.07F - 0.035F;
      this.motionZ += this.rand.nextFloat() * 0.07F - 0.035F;
      int var7 = MathHelper.floor(this.posX);
      int var8 = MathHelper.floor(this.posY);
      int var9 = MathHelper.floor(this.posZ);
      if (this.particleAge > 1 && this.world.getBlockState(new BlockPos(var7, var8, var9)).isOpaqueCube()) {
         this.motionX = 0.0F;
         this.motionY = 0.0F;
         this.motionZ = 0.0F;
         this.particleAge += 10;
      }
      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      ++this.particleAge;
      if (this.particleAge >= this.particleMaxAge) {
         this.setExpired();
      }

      float fs = (float)this.particleAge / (float)(this.particleMaxAge - 9);
      if (fs <= 1.0F) {
         this.setParticleTextureIndex((int)(151.0F + fs * 6.0F));
      } else {
         this.setParticleTextureIndex(159 - (this.particleMaxAge - this.particleAge) / 3);
      }

   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      float fs = (float)this.particleAge / (float)this.particleMaxAge;
      this.particleScale = this.transferParticleScale * (fs + 0.25F) * 2.0F;
      float fc = (float)this.particleAge * 9.0F / (float)this.particleMaxAge;
      if (fc > 1.0F) {
         fc = 1.0F;
      }

      this.particleRed = this.particleGreen = fc;
      this.particleBlue = 1.0F;

      // Replicate vanilla Particle.renderParticle with POSITION_TEX_COLOR format
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
   }
}
