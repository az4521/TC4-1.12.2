package thaumcraft.client.fx.beams;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FXArc extends Particle {
   public int particle = 16;
   ArrayList points = new ArrayList<>();
   private Entity targetEntity = null;
   private double tX = 0.0F;
   private double tY = 0.0F;
   private double tZ = 0.0F;
   public int blendmode = 1;
   public float length = 1.0F;

   public FXArc(World par1World, double x, double y, double z, double tx, double ty, double tz, float red, float green, float blue, double hg) {
      super(par1World, x, y, z, 0.0F, 0.0F, 0.0F);
      this.particleRed = red;
      this.particleGreen = green;
      this.particleBlue = blue;
      this.setSize(0.02F, 0.02F);
      this.canCollide = false;
      this.motionX = 0.0F;
      this.motionY = 0.0F;
      this.motionZ = 0.0F;
      this.tX = tx - x;
      this.tY = ty - y;
      this.tZ = tz - z;
      this.particleMaxAge = 1;
      double xx = 0.0F;
      double yy = 0.0F;
      double zz = 0.0F;
      double gravity = 0.115;
      double noise = 0.25F;
      Vec3d vs = new Vec3d(xx, yy, zz);
      Vec3d ve = new Vec3d(this.tX, this.tY, this.tZ);
      Vec3d vc = new Vec3d(xx, yy, zz);
      this.length = (float)ve.length();
      Vec3d vv = Utils.calculateVelocity(vs, ve, hg, gravity);
      double l = Utils.distanceSquared3d(new Vec3d(0.0F, 0.0F, 0.0F), vv);
      this.points.add(vs);

      for(int c = 0; Utils.distanceSquared3d(ve, vc) > l && c < 50; ++c) {
         Vec3d vt = vc.add(vv.x, vv.y, vv.z);
         vc = new Vec3d(vt.x, vt.y, vt.z);
         vt = new Vec3d(
             vt.x + (this.rand.nextDouble() - this.rand.nextDouble()) * noise,
             vt.y + (this.rand.nextDouble() - this.rand.nextDouble()) * noise,
             vt.z + (this.rand.nextDouble() - this.rand.nextDouble()) * noise
         );
         this.points.add(vt);
         vv = new Vec3d(vv.x, vv.y - gravity / 1.9, vv.z);
      }

      this.points.add(ve);
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

   }

   public void setRGB(float r, float g, float b) {
      this.particleRed = r;
      this.particleGreen = g;
      this.particleBlue = b;
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
      GlStateManager.pushMatrix();
      double ePX = this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX;
      double ePY = this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY;
      double ePZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ;
      GlStateManager.translate(ePX, ePY, ePZ);
      float size = 0.25F;
      UtilsFX.bindTexture("textures/misc/beamh.png");
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.disableCull();
      buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR); 
     
     
      float f9 = 0.0F;
      float f10 = 1.0F;

      for(int c = 0; c < this.points.size(); ++c) {
         Vec3d v = (Vec3d)this.points.get(c);
         float f13 = (float)c / this.length;
         double dx = v.x;
         double dy = v.y;
         double dz = v.z;
         buffer.pos(dx, dy - (double)size, dz).tex(f13, f10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.8f)
        .endVertex();
         buffer.pos(dx, dy + (double)size, dz).tex(f13, f9).color(this.particleRed, this.particleGreen, this.particleBlue, 0.8f)
        .endVertex();
      }

      tessellator.draw();
      buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR); 
     
     

      for(int c = 0; c < this.points.size(); ++c) {
         Vec3d v = (Vec3d)this.points.get(c);
         float f13 = (float)c / this.length;
         double dx = v.x;
         double dy = v.y;
         double dz = v.z;
         buffer.pos(dx - (double)size, dy, dz - (double)size).tex(f13, f10).color(this.particleRed, this.particleGreen, this.particleBlue, 0.8f)
        .endVertex();
         buffer.pos(dx + (double)size, dy, dz + (double)size).tex(f13, f9).color(this.particleRed, this.particleGreen, this.particleBlue, 0.8f)
        .endVertex();
      }

      tessellator.draw();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableCull();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }
}
