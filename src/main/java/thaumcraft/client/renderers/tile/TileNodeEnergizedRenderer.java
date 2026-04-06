package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileNodeEnergized;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TileNodeEnergizedRenderer extends TileEntitySpecialRenderer<TileNodeEnergized> {
   static String tx1 = "textures/items/lightningringv.png";

   @Override
   public void render(TileNodeEnergized tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (tile == null || tile.getWorld() == null) return;
      EntityLivingBase viewer = Minecraft.getMinecraft().player;
      TileNodeRenderer.renderNode(viewer, 64.0F, true, false, 1.0F, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), partialTicks, tile.getAuraBase(), tile.getNodeType(), tile.getNodeModifier());
      GlStateManager.pushMatrix();
      GlStateManager.alphaFunc(516, 0.003921569F);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      long nt = System.nanoTime();
      UtilsFX.bindTexture(tx1);
      int frames = UtilsFX.getTextureAnimationSize(tx1);
      int i = (int)(((double)(nt / 40000000L) + x) % (double)frames);
      UtilsFX.renderFacingQuad((double)tile.getPos().getX() + 0.5, (double)tile.getPos().getY() + 0.5, (double)tile.getPos().getZ() + 0.5, 0.0F, 0.33F, 0.9F, frames, i, partialTicks, 16777215);
      GlStateManager.disableBlend();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.popMatrix();
   }
}
