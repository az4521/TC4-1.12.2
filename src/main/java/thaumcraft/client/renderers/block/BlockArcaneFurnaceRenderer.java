package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import thaumcraft.common.blocks.BlockArcaneFurnace;
import thaumcraft.common.config.ConfigBlocks;

public class BlockArcaneFurnaceRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      setBrightness(world, x, y, z, block);
      int md = block.getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
      BlockArcaneFurnace furnace = (BlockArcaneFurnace)block;
      renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

      if (md <= 9) {
         if (md == 0) {
            renderer.overrideBlockTexture = sprite("minecraft:blocks/lava_still");
            renderer.renderStandardBlock(block, x, y, z);
         } else {
            renderer.renderFaceYNeg(block, x, y, z, furnaceSprite(furnace.calculateTextureIndex(world, x, y, z, 0)));
            renderer.renderFaceYPos(block, x, y, z, furnaceSprite(furnace.calculateTextureIndex(world, x, y, z, 1)));
            renderer.renderFaceZNeg(block, x, y, z, furnaceSprite(furnace.calculateTextureIndex(world, x, y, z, 2)));
            renderer.renderFaceZPos(block, x, y, z, furnaceSprite(furnace.calculateTextureIndex(world, x, y, z, 3)));
            renderer.renderFaceXNeg(block, x, y, z, furnaceSprite(furnace.calculateTextureIndex(world, x, y, z, 4)));
            renderer.renderFaceXPos(block, x, y, z, furnaceSprite(furnace.calculateTextureIndex(world, x, y, z, 5)));
         }
      } else if (md == 10) {
         TextureAtlasSprite bars = furnaceSprite(13);
         TextureAtlasSprite core = furnaceSprite(15);
         TextureAtlasSprite fire = sprite("minecraft:blocks/fire_layer_0");
         net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
         net.minecraft.block.state.IBlockState westState = world.getBlockState(pos.west());
         net.minecraft.block.state.IBlockState eastState = world.getBlockState(pos.east());
         net.minecraft.block.state.IBlockState northState = world.getBlockState(pos.north());
         if (westState.getBlock() == block && block.getMetaFromState(westState) == 0) {
            renderer.renderFaceXPos(block, (float)x - W10, y, z, bars);
            renderer.renderFaceXPos(block, (float)x - 0.8F, y, z, core);
            renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D);
            renderer.renderFaceXPos(block, (float)x - 0.9F, y, z, fire);
         } else if (eastState.getBlock() == block && block.getMetaFromState(eastState) == 0) {
            renderer.renderFaceXNeg(block, (float)x + W10, y, z, bars);
            renderer.renderFaceXNeg(block, (float)x + 0.8F, y, z, core);
            renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D);
            renderer.renderFaceXNeg(block, (float)x + 0.9F, y, z, fire);
         } else if (northState.getBlock() == block && block.getMetaFromState(northState) == 0) {
            renderer.renderFaceZPos(block, x, y, (float)z - W10, bars);
            renderer.renderFaceZPos(block, x, y, (float)z - 0.8F, core);
            renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D);
            renderer.renderFaceZPos(block, x, y, (float)z - 0.9F, fire);
         } else {
            renderer.renderFaceZNeg(block, x, y, (float)z + W10, bars);
            renderer.renderFaceZNeg(block, x, y, (float)z + 0.8F, core);
            renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.5D, 1.0D);
            renderer.renderFaceZNeg(block, x, y, (float)z + 0.9F, fire);
         }
      }

      renderer.clearOverrideBlockTexture();
      renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
      return true;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return false;
   }

   public int getRenderId() {
      return ConfigBlocks.blockArcaneFurnaceRI;
   }

   private TextureAtlasSprite furnaceSprite(int index) {
      return sprite("thaumcraft:blocks/furnace" + index);
   }

   private TextureAtlasSprite sprite(String name) {
      return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
   }
}
