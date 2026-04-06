package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelTable;
import thaumcraft.common.tiles.TileTable;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileTableRenderer extends TileEntitySpecialRenderer<TileTable> {
   private ModelTable tableModel = new ModelTable();

   @Override
   public void render(TileTable table, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      int md = 0;
      if (table.getWorld() != null) {
         md =
        table.getBlockMetadata();
      }

      if (md < 6) {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture("textures/models/table.png");
         GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.0F, (float)par6 + 0.5F);
         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         if (md == 1) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.tableModel.renderAll();
         GlStateManager.popMatrix();
      }
   }

}
