package thaumcraft.client.renderers.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelBellows;
import thaumcraft.common.tiles.TileBellows;
import net.minecraft.client.renderer.GlStateManager;

public class TileBellowsRenderer extends TileEntitySpecialRenderer<TileBellows> {
   private ModelBellows model = new ModelBellows();

   @Override
   public void render(TileBellows bellows, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (bellows == null) return;
      this.renderEntityAt(bellows, x, y, z, partialTicks);
   }

   private void translateFromOrientation(double x, double y, double z, int orientation) {
      GlStateManager.translate((float)x + 0.5F, (float)y - 0.5F, (float)z + 0.5F);
      if (orientation == 0) {
         GlStateManager.translate(0.0F, 1.0F, -1.0F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 1) {
         GlStateManager.translate(0.0F, 1.0F, 1.0F);
         GlStateManager.rotate(270.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 2) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      } else if (orientation == 4) {
         GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
      } else if (orientation == 5) {
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      }

   }

   public void renderEntityAt(TileBellows bellows, double x, double y, double z, float fq) {
      float scale = 0.0F;
      if (bellows.getWorld() == null) {
         EntityPlayer p = Minecraft.getMinecraft().player;
         scale = MathHelper.sin((float)p.ticksExisted / 8.0F) * 0.3F + 0.7F;
         bellows.orientation = 2;
      } else {
         scale = bellows.inflation;
      }

      float tscale = 0.125F + scale * 0.875F;
      UtilsFX.bindTexture("textures/models/bellows.png");
      GlStateManager.pushMatrix();
      GL11.glEnable(2977);
      GlStateManager.enableBlend();
      GlStateManager.enableRescaleNormal();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.translateFromOrientation((float)x, (float)y, (float)z, bellows.orientation);
      GlStateManager.translate(0.0F, 1.0F, 0.0F);
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.5F, (scale + 0.1F) / 2.0F, 0.5F);
      this.model.Bag.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.model.Bag.render(0.0625F);
      GlStateManager.scale(1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
      GlStateManager.translate(0.0F, -1.0F, 0.0F);
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, -tscale / 2.0F + 0.5F, 0.0F);
      this.model.TopPlank.render(0.0625F);
      GlStateManager.translate(0.0F, tscale / 2.0F - 0.5F, 0.0F);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, tscale / 2.0F - 0.5F, 0.0F);
      this.model.BottomPlank.render(0.0625F);
      GlStateManager.translate(0.0F, -tscale / 2.0F + 0.5F, 0.0F);
      GlStateManager.popMatrix();
      this.model.render();
      GlStateManager.disableRescaleNormal();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

}
