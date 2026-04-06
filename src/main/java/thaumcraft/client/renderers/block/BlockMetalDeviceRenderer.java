package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileMagicWorkbenchCharger;
import thaumcraft.common.tiles.TileVisRelay;
import net.minecraft.client.renderer.GlStateManager;

public class BlockMetalDeviceRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      if (metadata == 1) {
         GlStateManager.translate(-0.5F, 0.0F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileAlembic(), 0.0, 0.0, 0.0, 0.0F);
         GlStateManager.enableRescaleNormal();
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
      }
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockMetalDeviceRI;
   }
}
