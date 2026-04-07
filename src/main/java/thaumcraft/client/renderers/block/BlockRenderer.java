package thaumcraft.client.renderers.block;

import org.lwjgl.opengl.GL11;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.IBlockAccess;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class BlockRenderer {
   public static float W1 = 0.0625F;
   public static float W2 = 0.125F;
   public static float W3 = 0.1875F;
   public static float W4 = 0.25F;
   public static float W5 = 0.3125F;
   public static float W6 = 0.375F;
   public static float W7 = 0.4375F;
   public static float W8 = 0.5F;
   public static float W9 = 0.5625F;
   public static float W10 = 0.625F;
   public static float W11 = 0.6875F;
   public static float W12 = 0.75F;
   public static float W13 = 0.8125F;
   public static float W14 = 0.875F;
   public static float W15 = 0.9375F;

   public static void drawFaces(RenderBlocks renderblocks, Block block, TextureAtlasSprite icon, boolean st) {
      drawFaces(renderblocks, block, icon, icon, icon, icon, icon, icon, st);
   }

   public static void drawFaces(RenderBlocks renderblocks, Block block, TextureAtlasSprite i1, TextureAtlasSprite i2, TextureAtlasSprite i3, TextureAtlasSprite i4, TextureAtlasSprite i5, TextureAtlasSprite i6, boolean solidtop) {
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      renderblocks.renderFaceYNeg(block, 0.0F, 0.0F, 0.0F, i1);
      if (solidtop) {
         GlStateManager.disableAlpha();
      }

      renderblocks.renderFaceYPos(block, 0.0F, 0.0F, 0.0F, i2);
      if (solidtop) {
         GlStateManager.enableAlpha();
      }

      renderblocks.renderFaceXNeg(block, 0.0F, 0.0F, 0.0F, i3);
      renderblocks.renderFaceXPos(block, 0.0F, 0.0F, 0.0F, i4);
      renderblocks.renderFaceZNeg(block, 0.0F, 0.0F, 0.0F, i5);
      renderblocks.renderFaceZPos(block, 0.0F, 0.0F, 0.0F, i6);
      GlStateManager.translate(0.5F, 0.5F, 0.5F);
   }

   public static int setBrightness(IBlockAccess blockAccess, int i, int j, int k, Block block) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      int mb = 0;
     
      float f = 1.0F;
      int l = 0xFFFFFF;
      float f1 = (float)(l >> 16 & 255) / 255.0F;
      float f2 = (float)(l >> 8 & 255) / 255.0F;
      float f3 = (float)(l & 255) / 255.0F;
      if (false) {
         float f6 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
         float f4 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
         float f7 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
         f1 = f6;
         f2 = f4;
         f3 = f7;
      }
      return mb;
   }

   protected static void renderAllSides(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer, TextureAtlasSprite tex) {
      renderAllSides(world, x, y, z, block, renderer, tex, true);
   }

   protected static void renderAllSides(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer, TextureAtlasSprite tex, boolean allsides) {
      if (allsides || true) {
         renderer.renderFaceXPos(block, x, y, z, tex);
      }

      if (allsides || true) {
         renderer.renderFaceXNeg(block, x, y, z, tex);
      }

      if (allsides || true) {
         renderer.renderFaceZPos(block, x, y, z, tex);
      }

      if (allsides || true) {
         renderer.renderFaceZNeg(block, x, y, z, tex);
      }

      if (allsides || true) {
         renderer.renderFaceYPos(block, x, y, z, tex);
      }

      if (allsides || true) {
         renderer.renderFaceYNeg(block, x, y, z, tex);
      }

   }

   protected static void renderAllSides(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer, boolean allsides) {
      if (allsides || true) {
         renderer.renderFaceXPos(block, x, y, z, null);
      }

      if (allsides || true) {
         renderer.renderFaceXNeg(block, x, y, z, null);
      }

      if (allsides || true) {
         renderer.renderFaceZPos(block, x, y, z, null);
      }

      if (allsides || true) {
         renderer.renderFaceZNeg(block, x, y, z, null);
      }

      if (allsides || true) {
         renderer.renderFaceYPos(block, x, y, z, null);
      }

      if (allsides || true) {
         renderer.renderFaceYNeg(block, x, y, z, null);
      }

   }

   protected static void renderAllSidesInverted(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer, TextureAtlasSprite tex, boolean allsides) {
      if (allsides || !true) {
         renderer.renderFaceXPos(block, x - 1, y, z, tex);
      }

      if (allsides || !true) {
         renderer.renderFaceXNeg(block, x + 1, y, z, tex);
      }

      if (allsides || !true) {
         renderer.renderFaceZPos(block, x, y, z - 1, tex);
      }

      if (allsides || !true) {
         renderer.renderFaceZNeg(block, x, y, z + 1, tex);
      }

      if (allsides || !true) {
         renderer.renderFaceYPos(block, x, y - 1, z, tex);
      }

      if (allsides || !true) {
         renderer.renderFaceYNeg(block, x, y + 1, z, tex);
      }

   }

   protected static void renderAllSides(int x, int y, int z, Block block, RenderBlocks renderer, TextureAtlasSprite tex) {
      renderer.renderFaceXPos(block, x - 1, y, z, tex);
      renderer.renderFaceXNeg(block, x + 1, y, z, tex);
      renderer.renderFaceZPos(block, x, y, z - 1, tex);
      renderer.renderFaceZNeg(block, x, y, z + 1, tex);
      renderer.renderFaceYPos(block, x, y - 1, z, tex);
      renderer.renderFaceYNeg(block, x, y + 1, z, tex);
   }
}
