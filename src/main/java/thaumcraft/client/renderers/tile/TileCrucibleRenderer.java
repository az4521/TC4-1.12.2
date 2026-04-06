package thaumcraft.client.renderers.tile;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileCrucible;

public class TileCrucibleRenderer extends TileEntitySpecialRenderer<TileCrucible> {
   private static final ResourceLocation TEX_TOP = new ResourceLocation("thaumcraft", "blocks/crucible1");
   private static final ResourceLocation TEX_BOTTOM = new ResourceLocation("thaumcraft", "blocks/crucible2");
   private static final ResourceLocation TEX_SIDE = new ResourceLocation("thaumcraft", "blocks/crucible3");
   private static final ResourceLocation TEX_INNER_SIDE = new ResourceLocation("thaumcraft", "blocks/crucible5");
   private static final ResourceLocation TEX_INNER_BOTTOM = new ResourceLocation("thaumcraft", "blocks/crucible6");

   @Override
   public void render(TileCrucible cr, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (cr == null || cr.getWorld() == null) return;

      GlStateManager.pushMatrix();
      GlStateManager.translate((float) x, (float) y, (float) z);

      RenderHelper.disableStandardItemLighting();
      int light = 0;
      for (EnumFacing face : EnumFacing.VALUES) {
         light = Math.max(light, cr.getWorld().getCombinedLight(cr.getPos().offset(face), 0));
      }
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

      Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      TextureAtlasSprite top = getSprite(TEX_TOP);
      TextureAtlasSprite bottom = getSprite(TEX_BOTTOM);
      TextureAtlasSprite side = getSprite(TEX_SIDE);
      TextureAtlasSprite innerSide = getSprite(TEX_INNER_SIDE);
      TextureAtlasSprite innerBottom = getSprite(TEX_INNER_BOTTOM);

      Tessellator tess = Tessellator.getInstance();
      BufferBuilder buf = tess.getBuffer();

      // Outer cube with per-face textures (top=crucible1 with hole, bottom=crucible2, sides=crucible3)
      drawQuadY(buf, tess, bottom, 0, 0, 0, 1, 1, false);  // bottom face
      drawQuadY(buf, tess, top, 0, 1, 0, 1, 1, true);       // top face (has hole texture)
      drawQuadZ(buf, tess, side, 0, 0, 0, 1, 1, false);     // north
      drawQuadZ(buf, tess, side, 0, 0, 1, 1, 1, true);      // south
      drawQuadX(buf, tess, side, 0, 0, 0, 1, 1, false);     // west
      drawQuadX(buf, tess, side, 1, 0, 0, 1, 1, true);      // east

      // Inner walls (facing inward)
      float f5 = 0.123F;
      drawQuadX(buf, tess, innerSide, f5, 0, 0, 1, 1, true);
      drawQuadX(buf, tess, innerSide, 1.0F - f5, 0, 0, 1, 1, false);
      drawQuadZ(buf, tess, innerSide, 0, 0, f5, 1, 1, true);
      drawQuadZ(buf, tess, innerSide, 0, 0, 1.0F - f5, 1, 1, false);
      // Inner bottom (at y=0.25)
      drawQuadY(buf, tess, innerBottom, 0, 0.25F, 0, 1, 1, true);
      // Inner underside of rim (facing down)
      drawQuadY(buf, tess, innerBottom, 0, 0.75F, 0, 1, 1, false);

      // Render fluid if present
      if (cr.tank.getFluidAmount() > 0) {
         TextureAtlasSprite water = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/water_still");
         float recolor = (float) cr.tagAmount() / 100.0F;
         if (recolor > 0.0F) {
            recolor = 0.5F + recolor / 2.0F;
         }
         GlStateManager.color(1.0F - recolor / 3.0F, 1.0F - recolor, 1.0F - recolor / 2.0F, 1.0F);
         drawQuadY(buf, tess, water, 0, cr.getFluidHeight(), 0, 1, 1, true);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

      RenderHelper.enableStandardItemLighting();
      GlStateManager.popMatrix();
   }

   private static TextureAtlasSprite getSprite(ResourceLocation loc) {
      return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
   }

   private static void drawBox(BufferBuilder buf, Tessellator tess, TextureAtlasSprite s,
                                float x1, float y1, float z1, float x2, float y2, float z2) {
      float u0 = s.getMinU(), u1 = s.getMaxU(), v0 = s.getMinV(), v1 = s.getMaxV();
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      // Bottom
      buf.pos(x1, y1, z2).tex(u0, v1).endVertex();
      buf.pos(x1, y1, z1).tex(u0, v0).endVertex();
      buf.pos(x2, y1, z1).tex(u1, v0).endVertex();
      buf.pos(x2, y1, z2).tex(u1, v1).endVertex();
      // Top
      buf.pos(x1, y2, z1).tex(u0, v0).endVertex();
      buf.pos(x1, y2, z2).tex(u0, v1).endVertex();
      buf.pos(x2, y2, z2).tex(u1, v1).endVertex();
      buf.pos(x2, y2, z1).tex(u1, v0).endVertex();
      // North
      buf.pos(x2, y2, z1).tex(u0, v0).endVertex();
      buf.pos(x2, y1, z1).tex(u0, v1).endVertex();
      buf.pos(x1, y1, z1).tex(u1, v1).endVertex();
      buf.pos(x1, y2, z1).tex(u1, v0).endVertex();
      // South
      buf.pos(x1, y2, z2).tex(u0, v0).endVertex();
      buf.pos(x1, y1, z2).tex(u0, v1).endVertex();
      buf.pos(x2, y1, z2).tex(u1, v1).endVertex();
      buf.pos(x2, y2, z2).tex(u1, v0).endVertex();
      // West
      buf.pos(x1, y2, z1).tex(u0, v0).endVertex();
      buf.pos(x1, y1, z1).tex(u0, v1).endVertex();
      buf.pos(x1, y1, z2).tex(u1, v1).endVertex();
      buf.pos(x1, y2, z2).tex(u1, v0).endVertex();
      // East
      buf.pos(x2, y2, z2).tex(u0, v0).endVertex();
      buf.pos(x2, y1, z2).tex(u0, v1).endVertex();
      buf.pos(x2, y1, z1).tex(u1, v1).endVertex();
      buf.pos(x2, y2, z1).tex(u1, v0).endVertex();
      tess.draw();
   }

   private static void drawQuadY(BufferBuilder buf, Tessellator tess, TextureAtlasSprite s,
                                   float x, float y, float z, float w, float d, boolean up) {
      float u0 = s.getMinU(), u1 = s.getMaxU(), v0 = s.getMinV(), v1 = s.getMaxV();
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      if (up) {
         buf.pos(x, y, z).tex(u0, v0).endVertex();
         buf.pos(x, y, z + d).tex(u0, v1).endVertex();
         buf.pos(x + w, y, z + d).tex(u1, v1).endVertex();
         buf.pos(x + w, y, z).tex(u1, v0).endVertex();
      } else {
         buf.pos(x, y, z + d).tex(u0, v1).endVertex();
         buf.pos(x, y, z).tex(u0, v0).endVertex();
         buf.pos(x + w, y, z).tex(u1, v0).endVertex();
         buf.pos(x + w, y, z + d).tex(u1, v1).endVertex();
      }
      tess.draw();
   }

   private static void drawQuadX(BufferBuilder buf, Tessellator tess, TextureAtlasSprite s,
                                   float x, float y1, float z1, float y2, float z2, boolean facingPos) {
      float u0 = s.getMinU(), u1 = s.getMaxU(), v0 = s.getMinV(), v1 = s.getMaxV();
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      if (facingPos) {
         buf.pos(x, y2, z2).tex(u0, v0).endVertex();
         buf.pos(x, y1, z2).tex(u0, v1).endVertex();
         buf.pos(x, y1, z1).tex(u1, v1).endVertex();
         buf.pos(x, y2, z1).tex(u1, v0).endVertex();
      } else {
         buf.pos(x, y2, z1).tex(u0, v0).endVertex();
         buf.pos(x, y1, z1).tex(u0, v1).endVertex();
         buf.pos(x, y1, z2).tex(u1, v1).endVertex();
         buf.pos(x, y2, z2).tex(u1, v0).endVertex();
      }
      tess.draw();
   }

   private static void drawQuadZ(BufferBuilder buf, Tessellator tess, TextureAtlasSprite s,
                                   float x1, float y1, float z, float x2, float y2, boolean facingPos) {
      float u0 = s.getMinU(), u1 = s.getMaxU(), v0 = s.getMinV(), v1 = s.getMaxV();
      buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      if (facingPos) {
         buf.pos(x1, y2, z).tex(u0, v0).endVertex();
         buf.pos(x1, y1, z).tex(u0, v1).endVertex();
         buf.pos(x2, y1, z).tex(u1, v1).endVertex();
         buf.pos(x2, y2, z).tex(u1, v0).endVertex();
      } else {
         buf.pos(x2, y2, z).tex(u0, v0).endVertex();
         buf.pos(x2, y1, z).tex(u0, v1).endVertex();
         buf.pos(x1, y1, z).tex(u1, v1).endVertex();
         buf.pos(x1, y2, z).tex(u1, v0).endVertex();
      }
      tess.draw();
   }
}
