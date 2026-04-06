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
         if (metadata == 1) {
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            TileTubeValve tc = new TileTubeValve();
            tc.facing = EnumFacing.EAST;
            TileEntityRendererDispatcher.instance.render(tc, 0.0, 0.0, 0.0, 0.0F);
            GlStateManager.enableRescaleNormal();
         } else if (metadata == 2) {
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            TileEntityRendererDispatcher.instance.render(new TileCentrifuge(), 0.0, 0.0, 0.0, 0.0F);
            GlStateManager.enableRescaleNormal();
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
