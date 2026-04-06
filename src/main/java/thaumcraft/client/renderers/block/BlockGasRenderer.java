package thaumcraft.client.renderers.block;

import thaumcraft.client.renderers.compat.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.BlockFluidBase;
import thaumcraft.common.blocks.BlockFluxGas;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class BlockGasRenderer implements ISimpleBlockRenderingHandler {
   public static BlockGasRenderer instance = new BlockGasRenderer();
   static final float LIGHT_Y_NEG = 0.5F;
   static final float LIGHT_Y_POS = 1.0F;
   static final float LIGHT_XZ_NEG = 0.8F;
   static final float LIGHT_XZ_POS = 0.6F;
   static final double RENDER_OFFSET = 0.001F;

   public float getFluidHeightAverage(float[] flow) {
      float total = 0.0F;
      int count = 0;
      float end = 0.0F;

       for (float v : flow) {
           if (v >= 0.875F && end != 1.0F) {
               end = v;
           }

           if (v >= 0.0F) {
               total += v;
               ++count;
           }
       }

      if (end == 0.0F) {
         end = total / (float)count;
      }

      return end;
   }

   public float getFluidHeightForRender(IBlockAccess world, int x, int y, int z, BlockFluxGas block) {
      return 0.875F;
   }

   public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      return false;
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return false;
   }

   public int getRenderId() {
      return ConfigBlocks.blockFluxGasRI;
   }
}
