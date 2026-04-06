package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.tiles.TileEssentiaCrystalizer;
import thaumcraft.common.tiles.TileTube;

public class BlockTubeItem extends ItemBlock {
   public BlockTubeItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      EnumActionResult ret = super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
      if (ret == EnumActionResult.SUCCESS) {
         BlockPos placed = pos.offset(facing);
         TileEntity te = world.getTileEntity(placed);
         if (te instanceof TileTube) {
            ((TileTube)te).facing = facing;
         }
         if (te instanceof TileEssentiaCrystalizer) {
            ((TileEssentiaCrystalizer)te).facing = facing.getOpposite();
         }
      }
      return ret;
   }
}
