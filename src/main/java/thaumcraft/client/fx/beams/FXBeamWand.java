package thaumcraft.client.fx.beams;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FXBeamWand extends Particle {
   public int particle = 16;
   EntityPlayer player = null;
   private double offset = 0.0F;
   private float length = 0.0F;
   private float rotYaw = 0.0F;
   private float rotPitch = 0.0F;
   private float prevYaw = 0.0F;
   private float prevPitch = 0.0F;
   private Entity targetEntity = null;
   private double tX = 0.0F;
   private double tY = 0.0F;
   private double tZ = 0.0F;
   private double ptX = 0.0F;
   private double ptY = 0.0F;
   private double ptZ = 0.0F;
   private int type = 0;
   private float endMod = 1.0F;
   private boolean reverse = false;
   private boolean pulse = true;
   private int rotationspeed = 5;
   private float prevSize = 0.0F;
   public int impact;

   public FXBeamWand(World par1World, EntityPlayer player, double tx, double ty, double tz, float red, float green, float blue, int age) {
      super(par1World, player.posX, player.posY, player.posZ, 0.0F, 0.0F, 0.0F);
      if (player.getEntityId() != Minecraft.getMinecraft().getRenderViewEntity().getEntityId()) {
         this.offset = (double)(player.height / 2.0F) + (double)0.25F;
      }

      this.particleRed = red;
      this.particleGreen = green;
      this.particleBlue = blue;
      this.player = player;
      this.setSize(0.02F, 0.02F);
      this.canCollide = false;
      this.motionX = 0.0F;
      this.motionY = 0.0F;
      this.motionZ = 0.0F;
      this.tX = tx;
      this.tY = ty;
      this.tZ = tz;
      float xd = (float)(player.posX - this.tX);
      float yd = (float)(player.posY + this.offset - this.tY);
      float zd = (float)(player.posZ - this.tZ);
      this.length = MathHelper.sqrt(xd * xd + yd * yd + zd * zd);
      double var7 = MathHelper.sqrt(xd * xd + zd * zd);
      this.rotYaw = (float)(Math.atan2(xd, zd) * (double)180.0F / Math.PI);
      this.rotPitch = (float)(Math.atan2(yd, var7) * (double)180.0F / Math.PI);
      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      this.particleMaxAge = age;
      EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      int visibleDistance = 50;
      if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
         visibleDistance = 25;
      }

      if (renderentity.getDistance(player.posX, player.posY, player.posZ) > (double)visibleDistance) {
         this.particleMaxAge = 0;
      }

   }

   public void updateBeam(double x, double y, double z) {
      this.tX = x;
      this.tY = y;

      for(this.tZ = z; this.particleMaxAge - this.particleAge < 4; ++this.particleMaxAge) {
      }

   }

   public void onUpdate() {
      this.prevPosX = this.player.posX;
      this.prevPosY = this.player.posY + this.offset;
      this.prevPosZ = this.player.posZ;
      this.ptX = this.tX;
      this.ptY = this.tY;
      this.ptZ = this.tZ;
      this.prevYaw = this.rotYaw;
      this.prevPitch = this.rotPitch;
      float xd = (float)(this.player.posX - this.tX);
      float yd = (float)(this.player.posY + this.offset - this.tY);
      float zd = (float)(this.player.posZ - this.tZ);
      this.length = MathHelper.sqrt(xd * xd + yd * yd + zd * zd);
      double var7 = MathHelper.sqrt(xd * xd + zd * zd);
      this.rotYaw = (float)(Math.atan2(xd, zd) * (double)180.0F / Math.PI);

      for(this.rotPitch = (float)(Math.atan2(yd, var7) * (double)180.0F / Math.PI); this.rotPitch - this.prevPitch < -180.0F; this.prevPitch -= 360.0F) {
      }

      while(this.rotPitch - this.prevPitch >= 180.0F) {
         this.prevPitch += 360.0F;
      }

      while(this.rotYaw - this.prevYaw < -180.0F) {
         this.prevYaw -= 360.0F;
      }

      while(this.rotYaw - this.prevYaw >= 180.0F) {
         this.prevYaw += 360.0F;
      }

      if (this.impact > 0) {
         --this.impact;
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

   public void setType(int type) {
      this.type = type;
   }

   public void setEndMod(float endMod) {
      this.endMod = endMod;
   }

   public void setReverse(boolean reverse) {
      this.reverse = reverse;
   }

   public void setPulse(boolean pulse) {
      this.pulse = pulse;
   }

   public void setRotationspeed(int rotationspeed) {
      this.rotationspeed = rotationspeed;
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
      GlStateManager.pushMatrix();
      float var9 = 1.0F;
      float slide = (float)Minecraft.getMinecraft().player.ticksExisted;
      float rot = (float)(this.world.provider.getWorldTime() % (long)(360 / this.rotationspeed) * (long)this.rotationspeed) + (float)this.rotationspeed * f;
      float size = 1.0F;
      if (this.pulse) {
         size = Math.min((float)this.particleAge / 4.0F, 1.0F);
         size = (float)((double)this.prevSize + (double)(size - this.prevSize) * (double)f);
      }

      float op = 0.4F;
      if (this.pulse && this.particleMaxAge - this.particleAge <= 4) {
         op = 0.4F - (float)(4 - (this.particleMaxAge - this.particleAge)) * 0.1F;
      }

      switch (this.type) {
         case 1:
            UtilsFX.bindTexture("textures/misc/beam1.png");
            break;
         case 2:
            UtilsFX.bindTexture("textures/misc/beam2.png");
            break;
         case 3:
            UtilsFX.bindTexture("textures/misc/beam3.png");
            break;
         default:
            UtilsFX.bindTexture("textures/misc/beam.png");
      }

      GL11.glTexParameterf(3553, 10242, 10497.0F);
      GL11.glTexParameterf(3553, 10243, 10497.0F);
      GlStateManager.disableCull();
      float var11 = slide + f;
      if (this.reverse) {
         var11 *= -1.0F;
      }

      float var12 = -var11 * 0.2F - (float)MathHelper.floor(-var11 * 0.1F);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.depthMask(false);
      double prex = this.player.prevPosX;
      double prey = this.player.prevPosY + this.offset;
      double prez = this.player.prevPosZ;
      double px = this.player.posX;
      double py = this.player.posY + this.offset;
      double pz = this.player.posZ;
      prex -= MathHelper.cos(this.player.prevRotationYaw / 180.0F * 3.141593F) * 0.066F;
      prey -= 0.06;
      prez -= MathHelper.sin(this.player.prevRotationYaw / 180.0F * 3.141593F) * 0.04F;
      Vec3d vec3d = this.player.getLook(1.0F);
      prex += vec3d.x * 0.3;
      prey += vec3d.y * 0.3;
      prez += vec3d.z * 0.3;
      px -= MathHelper.cos(this.player.rotationYaw / 180.0F * 3.141593F) * 0.066F;
      py -= 0.06;
      pz -= MathHelper.sin(this.player.rotationYaw / 180.0F * 3.141593F) * 0.04F;
      vec3d = this.player.getLook(1.0F);
      px += vec3d.x * 0.3;
      py += vec3d.y * 0.3;
      pz += vec3d.z * 0.3;
      float xx = (float)(prex + (px - prex) * (double)f - interpPosX);
      float yy = (float)(prey + (py - prey) * (double)f - interpPosY);
      float zz = (float)(prez + (pz - prez) * (double)f - interpPosZ);
      GlStateManager.translate(xx, yy, zz);
      float ry = (float)((double)this.prevYaw + (double)(this.rotYaw - this.prevYaw) * (double)f);
      float rp = (float)((double)this.prevPitch + (double)(this.rotPitch - this.prevPitch) * (double)f);
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(180.0F + ry, 0.0F, 0.0F, -1.0F);
      GlStateManager.rotate(rp, 1.0F, 0.0F, 0.0F);
      double var44 = -0.15 * (double)size;
      double var17 = 0.15 * (double)size;
      double var44b = -0.15 * (double)size * (double)this.endMod;
      double var17b = 0.15 * (double)size * (double)this.endMod;
      GlStateManager.rotate(rot, 0.0F, 1.0F, 0.0F);

      for(int t = 0; t < 3; ++t) {
         double var29 = this.length * size * var9;
         double var31 = 0.0F;
         double var33 = 1.0F;
         double var35 = -1.0F + var12 + (float)t / 3.0F;
         double var37 = (double)(this.length * size * var9) + var35;
         GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
        
        
         buffer.pos(var44b, var29, 0.0F).tex(var33, var37).color(this.particleRed, this.particleGreen, this.particleBlue, op)
        .endVertex();
         buffer.pos(var44, 0.0F, 0.0F).tex(var33, var35).color(this.particleRed, this.particleGreen, this.particleBlue, op)
        .endVertex();
         buffer.pos(var17, 0.0F, 0.0F).tex(var31, var35).color(this.particleRed, this.particleGreen, this.particleBlue, op)
        .endVertex();
         buffer.pos(var17b, var29, 0.0F).tex(var31, var37).color(this.particleRed, this.particleGreen, this.particleBlue, op)
        .endVertex();
         tessellator.draw();
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.depthMask(true);
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
      if (this.impact > 0) {
         this.renderImpact(buffer, f, f1, f2, f3, f4, f5);
      }

      this.prevSize = size;
   }

   public void renderImpact(BufferBuilder buffer, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
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
      float var12 = this.endMod / 2.0F / (float)(6 - this.impact);
      float var13 = (float)(this.ptX + (this.tX - this.ptX) * (double)f - interpPosX);
      float var14 = (float)(this.ptY + (this.tY - this.ptY) * (double)f - interpPosY);
      float var15 = (float)(this.ptZ + (this.tZ - this.ptZ) * (double)f - interpPosZ);
      float var16 = 1.0F;
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
     
     
      buffer.pos(var13 - f1 * var12 - f4 * var12, var14 - f2 * var12, var15 - f3 * var12 - f5 * var12).tex(var9, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66f)
        .endVertex();
      buffer.pos(var13 - f1 * var12 + f4 * var12, var14 + f2 * var12, var15 - f3 * var12 + f5 * var12).tex(var9, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66f)
        .endVertex();
      buffer.pos(var13 + f1 * var12 + f4 * var12, var14 + f2 * var12, var15 + f3 * var12 + f5 * var12).tex(var8, var10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66f)
        .endVertex();
      buffer.pos(var13 + f1 * var12 - f4 * var12, var14 - f2 * var12, var15 + f3 * var12 - f5 * var12).tex(var8, var11).color(this.particleRed, this.particleGreen, this.particleBlue, 0.66f)
        .endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
      Minecraft.getMinecraft().renderEngine.bindTexture(UtilsFX.getParticleTexture());
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
   }

   public boolean isDead() { return this.isExpired; }
}
