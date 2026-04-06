package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.particle.Particle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class FXWisp extends Particle {
   Entity target;
   public boolean shrink;
   float moteParticleScale;
   int moteHalfLife;
   public boolean tinkle;
   public int blendmode;

   public FXWisp(World world, double d, double d1, double d2, float f, float f1, float f2) {
      this(world, d, d1, d2, 1.0F, f, f1, f2);
   }

   public FXWisp(World world, double d, double d1, double d2, float f, float red, float green, float blue) {
      super(world, d, d1, d2, 0.0F, 0.0F, 0.0F);
      this.target = null;
      this.shrink = false;
      this.tinkle = false;
      this.blendmode = 1;
      if (red == 0.0F) {
         red = 1.0F;
      }

      this.particleRed = red;
      this.particleGreen = green;
      this.particleBlue = blue;
      this.particleGravity = 0.0F;
      this.motionX = this.motionY = this.motionZ = 0.0F;
      this.particleScale *= f;
      this.moteParticleScale = this.particleScale;
      this.particleMaxAge = (int)((double)36.0F / (Math.random() * 0.3 + 0.7));
      this.moteHalfLife = this.particleMaxAge / 2;
      this.canCollide = true;
      this.setSize(0.1F, 0.1F);
      EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      int visibleDistance = 50;
      if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
         visibleDistance = 25;
      }

      if (renderentity.getDistance(this.posX, this.posY, this.posZ) > (double)visibleDistance) {
         this.particleMaxAge = 0;
      }

      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
   }

   public FXWisp(World world, double d, double d1, double d2, float f, int type) {
      this(world, d, d1, d2, f, 0.0F, 0.0F, 0.0F);
      switch (type) {
         case 0:
            this.particleRed = 0.75F + world.rand.nextFloat() * 0.25F;
            this.particleGreen = 0.25F + world.rand.nextFloat() * 0.25F;
            this.particleBlue = 0.75F + world.rand.nextFloat() * 0.25F;
            break;
         case 1:
            this.particleRed = 0.5F + world.rand.nextFloat() * 0.3F;
            this.particleGreen = 0.5F + world.rand.nextFloat() * 0.3F;
            this.particleBlue = 0.2F;
            break;
         case 2:
            this.particleRed = 0.2F;
            this.particleGreen = 0.2F;
            this.particleBlue = 0.7F + world.rand.nextFloat() * 0.3F;
            break;
         case 3:
            this.particleRed = 0.2F;
            this.particleGreen = 0.7F + world.rand.nextFloat() * 0.3F;
            this.particleBlue = 0.2F;
            break;
         case 4:
            this.particleRed = 0.7F + world.rand.nextFloat() * 0.3F;
            this.particleGreen = 0.2F;
            this.particleBlue = 0.2F;
            break;
         case 5:
            this.blendmode = 771;
            this.particleRed = world.rand.nextFloat() * 0.1F;
            this.particleGreen = world.rand.nextFloat() * 0.1F;
            this.particleBlue = world.rand.nextFloat() * 0.1F;
            break;
         case 6:
            this.particleRed = 0.8F + world.rand.nextFloat() * 0.2F;
            this.particleGreen = 0.8F + world.rand.nextFloat() * 0.2F;
            this.particleBlue = 0.8F + world.rand.nextFloat() * 0.2F;
            break;
         case 7:
            this.particleRed = 0.7F + world.rand.nextFloat() * 0.3F;
            this.particleGreen = 0.5F + world.rand.nextFloat() * 0.2F;
            this.particleBlue = 0.3F + world.rand.nextFloat() * 0.1F;
      }

   }

   public FXWisp(World world, double d, double d1, double d2, double x, double y, double z, float f, int type) {
      this(world, d, d1, d2, f, type);
      if (this.particleMaxAge > 0) {
         double dx = x - this.posX;
         double dy = y - this.posY;
         double dz = z - this.posZ;
         this.motionX = dx / (double)this.particleMaxAge;
         this.motionY = dy / (double)this.particleMaxAge;
         this.motionZ = dz / (double)this.particleMaxAge;
      }

   }

   public FXWisp(World world, double d, double d1, double d2, Entity tar, int type) {
      this(world, d, d1, d2, 0.4F, type);
      this.target = tar;
   }

   public FXWisp(World world, double d, double d1, double d2, double x, double y, double z, float f, float red, float green, float blue) {
      this(world, d, d1, d2, f, red, green, blue);
      if (this.particleMaxAge > 0) {
         double dx = x - this.posX;
         double dy = y - this.posY;
         double dz = z - this.posZ;
         this.motionX = dx / (double)this.particleMaxAge;
         this.motionY = dy / (double)this.particleMaxAge;
         this.motionZ = dz / (double)this.particleMaxAge;
      }

   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      float agescale = 0.0F;
      if (this.shrink) {
         agescale = ((float)this.particleMaxAge - (float)this.particleAge) / (float)this.particleMaxAge;
      } else {
         agescale = (float)this.particleAge / (float)this.moteHalfLife;
         if (agescale > 1.0F) {
            agescale = 2.0F - agescale;
         }
      }

      this.particleScale = this.moteParticleScale * agescale;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
      float f10 = 0.5F * this.particleScale;
      float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      float var8 = 0.0F;
      float var9 = 0.125F;
      float var10 = 0.875F;
      float var11 = 1.0F;
      buffer.pos(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10).tex(var9, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10).tex(var9, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10, f13 + f3 * f10 + f5 * f10).tex(var8, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10, f13 + f3 * f10 - f5 * f10).tex(var8, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
   }

   public int getFXLayer() {
      return this.blendmode == 1 ? 0 : 1;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge == 0 && this.tinkle && this.world.rand.nextInt(3) == 0) {
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.orb")); if (_snd != null) this.world.playSound(null, new BlockPos(this.posX, this.posY, this.posZ), _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.02F, 0.5F * ((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.6F + 2.0F)); }
      }

      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.motionY -= 0.04 * (double)this.particleGravity;
      if (this.canCollide) {
         this.pushOutOfBlocks(this.posX, this.posY, this.posZ);
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      if (this.target != null) {
         this.motionX *= 0.985;
         this.motionY *= 0.985;
         this.motionZ *= 0.985;
         double dx = this.target.posX - this.posX;
         double dy = this.target.posY + (double)(this.target.height / 2.0F) - this.posY;
         double dz = this.target.posZ - this.posZ;
         double d13 = 0.2;
         double d11 = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
         dx /= d11;
         dy /= d11;
         dz /= d11;
         this.motionX += dx * d13;
         this.motionY += dy * d13;
         this.motionZ += dz * d13;
         this.motionX = MathHelper.clamp((float)this.motionX, -0.2F, 0.2F);
         this.motionY = MathHelper.clamp((float)this.motionY, -0.2F, 0.2F);
         this.motionZ = MathHelper.clamp((float)this.motionZ, -0.2F, 0.2F);
      } else {
         this.motionX *= 0.98F;
         this.motionY *= 0.98F;
         this.motionZ *= 0.98F;
         if (this.onGround) {
            this.motionX *= 0.7F;
            this.motionZ *= 0.7F;
         }
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

   public void setNoClip(boolean v) { this.canCollide = !v; }
}
