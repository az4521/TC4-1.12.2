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
import thaumcraft.common.tiles.TileMagicWorkbenchCharger;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileMagicWorkbenchChargerRenderer extends TileEntitySpecialRenderer<TileMagicWorkbenchCharger> {
   private IModelCustom model;
   private static final ResourceLocation RELAY = new ResourceLocation("thaumcraft", "textures/models/vis_relay.obj");

   public TileMagicWorkbenchChargerRenderer() {
      this.model = AdvancedModelLoader.loadModel(RELAY);
   }

   @Override
   public void render(TileMagicWorkbenchCharger tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      int facing = 1;
      if (tile.getWorld() != null) {
         facing = tile.orientation;
      }

      int ticks = Minecraft.getMinecraft().getRenderViewEntity().ticksExisted;
      GlStateManager.pushMatrix();
      GlStateManager.translate(par2 + (double)0.5F, par4 + (double)0.5F, par6 + (double)0.5F);
      GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(45.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/vis_relay.png");
      this.model.renderPart("RingFloat");
      GlStateManager.pushMatrix();
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, 0.5F);

      for(int a = 0; a < 4; ++a) {
         this.model.renderPart("Support");
         GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      }

      GlStateManager.popMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      if (tile.color >= 0) {
         Color c = new Color(TileMagicWorkbenchCharger.colors[tile.color]);
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

}
