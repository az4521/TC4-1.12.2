package thaumcraft.common.entities;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class EntityItemGrate extends EntityItem {
   public EntityItemGrate(World par1World) {
      super(par1World);
   }

   public EntityItemGrate(World par1World, double par2, double par4, double par6, ItemStack par8ItemStack) {
      super(par1World, par2, par4, par6, par8ItemStack);
   }

   protected boolean pushOutOfBlocks(double x, double y, double z) {
      int i = MathHelper.floor(x);
      int j = MathHelper.floor(y);
      int k = MathHelper.floor(z);
      net.minecraft.block.state.IBlockState bstate = this.world.getBlockState(new BlockPos(i, j, k));
      int meta = bstate.getBlock().getMetaFromState(bstate);
      return bstate.getBlock() == ConfigBlocks.blockMetalDevice && (meta == 5 || meta == 6) || super.pushOutOfBlocks(x, y, z);
   }
}
