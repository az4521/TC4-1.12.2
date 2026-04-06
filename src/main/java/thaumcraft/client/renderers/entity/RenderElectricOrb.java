package thaumcraft.client.renderers.entity;

import java.util.Random;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.projectile.EntityGolemOrb;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderElectricOrb extends Render {
   private Random random = new Random();

   public RenderElectricOrb(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.0F;
   }

   public void renderEntityAt(Entity entity, double x, double y, double z, float fq, float pticks) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, z);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      UtilsFX.bindTexture(ParticleEngine.particleTexture);
      float f2 = (float)(1 + entity.ticksExisted % 6) / 8.0F;
      float f3 = f2 + 0.125F;
      float f4 = 0.875F;
      if (entity instanceof EntityGolemOrb && ((EntityGolemOrb)entity).red) {
         f4 = 0.75F;
      }

      float f5 = f4 + 0.125F;
      float f6 = 1.0F;
      float f7 = 0.5F;
      float f8 = 0.5F;
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.8F);
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      float bob = MathHelper.sin((float)entity.ticksExisted / 5.0F) * 0.2F + 0.2F;
      GlStateManager.scale(1.0F + bob, 1.0F + bob, 1.0F + bob);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
     
     
      buffer.pos(-f7, -f8, 0.0F).tex(f2, f5)
        .endVertex();
      buffer.pos(f6 - f7, -f8, 0.0F).tex(f3, f5)
        .endVertex();
      buffer.pos(f6 - f7, 1.0F - f8, 0.0F).tex(f3, f4)
        .endVertex();
      buffer.pos(-f7, 1.0F - f8, 0.0F).tex(f2, f4)
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
