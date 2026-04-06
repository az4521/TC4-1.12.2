package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileInfusionPillar;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileInfusionPillarRenderer extends TileEntitySpecialRenderer<TileInfusionPillar> {
   private IModelCustom model;
   private static final ResourceLocation PILLAR = new ResourceLocation("thaumcraft", "textures/models/pillar.obj");

   public TileInfusionPillarRenderer() {
      this.model = AdvancedModelLoader.loadModel(PILLAR);
   }

   @Override
   public void render(TileInfusionPillar tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2 + 0.5F, (float)par4, (float)par6 + 0.5F);
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.bindTexture("textures/models/pillar.png");
      if (tile.orientation == 3) {
         GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      } else if (tile.orientation == 4) {
         GlStateManager.rotate(270.0F, 0.0F, 0.0F, 1.0F);
      } else if (tile.orientation == 5) {
         GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      }

      this.model.renderAll();
      GlStateManager.popMatrix();
   }

}
