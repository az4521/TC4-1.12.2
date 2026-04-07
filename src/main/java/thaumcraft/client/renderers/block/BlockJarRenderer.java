package thaumcraft.client.renderers.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;

import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileJarBrain;
import net.minecraft.client.renderer.GlStateManager;

public class BlockJarRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

      TextureAtlasSprite top = sprite("jar_top");
      TextureAtlasSprite side = sprite("jar_side");
      if (metadata == 3) {
         top = sprite("jar_top_void");
         side = sprite("jar_side_void");
      }

      renderer.setRenderBounds(W3, 0.0D, W3, W13, W12, W13);
      drawFaces(renderer, block, sprite("jar_bottom"), top, side, side, side, side, true);
      renderer.setRenderBounds(W5, W12, W5, W11, W14, W11);
      drawFaces(renderer, block, sprite("jar_bottom"), top, side, side, side, side, true);
      GlStateManager.popMatrix();

      if (metadata == 1) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         TileEntityRendererDispatcher.instance.render(new TileJarBrain(), 0.0, 0.0, 0.0, 0.0F);
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
      return ConfigBlocks.blockJarRI;
   }

   private TextureAtlasSprite sprite(String path) {
      return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("thaumcraft:blocks/" + path);
   }
}
