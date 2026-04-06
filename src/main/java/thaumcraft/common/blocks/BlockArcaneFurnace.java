package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileArcaneFurnace;
import thaumcraft.common.tiles.TileArcaneFurnaceNozzle;

import java.util.List;
import java.util.Random;

public class BlockArcaneFurnace extends BlockContainer {

   public BlockArcaneFurnace() {
      super(Material.ROCK);
      this.setHardness(10.0F);
      this.setResistance(500.0F);
      this.setLightLevel(0.2F);
   }

   // registerBlockIcons, getIcon, calculateTexture, calculateLevel removed —
   // textures are handled by JSON models in 1.12.2.

   public int getLightValue(IBlockAccess world, BlockPos pos) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getBlock().getMetaFromState(state);
      return meta != 0 && meta != 10 ? (int)(this.lightValue * 15) : 13;
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      return FULL_BLOCK_AABB;
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      int meta = state.getBlock().getMetaFromState(state);
      return meta == 0
            ? new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0)
            : FULL_BLOCK_AABB;
   }

   @Override
   @SuppressWarnings("deprecation")
   public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                     AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                                     Entity entityIn, boolean isActualState) {
      int md = state.getBlock().getMetaFromState(state);
      AxisAlignedBB box;
      if (md == 10) {
         IBlockState west  = world.getBlockState(pos.west());
         IBlockState east  = world.getBlockState(pos.east());
         IBlockState north = world.getBlockState(pos.north());
         if (west.getBlock() == this && west.getBlock().getMetaFromState(west) == 0) {
            box = new AxisAlignedBB(0.0, 0.0, 0.0, 0.5, 1.0, 1.0);
         } else if (east.getBlock() == this && east.getBlock().getMetaFromState(east) == 0) {
            box = new AxisAlignedBB(0.5, 0.0, 0.0, 1.0, 1.0, 1.0);
         } else if (north.getBlock() == this && north.getBlock().getMetaFromState(north) == 0) {
            box = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.5);
         } else {
            box = new AxisAlignedBB(0.0, 0.0, 0.5, 1.0, 1.0, 1.0);
         }
      } else if (md != 0) {
         box = FULL_BLOCK_AABB;
      } else {
         box = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);
      }
      addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
   }

   @Override
   @SuppressWarnings("deprecation")
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 0) {
         if (entity.posX < (double) pos.getX() + 0.3) entity.motionX += 1.0E-4;
         if (entity.posX > (double) pos.getX() + 0.7) entity.motionX -= 1.0E-4;
         if (entity.posZ < (double) pos.getZ() + 0.3) entity.motionZ += 1.0E-4;
         if (entity.posZ > (double) pos.getZ() + 0.7) entity.motionZ -= 1.0E-4;

         if (entity instanceof EntityItem) {
            entity.motionY = 0.025;
            if (entity.onGround) {
               TileEntity te = world.getTileEntity(pos);
               if (te instanceof TileArcaneFurnace) {
                  if (((TileArcaneFurnace) te).addItemsToInventory(((EntityItem) entity).getItem())) {
                     entity.setDead();
                  }
               }
            }
         } else if (entity instanceof EntityLivingBase && !entity.isImmuneToFire()) {
            entity.attackEntityFrom(DamageSource.LAVA, 3.0F);
            entity.setFire(10);
         }
      }
   }

   @SuppressWarnings("deprecation")
   private void restoreBlocks(World world, BlockPos center) {
      for (int yy = -1; yy <= 1; ++yy) {
         for (int xx = -1; xx <= 1; ++xx) {
            for (int zz = -1; zz <= 1; ++zz) {
               BlockPos offset = center.add(xx, yy, zz);
               IBlockState bs = world.getBlockState(offset);
               if (bs.getBlock() == this) {
                  int md = bs.getBlock().getMetaFromState(bs);
                  Block replacement = Block.getBlockFromItem(this.getItemDropped(bs, new Random(), 0));
                  world.setBlockState(offset, replacement.getDefaultState(), 3);
                  world.notifyNeighborsOfStateChange(offset, replacement, true);
                  { net.minecraft.block.state.IBlockState _bs = world.getBlockState(offset); world.notifyBlockUpdate(offset, _bs, _bs, 3); }
               }
            }
         }
      }
   }

   @Override
   @SuppressWarnings("deprecation")
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 0) {
         for (int yy = -1; yy <= 1; ++yy) {
            for (int xx = -1; xx <= 1; ++xx) {
               for (int zz = -1; zz <= 1 && (yy != 1 && yy != 0 || zz != 0 || xx != 0); ++zz) {
                  BlockPos offset = pos.add(xx, yy, zz);
                  if (world.getBlockState(offset).getBlock() != this) {
                     this.restoreBlocks(world, pos);
                     world.setBlockToAir(pos);
                     world.notifyNeighborsOfStateChange(pos, this, true);
                     { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
                     return;
                  }
               }
            }
         }
      }
      super.neighborChanged(state, world, pos, block, fromPos);
   }

   // onBlockPreDestroy does not exist in 1.12.2 — logic moved to breakBlock.
   @Override
   @SuppressWarnings("deprecation")
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 0 && !world.isRemote) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileArcaneFurnace) {
            Entity blaze = EntityList.createEntityByIDFromName(
                  new ResourceLocation("blaze"), world);
            if (blaze != null) {
               blaze.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);
               ((EntityLivingBase) blaze).addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 6000, 2));
               ((EntityLivingBase) blaze).addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 12000, 0));
               world.spawnEntity(blaze);
            }
         }
         this.restoreBlocks(world, pos);
      }

      for (int yy = -1; yy <= 1; ++yy) {
         for (int xx = -1; xx <= 1; ++xx) {
            for (int zz = -1; zz <= 1; ++zz) {
               world.notifyNeighborsOfStateChange(pos.add(xx, yy, zz), this, true);
            }
         }
      }

      super.breakBlock(world, pos, state);
   }

   @Override
   @SuppressWarnings("deprecation")
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      int meta = state.getBlock().getMetaFromState(state);
      return meta == 0 ? Item.getItemById(0)
            : (meta == 10 ? Item.getItemFromBlock(Blocks.IRON_BARS)
            : (meta % 2 != 0 && meta != 5 ? Item.getItemFromBlock(Blocks.NETHER_BRICK)
            : Item.getItemFromBlock(Blocks.OBSIDIAN)));
   }

   @Override
   @SuppressWarnings("deprecation")
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return state.getBlock().getMetaFromState(state) != 0;
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   @Override
   @SuppressWarnings("deprecation")
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
      int meta = state.getBlock().getMetaFromState(state);
      BlockPos above = pos.up();
      IBlockState aboveState = world.getBlockState(above);
      if (meta == 0 && aboveState.getMaterial() == Material.AIR && !aboveState.isOpaqueCube()) {
         for (int a = 0; a < 3; ++a) {
            double x = pos.getX() + rand.nextFloat();
            double y = pos.getY() + 1.0 + rand.nextFloat() * 0.5;
            double z = pos.getZ() + rand.nextFloat();
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 0) {
         return new TileArcaneFurnace();
      } else if (meta == 2 || meta == 4 || meta == 5 || meta == 6 || meta == 8) {
         return new TileArcaneFurnaceNozzle();
      }
      return super.createTileEntity(world, state);
   }

   @Override
   public TileEntity createNewTileEntity(World world, int meta) {
      return null;
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      return true;
   }

   @Override
   @SuppressWarnings("deprecation")
   public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
      if (id == 1) {
         if (world.isRemote) {
            Thaumcraft.proxy.blockSparkle(world, pos.getX(), pos.getY(), pos.getZ(), 16736256, 5);
         }
         return true;
      }
      return super.eventReceived(state, world, pos, id, param);
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
