package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelBanner;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.tiles.TileBanner;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileBannerRenderer extends TileEntitySpecialRenderer<TileBanner> {
   private ModelBanner model = new ModelBanner();

   @Override
   public void render(TileBanner banner, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      if (banner == null){return;}
      boolean flag = banner.getWorld() != null;
      long k = flag ? banner.getWorld().getTotalWorldTime() : 0L;
      GlStateManager.pushMatrix();
      if (banner.getAspect() == null && banner.getColor() == -1) {
         UtilsFX.bindTexture("textures/models/banner_cultist.png");
      } else {
         UtilsFX.bindTexture("textures/models/banner_blank.png");
      }

      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.5F, (float)par6 + 0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (banner.getWorld() != null) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         float f2 = (float)(banner.getFacing() * 360) / 16.0F;
         GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
      }

      if (!banner.getWall()) {
         this.model.renderPole();
      } else {
         GlStateManager.translate(0.0F, 0.0F, -0.4125);
      }

      this.model.renderBeam();
      if (banner.getColor() != -1) {
         Color c = new Color(Utils.colors[banner.getColor()]);
         GlStateManager.color((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, 1.0F);
      }

      this.model.renderTabs();
      float f3 = (float)(banner.getPos().getX() * 7 + banner.getPos().getY() * 9 + banner.getPos().getZ() * 13) + (float)k + par8;
      float rx = (0.005F + 0.005F * MathHelper.cos(f3 * (float)Math.PI * 0.02F)) * (float)Math.PI;
      this.model.Banner.rotateAngleX = rx;
      this.model.renderBanner();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (banner.getAspect() != null) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.0F, 0.05001F);
         GlStateManager.scale(0.0375, 0.0375, 0.0375);
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(-rx * (180F / (float)Math.PI) * 2.0F, 1.0F, 0.0F, 0.0F);
         UtilsFX.drawTag(-8, 4, banner.getAspect(), 0.0F, 0, 0.0F, 771, 0.75F, false);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
   }
}
