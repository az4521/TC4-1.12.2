package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import thaumcraft.common.entities.golems.EntityGolemBobber;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class RenderGolemBobber extends Render<EntityGolemBobber> {

   public RenderGolemBobber(RenderManager renderManager) {
      super(renderManager);
   }
   private static final ResourceLocation tex = new ResourceLocation("textures/particle/particles.png");

   public void doRender(EntityGolemBobber bobber, double xx, double yy, double zz, float p_147922_8_, float p_147922_9_) {
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)xx, (float)yy, (float)zz);
      GlStateManager.enableRescaleNormal();
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      this.bindEntityTexture(bobber);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      byte b0 = 1;
      byte b1 = 2;
      float f2 = (float)(b0 * 8) / 128.0F;
      float f3 = (float)(b0 * 8 + 8) / 128.0F;
      float f4 = (float)(b1 * 8) / 128.0F;
      float f5 = (float)(b1 * 8 + 8) / 128.0F;
      float f6 = 1.0F;
      float f7 = 0.5F;
      float f8 = 0.5F;
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(0.0F - f7, 0.0F - f8, 0.0F).tex(f2, f5).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
      buffer.pos(f6 - f7, 0.0F - f8, 0.0F).tex(f3, f5).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
      buffer.pos(f6 - f7, 1.0F - f8, 0.0F).tex(f3, f4).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
      buffer.pos(0.0F - f7, 1.0F - f8, 0.0F).tex(f2, f4).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
      tessellator.draw();
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityGolemBobber par1Entity) {
      return tex;
   }
}
