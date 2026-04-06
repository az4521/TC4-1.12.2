package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.EntityAspectOrb;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class RenderAspectOrb extends Render<EntityAspectOrb> {
   public RenderAspectOrb(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.1F;
      this.shadowOpaque = 0.5F;
   }

   public void renderAspectOrb(EntityAspectOrb orb, double par2, double par4, double par6, float par8, float par9) {
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2, (float)par4, (float)par6);
      GlStateManager.enableBlend();
      if (orb.getAspect() != null) {
         GlStateManager.blendFunc(770, orb.getAspect().getBlend());
      } else {
         GlStateManager.blendFunc(770, 1);
      }

      UtilsFX.bindTexture(ParticleEngine.particleTexture);
      int i = (int)(System.nanoTime() / 25000000L % 16L);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      float f2 = (float)i / 16.0F;
      float f3 = (float)(i + 1) / 16.0F;
      float f4 = 0.5F;
      float f5 = 0.5625F;
      float f6 = 1.0F;
      float f7 = 0.5F;
      float f8 = 0.25F;
      int j = orb.getBrightnessForRender();
      int k = j % 65536;
      int l = j / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      float f11 = 0.1F + 0.3F * ((float)(orb.orbMaxAge - orb.orbAge) / (float)orb.orbMaxAge);
      GlStateManager.scale(f11, f11, f11);

      float cr = 1.0f, cg = 1.0f, cb = 1.0f, ca = 0.502f;
      if (orb.getAspect() != null) {
         int color = orb.getAspect().getColor();
         cr = (float)((color >> 16) & 0xFF) / 255.0F;
         cg = (float)((color >> 8) & 0xFF) / 255.0F;
         cb = (float)(color & 0xFF) / 255.0F;
         ca = 128.0F / 255.0F;
      }

      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(0.0F - f7, 0.0F - f8, 0.0F).tex(f2, f5).color(cr, cg, cb, ca).endVertex();
      buffer.pos(f6 - f7, 0.0F - f8, 0.0F).tex(f3, f5).color(cr, cg, cb, ca).endVertex();
      buffer.pos(f6 - f7, 1.0F - f8, 0.0F).tex(f3, f4).color(cr, cg, cb, ca).endVertex();
      buffer.pos(0.0F - f7, 1.0F - f8, 0.0F).tex(f2, f4).color(cr, cg, cb, ca).endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
   }

   @Override
   public void doRender(EntityAspectOrb orb, double par2, double par4, double par6, float par8, float par9) {
      this.renderAspectOrb(orb, par2, par4, par6, par8, par9);
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityAspectOrb entity) {
      return new ResourceLocation("textures/entity/steve.png");
   }
}
