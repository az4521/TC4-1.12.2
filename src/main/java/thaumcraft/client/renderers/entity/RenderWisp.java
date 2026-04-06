package thaumcraft.client.renderers.entity;

import java.awt.Color;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.monster.EntityWisp;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderWisp extends Render {
   int size1 = 0;
   int size2 = 0;

   public RenderWisp(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.0F;
   }

   public void renderEntityAt(Entity entity, double x, double y, double z, float fq, float pticks) {
      if (!(((EntityLiving)entity).getHealth() <= 0.0F)) {
         float yaw = this.renderManager.playerViewY;
         float pitch = this.renderManager.playerViewX;
         float f1 = MathHelper.cos(yaw * (float)(Math.PI / 180.0));                               // rotationX
         float f2 = MathHelper.cos(pitch * (float)(Math.PI / 180.0));                             // rotationXZ
         float f3 = MathHelper.sin(yaw * (float)(Math.PI / 180.0));                               // rotationZ
         float f4 = -f3 * MathHelper.sin(pitch * (float)(Math.PI / 180.0));                      // rotationYZ
         float f5 = f1 * MathHelper.sin(pitch * (float)(Math.PI / 180.0));                       // rotationXY
         float f10 = 1.0F;
         float f11 = (float)x;
         float f12 = (float)y + 0.45F;
         float f13 = (float)z;
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         Color color = new Color(0);
         if (Aspect.getAspect(((EntityWisp)entity).getType()) != null) {
            color = new Color(Aspect.getAspect(((EntityWisp)entity).getType()).getColor());
         }

         float cr, cg, cb;
         if (((EntityLiving)entity).hurtTime > 0) {
            cr = 1.0F; cg = (float)color.getGreen() / 300.0F; cb = (float)color.getBlue() / 300.0F;
         } else {
            cr = (float)color.getRed() / 255.0F; cg = (float)color.getGreen() / 255.0F; cb = (float)color.getBlue() / 255.0F;
         }

         GlStateManager.pushMatrix();
         GlStateManager.depthMask(false);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1);
         UtilsFX.bindTexture("textures/misc/wisp.png");
         int i = entity.ticksExisted % 16;
         float size4 = (float)(this.size1 * 4);
         float float_sizeMinus0_01 = (float)this.size1 - 0.01F;
         float x0 = ((float)(i % 4 * this.size1) + 0.0F) / size4;
         float x1 = ((float)(i % 4 * this.size1) + float_sizeMinus0_01) / size4;
         float x2 = ((float)(i / 4 * this.size1) + 0.0F) / size4;
         float x3 = ((float)(i / 4 * this.size1) + float_sizeMinus0_01) / size4;
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
         buffer.pos(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10).tex(x1, x3).color(cr, cg, cb, 1.0f).endVertex();
         buffer.pos(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10).tex(x1, x2).color(cr, cg, cb, 1.0f).endVertex();
         buffer.pos(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10, f13 + f3 * f10 + f5 * f10).tex(x0, x2).color(cr, cg, cb, 1.0f).endVertex();
         buffer.pos(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10, f13 + f3 * f10 - f5 * f10).tex(x0, x3).color(cr, cg, cb, 1.0f).endVertex();
         tessellator.draw();
         GlStateManager.disableBlend();
         GlStateManager.depthMask(true);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.alphaFunc(516, 0.003921569F);
         GlStateManager.depthMask(false);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1);
         UtilsFX.bindTexture(ParticleEngine.particleTexture);
         int qq = entity.ticksExisted % 16;
         float size8 = 16.0F;
         x0 = (float)qq / size8;
         x1 = (float)(qq + 1) / size8;
         x2 = 5.0F / size8;
         x3 = 6.0F / size8;
         float var11 = MathHelper.sin(((float)entity.ticksExisted + pticks) / 10.0F) * 0.1F;
         f10 = 0.4F + var11;
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
         buffer.pos(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10).tex(x1, x3).color(cr, cg, cb, 1.0f).endVertex();
         buffer.pos(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10).tex(x1, x2).color(cr, cg, cb, 1.0f).endVertex();
         buffer.pos(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10, f13 + f3 * f10 + f5 * f10).tex(x0, x2).color(cr, cg, cb, 1.0f).endVertex();
         buffer.pos(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10, f13 + f3 * f10 - f5 * f10).tex(x0, x3).color(cr, cg, cb, 1.0f).endVertex();
         tessellator.draw();
         GlStateManager.disableBlend();
         GlStateManager.depthMask(true);
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.popMatrix();
      }
   }

   public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
      if (this.size1 == 0) {
         this.size1 = UtilsFX.getTextureSize("textures/misc/wisp.png", 64);
      }

      this.renderEntityAt(entity, d, d1, d2, f, f1);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return new net.minecraft.util.ResourceLocation("textures/entity/steve.png");
   }
}
