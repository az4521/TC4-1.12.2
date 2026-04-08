package thaumcraft.client.renderers.tile;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import thaumcraft.client.renderers.block.BlockEldritchRenderer;
import thaumcraft.client.renderers.compat.BlockRendererDispatcherTESR;
import thaumcraft.common.tiles.TileEldritchLock;

public class TileEldritchLockCombinedRenderer extends TileEntitySpecialRenderer<TileEldritchLock> {
   private final BlockRendererDispatcherTESR<TileEldritchLock> blockRenderer =
         new BlockRendererDispatcherTESR<>(new BlockEldritchRenderer());
   private final TileEldritchLockRenderer lockRenderer = new TileEldritchLockRenderer();

   @Override
   public void render(TileEldritchLock te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      this.blockRenderer.render(te, x, y, z, partialTicks, destroyStage, alpha);
      this.lockRenderer.render(te, x, y, z, partialTicks, destroyStage, alpha);
   }
}
