package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileBellows;
import thaumcraft.common.tiles.TileCentrifuge;
import thaumcraft.common.tiles.TileEssentiaCrystalizer;
import thaumcraft.common.tiles.TileTube;
import thaumcraft.common.tiles.TileTubeValve;
import net.minecraft.client.renderer.GlStateManager;

public class BlockTubeRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      try {
         if (metadata == 0 || metadata == 1 || metadata == 3 || metadata == 5 || metadata == 6) {
            renderer.setRenderBounds(W7, 0.0D, W7, W9, 1.0D, W9);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[metadata == 5 ? 6 : 0], true);
         }

         if (metadata == 6) {
            renderer.setRenderBounds(W7 - 0.001D, 0.1D, W7 - 0.001D, W9 + 0.001D, 0.9D, W9 + 0.001D);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[7], true);
         }

         if (metadata == 0 || metadata == 5 || metadata == 6) {
            renderer.setRenderBounds(W7 - 0.03125D, W7 - 0.03125D, W7 - 0.03125D, W9 + 0.03125D, W9 + 0.03125D, W9 + 0.03125D);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[metadata == 5 ? 6 : 2], true);
         }

         if (metadata == 1) {
            renderer.setRenderBounds(W6, W6, W6, W10, W10, W10);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[1], true);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            TileTubeValve tc = new TileTubeValve();
            tc.facing = EnumFacing.EAST;
            TileEntityRendererDispatcher.instance.render(tc, 0.0, 0.0, 0.0, 0.0F);
            GlStateManager.enableRescaleNormal();
         } else if (metadata == 2) {
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            TileEntityRendererDispatcher.instance.render(new TileCentrifuge(), 0.0, 0.0, 0.0, 0.0F);
            GlStateManager.enableRescaleNormal();
         } else if (metadata == 3) {
            renderer.setRenderBounds(W6 - 0.03125D, W6 - 0.03125D, W6 - 0.03125D, W10 + 0.03125D, W10 + 0.03125D, W10 + 0.03125D);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[3], false);
            renderer.setRenderBounds(W6 - 0.03125D, W6 - 0.03125D, W6 - 0.03125D, W10 + 0.03125D, W10 + 0.03125D, W10 + 0.03125D);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[4], false);
         } else if (metadata == 4) {
            renderer.setRenderBounds(W4, W4, W4, W12, W12, W12);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[5], false);
            renderer.setRenderBounds(W7, 0.0D, W7, W9, 1.0D, W9);
            drawFaces(renderer, block, ((thaumcraft.common.blocks.BlockTube)block).icon[5], false);
         } else if (metadata == 7) {
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            TileEntityRendererDispatcher.instance.render(new TileEssentiaCrystalizer(), 0.0, 0.0, 0.0, 0.0F);
            GlStateManager.enableRescaleNormal();
         }
      } catch (Exception ignored) {
      }
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockTubeRI;
   }

   public TileEntity getConnectableTile(IBlockAccess world, int x, int y, int z, EnumFacing face) {
      BlockPos neighbor = new BlockPos(x + face.getXOffset(), y + face.getYOffset(), z + face.getZOffset());
      TileEntity te = world.getTileEntity(neighbor);
      if (te instanceof IEssentiaTransport && ((IEssentiaTransport)te).isConnectable(face.getOpposite())) {
         return te;
      } else {
         return te instanceof TileBellows && ((TileBellows)te).orientation == face.getOpposite().ordinal() ? te : null;
      }
   }
}
