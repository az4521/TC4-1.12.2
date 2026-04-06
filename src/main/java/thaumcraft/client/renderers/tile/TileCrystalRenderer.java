package thaumcraft.client.renderers.tile;

import java.awt.Color;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelCrystal;
import thaumcraft.common.blocks.BlockCustomOreItem;
import thaumcraft.common.tiles.TileCrystal;
import net.minecraft.client.renderer.GlStateManager;

public class TileCrystalRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private ModelCrystal model = new ModelCrystal();

   private void translateFromOrientation(float x, float y, float z, int orientation) {
      if (orientation == 0) {
         GlStateManager.translate(x + 0.5F, y + 1.3F, z + 0.5F);
         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 1) {
         GlStateManager.translate(x + 0.5F, y - 0.3F, z + 0.5F);
      } else if (orientation == 2) {
         GlStateManager.translate(x + 0.5F, y + 0.5F, z + 1.3F);
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 3) {
         GlStateManager.translate(x + 0.5F, y + 0.5F, z - 0.3F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 4) {
         GlStateManager.translate(x + 1.3F, y + 0.5F, z + 0.5F);
         GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      } else if (orientation == 5) {
         GlStateManager.translate(x - 0.3F, y + 0.5F, z + 0.5F);
         GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
      }

   }

   private void drawCrystal(int ori, float x, float y, float z, float a1, float a2, Random rand, int color, float size) {
      EntityPlayer p = Minecraft.getMinecraft().player;
      float shade = MathHelper.sin((float)(p.ticksExisted + rand.nextInt(10)) / (5.0F + rand.nextFloat())) * 0.075F + 0.925F;
      Color c = new Color(color);
      float r = (float)c.getRed() / 220.0F;
      float g = (float)c.getGreen() / 220.0F;
      float b = (float)c.getBlue() / 220.0F;
      GlStateManager.pushMatrix();
      GL11.glEnable(2977);
      GlStateManager.enableBlend();
      GlStateManager.enableRescaleNormal();
      GlStateManager.blendFunc(770, 771);
      this.translateFromOrientation(x, y, z, ori);
      GlStateManager.rotate(a1, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(a2, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale((0.15F + rand.nextFloat() * 0.075F) * size, (0.5F + rand.nextFloat() * 0.1F) * size, (0.15F + rand.nextFloat() * 0.05F) * size);
      int var19 = (int)(210.0F * shade);
      int var20 = var19 % 65536;
      int var21 = var19 / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) var20, (float) var21);
      GlStateManager.color(r, g, b, 1.0F);
      this.model.render();
      GlStateManager.scale(1.0F, 1.0F, 1.0F);
      GlStateManager.disableRescaleNormal();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   @Override


   public void render(TileEntity te, double x, double y, double z, float f, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      TileCrystal tco = (TileCrystal)te;
      int md =
        tco.getBlockMetadata();
      int color = BlockCustomOreItem.colors[5];
      if (md != 6) {
         color = BlockCustomOreItem.colors[md + 1];
      }

      UtilsFX.bindTexture("textures/models/crystal.png");
      Random rand = new Random(
        tco.getBlockMetadata() + tco.getPos().getX() + (long) tco.getPos().getY() * tco.getPos().getZ());
      this.drawCrystal(tco.orientation, (float)x, (float)y, (float)z, (rand.nextFloat() - rand.nextFloat()) * 5.0F, (rand.nextFloat() - rand.nextFloat()) * 5.0F, rand, color, 1.1F);

      for(int a = 1; a < 6; ++a) {
         if (md == 6) {
            color = BlockCustomOreItem.colors[a == 5 ? 6 : a];
         }

         int angle1 = rand.nextInt(36) + 72 * a;
         int angle2 = 15 + rand.nextInt(15);
         this.drawCrystal(tco.orientation, (float)x, (float)y, (float)z, (float)angle1, (float)angle2, rand, color, 0.8F);
      }

      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
   }
}
