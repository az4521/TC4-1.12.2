package thaumcraft.client.renderers.tile;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelTubeValve;
import thaumcraft.common.tiles.TileTubeOneway;
import net.minecraft.client.renderer.GlStateManager;

public class TileTubeOnewayRenderer extends TileEntitySpecialRenderer<TileTubeOneway> {
   private ModelTubeValve model = new ModelTubeValve();

   @Override
   public void render(TileTubeOneway valve, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (valve == null || valve.getWorld() == null) return;
      // Render base tube connections first
      TileTubeRenderer.renderTubeBase(valve, x, y, z);
      // Render oneway arrows
      if (ThaumcraftApiHelper.getConnectableTile(valve.getWorld(), valve.getPos().getX(), valve.getPos().getY(), valve.getPos().getZ(), valve.facing.getOpposite()) != null) {
         UtilsFX.bindTexture("textures/models/valve.png");
         EnumFacing fd = valve.facing;
         GlStateManager.pushMatrix();
         GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
         if (fd.getYOffset() == 0) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         } else {
            GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, (float)fd.getYOffset(), 0.0F, 0.0F);
         }
         GlStateManager.rotate(90.0F, (float)fd.getXOffset(), (float)fd.getYOffset(), (float)fd.getZOffset());
         GlStateManager.color(0.45F, 0.5F, 1.0F);
         GlStateManager.scale(1.1, 0.5F, 1.1);
         GlStateManager.translate(0.0F, -0.5F, 0.0F);
         this.model.render();
         GlStateManager.translate(0.0F, -0.25F, 0.0F);
         this.model.render();
         GlStateManager.translate(0.0F, -0.25F, 0.0F);
         this.model.render();
         GlStateManager.popMatrix();
      }
   }
}
