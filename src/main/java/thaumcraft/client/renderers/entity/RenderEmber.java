package thaumcraft.client.renderers.entity;

import java.util.Random;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.projectile.EntityEmber;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderEmber extends Render {
   private Random random = new Random();

   public RenderEmber(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.0F;
   }

   public void renderEntityAt(EntityEmber entity, double x, double y, double z, float fq, float pticks) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, z);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      UtilsFX.bindTexture(ParticleEngine.particleTexture);
      int p = (int)(8.0F * ((float)entity.ticksExisted / (float)entity.duration));
      float f2 = (float)(7 + p) / 16.0F;
      float f3 = f2 + 0.0625F;
      float f4 = 0.5625F;
      float f5 = f4 + 0.0625F;
      float f6 = 1.0F;
      float f7 = 0.5F;
      float f8 = 0.5F;
      float fc = (float)entity.ticksExisted / (float)entity.duration;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
      float particleScale = 0.25F + fc;
      GlStateManager.scale(particleScale, particleScale, particleScale);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
     
     
     
      buffer.pos(-f7, -f8, 0.0F).tex(f2, f5).color(1.0f, 1.0f, 1.0f, 0.9F)
        .endVertex();
      buffer.pos(f6 - f7, -f8, 0.0F).tex(f3, f5).color(1.0f, 1.0f, 1.0f, 0.9F)
        .endVertex();
      buffer.pos(f6 - f7, 1.0F - f8, 0.0F).tex(f3, f4).color(1.0f, 1.0f, 1.0f, 0.9F)
        .endVertex();
      buffer.pos(-f7, 1.0F - f8, 0.0F).tex(f2, f4).color(1.0f, 1.0f, 1.0f, 0.9F)
        .endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
   }

   public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
      this.renderEntityAt((EntityEmber)entity, d, d1, d2, f, f1);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return new net.minecraft.util.ResourceLocation("textures/entity/steve.png");
   }
}
