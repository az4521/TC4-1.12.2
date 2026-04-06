package thaumcraft.client.fx.other;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

import thaumcraft.client.lib.UtilsFX;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FXBlockWard extends Particle {
   EnumFacing side;
   int rotation = 0;
   float sx = 0.0F;
   float sy = 0.0F;
   float sz = 0.0F;

   public FXBlockWard(World world, double d, double d1, double d2, EnumFacing side, float f, float f1, float f2) {
      super(world, d, d1, d2, 0.0F, 0.0F, 0.0F);
      this.side = side;
      this.particleGravity = 0.0F;
      this.motionX = this.motionY = this.motionZ = 0.0F;
      this.particleMaxAge = 12 + this.rand.nextInt(5);
      this.canCollide = true;
      this.setSize(0.01F, 0.01F);
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.canCollide = false;
      this.particleScale = (float)(1.4 + this.rand.nextGaussian() * (double)0.3F);
      this.rotation = this.rand.nextInt(360);
      this.sx = MathHelper.clamp(f - 0.6F + this.rand.nextFloat() * 0.2F, -0.4F, 0.4F);
      this.sy = MathHelper.clamp(f1 - 0.6F + this.rand.nextFloat() * 0.2F, -0.4F, 0.4F);
      this.sz = MathHelper.clamp(f2 - 0.6F + this.rand.nextFloat() * 0.2F, -0.4F, 0.4F);
      if (side.getXOffset() != 0) {
         this.sx = 0.0F;
      }

      if (side.getYOffset() != 0) {
         this.sy = 0.0F;
      }

      if (side.getZOffset() != 0) {
         this.sz = 0.0F;
      }

   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
      GlStateManager.pushMatrix();
      float fade = ((float)this.particleAge + f) / (float)this.particleMaxAge;
      int frame = Math.min(15, (int)(15.0F * fade));
      UtilsFX.bindTexture("textures/models/hemis" + frame + ".png");
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.color(1.0F, 1.0F, 1.0F, this.particleAlpha / 2.0F);
      float var13 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float var14 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float var15 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      GlStateManager.translate(var13 + this.sx, var14 + this.sy, var15 + this.sz);
      GlStateManager.rotate(90.0F, (float)this.side.getYOffset(), (float)(-this.side.getXOffset()), (float)this.side.getZOffset());
      GlStateManager.rotate((float)this.rotation, 0.0F, 0.0F, 1.0F);
      if (this.side.getZOffset() > 0) {
         GlStateManager.translate(0.0F, 0.0F, 0.505F);
         GlStateManager.rotate(180.0F, 0.0F, -1.0F, 0.0F);
      } else {
         GlStateManager.translate(0.0F, 0.0F, -0.505F);
      }

      float var12 = this.particleScale;
      float var16 = 1.0F;
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
     
     
      buffer.pos((double)-0.5F * (double)var12, (double)0.5F * (double)var12, 0.0F).tex(0.0F, 1.0F).color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha / 2.0f)
        .endVertex();
      buffer.pos((double)0.5F * (double)var12, (double)0.5F * (double)var12, 0.0F).tex(1.0F, 1.0F).color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha / 2.0f)
        .endVertex();
      buffer.pos((double)0.5F * (double)var12, (double)-0.5F * (double)var12, 0.0F).tex(1.0F, 0.0F).color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha / 2.0f)
        .endVertex();
      buffer.pos((double)-0.5F * (double)var12, (double)-0.5F * (double)var12, 0.0F).tex(0.0F, 0.0F).color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha / 2.0f)
        .endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      float threshold = (float)this.particleMaxAge / 5.0F;
      if ((float)this.particleAge <= threshold) {
         this.particleAlpha = (float)this.particleAge / threshold;
      } else {
         this.particleAlpha = (float)(this.particleMaxAge - this.particleAge) / (float)this.particleMaxAge;
      }

      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.motionY -= 0.04 * (double)this.particleGravity;
      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
   }

   public void setGravity(float value) {
      this.particleGravity = value;
   }
}
