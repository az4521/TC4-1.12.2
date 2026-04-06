package thaumcraft.client.renderers.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileFluxScrubber;
import net.minecraft.client.renderer.GlStateManager;

public class TileFluxScrubberRenderer extends TileEntitySpecialRenderer<TileFluxScrubber> {
   private IModelCustom model;
   private static final ResourceLocation CAP = new ResourceLocation("thaumcraft", "textures/models/obelisk_cap.obj");

   public TileFluxScrubberRenderer() {
      this.model = AdvancedModelLoader.loadModel(CAP);
   }

   @Override
   public void render(TileFluxScrubber te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (te == null) return;
      GlStateManager.pushMatrix();
      int facing = te.facing.ordinal();
      this.translateFromOrientation(x, y, z, facing);
      UtilsFX.bindTexture("textures/models/fluxscrubber.png");
      this.model.renderPart("Cap");
      float q = (float) Minecraft.getMinecraft().player.ticksExisted + partialTicks + (float) te.count;
      float bob = MathHelper.sin(q / 8.0F) * 0.075F + 0.075F;
      GlStateManager.translate(0.0F, 0.0F, -bob);
      this.model.renderPart("Tip");
      GlStateManager.popMatrix();
   }

   private void translateFromOrientation(double x, double y, double z, int orientation) {
      GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
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
