package thaumcraft.client.renderers.entity;

import java.util.Random;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.blocks.BlockCustomOreItem;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderPrimalOrb extends Render {
   public RenderPrimalOrb(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.0F;
   }

   public void renderEntityAt(Entity entity, double x, double y, double z, float fq, float pticks) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      RenderHelper.disableStandardItemLighting();
      float f1 = (float)entity.ticksExisted / 80.0F;
      float f3 = 0.9F;
      float f2 = 0.0F;
      Random random = new Random(entity.getEntityId());
      GlStateManager.translate((float)x, (float)y, (float)z);
      GlStateManager.disableTexture2D();
      GlStateManager.shadeModel(7425);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.disableAlpha();
      GlStateManager.enableCull();
      GlStateManager.depthMask(false);
      GlStateManager.pushMatrix();

      for(int i = 0; i < 12; ++i) {
         GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(random.nextFloat() * 360.0F + f1 * 360.0F, 0.0F, 0.0F, 1.0F);
         buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
         float fa = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
         float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
         fa /= 30.0F / ((float)Math.min(entity.ticksExisted, 10) / 10.0F);
         f4 /= 30.0F / ((float)Math.min(entity.ticksExisted, 10) / 10.0F);
         float ca = 1.0F - f2;
         buffer.pos(0.0F, 0.0F, 0.0F).color(1.0f, 1.0f, 1.0f, ca).endVertex();
         buffer.pos(-0.866 * (double)f4, fa, -0.5F * f4).color(1.0f, 1.0f, 1.0f, ca).endVertex();
         buffer.pos(0.866 * (double)f4, fa, -0.5F * f4).color(1.0f, 1.0f, 1.0f, ca).endVertex();
         buffer.pos(0.0F, fa, f4).color(1.0f, 1.0f, 1.0f, ca).endVertex();
         buffer.pos(-0.866 * (double)f4, fa, -0.5F * f4).color(1.0f, 1.0f, 1.0f, ca).endVertex();
         tessellator.draw();
      }

      GlStateManager.popMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.disableCull();
      GlStateManager.disableBlend();
      GlStateManager.shadeModel(7424);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableTexture2D();
      GlStateManager.enableAlpha();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, z);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      UtilsFX.bindTexture(ParticleEngine.particleTexture);
      f2 = (float)(entity.ticksExisted % 13) / 16.0F;
      f3 = f2 + 0.0624375F;
      float f4 = 0.125F;
      float f5 = f4 + 0.0624375F;
      float f6 = 1.0F;
      float f7 = 0.5F;
      float f8 = 0.5F;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.8F);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
     
      buffer.pos(0.0F - f7, 0.0F - f8, 0.0F).tex(f2, f5)
        .endVertex();
      buffer.pos(f6 - f7, 0.0F - f8, 0.0F).tex(f3, f5)
        .endVertex();
      buffer.pos(f6 - f7, 1.0F - f8, 0.0F).tex(f3, f4)
        .endVertex();
      buffer.pos(0.0F - f7, 1.0F - f8, 0.0F).tex(f2, f4)
        .endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
   }

   public void doRender(Entity entity, double d, double d1, double d2, float f, float f1) {
      this.renderEntityAt(entity, d, d1, d2, f, f1);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return new net.minecraft.util.ResourceLocation("textures/entity/steve.png");
   }
}
