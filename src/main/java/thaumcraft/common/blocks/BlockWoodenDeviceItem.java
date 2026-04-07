package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TileArcaneBore;
import thaumcraft.common.tiles.TileArcaneBoreBase;
import thaumcraft.common.tiles.TileBanner;
import thaumcraft.common.tiles.TileBellows;

public class BlockWoodenDeviceItem extends ItemBlock {
   public BlockWoodenDeviceItem(Block par1) {
      super(par1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int par1) {
      return par1;
   }

   public String getTranslationKey(ItemStack stack) {
      return stack.hasTagCompound() && stack.getTagCompound().hasKey("color")
         ? super.getTranslationKey() + "." + stack.getItemDamage() + "." + stack.getTagCompound().getByte("color")
         : super.getTranslationKey() + "." + stack.getItemDamage();
   }

   @Override
   public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
      int metadata = stack.getMetadata();
      boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
      if (!ret) {
         return false;
      }

      if (metadata == 0) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileBellows) {
            EnumFacing dir = side.getOpposite();
            ((TileBellows) tile).orientation = (byte) dir.ordinal();
            BlockPos neighbor = pos.offset(dir);
            Block bi = world.getBlockState(neighbor).getBlock();
            if (bi == Blocks.FURNACE || bi == Blocks.LIT_FURNACE) {
               ((TileBellows) tile).onVanillaFurnace = true;
            }
            tile.markDirty();
            { IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      }

      if (metadata == 4) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileArcaneBoreBase) {
            int var6 = MathHelper.floor((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            switch (var6) {
               case 0: ((TileArcaneBoreBase)tile).orientation = EnumFacing.NORTH; break;
               case 1: ((TileArcaneBoreBase)tile).orientation = EnumFacing.EAST;  break;
               case 2: ((TileArcaneBoreBase)tile).orientation = EnumFacing.SOUTH; break;
               case 3: ((TileArcaneBoreBase)tile).orientation = EnumFacing.WEST;  break;
            }
            tile.markDirty();
            { IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      }

      if (metadata == 5) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileArcaneBore) {
            ((TileArcaneBore)tile).baseOrientation = side;
            int var6 = MathHelper.floor((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            EnumFacing[] dirs = { EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST };
            ((TileArcaneBore)tile).orientation = dirs[var6];
            { IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
            tile.markDirty();
         }
      }

      if (metadata == 8) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileBanner) {
            TileBanner banner = (TileBanner) tile;
            if (side == EnumFacing.UP || side == EnumFacing.DOWN) {
               int i = MathHelper.floor((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
               banner.setFacing((byte)i);
            } else {
               banner.setWall(true);
               int i = 0;
               if (side == EnumFacing.NORTH) i = 8;
               if (side == EnumFacing.WEST)  i = 4;
               if (side == EnumFacing.SOUTH) i = 12;
               banner.setFacing((byte)i);
            }

            if (stack.hasTagCompound()) {
               String aspectTag = stack.getTagCompound().getString("aspect");
               if (!aspectTag.isEmpty()) {
                  banner.setAspect(Aspect.getAspect(aspectTag));
               } else {
                  banner.setAspect(null);
               }
               if (stack.getTagCompound().hasKey("color")) {
                  banner.setColor(stack.getTagCompound().getByte("color"));
               }
            }

            banner.markDirty();
            { IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         }
      }

      return true;
   }
}
