package thaumcraft.client.renderers.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.client.renderers.compat.RenderBlocks;
import thaumcraft.common.tiles.TileBrainbox;

public class TileBrainboxRenderer extends TileEntitySpecialRenderer<TileBrainbox> {
   private final RenderBlocks renderBlocks = new RenderBlocks();

   private void renderBox(TileBrainbox tile, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, TextureAtlasSprite icon) {
      this.renderBlocks.offsetX = tile.getPos().getX();
      this.renderBlocks.offsetY = tile.getPos().getY();
      this.renderBlocks.offsetZ = tile.getPos().getZ();
      this.renderBlocks.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
      net.minecraft.block.Block block = tile.getBlockType();
      double x = tile.getPos().getX();
      double y = tile.getPos().getY();
      double z = tile.getPos().getZ();
      this.renderBlocks.renderFaceYNeg(block, x, y, z, icon);
      this.renderBlocks.renderFaceYPos(block, x, y, z, icon);
      this.renderBlocks.renderFaceXNeg(block, x, y, z, icon);
      this.renderBlocks.renderFaceXPos(block, x, y, z, icon);
      this.renderBlocks.renderFaceZNeg(block, x, y, z, icon);
      this.renderBlocks.renderFaceZPos(block, x, y, z, icon);
   }

   @Override
   public void render(TileBrainbox tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (tile == null || tile.getWorld() == null) {
         return;
      }

      TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("thaumcraft:blocks/brainbox");
      Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

      this.renderBox(tile, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W13, BlockRenderer.W13, icon);
      EnumFacing facing = tile.facing == null ? EnumFacing.UP : tile.facing;
      switch (facing) {
         case DOWN:
            this.renderBox(tile, BlockRenderer.W6, 0.0, BlockRenderer.W6, BlockRenderer.W10, BlockRenderer.W3, BlockRenderer.W10, icon);
            break;
         case EAST:
            this.renderBox(tile, BlockRenderer.W13, BlockRenderer.W6, BlockRenderer.W6, 1.0, BlockRenderer.W10, BlockRenderer.W10, icon);
            break;
         case WEST:
            this.renderBox(tile, 0.0, BlockRenderer.W6, BlockRenderer.W6, BlockRenderer.W3, BlockRenderer.W10, BlockRenderer.W10, icon);
            break;
         case SOUTH:
            this.renderBox(tile, BlockRenderer.W6, BlockRenderer.W6, BlockRenderer.W13, BlockRenderer.W10, BlockRenderer.W10, 1.0, icon);
            break;
         case NORTH:
            this.renderBox(tile, BlockRenderer.W6, BlockRenderer.W6, 0.0, BlockRenderer.W10, BlockRenderer.W10, BlockRenderer.W3, icon);
            break;
         case UP:
         default:
            this.renderBox(tile, BlockRenderer.W6, BlockRenderer.W13, BlockRenderer.W6, BlockRenderer.W10, 1.0, BlockRenderer.W10, icon);
            break;
      }
   }
}
