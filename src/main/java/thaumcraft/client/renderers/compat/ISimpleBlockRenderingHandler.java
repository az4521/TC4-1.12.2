package thaumcraft.client.renderers.compat;

import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.world.IBlockAccess;

/**
 * ISimpleBlockRenderingHandler was removed in MC 1.8.
 * Blocks now use JSON blockstate/model files or IBakedModel.
 * This shim exists only to allow compilation; implementing classes
 * need to be ported to the 1.12.2 model system.
 */
@Deprecated
public interface ISimpleBlockRenderingHandler {
    void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer);
    boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer);
    boolean shouldRender3DInInventory(int modelId);
    int getRenderId();
}
