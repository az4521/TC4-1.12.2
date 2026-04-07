package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;

import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileCrystal;
import thaumcraft.common.tiles.TileEldritchCrystal;
import net.minecraft.client.renderer.GlStateManager;

public class BlockCrystalRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      if (metadata <= 6) {
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileCrystal tc = new TileCrystal() {
            @Override
            public int getBlockMetadata() {
               return metadata;
            }
         };
         tc.setWorld(net.minecraft.client.Minecraft.getMinecraft().world);
         tc.setPos(net.minecraft.util.math.BlockPos.ORIGIN);
         TileEntityRendererDispatcher.instance.render(tc, 0.0, 0.0, 0.0, 0.0F);
         GlStateManager.enableRescaleNormal();
      }

      if (metadata == 7) {
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileEldritchCrystal(), 0.0, 0.0, 0.0, 0.0F);
         GlStateManager.enableRescaleNormal();
      }

   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ConfigBlocks.blockCrystalRI;
   }
}
