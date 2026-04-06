package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.EntityTaintSpore;
import thaumcraft.common.lib.CustomSoundType;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

import java.util.List;
import java.util.Random;

public class BlockTaintFibres extends Block {
   public static final net.minecraft.block.properties.PropertyInteger META =
         net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, META);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(META, meta);
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(META);
   }


   public BlockTaintFibres() {
      super(Config.taintMaterial);
      this.setHardness(1.0F);
      this.setResistance(5.0F);
      this.setSoundType(new CustomSoundType("gore", 0.5F, 0.8F));
      this.setTickRandomly(true);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      for (int var4 = 0; var4 <= 3; ++var4) {
         par3List.add(new ItemStack(this, 1, var4));
      }
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 2) {
         return 8;
      } else {
         return md == 4 ? 10 : super.getLightValue(state, world, pos);
      }
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (isOnlyAdjacentToTaint(world, pos.getX(), pos.getY(), pos.getZ())) {
         world.setBlockToAir(pos);
      }

      super.neighborChanged(state, world, pos, blockIn, fromPos);
   }

   @Override
   public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
      if (!world.isRemote) {
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();
         int md = state.getBlock().getMetaFromState(state);
         taintBiomeSpread(world, x, y, z, random, this);
         if ((md == 0 && isOnlyAdjacentToTaint(world, x, y, z)) || net.minecraft.world.biome.Biome.getIdForBiome(world.getBiome(new BlockPos(x, 0, z))) != Config.biomeTaintID) {
            world.setBlockToAir(pos);
            return;
         }

         int xx = x + random.nextInt(3) - 1;
         int yy = y + random.nextInt(5) - 3;
         int zz = z + random.nextInt(3) - 1;
         if (net.minecraft.world.biome.Biome.getIdForBiome(world.getBiome(new BlockPos(xx, 0, zz))) == Config.biomeTaintID) {
            Block bi = world.getBlockState(new BlockPos(xx, yy, zz)).getBlock();
            if (!spreadFibres(world, xx, yy, zz)) {
               int adjacentTaint = getAdjacentTaint(world, xx, yy, zz);
               IBlockState targetState = world.getBlockState(new BlockPos(xx, yy, zz));
               Material bm = targetState.getMaterial();
               if (adjacentTaint >= 2 && (Utils.isWoodLog(world, xx, yy, zz) || bm == Material.GOURD || bm == Material.CACTUS)) {
                  world.setBlockState(new BlockPos(xx, yy, zz), ConfigBlocks.blockTaint.getStateFromMeta(0), 3);
                  world.addBlockEvent(new BlockPos(xx, yy, zz), ConfigBlocks.blockTaint, 1, 0);
               }

               if (adjacentTaint >= 3 && bi != Blocks.AIR && (bm == Material.SAND || bm == Material.GROUND || bm == Material.GRASS || bm == Material.CLAY)) {
                  world.setBlockState(new BlockPos(xx, yy, zz), ConfigBlocks.blockTaint.getStateFromMeta(1), 3);
                  world.addBlockEvent(new BlockPos(xx, yy, zz), ConfigBlocks.blockTaint, 1, 0);
               }

               if (md == 3 && Config.spawnTaintSpore && random.nextInt(10) == 0 && world.isAirBlock(pos.up())) {
                  world.setBlockState(pos, this.getStateFromMeta(4), 3);
                  EntityTaintSpore spore = new EntityTaintSpore(world);
                  spore.setLocationAndAngles((float)x + 0.5F, y + 1, (float)z + 0.5F, 0.0F, 0.0F);
                  world.spawnEntity(spore);
               } else if (md == 4) {
                  List<Entity> targets = world.getEntitiesWithinAABB(EntityTaintSpore.class, new AxisAlignedBB(x, y + 1, z, x + 1, y + 2, z + 1));
                  if (targets.isEmpty()) {
                     world.setBlockState(pos, this.getStateFromMeta(3), 3);
                  }
               }
            }
         }
      }
   }

   public static boolean spreadFibres(World world, int x, int y, int z) {
      BlockPos pos = new BlockPos(x, y, z);
      IBlockState state = world.getBlockState(pos);
      Block bi = state.getBlock();
      if (BlockUtils.isAdjacentToSolidBlock(world, x, y, z)
            && !isOnlyAdjacentToTaint(world, x, y, z)
            && !state.getMaterial().isLiquid()
            && (world.isAirBlock(pos)
               || bi.isReplaceable(world, pos)
               || bi instanceof BlockFlower
               || bi.isLeaves(state, world, pos))) {
         if (world.rand.nextInt(10) == 0 && world.isAirBlock(pos.up()) && world.isSideSolid(pos.down(), EnumFacing.UP)) {
            if (world.rand.nextInt(10) < 9) {
               world.setBlockState(pos, ConfigBlocks.blockTaintFibres.getStateFromMeta(1), 3);
            } else if (world.rand.nextInt(12) < 10) {
               world.setBlockState(pos, ConfigBlocks.blockTaintFibres.getStateFromMeta(2), 3);
            } else {
               world.setBlockState(pos, ConfigBlocks.blockTaintFibres.getStateFromMeta(3), 3);
            }
         } else {
            world.setBlockState(pos, ConfigBlocks.blockTaintFibres.getStateFromMeta(0), 3);
         }

         world.addBlockEvent(pos, ConfigBlocks.blockTaintFibres, 1, 0);
         return true;
      } else {
         return false;
      }
   }

   public static void taintBiomeSpread(World world, int x, int y, int z, Random rand, Block block) {
      if (Config.taintSpreadRate > 0) {
         int xx = rand.nextInt(3) - 1;
         int zz = rand.nextInt(3) - 1;
         if (net.minecraft.world.biome.Biome.getIdForBiome(world.getBiome(new BlockPos(x + xx, 0, z + zz))) != Config.biomeTaintID
               && rand.nextInt(Config.taintSpreadRate * 5) == 0
               && getAdjacentTaint(world, x, y, z) >= 2) {
            Utils.setBiomeAt(world, x + xx, z + zz, ThaumcraftWorldGenerator.biomeTaint);
            world.addBlockEvent(new BlockPos(x, y, z), block, 1, 0);
         }
      }
   }

   public static int getAdjacentTaint(IBlockAccess world, int x, int y, int z) {
      int count = 0;

      for (int a = 0; a < 6; ++a) {
         EnumFacing d = EnumFacing.byIndex(a);
         int xx = x + d.getXOffset();
         int yy = y + d.getYOffset();
         int zz = z + d.getZOffset();
         Block bi = world.getBlockState(new BlockPos(xx, yy, zz)).getBlock();
         if (bi == ConfigBlocks.blockTaint || bi == ConfigBlocks.blockTaintFibres) {
            ++count;
         }
      }

      return count;
   }

   public static boolean isOnlyAdjacentToTaint(World world, int x, int y, int z) {
      for (int a = 0; a < 6; ++a) {
         EnumFacing d = EnumFacing.byIndex(a);
         BlockPos adjPos = new BlockPos(x + d.getXOffset(), y + d.getYOffset(), z + d.getZOffset());
         if (!world.isAirBlock(adjPos) && world.getBlockState(adjPos).getMaterial() != Config.taintMaterial) {
            return false;
         }
      }

      return true;
   }

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemById(0);
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      if (!world.isRemote && entity instanceof EntityLivingBase && !((EntityLivingBase)entity).isEntityUndead()) {
         if (entity instanceof EntityPlayer && world.rand.nextInt(1000) == 0) {
            ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.getPotionById(Config.potionTaintPoisonID), 80, 0, false, false));
         } else if (!(entity instanceof EntityPlayer) && world.rand.nextInt(500) == 0) {
            ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.getPotionById(Config.potionTaintPoisonID), 160, 0, false, false));
         }
      }
   }

   @Override
   public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int cd) {
      if (id == 1) {
         if (world.isRemote) {
            world.playSound(null, pos,
                  new SoundEvent(new ResourceLocation("thaumcraft", "roots")),
                  SoundCategory.BLOCKS, 0.1F, 0.9F + world.rand.nextFloat() * 0.2F);
         }

         return true;
      } else {
         return super.eventReceived(state, world, pos, id, cd);
      }
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      return NULL_AABB;
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 0) {
         float f = 0.0625F;
         try {
            for (int a = 0; a < 6; ++a) {
               EnumFacing side = EnumFacing.byIndex(a);
               BlockPos adjPos = pos.add(side.getXOffset(), side.getYOffset(), side.getZOffset());
               if (world.isSideSolid(adjPos, side.getOpposite(), false)) {
                  switch (a) {
                     case 0: return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, f, 1.0);
                     case 1: return new AxisAlignedBB(0.0, 1.0 - f, 0.0, 1.0, 1.0, 1.0);
                     case 2: return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, f);
                     case 3: return new AxisAlignedBB(0.0, 0.0, 1.0 - f, 1.0, 1.0, 1.0);
                     case 4: return new AxisAlignedBB(0.0, 0.0, 0.0, f, 1.0, 1.0);
                     case 5: return new AxisAlignedBB(1.0 - f, 0.0, 0.0, 1.0, 1.0, 1.0);
                  }
               }
            }
         } catch (Throwable ignored) {
         }
         return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, f, 1.0);
      } else {
         return new AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.8, 0.8);
      }
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return false;
   }

   @Override
   public boolean canPlaceBlockAt(World par1World, BlockPos pos) {
      boolean biome = net.minecraft.world.biome.Biome.getIdForBiome(par1World.getBiome(new BlockPos(pos.getX(), 0, pos.getZ()))) == Config.biomeTaintID;
      return biome && super.canPlaceBlockAt(par1World, pos);
   }
}
