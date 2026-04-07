package thaumcraft.client.renderers.block;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import thaumcraft.client.renderers.compat.RenderBlocks;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileArcaneBore;
import thaumcraft.common.tiles.TileArcaneBoreBase;
import thaumcraft.common.tiles.TileArcanePressurePlate;
import thaumcraft.common.tiles.TileBanner;
import thaumcraft.common.tiles.TileBellows;
import thaumcraft.common.tiles.TileSensor;

public class BlockWoodenDeviceRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   private TextureAtlasSprite sprite(String name) {
      return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("thaumcraft:blocks/" + name);
   }

   private void renderBoxFaces(RenderBlocks renderblocks, Block block, int x, int y, int z,
                               double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                               TextureAtlasSprite bottom, TextureAtlasSprite top,
                               TextureAtlasSprite north, TextureAtlasSprite south,
                               TextureAtlasSprite west, TextureAtlasSprite east) {
      renderblocks.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
      renderblocks.renderFaceYNeg(block, x, y, z, bottom);
      renderblocks.renderFaceYPos(block, x, y, z, top);
      renderblocks.renderFaceXNeg(block, x, y, z, west);
      renderblocks.renderFaceXPos(block, x, y, z, east);
      renderblocks.renderFaceZNeg(block, x, y, z, north);
      renderblocks.renderFaceZPos(block, x, y, z, south);
   }

   @Override
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderblocks) {
      if (metadata == 0) {
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileBellows(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 1) {
         TextureAtlasSprite earSide = sprite("arcaneearsideon");
         TextureAtlasSprite earTop = sprite("arcaneeartopon");
         TextureAtlasSprite earBottom = sprite("arcaneearbottom");
         TextureAtlasSprite bellSide = sprite("arcaneearbellside");
         TextureAtlasSprite bellTop = sprite("arcaneearbelltop");
         renderblocks.setRenderBounds(0.0, 0.0, 0.0, 1.0, W3, 1.0);
         drawFaces(renderblocks, block, earBottom, earTop, earSide, earSide, earSide, earSide, true);
         renderblocks.setRenderBounds(W4, W3, W4, W12, 1.0, W12);
         drawFaces(renderblocks, block, earBottom, earTop, earSide, earSide, earSide, earSide, true);
         renderblocks.setRenderBounds(W4, 0.5, W1, W12, 1.0, W3);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
         renderblocks.setRenderBounds(W5, 0.5, W3, W11, W15, W4);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
         renderblocks.setRenderBounds(W1, 0.5, W4, W3, 1.0, W12);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
         renderblocks.setRenderBounds(W3, 0.5, W5, W4, W15, W11);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
         renderblocks.setRenderBounds(W4, 0.5, W13, W12, 1.0, W15);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
         renderblocks.setRenderBounds(W5, 0.5, W12, W11, W15, W13);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
         renderblocks.setRenderBounds(W13, 0.5, W4, W15, 1.0, W12);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
         renderblocks.setRenderBounds(W12, 0.5, W5, W13, W15, W11);
         drawFaces(renderblocks, block, bellTop, bellTop, bellSide, bellSide, bellSide, bellSide, true);
      } else if (metadata == 2) {
         GlStateManager.translate(0.0F, 0.6F, 0.0F);
         GlStateManager.scale(1.3F, 1.3F, 1.3F);
         float inset = 0.0625F;
         renderblocks.setRenderBounds(inset, 0.0, inset, 1.0 - inset, 0.125, 1.0 - inset);
         drawFaces(renderblocks, block, sprite("applate1"), true);
      } else if (metadata == 4) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileArcaneBoreBase(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 5) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.75F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileArcaneBore(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 6) {
         renderblocks.setRenderBounds(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
         drawFaces(renderblocks, block, sprite("planks_greatwood"), true);
      } else if (metadata == 7) {
         renderblocks.setRenderBounds(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
         drawFaces(renderblocks, block, sprite("planks_silverwood"), true);
      } else if (metadata == 8) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -1.0F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileBanner(), 0.0, 0.0, 0.0, 0.0F);
      }
      GlStateManager.enableRescaleNormal();
   }

   @Override
   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderblocks) {
      BlockPos pos = new BlockPos(x, y, z);
      int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (md == 1) {
         TextureAtlasSprite earSide = sprite("arcaneearsideon");
         TextureAtlasSprite earTop = sprite("arcaneeartopon");
         TextureAtlasSprite earBottom = sprite("arcaneearbottom");
         TextureAtlasSprite bellSide = sprite("arcaneearbellside");
         TextureAtlasSprite bellTop = sprite("arcaneearbelltop");
         renderBoxFaces(renderblocks, block, x, y, z, 0.0, 0.0, 0.0, 1.0, W3, 1.0,
               earBottom, earTop, earSide, earSide, earSide, earSide);
         renderBoxFaces(renderblocks, block, x, y, z, W4, W3, W4, W12, 1.0, W12,
               earBottom, earTop, earSide, earSide, earSide, earSide);
         renderBoxFaces(renderblocks, block, x, y, z, W4, 0.5, W1, W12, 1.0, W3,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         renderBoxFaces(renderblocks, block, x, y, z, W5, 0.5, W3, W11, W15, W4,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         renderBoxFaces(renderblocks, block, x, y, z, W1, 0.5, W4, W3, 1.0, W12,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         renderBoxFaces(renderblocks, block, x, y, z, W3, 0.5, W5, W4, W15, W11,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         renderBoxFaces(renderblocks, block, x, y, z, W4, 0.5, W13, W12, 1.0, W15,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         renderBoxFaces(renderblocks, block, x, y, z, W5, 0.5, W12, W11, W15, W13,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         renderBoxFaces(renderblocks, block, x, y, z, W13, 0.5, W4, W15, 1.0, W12,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         renderBoxFaces(renderblocks, block, x, y, z, W12, 0.5, W5, W13, W15, W11,
               bellTop, bellTop, bellSide, bellSide, bellSide, bellSide);
         return true;
      } else if (md == 2 || md == 3) {
         int setting = 0;
         if (world.getTileEntity(pos) instanceof TileArcanePressurePlate) {
            setting = ((TileArcanePressurePlate) world.getTileEntity(pos)).setting;
         }
         String texture = setting == 1 ? "applate2" : (setting == 2 ? "applate3" : "applate1");
         float inset = 0.0625F;
         renderblocks.overrideBlockTexture = sprite(texture);
         renderblocks.setRenderBounds(inset, 0.0, inset, 1.0 - inset, md == 3 ? 0.03125 : 0.0625, 1.0 - inset);
         renderblocks.renderStandardBlock(block, x, y, z);
         renderblocks.overrideBlockTexture = null;
         return true;
      } else if (md == 6 || md == 7) {
         renderblocks.overrideBlockTexture = sprite(md == 6 ? "planks_greatwood" : "planks_silverwood");
         renderblocks.setRenderBounds(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
         renderblocks.renderStandardBlock(block, x, y, z);
         renderblocks.overrideBlockTexture = null;
         return true;
      }
      return false;
   }

   @Override
   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   @Override
   public int getRenderId() {
      return ConfigBlocks.blockWoodenDeviceRI;
   }
}
