package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;

import thaumcraft.common.blocks.BlockEssentiaReservoir;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import net.minecraft.client.renderer.GlStateManager;

public class BlockEssentiaReservoirRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      TileEntityRendererDispatcher.instance.render(new TileEssentiaReservoir(), 0.0, 0.0, 0.0, 0.0F);
      GlStateManager.enableRescaleNormal();
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockEssentiaReservoirRI;
   }
}
