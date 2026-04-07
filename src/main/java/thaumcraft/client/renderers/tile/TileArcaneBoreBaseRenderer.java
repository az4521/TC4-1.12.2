package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelBoreBase;
import thaumcraft.common.tiles.TileArcaneBoreBase;
import net.minecraft.client.renderer.GlStateManager;

public class TileArcaneBoreBaseRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private ModelBoreBase model = new ModelBoreBase();

   public void renderEntityAt(TileArcaneBoreBase bore, double x, double y, double z, float fq) {
      if (bore == null){return;}
      Minecraft mc = FMLClientHandler.instance().getClient();
      UtilsFX.bindTexture("textures/models/bore.png");
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)x + 0.5F, (float)y, (float)z + 0.5F);
      GlStateManager.pushMatrix();
      this.model.render();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      switch (bore.orientation.ordinal()) {
         case 2:
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 3:
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 4:
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 5:
            GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
      }

      this.model.renderNozzle();
      GlStateManager.popMatrix();
      GlStateManager.popMatrix();
   }

   @Override


   public void render(TileEntity tileentity, double d, double d1, double d2, float f, int destroyStage, float alpha) {
      if (! (tileentity instanceof TileArcaneBoreBase)) {return;}
      this.renderEntityAt((TileArcaneBoreBase)tileentity, d, d1, d2, f);
   }
}
