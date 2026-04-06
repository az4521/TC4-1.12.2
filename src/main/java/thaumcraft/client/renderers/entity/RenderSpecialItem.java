package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.client.FMLClientHandler;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import thaumcraft.common.entities.EntitySpecialItem;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;

public class RenderSpecialItem extends Render<EntitySpecialItem> {
   private Random random = new Random();
   public boolean renderWithColor = true;
   public float zLevel = 0.0F;

   public RenderSpecialItem(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.15F;
      this.shadowOpaque = 0.75F;
   }

   @Override
   public void doRender(EntitySpecialItem entity, double par2, double par4, double par6, float par8, float par9) {
      this.random.setSeed(187L);
      float var11 = MathHelper.sin(((float)entity.ticksExisted + par9) / 10.0F + entity.hoverStart) * 0.1F + 0.1F;
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2, (float)par4 + var11 + 0.15F, (float)par6);
      int q = !FMLClientHandler.instance().getClient().gameSettings.fancyGraphics ? 5 : 10;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      RenderHelper.disableStandardItemLighting();
      float f1 = (float)entity.ticksExisted / 500.0F;
      float f2 = 0.0F;
      Random random = new Random(245L);
      GlStateManager.disableTexture2D();
      GlStateManager.shadeModel(7425);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.disableAlpha();
      GlStateManager.enableCull();
      GlStateManager.depthMask(false);
      GlStateManager.pushMatrix();

      for(int i = 0; i < q; ++i) {
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

      ItemStack stack = entity.getItem();
      if (stack != null && !stack.isEmpty()) {
         Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
      }

      GlStateManager.popMatrix();
   }

   @Override
   protected ResourceLocation getEntityTexture(EntitySpecialItem entity) {
      return null;
   }
}
