package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileEssentiaCrystalizer;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileEssentiaCrystalizerRenderer extends TileEntitySpecialRenderer<TileEssentiaCrystalizer> {
   private IModelCustom model;
   private IModelCustom model2;
   private static final ResourceLocation RELAY = new ResourceLocation("thaumcraft", "textures/models/crystalizer.obj");
   private static final ResourceLocation CRYSTAL = new ResourceLocation("thaumcraft", "textures/models/vis_relay.obj");

   public TileEssentiaCrystalizerRenderer() {
      this.model = AdvancedModelLoader.loadModel(RELAY);
      this.model2 = AdvancedModelLoader.loadModel(CRYSTAL);
   }

   @Override
   public void render(TileEssentiaCrystalizer tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      int facing = tile.facing.ordinal();
      int ticks = Minecraft.getMinecraft().getRenderViewEntity().ticksExisted;
      GlStateManager.pushMatrix();
      this.translateFromOrientation(par2, par4, par6, facing);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/crystalizer.png");
      this.model.renderAll();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      UtilsFX.bindTexture("textures/models/vis_relay.png");
      GlStateManager.color(tile.cr, tile.cg, tile.cb);

      for(int q = 0; q < 4; ++q) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.75F, 0.75F, 0.75F);
         float glow = MathHelper.sin(((float)ticks + par8 + (float)(q * 10)) / 2.0F) * 0.05F + 0.95F;
         int j = 50 + (int)(150.0F * glow);
         int k = j % 65536;
         int l = j / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         GlStateManager.rotate((float)(90 * q), 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.34, 0.0F, 1.2125);
         GlStateManager.rotate(tile.spin + tile.spinInc * par8, 0.0F, 0.0F, 1.0F);
         this.model2.renderPart("Crystal");
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      GlStateManager.popMatrix();
   }

   private void translateFromOrientation(double x, double y, double z, int orientation) {
      GlStateManager.translate(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F);
      if (orientation == 0) {
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 1) {
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation != 2) {
         if (orientation == 3) {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         } else if (orientation == 4) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         } else if (orientation == 5) {
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
         }
      }

      GlStateManager.translate(0.0F, 0.0F, -0.5F);
   }

}
