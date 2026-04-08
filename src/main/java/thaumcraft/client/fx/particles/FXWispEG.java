package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class FXWispEG extends Particle {
   public final World world;
   Entity target = null;
   double rx = 0.0F;
   double ry = 0.0F;
   double rz = 0.0F;
   public int blendmode = 1;

   public FXWispEG(World world, double posX, double posY, double posZ, Entity target2) {
      super(world, posX, posY, posZ, 0.0F, 0.0F, 0.0F);
      this.world = world;
      this.target = target2;
      this.motionX = this.rand.nextGaussian() * 0.03;
      this.motionY = -0.05;
      this.motionZ = this.rand.nextGaussian() * 0.03;
      this.particleScale *= 0.4F;
      this.particleMaxAge = (int)((double)40.0F / (Math.random() * 0.3 + 0.7));
      this.canCollide = true;
      this.setSize(0.01F, 0.01F);
      EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      int visibleDistance = 50;
      if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
         visibleDistance = 25;
      }

      if (renderentity.getDistance(posX, posY, posZ) > (double)visibleDistance) {
         this.particleMaxAge = 0;
      }

      this.prevPosX = posX;
      this.prevPosY = posY;
      this.prevPosZ = posZ;
      this.blendmode = 771;
      this.particleRed = this.rand.nextFloat() * 0.05F;
      this.particleGreen = this.rand.nextFloat() * 0.05F;
      this.particleBlue = this.rand.nextFloat() * 0.05F;
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      Entity e = Minecraft.getMinecraft().getRenderViewEntity();
      float agescale = 1.0F - (float)this.particleAge / (float)this.particleMaxAge;
      float d6 = 1024.0F;
      double _dx = this.posX - e.posX, _dy = this.posY - e.posY, _dz = this.posZ - e.posZ;
      float base = (float)((double)1.0F - Math.min(d6, _dx*_dx + _dy*_dy + _dz*_dz) / (double)d6);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F * base);
      float f10 = 0.5F * this.particleScale;
      float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      float var8 = (float)(this.particleAge % 13) / 16.0F;
      float var9 = var8 + 0.0624375F;
      float var10 = 0.1875F;
      float var11 = var10 + 0.0624375F;
      buffer.pos(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10).tex(var9, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.2F * agescale * base)
        .endVertex();
      buffer.pos(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10).tex(var9, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.2F * agescale * base)
        .endVertex();
      buffer.pos(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10, f13 + f3 * f10 + f5 * f10).tex(var8, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.2F * agescale * base)
        .endVertex();
      buffer.pos(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10, f13 + f3 * f10 - f5 * f10).tex(var8, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.2F * agescale * base)
        .endVertex();
   }

   public int getFXLayer() {
      return this.blendmode == 1 ? 0 : 1;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.target != null && !this.onGround) {
         this.posX += this.target.motionX;
         this.posZ += this.target.motionZ;
      }

      this.pushOutOfBlocks(this.posX, this.posY, this.posZ);
      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.98F;
      this.motionY *= 0.98F;
      this.motionZ *= 0.98F;
      if (this.onGround) {
         this.motionX *= 0.8500000190734863;
         this.motionZ *= 0.8500000190734863;
      }

      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

   }

   protected boolean pushOutOfBlocks(double par1, double par3, double par5) {
      int var7 = MathHelper.floor(par1);
      int var8 = MathHelper.floor(par3);
      int var9 = MathHelper.floor(par5);
      double var10 = par1 - (double)var7;
      double var12 = par3 - (double)var8;
      double var14 = par5 - (double)var9;
      if (!this.world.isAirBlock(new BlockPos(var7, var8, var9)) && this.world.getBlockState(new BlockPos(var7, var8, var9)).getMaterial().blocksMovement()) {
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

   public void setGravity(float value) {
      this.particleGravity = value;
   }
}
