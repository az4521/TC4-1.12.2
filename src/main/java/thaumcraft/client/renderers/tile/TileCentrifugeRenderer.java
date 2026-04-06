package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelCentrifuge;
import thaumcraft.common.tiles.TileCentrifuge;
import net.minecraft.client.renderer.GlStateManager;

public class TileCentrifugeRenderer extends TileEntitySpecialRenderer<TileCentrifuge> {
   private ModelCentrifuge model = new ModelCentrifuge();

   @Override
   public void render(TileCentrifuge cf, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (cf == null) return;
      this.renderEntityAt(cf, x, y, z, partialTicks);
   }

   public void renderEntityAt(TileCentrifuge cf, double x, double y, double z, float fq) {
      Minecraft mc = FMLClientHandler.instance().getClient();
      UtilsFX.bindTexture("textures/models/centrifuge.png");
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F);
      this.model.renderBoxes();
      GlStateManager.rotate(cf.rotation, 0.0F, 1.0F, 0.0F);
      this.model.renderSpinnyBit();
      GlStateManager.popMatrix();
   }

}
