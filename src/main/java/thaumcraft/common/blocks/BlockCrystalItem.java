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
import thaumcraft.common.tiles.TileCrystal;

public class BlockCrystalItem extends ItemBlock {
   public BlockCrystalItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   @Override
   public int getMetadata(int par1) {
      return par1;
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                     EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.getHeldItem(hand);
      int metadata = this.getMetadata(stack.getMetadata());
      EnumActionResult result = super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
      if (result == EnumActionResult.SUCCESS && metadata <= 7) {
         // The block was placed on the face of the clicked block, so the actual placed pos is:
         BlockPos placedPos = pos.offset(facing);
         try {
            TileCrystal ts = (TileCrystal) world.getTileEntity(placedPos);
            if (ts != null) {
               ts.orientation = (short) facing.getIndex();
            }
         } catch (Exception ignored) {
         }
      }
      return result;
   }
}
