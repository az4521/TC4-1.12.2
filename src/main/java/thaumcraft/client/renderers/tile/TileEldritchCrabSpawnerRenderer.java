package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileEldritchCrabSpawner;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileEldritchCrabSpawnerRenderer extends TileEntitySpecialRenderer<TileEldritchCrabSpawner> {
   private IModelCustom model;
   private static final ResourceLocation VENT = new ResourceLocation("thaumcraft", "textures/models/crabvent.obj");

   public TileEldritchCrabSpawnerRenderer() {
      this.model = AdvancedModelLoader.loadModel(VENT);
   }

   @Override
   public void render(TileEldritchCrabSpawner tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      this.translateFromOrientation(par2, par4, par6, tile.getFacing());
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.bindTexture("textures/models/crabvent.png");
      this.model.renderAll();
      GlStateManager.popMatrix();
   }

   private void translateFromOrientation(double x, double y, double z, int orientation) {
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

   }

}
