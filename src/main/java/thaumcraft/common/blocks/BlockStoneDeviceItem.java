package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import thaumcraft.common.tiles.TileFluxScrubber;
import net.minecraft.util.math.BlockPos;

public class BlockStoneDeviceItem extends ItemBlock {
   public BlockStoneDeviceItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   @Override
   public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
      int metadata = stack.getItemDamage();
      if (metadata == 0 && player != null) {
         EnumFacing facing = player.getHorizontalFacing().getOpposite();
         newState = this.block.getStateFromMeta(metadata).withProperty(BlockStoneDevice.FACING, facing);
      }

      boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
      if (metadata == 14) {
         TileFluxScrubber tile = (TileFluxScrubber)world.getTileEntity(pos);
         if (tile instanceof TileFluxScrubber) {
            tile.facing = side.getOpposite();
            tile.markDirty();
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      }

      return ret;
   }
}
