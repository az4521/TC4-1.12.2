package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileVisRelay;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileVisRelayRenderer extends TileEntitySpecialRenderer<TileVisRelay> {
   private IModelCustom model;
   private static final ResourceLocation RELAY = new ResourceLocation("thaumcraft", "textures/models/vis_relay.obj");

   public TileVisRelayRenderer() {
      this.model = AdvancedModelLoader.loadModel(RELAY);
   }

   @Override
   public void render(TileVisRelay tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      int facing = 1;
      if (tile.getWorld() != null) {
         facing = tile.orientation;
      }

      int ticks = Minecraft.getMinecraft().getRenderViewEntity().ticksExisted;
      GlStateManager.pushMatrix();
      this.translateFromOrientation(par2, par4, par6, facing);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(45.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/vis_relay.png");
      GlStateManager.pushMatrix();
      GlStateManager.scale(0.75F, 0.75F, 0.75F);
      GlStateManager.translate(0.0F, 0.0F, -0.16);
      this.model.renderPart("RingBase");
      GlStateManager.popMatrix();
      this.model.renderPart("RingFloat");
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      if (tile.color >= 0) {
         Color c = new Color(TileVisRelay.colors[tile.color]);
         GlStateManager.color((float)c.getRed() / 200.0F, (float)c.getGreen() / 200.0F, (float)c.getBlue() / 200.0F);
      }

      float scale = MathHelper.sin(((float)ticks + par8) / 2.0F) * 0.05F + 0.95F;
      int j = (VisNetHandler.isNodeValid(tile.getParent()) ? 50 : 0) + (int)(150.0F * scale);
      int k = j % 65536;
      int l = j / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
      this.model.renderPart("Crystal");
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      GlStateManager.popMatrix();
   }

   private void translateFromOrientation(double x, double y, double z, int orientation) {
      GlStateManager.translate(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F);
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

   }

}
