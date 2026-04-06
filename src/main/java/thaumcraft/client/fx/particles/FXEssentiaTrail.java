package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.client.FMLClientHandler;
import java.awt.Color;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class FXEssentiaTrail extends Particle {
   private double targetX;
   private double targetY;
   private double targetZ;
   private int count = 0;
   public int particle = 24;

   public FXEssentiaTrail(World par1World, double par2, double par4, double par6, double tx, double ty, double tz, int count, int color, float scale) {
      super(par1World, par2, par4, par6, 0.0F, 0.0F, 0.0F);
      this.particleRed = this.particleGreen = this.particleBlue = 0.6F;
      this.particleScale = (MathHelper.sin((float)count / 2.0F) * 0.1F + 1.0F) * scale;
      this.count = count;
      this.targetX = tx;
      this.targetY = ty;
      this.targetZ = tz;
      double dx = tx - this.posX;
      double dy = ty - this.posY;
      double dz = tz - this.posZ;
      int base = (int)(MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 30.0F);
      if (base < 1) {
         base = 1;
      }

      this.particleMaxAge = base / 2 + this.rand.nextInt(base);
      this.motionX = (double)(MathHelper.sin((float)count / 4.0F) * 0.015F) + this.rand.nextGaussian() * (double)0.002F;
      this.motionY = 0.1F + MathHelper.sin((float)count / 3.0F) * 0.01F;
      this.motionZ = (double)(MathHelper.sin((float)count / 2.0F) * 0.015F) + this.rand.nextGaussian() * (double)0.002F;
      Color c = new Color(color);
      float mr = (float)c.getRed() / 255.0F * 0.2F;
      float mg = (float)c.getGreen() / 255.0F * 0.2F;
      float mb = (float)c.getBlue() / 255.0F * 0.2F;
      this.particleRed = (float)c.getRed() / 255.0F - mr + this.rand.nextFloat() * mr;
      this.particleGreen = (float)c.getGreen() / 255.0F - mg + this.rand.nextFloat() * mg;
      this.particleBlue = (float)c.getBlue() / 255.0F - mb + this.rand.nextFloat() * mb;
      this.particleGravity = 0.2F;
      this.canCollide = true;

      try {
         EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
         int visibleDistance = 64;
         if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
            visibleDistance = 32;
         }

         if (renderentity.getDistance(this.posX, this.posY, this.posZ) > (double)visibleDistance) {
            this.particleMaxAge = 0;
         }
      } catch (Exception ignored) {
      }

   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      float t2 = 0.5625F;
      float t3 = 0.625F;
      float t4 = 0.0625F;
      float t5 = 0.125F;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      int var10000 = this.particle + this.particleAge % 16;
      float s = MathHelper.sin((float)(this.particleAge - this.count) / 5.0F) * 0.25F + 1.0F;
      float var12 = 0.1F * this.particleScale * s;
      float var13 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float var14 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float var15 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      float var16 = 1.0F;
      buffer.pos(var13 - f1 * var12 - f4 * var12, var14 - f2 * var12, var15 - f3 * var12 - f5 * var12).tex(t2, t5).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(var13 - f1 * var12 + f4 * var12, var14 + f2 * var12, var15 - f3 * var12 + f5 * var12).tex(t3, t5).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(var13 + f1 * var12 + f4 * var12, var14 + f2 * var12, var15 + f3 * var12 + f5 * var12).tex(t3, t4).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(var13 + f1 * var12 - f4 * var12, var14 - f2 * var12, var15 + f3 * var12 - f5 * var12).tex(t2, t4).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
   }

   public int getFXLayer() {
      return 1;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      } else {
         this.motionY += 0.01 * (double)this.particleGravity;
         if (this.canCollide) {
            this.pushOutOfBlocks(this.posX, this.posY, this.posZ);
         }

         this.move(this.motionX, this.motionY, this.motionZ);
         this.motionX *= 0.985;
         this.motionY *= 0.985;
         this.motionZ *= 0.985;
         this.motionX = MathHelper.clamp((float)this.motionX, -0.05F, 0.05F);
         this.motionY = MathHelper.clamp((float)this.motionY, -0.05F, 0.05F);
         this.motionZ = MathHelper.clamp((float)this.motionZ, -0.05F, 0.05F);
         double dx = this.targetX - this.posX;
         double dy = this.targetY - this.posY;
         double dz = this.targetZ - this.posZ;
         double d13 = 0.01;
         double d11 = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
         if (d11 < (double)2.0F) {
            this.particleScale *= 0.98F;
         }

         if (this.particleScale < 0.2F) {
            this.setExpired();
         } else {
            dx /= d11;
            dy /= d11;
            dz /= d11;
            this.motionX += dx * (d13 / Math.min(1.0F, d11));
            this.motionY += dy * (d13 / Math.min(1.0F, d11));
            this.motionZ += dz * (d13 / Math.min(1.0F, d11));
         }
      }
   }

   public void setGravity(float value) {
      this.particleGravity = value;
   }

   protected boolean pushOutOfBlocks(double par1, double par3, double par5) {
      int var7 = MathHelper.floor(par1);
      int var8 = MathHelper.floor(par3);
      int var9 = MathHelper.floor(par5);
      double var10 = par1 - (double)var7;
      double var12 = par3 - (double)var8;
      double var14 = par5 - (double)var9;
      if (!this.world.isAirBlock(new BlockPos(var7, var8, var9)) && this.world.getBlockState(new BlockPos(var7, var8, var9)).getMaterial().blocksMovement() ) {
         boolean var16 = !this.world.getBlockState(new BlockPos(var7-1,var8,var9)).getMaterial().blocksMovement();
         boolean var17 = !this.world.getBlockState(new BlockPos(var7+1,var8,var9)).getMaterial().blocksMovement();
         boolean var18 = !this.world.getBlockState(new BlockPos(var7,var8-1,var9)).getMaterial().blocksMovement();
         boolean var19 = !this.world.getBlockState(new BlockPos(var7,var8+1,var9)).getMaterial().blocksMovement();
         boolean var20 = !this.world.getBlockState(new BlockPos(var7,var8,var9-1)).getMaterial().blocksMovement();
         boolean var21 = !this.world.getBlockState(new BlockPos(var7,var8,var9+1)).getMaterial().blocksMovement();
         byte var22 = -1;
         double var23 = 9999.0F;
         if (var16 && var10 < var23) {
            var23 = var10;
            var22 = 0;
         }

         if (var17 && (double)1.0F - var10 < var23) {
            var23 = (double)1.0F - var10;
            var22 = 1;
         }

         if (var18 && var12 < var23) {
            var23 = var12;
            var22 = 2;
         }

         if (var19 && (double)1.0F - var12 < var23) {
            var23 = (double)1.0F - var12;
            var22 = 3;
         }

         if (var20 && var14 < var23) {
            var23 = var14;
            var22 = 4;
         }

         if (var21 && (double)1.0F - var14 < var23) {
            var23 = (double)1.0F - var14;
            var22 = 5;
         }

         float var25 = this.rand.nextFloat() * 0.05F + 0.025F;
         float var26 = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F;
         if (var22 == 0) {
            this.motionX = -var25;
            this.motionY = this.motionZ = var26;
         }

         if (var22 == 1) {
            this.motionX = var25;
            this.motionY = this.motionZ = var26;
         }

         if (var22 == 2) {
            this.motionY = -var25;
            this.motionX = this.motionZ = var26;
         }

         if (var22 == 3) {
            this.motionY = var25;
            this.motionX = this.motionZ = var26;
         }

         if (var22 == 4) {
            this.motionZ = -var25;
            this.motionY = this.motionX = var26;
         }

         if (var22 == 5) {
            this.motionZ = var25;
            this.motionY = this.motionX = var26;
         }

         return true;
      } else {
         return false;
      }
   }
}
