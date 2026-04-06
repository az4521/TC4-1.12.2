package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FXWispArcing extends Particle {
   private double coordX;
   private double coordY;
   private double coordZ;
   float moteParticleScale;
   int moteHalfLife;
   public boolean tinkle;
   public int blendmode;

   public FXWispArcing(World world, double d, double d1, double d2, float f, float red, float green, float blue) {
      super(world, d, d1, d2, 0.0F, 0.0F, 0.0F);
      this.tinkle = false;
      this.blendmode = 1;
      if (red == 0.0F) {
         red = 1.0F;
      }

      this.coordX = this.posX = d;
      this.coordY = this.posY = d1;
      this.coordZ = this.posZ = d2;
      this.particleRed = red;
      this.particleGreen = green;
      this.particleBlue = blue;
      this.particleGravity = 0.0F;
      this.particleScale *= f;
      this.moteParticleScale = this.particleScale;
      this.particleMaxAge = (int)((double)36.0F / (Math.random() * 0.3 + 0.7));
      this.moteHalfLife = this.particleMaxAge / 2;
      this.canCollide = true;
      this.setSize(0.01F, 0.01F);
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

   public FXWispArcing(World world, double d, double d1, double d2, double x, double y, double z, float f, float red, float green, float blue) {
      this(world, d, d1, d2, f, red, green, blue);
      this.motionX = x - d;
      this.motionY = y - d1;
      this.motionZ = z - d2;
      this.setPosition(x, y, z);
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      float agescale = 0.0F;
      agescale = (float)this.particleAge / (float)this.moteHalfLife;
      if (agescale > 1.0F) {
         agescale = 2.0F - agescale;
      }

      this.particleScale = this.moteParticleScale * agescale;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
      float f10 = 0.5F * this.particleScale;
      float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      float var8 = 0.0F;
      float var9 = 0.0F;
      float var10 = 0.875F;
      float var11 = 1.0F;
      buffer.pos(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10).tex(var9, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
        .endVertex();
      buffer.pos(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10).tex(var9, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.5F)
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
      float var1 = (float)this.particleAge / (float)this.particleMaxAge;
      float var2 = (float)this.particleAge / ((float)this.particleMaxAge / 2.0F);
      var1 = 1.0F - var1;
      var2 = 1.0F - var2;
      var2 *= var2;
      this.posX = this.coordX + this.motionX * (double)var1;
      this.posY = this.coordY + this.motionY * (double)var1 - (double)var2 + (double)1.0F;
      this.posZ = this.coordZ + this.motionZ * (double)var1;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

   }

   public void setGravity(float value) {
      this.particleGravity = value;
   }
}
