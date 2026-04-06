package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileWandPedestal;

@SideOnly(Side.CLIENT)
public class TileWandPedestalRenderer extends TileEntitySpecialRenderer<TileWandPedestal> {
   @Override
   public void render(TileWandPedestal ped, double par2, double par4, double par6, float partialTicks, int destroyStage, float alpha) {
      if (ped == null || ped.getWorld() == null) return;

      // Render pedestal block shape
      renderPedestalBlock(ped, par2, par4, par6);

      // Render floating item and drain effects
      if (!ped.getStackInSlot(0).isEmpty()) {
         float ticks = (float) Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + partialTicks;
         GlStateManager.pushMatrix();
         float h = MathHelper.sin(ticks % 32767.0F / 16.0F) * 0.05F;
         GlStateManager.translate((float) par2 + 0.5F, (float) par4 + 1.15F + h, (float) par6 + 0.5F);
         GlStateManager.rotate(ticks % 360.0F, 0.0F, 1.0F, 0.0F);
         ItemStack is = ped.getStackInSlot(0).copy();
         is.setCount(1);
         EntityItem entityitem = new EntityItem(ped.getWorld(), 0.0F, 0.0F, 0.0F, is);
         entityitem.hoverStart = 0.0F;
         Minecraft.getMinecraft().getRenderManager().renderEntity(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
         GlStateManager.popMatrix();
         if (ped.draining) {
            GlStateManager.pushMatrix();
            UtilsFX.drawFloatyLine((double) ped.getPos().getX() + 0.5, (double) ped.getPos().getY() + 1.65 - (double) (h * 2.0F), (double) ped.getPos().getZ() + 0.5, (double) ped.drainX + 0.5, (double) ped.drainY + 0.5, (double) ped.drainZ + 0.5, partialTicks, ped.drainColor, "textures/misc/wispy.png", -0.02F, Math.min(ticks, 10.0F) / 10.0F);
            GlStateManager.popMatrix();
         }
      }
   }

   private void renderPedestalBlock(TileWandPedestal ped, double x, double y, double z) {
      GlStateManager.pushMatrix();
      GlStateManager.translate((float) x, (float) y, (float) z);

      RenderHelper.disableStandardItemLighting();
      int light = 0;
      for (EnumFacing face : EnumFacing.VALUES) {
         light = Math.max(light, ped.getWorld().getCombinedLight(ped.getPos().offset(face), 0));
      }
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

      Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      TextureAtlasSprite side = getSprite("thaumcraft:blocks/wandpedestal_side");
      TextureAtlasSprite top = getSprite("thaumcraft:blocks/wandpedestal_top");

      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.getBuffer();

      // Base (0-0.25) — full width
      drawBox(buf, tess, side, top, 0, 0, 0, 1, 0.25F, 1);
      // Pillar (0.25-0.75) — thin middle
      drawBox(buf, tess, side, top, 0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
      // Top cap (0.75-1.0) — wider top
      drawBox(buf, tess, side, top, 0.125F, 0.75F, 0.125F, 0.875F, 1.0F, 0.875F);

      RenderHelper.enableStandardItemLighting();
      GlStateManager.popMatrix();
   }

   private TextureAtlasSprite getSprite(String name) {
      return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
   }

   private void drawBox(BufferBuilder buf, Tessellator tess, TextureAtlasSprite side, TextureAtlasSprite top,
                         float x1, float y1, float z1, float x2, float y2, float z2) {
      float su0 = side.getMinU(), su1 = side.getMaxU(), sv0 = side.getMinV(), sv1 = side.getMaxV();
      float tu0 = top.getMinU(), tu1 = top.getMaxU(), tv0 = top.getMinV(), tv1 = top.getMaxV();
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      buf.pos(x1, y1, z2).tex(tu0, tv1).endVertex();
      buf.pos(x1, y1, z1).tex(tu0, tv0).endVertex();
      buf.pos(x2, y1, z1).tex(tu1, tv0).endVertex();
      buf.pos(x2, y1, z2).tex(tu1, tv1).endVertex();
      buf.pos(x1, y2, z1).tex(tu0, tv0).endVertex();
      buf.pos(x1, y2, z2).tex(tu0, tv1).endVertex();
      buf.pos(x2, y2, z2).tex(tu1, tv1).endVertex();
      buf.pos(x2, y2, z1).tex(tu1, tv0).endVertex();
      buf.pos(x2, y2, z1).tex(su0, sv0).endVertex();
      buf.pos(x2, y1, z1).tex(su0, sv1).endVertex();
      buf.pos(x1, y1, z1).tex(su1, sv1).endVertex();
      buf.pos(x1, y2, z1).tex(su1, sv0).endVertex();
      buf.pos(x1, y2, z2).tex(su0, sv0).endVertex();
      buf.pos(x1, y1, z2).tex(su0, sv1).endVertex();
      buf.pos(x2, y1, z2).tex(su1, sv1).endVertex();
      buf.pos(x2, y2, z2).tex(su1, sv0).endVertex();
      buf.pos(x1, y2, z1).tex(su0, sv0).endVertex();
      buf.pos(x1, y1, z1).tex(su0, sv1).endVertex();
      buf.pos(x1, y1, z2).tex(su1, sv1).endVertex();
      buf.pos(x1, y2, z2).tex(su1, sv0).endVertex();
      buf.pos(x2, y2, z2).tex(su0, sv0).endVertex();
      buf.pos(x2, y1, z2).tex(su0, sv1).endVertex();
      buf.pos(x2, y1, z1).tex(su1, sv1).endVertex();
      buf.pos(x2, y2, z1).tex(su1, sv0).endVertex();
      tess.draw();
   }
}
