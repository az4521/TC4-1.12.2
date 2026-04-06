package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileArcaneBore;
import thaumcraft.common.tiles.TileArcaneBoreBase;
import thaumcraft.common.tiles.TileBanner;
import thaumcraft.common.tiles.TileBellows;
import net.minecraft.client.renderer.GlStateManager;

public class BlockWoodenDeviceRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderblocks) {
      if (metadata == 0) {
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileBellows(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 4) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileArcaneBoreBase(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 5) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.75F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileArcaneBore(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 8) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -1.0F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileBanner(), 0.0, 0.0, 0.0, 0.0F);
      }
      GlStateManager.enableRescaleNormal();
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderblocks) {
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockWoodenDeviceRI;
   }
}
