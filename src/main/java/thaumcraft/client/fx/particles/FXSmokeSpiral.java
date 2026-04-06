package thaumcraft.client.fx.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class FXSmokeSpiral extends Particle {
   private float radius = 1.0F;
   private int start = 0;
   private int miny = 0;

   public FXSmokeSpiral(World world, double d, double d1, double d2, float radius, int start, int miny) {
      super(world, d, d1, d2, 0.0F, 0.0F, 0.0F);
      this.particleGravity = -0.01F;
      this.motionX = this.motionY = this.motionZ = 0.0F;
      this.particleScale *= 1.0F;
      this.particleMaxAge = 20 + world.rand.nextInt(10);
      this.canCollide = true;
      this.setSize(0.01F, 0.01F);
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.radius = radius;
      this.start = start;
      this.miny = miny;
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.66F * this.particleAlpha);
      int particle = (int)(1.0F + (float)this.particleAge / (float)this.particleMaxAge * 4.0F);
      float r1 = (float)this.start + 720.0F * (((float)this.particleAge + f) / (float)this.particleMaxAge);
      float r2 = 90.0F - 180.0F * (((float)this.particleAge + f) / (float)this.particleMaxAge);
      float mX = -MathHelper.sin(r1 / 180.0F * (float)Math.PI) * MathHelper.cos(r2 / 180.0F * (float)Math.PI);
      float mZ = MathHelper.cos(r1 / 180.0F * (float)Math.PI) * MathHelper.cos(r2 / 180.0F * (float)Math.PI);
      float mY = -MathHelper.sin(r2 / 180.0F * (float)Math.PI);
      mX *= this.radius;
      mY *= this.radius;
      mZ *= this.radius;
      float var8 = (float)(particle % 16) / 16.0F;
      float var9 = var8 + 0.0624375F;
      float var10 = (float)(particle / 16) / 16.0F;
      float var11 = var10 + 0.0624375F;
      float var12 = 0.15F * this.particleScale;
      float var13 = (float)(this.posX + (double)mX - interpPosX);
      float var14 = (float)(Math.max(this.posY + (double)mY, (float)this.miny + 0.1F) - interpPosY);
      float var15 = (float)(this.posZ + (double)mZ - interpPosZ);
      float var16 = 1.0F;
      buffer.pos(var13 - f1 * var12 - f4 * var12, var14 - f2 * var12, var15 - f3 * var12 - f5 * var12).tex(var9, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66F * this.particleAlpha)
        .endVertex();
      buffer.pos(var13 - f1 * var12 + f4 * var12, var14 + f2 * var12, var15 - f3 * var12 + f5 * var12).tex(var9, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66F * this.particleAlpha)
        .endVertex();
      buffer.pos(var13 + f1 * var12 + f4 * var12, var14 + f2 * var12, var15 + f3 * var12 + f5 * var12).tex(var8, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66F * this.particleAlpha)
        .endVertex();
      buffer.pos(var13 + f1 * var12 - f4 * var12, var14 - f2 * var12, var15 + f3 * var12 - f5 * var12).tex(var8, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66F * this.particleAlpha)
        .endVertex();
   }

   public int getFXLayer() {
      return 1;
   }

   public void onUpdate() {
      this.setAlphaF((float)(this.particleMaxAge - this.particleAge) / (float)this.particleMaxAge);
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
      if (!this.world.isAirBlock(new BlockPos(var7, var8, var9))) {
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
