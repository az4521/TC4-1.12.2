package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileCrystal;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileEldritchCrystalRenderer extends TileEntitySpecialRenderer<TileCrystal> {
   private IModelCustom model;
   private static final ResourceLocation CRYSTAL = new ResourceLocation("thaumcraft", "textures/models/vcrystal.obj");

   public TileEldritchCrystalRenderer() {
      this.model = AdvancedModelLoader.loadModel(CRYSTAL);
   }

   @Override
   public void render(TileCrystal tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      this.translateFromOrientation(par2, par4, par6, tile.orientation, (tile.getWorld() == null ? 0 : tile.hashCode()) % 4);
      EntityPlayer p = Minecraft.getMinecraft().player;
      UtilsFX.bindTexture("textures/blocks/crust.png");
      this.model.renderPart("Base");
      float shade = MathHelper.sin((float)p.ticksExisted / 6.0F) * 0.075F + 0.925F;
      int var19 = (int)(210.0F * shade);
      int var20 = var19 % 65536;
      int var21 = var19 / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) var20, (float) var21);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
      UtilsFX.bindTexture("textures/models/vcrystal.png");
      this.model.renderPart("Crystal");
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   private void translateFromOrientation(double x, double y, double z, int orientation, int r) {
      GlStateManager.translate(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F);
      if (orientation == 0) {
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 1) {
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 2) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      } else if (orientation != 3) {
         if (orientation == 4) {
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
         } else if (orientation == 5) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         }
      }

      GlStateManager.translate(0.0F, 0.0F, -0.5F);
      GlStateManager.rotate(90.0F * (float)r, 0.0F, 0.0F, 1.0F);
   }

}
