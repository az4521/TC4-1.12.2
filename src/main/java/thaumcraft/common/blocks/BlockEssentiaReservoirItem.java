package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.tiles.TileEssentiaReservoir;

public class BlockEssentiaReservoirItem extends ItemBlock {
   public BlockEssentiaReservoirItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                     EnumHand hand, EnumFacing facing,
                                     float hitX, float hitY, float hitZ) {
      EnumActionResult result = super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
      if (result == EnumActionResult.SUCCESS && !world.isRemote) {
         // The block was placed one step in the facing direction from pos
         BlockPos placed = pos.offset(facing);
         try {
            TileEssentiaReservoir ts = (TileEssentiaReservoir) world.getTileEntity(placed);
            if (ts != null) {
               ts.facing = facing.getOpposite();
               ts.markDirty();
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(placed); world.notifyBlockUpdate(placed, _bs, _bs, 3); }
            }
         } catch (Exception ignored) {
         }
      }
      return result;
   }
}
