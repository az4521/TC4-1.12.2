package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileFluxScrubber;
import thaumcraft.common.tiles.TileFocalManipulator;
import thaumcraft.common.tiles.TileInfusionMatrix;
import thaumcraft.common.tiles.TileNodeConverter;
import thaumcraft.common.tiles.TileNodeStabilizer;
import net.minecraft.client.renderer.GlStateManager;

public class BlockStoneDeviceRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      if (metadata == 2) {
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileInfusionMatrix(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 13) {
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileFocalManipulator(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 14) {
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileFluxScrubber(), 0.0, 0.0, 0.0, 0.0F);
      } else if (metadata == 11) {
         try {
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            TileEntityRendererDispatcher.instance.render(new TileNodeConverter(), 0.0, 0.0, 0.0, 0.0F);
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else if (metadata == 9 || metadata == 10) {
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileNodeStabilizer(metadata), 0.0, 0.0, 0.0, 0.0F);
      }
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockStoneDeviceRI;
   }
}
