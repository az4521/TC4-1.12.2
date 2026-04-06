package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileOwned;

public class ItemArcaneDoor extends Item {

   public ItemArcaneDoor() {
      this.maxStackSize = 1;
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
         EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      if (facing != EnumFacing.UP) {
         return EnumActionResult.FAIL;
      }

      ItemStack stack = player.getHeldItem(hand);
      BlockPos placePos = pos.up();
      Block door = ConfigBlocks.blockArcaneDoor;

      if (!player.canPlayerEdit(placePos, facing, stack)
            || !player.canPlayerEdit(placePos.up(), facing, stack)) {
         return EnumActionResult.FAIL;
      }
      if (!door.canPlaceBlockAt(world, placePos)) {
         return EnumActionResult.FAIL;
      }

      int facing4 = MathHelper.floor((double)((player.rotationYaw + 180.0F) * 4.0F / 360.0F) - 0.5) & 3;
      placeDoorBlock(world, placePos.getX(), placePos.getY(), placePos.getZ(), facing4, door, player);
      stack.shrink(1);
      return EnumActionResult.SUCCESS;
   }

   public static void placeDoorBlock(World world, int x, int y, int z, int par4, Block par5Block, EntityPlayer player) {
      byte var6 = 0;
      byte var7 = 0;
      if (par4 == 0) {
         var7 = 1;
      }
      if (par4 == 1) {
         var6 = -1;
      }
      if (par4 == 2) {
         var7 = -1;
      }
      if (par4 == 3) {
         var6 = 1;
      }

      BlockPos pos      = new BlockPos(x, y, z);
      BlockPos posLeft  = new BlockPos(x - var6, y, z - var7);
      BlockPos posRight = new BlockPos(x + var6, y, z + var7);

      int var8 = (world.getBlockState(posLeft).isNormalCube() ? 1 : 0)
               + (world.getBlockState(posLeft.up()).isNormalCube() ? 1 : 0);
      int var9 = (world.getBlockState(posRight).isNormalCube() ? 1 : 0)
               + (world.getBlockState(posRight.up()).isNormalCube() ? 1 : 0);

      boolean var10 = world.getBlockState(posLeft).getBlock() == par5Block
                   || world.getBlockState(posLeft.up()).getBlock() == par5Block;
      boolean var11 = world.getBlockState(posRight).getBlock() == par5Block
                   || world.getBlockState(posRight.up()).getBlock() == par5Block;
      boolean hingeRight = false;
      if (var10 && !var11) {
         hingeRight = true;
      } else if (var9 > var8) {
         hingeRight = true;
      }

      IBlockState lowerState = par5Block.getStateFromMeta(par4);
      IBlockState upperState = par5Block.getStateFromMeta(8 | (hingeRight ? 1 : 0));

      world.setBlockState(pos, lowerState, 2);
      TileEntity tad = world.getTileEntity(pos);
      if (tad instanceof TileOwned) {
         ((TileOwned) tad).owner = player.getName();
      }

      world.setBlockState(pos.up(), upperState, 2);
      TileEntity tad2 = world.getTileEntity(pos.up());
      if (tad2 instanceof TileOwned) {
         ((TileOwned) tad2).owner = player.getName();
      }

      world.notifyNeighborsOfStateChange(pos, par5Block, false);
      world.notifyNeighborsOfStateChange(pos.up(), par5Block, false);

      world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS,
            1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
   }
}
