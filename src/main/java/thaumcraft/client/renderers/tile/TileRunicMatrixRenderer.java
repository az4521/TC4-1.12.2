package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelCube;
import thaumcraft.common.tiles.TileInfusionMatrix;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class TileRunicMatrixRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private ModelCube model = new ModelCube(0);
   private ModelCube model_over = new ModelCube(32);
   int type = 0;

   public TileRunicMatrixRenderer(int type) {
      this.type = type;
   }

   private void drawHalo(TileEntity is, double x, double y, double z, float par8, int count) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F);
      int q = !FMLClientHandler.instance().getClient().gameSettings.fancyGraphics ? 10 : 20;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      RenderHelper.disableStandardItemLighting();
      float f1 = (float)count / 500.0F;
      float f3 = 0.9F;
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
         fa /= 20.0F / ((float)Math.min(count, 50) / 50.0F);
         f4 /= 20.0F / ((float)Math.min(count, 50) / 50.0F);
         int colorA = (int)(255.0F * (1.0F - f2));
         buffer.pos(0.0F, 0.0F, 0.0F).color(255, 255, 255, colorA).endVertex();
         buffer.pos(-0.866 * (double)f4, fa, -0.5F * f4).color(255, 255, 255, colorA).endVertex();
         buffer.pos(0.866 * (double)f4, fa, -0.5F * f4).color(255, 255, 255, colorA).endVertex();
         buffer.pos(0.0F, fa, f4).color(255, 255, 255, colorA).endVertex();
         buffer.pos(-0.866 * (double)f4, fa, -0.5F * f4).color(255, 255, 255, colorA).endVertex();
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
      GlStateManager.blendFunc(770, 771);
      GlStateManager.popMatrix();
   }

   public void renderInfusionMatrix(TileInfusionMatrix is, double par2, double par4, double par6, float par8) {
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/infuser.png");
      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 0.5F, (float)par6 + 0.5F);
      float ticks = (float)Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + par8;
      if (is.getWorld() != null) {
         GlStateManager.rotate(ticks % 360.0F * is.startUp, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(35.0F * is.startUp, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(45.0F * is.startUp, 0.0F, 0.0F, 1.0F);
      }

      float instability = Math.min(6.0F, 1.0F + (float)is.instability * 0.66F * ((float)Math.min(is.craftCount, 50) / 50.0F));
      float b1 = 0.0F;
      float b2 = 0.0F;
      float b3 = 0.0F;
      int aa = 0;
      int bb = 0;
      int cc = 0;

      for(int a = 0; a < 2; ++a) {
         for(int b = 0; b < 2; ++b) {
            for(int c = 0; c < 2; ++c) {
               if (is.active) {
                  b1 = MathHelper.sin((ticks + (float)(a * 10)) / (15.0F - instability / 2.0F)) * 0.01F * is.startUp * instability;
                  b2 = MathHelper.sin((ticks + (float)(b * 10)) / (14.0F - instability / 2.0F)) * 0.01F * is.startUp * instability;
                  b3 = MathHelper.sin((ticks + (float)(c * 10)) / (13.0F - instability / 2.0F)) * 0.01F * is.startUp * instability;
               }

               aa = a == 0 ? -1 : 1;
               bb = b == 0 ? -1 : 1;
               cc = c == 0 ? -1 : 1;
               GlStateManager.pushMatrix();
               GlStateManager.translate(b1 + (float)aa * 0.25F, b2 + (float)bb * 0.25F, b3 + (float)cc * 0.25F);
               if (a > 0) {
                  GlStateManager.rotate(90.0F, (float)a, 0.0F, 0.0F);
               }

               if (b > 0) {
                  GlStateManager.rotate(90.0F, 0.0F, (float)b, 0.0F);
               }

               if (c > 0) {
                  GlStateManager.rotate(90.0F, 0.0F, 0.0F, (float)c);
               }

               GlStateManager.scale(0.45, 0.45, 0.45);
               this.model.render();
               GlStateManager.popMatrix();
            }
         }
      }

      if (is.active) {
         GlStateManager.pushMatrix();
         GlStateManager.alphaFunc(516, 0.003921569F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1);

         for(int a = 0; a < 2; ++a) {
            for(int b = 0; b < 2; ++b) {
               for(int c = 0; c < 2; ++c) {
                  b1 = MathHelper.sin((ticks + (float)(a * 10)) / (15.0F - instability / 2.0F)) * 0.01F * is.startUp * instability;
                  b2 = MathHelper.sin((ticks + (float)(b * 10)) / (14.0F - instability / 2.0F)) * 0.01F * is.startUp * instability;
                  b3 = MathHelper.sin((ticks + (float)(c * 10)) / (13.0F - instability / 2.0F)) * 0.01F * is.startUp * instability;
                  aa = a == 0 ? -1 : 1;
                  bb = b == 0 ? -1 : 1;
                  cc = c == 0 ? -1 : 1;
                  GlStateManager.pushMatrix();
                  GlStateManager.translate(b1 + (float)aa * 0.25F, b2 + (float)bb * 0.25F, b3 + (float)cc * 0.25F);
                  if (a > 0) {
                     GlStateManager.rotate(90.0F, (float)a, 0.0F, 0.0F);
                  }

                  if (b > 0) {
                     GlStateManager.rotate(90.0F, 0.0F, (float)b, 0.0F);
                  }

                  if (c > 0) {
                     GlStateManager.rotate(90.0F, 0.0F, 0.0F, (float)c);
                  }

                  GlStateManager.scale(0.45, 0.45, 0.45);
                  int j = 15728880;
                  int k = j % 65536;
                  int l = j / 65536;
                  OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
                  GlStateManager.color(0.8F, 0.1F, 1.0F, (MathHelper.sin((ticks + (float)(a * 2) + (float)(b * 3) + (float)(c * 4)) / 4.0F) * 0.1F + 0.2F) * is.startUp);
                  this.model_over.render();
                  GlStateManager.popMatrix();
               }
            }
         }

         GlStateManager.disableBlend();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      if (is.crafting) {
         this.drawHalo(is, par2, par4, par6, par8, is.craftCount);
      }

   }

   @Override


   public void render(TileEntity tileEntity, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      switch (this.type) {
         case 0:
            this.renderInfusionMatrix((TileInfusionMatrix)tileEntity, par2, par4, par6, par8);
            break;
         case 1:
            break;
      }

   }
}
