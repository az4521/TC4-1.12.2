package thaumcraft.client.renderers.tile;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelTubeValve;
import thaumcraft.common.tiles.TileTubeValve;
import net.minecraft.client.renderer.GlStateManager;

public class TileTubeValveRenderer extends TileEntitySpecialRenderer<TileTubeValve> {
   private ModelTubeValve model = new ModelTubeValve();

   @Override
   public void render(TileTubeValve valve, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (valve == null || valve.getWorld() == null) return;
      // Render base tube connections first
      TileTubeRenderer.renderTubeBase(valve, x, y, z);
      // Render valve model
      UtilsFX.bindTexture("textures/models/valve.png");
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
      if (valve.facing.getYOffset() == 0) {
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      } else {
         GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(90.0F, (float)valve.facing.getYOffset(), 0.0F, 0.0F);
      }
      GlStateManager.rotate(90.0F, (float)valve.facing.getXOffset(), (float)valve.facing.getYOffset(), (float)valve.facing.getZOffset());
      GlStateManager.rotate((float)((-valve.rotation) * 1.5F), 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, -(valve.rotation / 360.0F) * 0.12F, 0.0F);
      this.model.render();
      GlStateManager.popMatrix();
   }
}
