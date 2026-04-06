package thaumcraft.client.fx.beams;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FXBeamPower extends Particle {
   public int particle = 16;
   private double offset = 0.0F;
   private double tX = 0.0F;
   private double tY = 0.0F;
   private double tZ = 0.0F;
   private double ptX = 0.0F;
   private double ptY = 0.0F;
   private double ptZ = 0.0F;
   private float length = 0.0F;
   private float rotYaw = 0.0F;
   private float rotPitch = 0.0F;
   private float prevYaw = 0.0F;
   private float prevPitch = 0.0F;
   private Entity targetEntity = null;
   private float opacity = 0.3F;
   private float prevSize = 0.0F;

   public FXBeamPower(World par1World, double px, double py, double pz, double tx, double ty, double tz, float red, float green, float blue, int age) {
      super(par1World, px, py, pz, 0.0F, 0.0F, 0.0F);
      this.particleRed = 0.5F;
      this.particleGreen = 0.5F;
      this.particleBlue = 0.5F;
      this.setSize(0.02F, 0.02F);
      this.canCollide = false;
      this.motionX = 0.0F;
      this.motionY = 0.0F;
      this.motionZ = 0.0F;
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;
      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      this.particleMaxAge = age;
      EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      int visibleDistance = 50;
      if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
         visibleDistance = 25;
      }

      if (renderentity.getDistance(this.posX, this.posY, this.posZ) > (double)visibleDistance) {
         this.particleMaxAge = 0;
      }

   }

   public void updateBeam(double xx, double yy, double zz, double x, double y, double z) {
      this.setPosition(xx, yy, zz);
      this.tX = x;
      this.tY = y;

      for(this.tZ = z; this.particleMaxAge - this.particleAge < 4; ++this.particleMaxAge) {
      }

   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY + this.offset;
      this.prevPosZ = this.posZ;
      this.ptX = this.tX;
      this.ptY = this.tY;
      this.ptZ = this.tZ;
      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      float xd = (float)(this.posX - this.tX);
      float yd = (float)(this.posY - this.tY);
      float zd = (float)(this.posZ - this.tZ);
      this.length = MathHelper.sqrt(xd * xd + yd * yd + zd * zd);
      double var7 = MathHelper.sqrt(xd * xd + zd * zd);
      this.rotYaw = (float)(Math.atan2(xd, zd) * (double)180.0F / Math.PI);
      this.rotPitch = (float)(Math.atan2(yd, var7) * (double)180.0F / Math.PI);
      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      if (this.opacity > 0.3F) {
         this.opacity -= 0.025F;
      }

      if (this.opacity < 0.3F) {
         this.opacity = 0.3F;
      }

      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

   }

   public void setRGB(float r, float g, float b) {
      this.particleRed = r;
      this.particleGreen = g;
      this.particleBlue = b;
   }

   public void setPulse(boolean pulse, float r, float g, float b) {
      this.setRGB(r, g, b);
      if (pulse) {
         this.opacity = 0.8F;
      }

   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
      GlStateManager.pushMatrix();
      float var9 = 1.0F;
      float slide = (float)Minecraft.getMinecraft().player.ticksExisted;
      float size = 0.7F;
      UtilsFX.bindTexture("textures/misc/beam1.png");
      GL11.glTexParameterf(3553, 10242, 10497.0F);
      GL11.glTexParameterf(3553, 10243, 10497.0F);
      GlStateManager.disableCull();
      float var11 = slide + f;
      float var12 = -var11 * 0.2F - (float)MathHelper.floor(-var11 * 0.1F);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.depthMask(false);
      float xx = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float yy = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float zz = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      GlStateManager.translate(xx, yy, zz);
      float ry = (float)((double)this.prevYaw + (double)(this.rotYaw - this.prevYaw) * (double)f);
      float rp = (float)((double)this.prevPitch + (double)(this.rotPitch - this.prevPitch) * (double)f);
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(180.0F + ry, 0.0F, 0.0F, -1.0F);
      GlStateManager.rotate(rp, 1.0F, 0.0F, 0.0F);
      double var44 = -0.15 * (double)size;
      double var17 = 0.15 * (double)size;
      float opmod = 0.1F;
      EntityLivingBase v = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      if (v instanceof EntityPlayer && ((EntityPlayer)v).inventory.armorItemInSlot(3) != null && ((EntityPlayer)v).inventory.armorItemInSlot(3).getItem() instanceof IRevealer) {
         opmod = 1.0F;
      }

      for(int t = 0; t < 2; ++t) {
         double var29 = this.length * var9;
         double var31 = 0.0F;
         double var33 = 1.0F;
         double var35 = -1.0F + var12 + (float)t / 3.0F;
         double var37 = (double)(this.length * var9) + var35;
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
        
        
         buffer.pos(var44, var29, 0.0F).tex(var33, var37).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
         buffer.pos(var44, 0.0F, 0.0F).tex(var33, var35).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
         buffer.pos(var17, 0.0F, 0.0F).tex(var31, var35).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
         buffer.pos(var17, var29, 0.0F).tex(var31, var37).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
         tessellator.draw();
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.depthMask(true);
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
      this.renderFlare(buffer, f, f1, f2, f3, f4, f5);
      this.prevSize = size;
   }

   public void renderFlare(BufferBuilder buffer, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
      float opmod = 0.2F;
      EntityLivingBase v = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      if (v instanceof EntityPlayer && ((EntityPlayer)v).inventory.armorItemInSlot(3) != null && ((EntityPlayer)v).inventory.armorItemInSlot(3).getItem() instanceof IRevealer) {
         opmod = 1.0F;
      }

      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      UtilsFX.bindTexture(ParticleEngine.particleTexture);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.66F);
      int part = this.particleAge % 16;
      float var8 = (float)part / 16.0F;
      float var9 = var8 + 0.0624375F;
      float var10 = 0.3125F;
      float var11 = var10 + 0.0624375F;
      float var12 = 0.66F * this.opacity;
      float var13 = (float)(this.ptX + (this.tX - this.ptX) * (double)f - interpPosX);
      float var14 = (float)(this.ptY + (this.tY - this.ptY) * (double)f - interpPosY);
      float var15 = (float)(this.ptZ + (this.tZ - this.ptZ) * (double)f - interpPosZ);
      float var16 = 1.0F;
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
     
     
      buffer.pos(var13 - f1 * var12 - f4 * var12, var14 - f2 * var12, var15 - f3 * var12 - f5 * var12).tex(var9, var11).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
      buffer.pos(var13 - f1 * var12 + f4 * var12, var14 + f2 * var12, var15 - f3 * var12 + f5 * var12).tex(var9, var10).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
      buffer.pos(var13 + f1 * var12 + f4 * var12, var14 + f2 * var12, var15 + f3 * var12 + f5 * var12).tex(var8, var10).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
      buffer.pos(var13 + f1 * var12 - f4 * var12, var14 - f2 * var12, var15 + f3 * var12 - f5 * var12).tex(var8, var11).color(this.particleRed, this.particleGreen, this.particleBlue, this.opacity * opmod)
        .endVertex();
      tessellator.draw();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
      net.minecraft.client.Minecraft.getMinecraft().renderEngine.bindTexture(thaumcraft.client.lib.UtilsFX.getParticleTexture());
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
   }

   public boolean isDead() { return this.isExpired; }
}
