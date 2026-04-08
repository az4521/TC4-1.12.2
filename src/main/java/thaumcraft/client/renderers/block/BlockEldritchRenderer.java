package thaumcraft.client.renderers.block;

import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import thaumcraft.client.renderers.compat.RenderBlocks;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileEldritchLock;

public class BlockEldritchRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      if (metadata == 4 || metadata == 5 || metadata == 6) {
         renderer.setRenderBounds(W2, W2, W2, W14, W14, W14);
         drawFaces(renderer, block, Minecraft.getMinecraft().getBlockRendererDispatcher()
               .getBlockModelShapes().getTexture(block.getStateFromMeta(metadata)), false);
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
      } else if (metadata == 7) {
         TextureAtlasSprite doorway = Minecraft.getMinecraft().getTextureMapBlocks()
               .getAtlasSprite("thaumcraft:blocks/deco_3");
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         drawFaces(renderer, block, doorway, false);
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
      } else if (metadata == 8) {
         TextureAtlasSprite front = Minecraft.getMinecraft().getTextureMapBlocks()
               .getAtlasSprite("thaumcraft:blocks/deco_2");
         TextureAtlasSprite side = Minecraft.getMinecraft().getTextureMapBlocks()
               .getAtlasSprite("thaumcraft:blocks/deco_3");
         drawFaces(renderer, block, side, side, side, side, front, side, false);
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
      }
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      int metadata = block.getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
      if (metadata == 4 || metadata == 5 || metadata == 6) {
         renderer.field_152631_f = true;
         setBrightness(world, x, y, z, block);
         net.minecraft.client.renderer.texture.TextureAtlasSprite texture =
               Minecraft.getMinecraft().getBlockRendererDispatcher()
                     .getBlockModelShapes().getTexture(block.getStateFromMeta(metadata));
         float s1 = 0.0F;
         float s2 = 0.0F;
         float s3 = 0.0F;
         float s4 = 1.0F;
         float s5 = 1.0F;
         float s6 = 1.0F;
         BlockPos pos = new BlockPos(x, y, z);
         BlockPos east = pos.east();
         BlockPos west = pos.west();
         BlockPos south = pos.south();
         BlockPos north = pos.north();
         BlockPos up = pos.up();
         BlockPos down = pos.down();

         if (!world.getBlockState(east).isSideSolid(world, east, EnumFacing.WEST)) s4 -= W2;
         if (!world.getBlockState(west).isSideSolid(world, west, EnumFacing.EAST)) s1 += W2;
         if (!world.getBlockState(south).isSideSolid(world, south, EnumFacing.NORTH)) s6 -= W2;
         if (!world.getBlockState(north).isSideSolid(world, north, EnumFacing.SOUTH)) s3 += W2;
         if (!world.getBlockState(up).isSideSolid(world, up, EnumFacing.DOWN)) s5 -= W2;
         if (!world.getBlockState(down).isSideSolid(world, down, EnumFacing.UP)) s2 += W2;

         renderer.setRenderBounds(s1, s2, s3, s4, s5, s6);
         renderAllSides(world, x, y, z, block, renderer, texture, false);
         renderer.field_152631_f = false;
         renderer.clearOverrideBlockTexture();
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         return true;
      }

      if (metadata == 7) {
         renderer.overrideBlockTexture = Minecraft.getMinecraft().getTextureMapBlocks()
               .getAtlasSprite("thaumcraft:blocks/deco_3");
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         renderer.renderStandardBlock(block, x, y, z);
         renderer.clearOverrideBlockTexture();
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         return true;
      }

      if (metadata == 8) {
         TextureAtlasSprite front = Minecraft.getMinecraft().getTextureMapBlocks()
               .getAtlasSprite("thaumcraft:blocks/deco_2");
         TextureAtlasSprite side = Minecraft.getMinecraft().getTextureMapBlocks()
               .getAtlasSprite("thaumcraft:blocks/deco_3");
         TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
         EnumFacing facing = te instanceof TileEldritchLock ? EnumFacing.byIndex(((TileEldritchLock) te).getFacing()) : EnumFacing.NORTH;

         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         renderer.renderFaceYNeg(block, x, y, z, side);
         renderer.renderFaceYPos(block, x, y, z, side);
         renderer.renderFaceXNeg(block, x, y, z, facing == EnumFacing.WEST ? front : side);
         renderer.renderFaceXPos(block, x, y, z, facing == EnumFacing.EAST ? front : side);
         renderer.renderFaceZNeg(block, x, y, z, facing == EnumFacing.NORTH ? front : side);
         renderer.renderFaceZPos(block, x, y, z, facing == EnumFacing.SOUTH ? front : side);
         renderer.clearOverrideBlockTexture();
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         return true;
      }

      if (metadata == 9 || metadata == 10) {
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         renderer.renderStandardBlock(block, x, y, z);
         renderer.clearOverrideBlockTexture();
         renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
         return true;
      }

      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockEldritchRI;
   }
}
