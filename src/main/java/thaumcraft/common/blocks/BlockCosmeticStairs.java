package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import thaumcraft.common.Thaumcraft;

public class BlockCosmeticStairs extends BlockStairs {
   Block refBlock;
   int refMeta;

   public BlockCosmeticStairs(Block block, int type) {
      super(block.getStateFromMeta(type));
      this.refBlock = block;
      this.refMeta = type;
      this.setLightOpacity(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }
}
