package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.IBlockAccess;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.blocks.BlockMetalDevice;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileArcaneLampFertility;
import thaumcraft.common.tiles.TileArcaneLampGrowth;
import thaumcraft.common.tiles.TileBrainbox;
import thaumcraft.common.tiles.TileMagicWorkbenchCharger;
import thaumcraft.common.tiles.TileVisRelay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;

public class BlockMetalDeviceRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      if (metadata == 0) {
         BlockMetalDevice bmd = (BlockMetalDevice) block;
         renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         drawFaces(renderer, block, bmd.icon[2], bmd.icon[4], bmd.icon[3], bmd.icon[3], bmd.icon[3], bmd.icon[3], true);
      } else if (metadata == 1) {
         GlStateManager.translate(-0.5F, 0.0F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileAlembic(), 0.0, 0.0, 0.0, 0.0F);
         GlStateManager.enableRescaleNormal();
      } else if (metadata == 5 || metadata == 6) {
         BlockMetalDevice bmd = (BlockMetalDevice) block;
         GlStateManager.translate(0.0F, -0.3F, 0.0F);
         renderer.setRenderBounds(0.0F, 0.8125F, 0.0F, 1.0F, 1.0F, 1.0F);
         drawFaces(renderer, block, bmd.icon[8], false);
         drawFaces(renderer, block, bmd.icon[9], false);
      } else if (metadata == 14) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.5F, 1.5F, 1.5F);
         GlStateManager.translate(-0.5F, -0.25F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileVisRelay(), 0.0, 0.0, 0.0, 0.0F);
         GlStateManager.enableRescaleNormal();
         GlStateManager.popMatrix();
      } else if (metadata == 2) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileMagicWorkbenchCharger(), 0.0, 0.0, 0.0, 0.0F);
         GlStateManager.enableRescaleNormal();
         GlStateManager.popMatrix();
      } else if (metadata == 7 || metadata == 8 || metadata == 13) {
         BlockMetalDevice bmd = (BlockMetalDevice) block;
         TextureAtlasSprite top;
         TextureAtlasSprite side;
         if (metadata == 8) {
            top = bmd.icon[13];
            side = bmd.icon[12];
         } else if (metadata == 13) {
            top = bmd.icon[19];
            side = bmd.icon[18];
         } else {
            top = bmd.icon[11];
            side = bmd.icon[10];
         }

         renderer.setRenderBounds(W4, W2, W4, W12, W14, W12);
         drawFaces(renderer, block, top, top, side, side, side, side, true);
      } else if (metadata == 12) {
         renderer.setRenderBounds(W3, W3, W3, W13, W13, W13);
         drawFaces(renderer, block, ((BlockMetalDevice) block).icon[17], true);
      } else if (metadata == 9) {
         renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         drawFaces(renderer, block, ((BlockMetalDevice) block).icon[16], true);
      } else if (metadata == 3) {
         renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         drawFaces(renderer, block, ((BlockMetalDevice) block).icon[22], true);
      }
   }

   private void renderBox(RenderBlocks renderer, Block block, int x, int y, int z,
                          TextureAtlasSprite down, TextureAtlasSprite up,
                          TextureAtlasSprite north, TextureAtlasSprite south,
                          TextureAtlasSprite west, TextureAtlasSprite east) {
      renderer.renderFaceYNeg(block, x, y, z, down);
      renderer.renderFaceYPos(block, x, y, z, up);
      renderer.renderFaceXNeg(block, x, y, z, west);
      renderer.renderFaceXPos(block, x, y, z, east);
      renderer.renderFaceZNeg(block, x, y, z, north);
      renderer.renderFaceZPos(block, x, y, z, south);
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
      int metadata = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (metadata == 7 || metadata == 8 || metadata == 13) {
         BlockMetalDevice bmd = (BlockMetalDevice) block;
         TextureAtlasSprite top;
         TextureAtlasSprite side;
         if (metadata == 8) {
            TileEntity te = world.getTileEntity(pos);
            boolean on = te instanceof TileArcaneLampGrowth && ((TileArcaneLampGrowth) te).charges > 0;
            top = bmd.icon[on ? 13 : 15];
            side = bmd.icon[on ? 12 : 14];
         } else if (metadata == 13) {
            TileEntity te = world.getTileEntity(pos);
            boolean on = te instanceof TileArcaneLampFertility && ((TileArcaneLampFertility) te).charges > 0;
            top = bmd.icon[on ? 19 : 21];
            side = bmd.icon[on ? 18 : 20];
         } else {
            top = bmd.icon[11];
            side = bmd.icon[10];
         }

         renderer.setRenderBounds(W4, W2, W4, W12, W14, W12);
         renderBox(renderer, block, x, y, z, top, top, side, side, side, side);
         renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         return true;
      } else if (metadata == 9) {
         TextureAtlasSprite alchemy = ((BlockMetalDevice) block).icon[16];
         renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         renderBox(renderer, block, x, y, z, alchemy, alchemy, alchemy, alchemy, alchemy, alchemy);
         return true;
      } else if (metadata == 3) {
         TextureAtlasSprite alchemyAdv = ((BlockMetalDevice) block).icon[22];
         renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         renderBox(renderer, block, x, y, z, alchemyAdv, alchemyAdv, alchemyAdv, alchemyAdv, alchemyAdv, alchemyAdv);
         return true;
      } else if (metadata == 12) {
         TextureAtlasSprite brain = ((BlockMetalDevice) block).icon[17];
         renderer.setRenderBounds(W3, W3, W3, W13, W13, W13);
         renderBox(renderer, block, x, y, z, brain, brain, brain, brain, brain, brain);
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileBrainbox) {
            switch (((TileBrainbox) te).facing) {
               case UP:
                  renderer.setRenderBounds(W6, W13, W6, W10, 1.0F, W10);
                  break;
               case DOWN:
                  renderer.setRenderBounds(W6, 0.0F, W6, W10, W3, W10);
                  break;
               case EAST:
                  renderer.setRenderBounds(W13, W6, W6, 1.0F, W10, W10);
                  break;
               case WEST:
                  renderer.setRenderBounds(0.0F, W6, W6, W3, W10, W10);
                  break;
               case SOUTH:
                  renderer.setRenderBounds(W6, W6, W13, W10, W10, 1.0F);
                  break;
               case NORTH:
               default:
                  renderer.setRenderBounds(W6, W6, 0.0F, W10, W10, W3);
                  break;
            }
            renderBox(renderer, block, x, y, z, brain, brain, brain, brain, brain, brain);
         }
         renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         return true;
      }
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockMetalDeviceRI;
   }
}
