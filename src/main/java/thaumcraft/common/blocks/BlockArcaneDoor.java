package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileOwned;

import java.util.ArrayList;
import java.util.Random;

public class BlockArcaneDoor extends BlockContainer {

   public BlockArcaneDoor() {
      super(Material.IRON);
      // setStepSound removed — sound is now data-driven in 1.12.2
      this.setResistance(999.0F);
      this.setHardness(Config.wardedStone ? -1.0F : 15.0F);
   }

   // registerBlockIcons, getIcon removed — JSON models handle textures in 1.12.2

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean getBlocksMovement(IBlockAccess world, BlockPos pos) {
      return (this.getFullMetadata(world, pos) & 4) != 0;
   }

   /**
    * Returns the AABB for this door based on its orientation/open metadata.
    * Replaces the old setBlockBounds()/setDoorRotation() mutable pattern.
    */
   private AxisAlignedBB getDoorAABB(int meta) {
      float var2 = 0.1875F;
      int var3 = meta & 3;
      boolean var4 = (meta & 4) != 0;   // open
      boolean var5 = (meta & 16) != 0;  // hinge side
      if (var3 == 0) {
         if (var4) {
            return var5 ? new AxisAlignedBB(0, 0, 1 - var2, 1, 1, 1)
                        : new AxisAlignedBB(0, 0, 0, 1, 1, var2);
         } else {
            return new AxisAlignedBB(0, 0, 0, var2, 1, 1);
         }
      } else if (var3 == 1) {
         if (var4) {
            return var5 ? new AxisAlignedBB(0, 0, 0, var2, 1, 1)
                        : new AxisAlignedBB(1 - var2, 0, 0, 1, 1, 1);
         } else {
            return new AxisAlignedBB(0, 0, 0, 1, 1, var2);
         }
      } else if (var3 == 2) {
         if (var4) {
            return var5 ? new AxisAlignedBB(0, 0, 0, 1, 1, var2)
                        : new AxisAlignedBB(0, 0, 1 - var2, 1, 1, 1);
         } else {
            return new AxisAlignedBB(1 - var2, 0, 0, 1, 1, 1);
         }
      } else {
         if (var4) {
            return var5 ? new AxisAlignedBB(1 - var2, 0, 0, 1, 1, 1)
                        : new AxisAlignedBB(0, 0, 0, var2, 1, 1);
         } else {
            return new AxisAlignedBB(0, 0, 1 - var2, 1, 1, 1);
         }
      }
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      return getDoorAABB(this.getFullMetadata(world, pos));
   }

   public int getDoorOrientation(IBlockAccess world, BlockPos pos) {
      return this.getFullMetadata(world, pos) & 3;
   }

   public boolean isDoorOpen(IBlockAccess world, BlockPos pos) {
      return (this.getFullMetadata(world, pos) & 4) != 0;
   }

   @Override
   @SuppressWarnings("deprecation")
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                   EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      if (!world.isRemote) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileOwned) {
            TileOwned owned = (TileOwned) tile;
            if (!player.getName().equals(owned.owner)
                  && !owned.accessList.contains("0" + player.getName())
                  && !owned.accessList.contains("1" + player.getName())) {
               player.sendMessage(new net.minecraft.util.text.TextComponentTranslation("The door refuses to budge."));
               world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN,
                     SoundCategory.BLOCKS, 0.66F, 1.0F);
            } else {
               int var10 = this.getFullMetadata(world, pos);
               int var11 = (var10 & 7) ^ 4;
               if ((var10 & 8) == 0) {
                  world.setBlockState(pos, this.getStateFromMeta(var11), 2);
                  { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
                  this.playDoorSound(world, pos);
               } else {
                  BlockPos below = pos.down();
                  world.setBlockState(below, this.getStateFromMeta(var11), 2);
                  { net.minecraft.block.state.IBlockState _bs = world.getBlockState(below); world.notifyBlockUpdate(below, _bs, _bs, 3); }
                  this.playDoorSound(world, pos);
               }
            }
         }
      }
      return true;
   }

   private void playDoorSound(World world, BlockPos pos) {
      SoundEvent sound = Math.random() < 0.5 ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE;
      world.playSound(null, pos, sound, SoundCategory.BLOCKS,
            1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
   }

   @SuppressWarnings("deprecation")
   public void onPoweredBlockChange(World world, BlockPos pos, boolean powered) {
      int var6 = this.getFullMetadata(world, pos);
      boolean var7 = (var6 & 4) != 0;
      if (var7 != powered) {
         int var8 = (var6 & 7) ^ 4;
         if ((var6 & 8) == 0) {
            world.setBlockState(pos, this.getStateFromMeta(var8), 2);
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         } else {
            BlockPos below = pos.down();
            world.setBlockState(below, this.getStateFromMeta(var8), 2);
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(below); world.notifyBlockUpdate(below, _bs, _bs, 3); }
         }
         world.playEvent(null, 1003, pos, 0);
      }
   }

   @Override
   @SuppressWarnings("deprecation")
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
      int var6 = this.getMetaFromState(state);
      if (block == ConfigBlocks.blockWoodenDevice) {
         ArrayList<String> users = new ArrayList<>();
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileOwned) {
            TileOwned owned = (TileOwned) tile;
            users.add(owned.owner);
            for (String u : owned.accessList) {
               users.add(u.substring(1));
            }
         }

         int open = 0;

         label98:
         for (int a = 2; a <= 5; ++a) {
            EnumFacing dir = EnumFacing.byIndex(a);
            BlockPos neighbor = pos.offset(dir);
            IBlockState neighborState = world.getBlockState(neighbor);
            Block bi = neighborState.getBlock();
            int md = bi.getMetaFromState(neighborState);
            if (bi == ConfigBlocks.blockWoodenDevice && md == 3) {
               TileEntity tileNeighbor = world.getTileEntity(neighbor);
               if (tileNeighbor instanceof TileOwned) {
                  TileOwned to = (TileOwned) tileNeighbor;
                  for (String u : users) {
                     if (to.owner.equals(u) || to.accessList.contains(u)) {
                        open = 1;
                        break label98;
                     }
                  }
               }
            } else if (bi == ConfigBlocks.blockWoodenDevice && md == 2) {
               TileEntity tileNeighbor = world.getTileEntity(neighbor);
               if (tileNeighbor instanceof TileOwned) {
                  TileOwned to = (TileOwned) tileNeighbor;
                  for (String u : users) {
                     if (to.owner.equals(u) || to.accessList.contains(u)) {
                        open = -1;
                        break;
                     }
                  }
               }
            }
         }

         if (open != 0) {
            this.onPoweredBlockChange(world, pos, open == 1);
         }
      } else if ((var6 & 8) == 0) {
         boolean destroyed = false;
         if (world.getBlockState(pos.up()).getBlock() != this) {
            world.setBlockToAir(pos);
            destroyed = true;
         }
         if (destroyed && !world.isRemote) {
            this.dropBlockAsItem(world, pos, state, 0);
         }
      } else {
         if (world.getBlockState(pos.down()).getBlock() != this) {
            world.setBlockToAir(pos);
         }
         if (block != Blocks.AIR && block != this) {
            IBlockState stateBelow = world.getBlockState(pos.down());
            this.neighborChanged(stateBelow, world, pos.down(), block, fromPos);
         }
      }
   }

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      int meta = this.getMetaFromState(state);
      return Config.wardedStone ? Item.getItemById(0) : ((meta & 8) != 0 ? Item.getItemById(0) : ConfigItems.itemArcaneDoor);
   }

   @Override
   @SuppressWarnings("deprecation")
   public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
      return super.collisionRayTrace(state, world, pos, start, end);
   }

   @Override
   public boolean canPlaceBlockAt(World world, BlockPos pos) {
      return pos.getY() < 255
            && world.isSideSolid(pos.down(), EnumFacing.UP)
            && super.canPlaceBlockAt(world, pos)
            && super.canPlaceBlockAt(world, pos.up());
   }

   public int getFullMetadata(IBlockAccess world, BlockPos pos) {
      IBlockState state = world.getBlockState(pos);
      int var5 = state.getBlock().getMetaFromState(state);
      boolean isTop = (var5 & 8) != 0;
      int var7, var8;
      if (isTop) {
         IBlockState below = world.getBlockState(pos.down());
         var7 = below.getBlock().getMetaFromState(below);
         var8 = var5;
      } else {
         var7 = var5;
         IBlockState above = world.getBlockState(pos.up());
         var8 = above.getBlock().getMetaFromState(above);
      }
      boolean hingeRight = (var8 & 1) != 0;
      return var7 & 7 | (isTop ? 8 : 0) | (hingeRight ? 16 : 0);
   }

   @Override
   public TileEntity createNewTileEntity(World world, int meta) {
      return new TileOwned();
   }

   @Override
   public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
      return true;
   }

   @Override
   public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
      return false;
   }

   @Override
   public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
      // arcane door is immune to explosions
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
