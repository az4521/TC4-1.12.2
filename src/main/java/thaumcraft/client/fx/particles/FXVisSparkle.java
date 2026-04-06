package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FXVisSparkle extends Particle {
   private double targetX;
   private double targetY;
   private double targetZ;
   float sizeMod = 0.0F;

   public FXVisSparkle(World par1World, double par2, double par4, double par6, double tx, double ty, double tz) {
      super(par1World, par2, par4, par6, 0.0F, 0.0F, 0.0F);
      this.particleRed = this.particleGreen = this.particleBlue = 0.6F;
      this.particleScale = 0.0F;
      this.targetX = tx;
      this.targetY = ty;
      this.targetZ = tz;
      this.particleMaxAge = 1000;
      float f3 = 0.01F;
      this.motionX = (float)this.rand.nextGaussian() * f3;
      this.motionY = (float)this.rand.nextGaussian() * f3;
      this.motionZ = (float)this.rand.nextGaussian() * f3;
      this.sizeMod = (float)(45 + this.rand.nextInt(15));
      this.particleRed = 0.2F;
      this.particleGreen = 0.6F + this.rand.nextFloat() * 0.3F;
      this.particleBlue = 0.2F;
      this.particleGravity = 0.2F;
      this.canCollide = false;
      EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      int visibleDistance = 64;
      if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
         visibleDistance = 32;
      }

      if (renderentity.getDistance(this.posX, this.posY, this.posZ) > (double)visibleDistance) {
         this.particleMaxAge = 0;
      }

   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      float bob = MathHelper.sin((float)this.particleAge / 3.0F) * 0.3F + 6.0F;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
      int part = this.particleAge % 16;
      float var8 = (float)part / 16.0F;
      float var9 = var8 + 0.0624375F;
      float var10 = 0.5F;
      float var11 = var10 + 0.0624375F;
      float var12 = 0.1F * this.particleScale * bob;
      float var13 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float var14 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float var15 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      float var16 = 1.0F;
      buffer.pos(var13 - f1 * var12 - f4 * var12, var14 - f2 * var12, var15 - f3 * var12 - f5 * var12).tex(var9, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(var13 - f1 * var12 + f4 * var12, var14 + f2 * var12, var15 - f3 * var12 + f5 * var12).tex(var9, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(var13 + f1 * var12 + f4 * var12, var14 + f2 * var12, var15 + f3 * var12 + f5 * var12).tex(var8, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(var13 + f1 * var12 - f4 * var12, var14 - f2 * var12, var15 + f3 * var12 - f5 * var12).tex(var8, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      } else {
         this.move(this.motionX, this.motionY, this.motionZ);
         this.motionX *= 0.985;
         this.motionY *= 0.985;
         this.motionZ *= 0.985;
         double dx = this.targetX - this.posX;
         double dy = this.targetY - this.posY;
         double dz = this.targetZ - this.posZ;
         double d13 = 0.1F;
         double d11 = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
         if (d11 < (double)2.0F) {
            this.particleScale *= 0.95F;
         }

         if (d11 < 0.2) {
            this.particleMaxAge = this.particleAge;
         }

         if (this.particleAge < 10) {
            this.particleScale = (float)this.particleAge / this.sizeMod;
         }

         dx /= d11;
         dy /= d11;
         dz /= d11;
         this.motionX += dx * d13;
         this.motionY += dy * d13;
         this.motionZ += dz * d13;
         this.motionX = MathHelper.clamp((float)this.motionX, -0.1F, 0.1F);
         this.motionY = MathHelper.clamp((float)this.motionY, -0.1F, 0.1F);
         this.motionZ = MathHelper.clamp((float)this.motionZ, -0.1F, 0.1F);
      }
   }

   public void setGravity(float value) {
      this.particleGravity = value;
   }
}
