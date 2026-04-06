package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.blocks.BlockCustomOreItem;
import thaumcraft.common.entities.projectile.EntityPrimalArrow;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class RenderPrimalArrow extends Render<EntityPrimalArrow> {

   public RenderPrimalArrow(RenderManager renderManager) {
      super(renderManager);
   }
   private static final ResourceLocation arrowTextures = new ResourceLocation("textures/entity/arrow.png");
   int size1 = 0;
   int size2 = 0;

   public void renderArrow(EntityPrimalArrow arrow, double x, double y, double z, float ns, float prt) {
      Color color = new Color(BlockCustomOreItem.colors[arrow.type + 1]);
      this.bindEntityTexture(arrow);
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.translate((float)x, (float)y, (float)z);
      GlStateManager.rotate(arrow.prevRotationYaw + (arrow.rotationYaw - arrow.prevRotationYaw) * prt - 90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(arrow.prevRotationPitch + (arrow.rotationPitch - arrow.prevRotationPitch) * prt, 0.0F, 0.0F, 1.0F);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      byte b0 = 0;
      float f2 = 0.0F;
      float f3 = 0.5F;
      float f4 = (float)(b0 * 10) / 32.0F;
      float f5 = (float)(5 + b0 * 10) / 32.0F;
      float f6 = 0.0F;
      float f7 = 0.15625F;
      float f8 = (float)(5 + b0 * 10) / 32.0F;
      float f9 = (float)(10 + b0 * 10) / 32.0F;
      float f10 = 0.05625F;
      GlStateManager.enableRescaleNormal();
      float f11 = (float)arrow.arrowShake - prt;
      if (f11 > 0.0F) {
         float f12 = -MathHelper.sin(f11 * 3.0F) * f11;
         GlStateManager.rotate(f12, 0.0F, 0.0F, 1.0F);
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // ticksInGround private in 1.12.2; no fade
      GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(f10, f10, f10);
      GlStateManager.translate(-4.0F, 0.0F, 0.0F);
      GL11.glNormal3f(f10, 0.0F, 0.0F);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(-7.0F, -2.0F, -2.0F).tex(f6, f8).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      buffer.pos(-7.0F, -2.0F, 2.0F).tex(f7, f8).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      buffer.pos(-7.0F, 2.0F, 2.0F).tex(f7, f9).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      buffer.pos(-7.0F, 2.0F, -2.0F).tex(f6, f9).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      tessellator.draw();
      GL11.glNormal3f(-f10, 0.0F, 0.0F);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(-7.0F, 2.0F, -2.0F).tex(f6, f8).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      buffer.pos(-7.0F, 2.0F, 2.0F).tex(f7, f8).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      buffer.pos(-7.0F, -2.0F, 2.0F).tex(f7, f9).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      buffer.pos(-7.0F, -2.0F, -2.0F).tex(f6, f9).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
      tessellator.draw();

      for(int i = 0; i < 4; ++i) {
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GL11.glNormal3f(0.0F, 0.0F, f10);
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
         buffer.pos(-8.0F, -2.0F, 0.0F).tex(f2, f4).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
         buffer.pos(8.0F, -2.0F, 0.0F).tex(f3, f4).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
         buffer.pos(8.0F, 2.0F, 0.0F).tex(f3, f5).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
         buffer.pos(-8.0F, 2.0F, 0.0F).tex(f2, f5).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, 1.0F).endVertex();
         tessellator.draw();
      }

      GlStateManager.disableRescaleNormal();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      // ActiveRenderInfo.rotationX/XZ/Z/YZ/XY are private in 1.12.2; compute from renderManager view angles
      float _yaw   = renderManager.playerViewY * ((float)Math.PI / 180F);
      float _pitch = renderManager.playerViewX * ((float)Math.PI / 180F);
      float f1 = MathHelper.cos(_yaw);
      f2 = MathHelper.cos(_pitch);
      f3 = MathHelper.sin(_yaw);
      f4 = -f3 * MathHelper.sin(_pitch);
      f5 = f1 * MathHelper.sin(_pitch);
      f10 = 0.5F;
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)x, (float)y, (float)z);
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      if (arrow.type < 5) {
         GlStateManager.blendFunc(770, 1);
      } else {
         GlStateManager.blendFunc(770, 771);
      }

      UtilsFX.bindTexture("textures/misc/wisp.png");
      int i = arrow.ticksExisted % 16;
      float size4 = (float)(this.size1 * 4);
      float float_sizeMinus0_01 = (float)this.size1 - 0.01F;
      float x0 = ((float)(i % 4 * this.size1) + 0.0F) / size4;
      float x1 = ((float)(i % 4 * this.size1) + float_sizeMinus0_01) / size4;
      float x2 = ((float)(i / 4 * this.size1) + 0.0F) / size4;
      float x3 = ((float)(i / 4 * this.size1) + float_sizeMinus0_01) / size4;
      float cr = (float)color.getRed() / 255.0F;
      float cg = (float)color.getGreen() / 255.0F;
      float cb = (float)color.getBlue() / 255.0F;
      float ca = 1.0F; // ticksInGround private in 1.12.2; no fade
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(-f1 * f10 - f4 * f10, -f2 * f10, -f3 * f10 - f5 * f10).tex(x1, x3).color(cr, cg, cb, ca).endVertex();
      buffer.pos(-f1 * f10 + f4 * f10,  f2 * f10, -f3 * f10 + f5 * f10).tex(x1, x2).color(cr, cg, cb, ca).endVertex();
      buffer.pos( f1 * f10 + f4 * f10,  f2 * f10,  f3 * f10 + f5 * f10).tex(x0, x2).color(cr, cg, cb, ca).endVertex();
      buffer.pos( f1 * f10 - f4 * f10, -f2 * f10,  f3 * f10 - f5 * f10).tex(x0, x3).color(cr, cg, cb, ca).endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }

   protected ResourceLocation getArrowTextures(EntityPrimalArrow par1EntityArrow) {
      return arrowTextures;
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityPrimalArrow par1Entity) {
      return this.getArrowTextures(par1Entity);
   }

   @Override
   public void doRender(EntityPrimalArrow par1Entity, double par2, double par4, double par6, float par8, float par9) {
      if (this.size1 == 0) {
         this.size1 = UtilsFX.getTextureSize("textures/misc/wisp.png", 64);
      }
      this.renderArrow(par1Entity, par2, par4, par6, par8, par9);
   }
}
