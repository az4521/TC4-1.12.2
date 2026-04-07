package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.*;

public class BlockMetalDeviceItem extends ItemBlock {

   public BlockMetalDeviceItem(Block par1) {
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
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.getHeldItem(hand);
      int dmg = stack.getItemDamage();
      if (dmg != 0 && dmg != 1 && dmg != 2 && dmg != 3 && dmg != 5 && dmg != 6 && dmg != 7 && dmg != 8 && dmg != 9 && dmg != 13 && dmg != 14) {
         Block bi = world.getBlockState(pos).getBlock();
         IBlockState posState = world.getBlockState(pos);
         int md = posState.getBlock().getMetaFromState(posState);

         if (dmg == 12) {
            return bi == ConfigBlocks.blockMetalDevice && (md == 10 || md == 11)
                  ? super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ)
                  : EnumActionResult.FAIL;
         } else {
            BlockPos placePos = pos;
            if (bi == ConfigBlocks.blockMetalDevice && md == 0) {
               int sideIdx = facing.getIndex();
               if (sideIdx == 0 || sideIdx == 1) {
                  return EnumActionResult.FAIL;
               }
               if (sideIdx == 2) placePos = pos.north();
               if (sideIdx == 3) placePos = pos.south();
               if (sideIdx == 4) placePos = pos.west();
               if (sideIdx == 5) placePos = pos.east();
            }

            if (stack.isEmpty()) {
               return EnumActionResult.FAIL;
            } else if (!player.canPlayerEdit(placePos, facing, stack)) {
               return EnumActionResult.FAIL;
            } else if (placePos.getY() == 255 && this.block.getDefaultState().getMaterial().isSolid()) {
               return EnumActionResult.FAIL;
            } else {
               Block var11 = world.getBlockState(placePos).getBlock();
               if (world.isAirBlock(placePos) || var11.isReplaceable(world, placePos) || var11 == Blocks.VINE || var11 == Blocks.TALLGRASS || var11 == Blocks.DEADBUSH || var11 == Blocks.SNOW_LAYER) {
                  for (int a = 2; a < 6; ++a) {
                     EnumFacing dir = EnumFacing.byIndex(a);
                     BlockPos adjPos = placePos.add(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
                     IBlockState adjState = world.getBlockState(adjPos);
                     Block bid = adjState.getBlock();
                     int meta = adjState.getBlock().getMetaFromState(adjState);
                     if (bid == ConfigBlocks.blockMetalDevice && meta == 0) {
                        IBlockState newState = this.block.getStateFromMeta(dmg);
                        if (placeBlockAt(stack, player, world, placePos, facing, hitX, hitY, hitZ, newState)) {
                           world.playSound(null, placePos,
                                 this.block.getSoundType(newState, world, placePos, player).getPlaceSound(),
                                 SoundCategory.BLOCKS,
                                 (this.block.getSoundType(newState, world, placePos, player).getVolume() + 1.0F) / 2.0F,
                                 this.block.getSoundType(newState, world, placePos, player).getPitch() * 0.8F);
                           stack.shrink(1);
                           world.setBlockState(placePos,
                                 ConfigBlocks.blockMetalDevice.getStateFromMeta(dir.getOpposite().ordinal() - 1), 3);
                           return EnumActionResult.SUCCESS;
                        }
                     }
                  }
               }
               return EnumActionResult.FAIL;
            }
         }
      } else {
         return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
      }
   }

   @Override
   public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
      int metadata = stack.getItemDamage();
      newState = this.block.getStateFromMeta(metadata);
      boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
      if (metadata == 7) {
         TileArcaneLamp tile = (TileArcaneLamp) world.getTileEntity(pos);
         if (tile instanceof TileArcaneLamp) {
            tile.facing = side.getOpposite();
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      } else if (metadata == 8) {
         TileArcaneLampGrowth tile = (TileArcaneLampGrowth) world.getTileEntity(pos);
         if (tile instanceof TileArcaneLampGrowth) {
            tile.facing = side.getOpposite();
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      } else if (metadata == 12) {
         TileBrainbox tile = (TileBrainbox) world.getTileEntity(pos);
         if (tile instanceof TileBrainbox) {
            tile.facing = side.getOpposite();
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      } else if (metadata == 13) {
         TileArcaneLampFertility tile = (TileArcaneLampFertility) world.getTileEntity(pos);
         if (tile instanceof TileArcaneLampFertility) {
            tile.facing = side.getOpposite();
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      } else if (metadata == 14) {
         TileVisRelay tile = (TileVisRelay) world.getTileEntity(pos);
         if (tile instanceof TileVisRelay) {
            tile.orientation = (short) side.getIndex();
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      }
      return ret;
   }
}
