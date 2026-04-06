package thaumcraft.client.renderers.tile;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelTubeValve;
import thaumcraft.common.tiles.TileTubeBuffer;
import net.minecraft.client.renderer.GlStateManager;

public class TileTubeBufferRenderer extends TileEntitySpecialRenderer<TileTubeBuffer> {
   private ModelTubeValve model = new ModelTubeValve();

   @Override
   public void render(TileTubeBuffer buffer, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (buffer == null || buffer.getWorld() == null) return;
      // Render base tube connections first
      TileTubeRenderer.renderTubeBase(buffer, x, y, z);
      // Render choke indicators
      UtilsFX.bindTexture("textures/models/valve.png");
      for (EnumFacing dir : EnumFacing.values()) {
         if (buffer.chokedSides[dir.ordinal()] != 0 && buffer.openSides[dir.ordinal()] && ThaumcraftApiHelper.getConnectableTile(buffer.getWorld(), buffer.getPos().getX(), buffer.getPos().getY(), buffer.getPos().getZ(), dir) != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            if (dir.getOpposite().getYOffset() == 0) {
               GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            } else {
               GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
               GlStateManager.rotate(90.0F, (float)dir.getOpposite().getYOffset(), 0.0F, 0.0F);
            }
            GlStateManager.rotate(90.0F, (float)dir.getOpposite().getXOffset(), (float)dir.getOpposite().getYOffset(), (float)dir.getOpposite().getZOffset());
            if (buffer.chokedSides[dir.ordinal()] == 2) {
               GlStateManager.color(1.0F, 0.3F, 0.3F);
            } else {
               GlStateManager.color(0.3F, 0.3F, 1.0F);
            }
            GlStateManager.scale(1.2, 1.0F, 1.2);
            GlStateManager.translate(0.0F, -0.5F, 0.0F);
            this.model.render();
            GlStateManager.popMatrix();
         }
      }
   }
}
