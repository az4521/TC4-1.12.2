package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.EntityFallingTaint;
import thaumcraft.common.entities.monster.EntityTaintSporeSwarmer;
import thaumcraft.common.lib.CustomSoundType;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

import java.util.List;
import java.util.Random;

public class BlockTaint extends Block {
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


   public BlockTaint() {
      super(Config.taintMaterial);
      this.setHardness(2.0F);
      this.setResistance(10.0F);
      this.setSoundType(new CustomSoundType("gore", 0.5F, 0.8F));
      this.setTickRandomly(true);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      for (int var4 = 0; var4 <= 2; ++var4) {
         par3List.add(new ItemStack(this, 1, var4));
      }
   }

   @Override
   public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
      if (!world.isRemote) {
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();
         int md = state.getBlock().getMetaFromState(state);
         if (md == 2) {
            return;
         }

         BlockTaintFibres.taintBiomeSpread(world, x, y, z, random, this);
         if (md == 0) {
            if (this.tryToFall(world, x, y, z, x, y, z)) {
               return;
            }

            if (world.isAirBlock(new BlockPos(x, y + 1, z))) {
               boolean doIt = true;
               EnumFacing dir = EnumFacing.byIndex(2 + random.nextInt(4));

               for (int a = 0; a < 4; ++a) {
                  if (!world.isAirBlock(new BlockPos(x + dir.getXOffset(), y - a, z + dir.getZOffset()))) {
                     doIt = false;
                     break;
                  }

                  if (world.getBlockState(new BlockPos(x, y - a, z)).getBlock() != this) {
                     doIt = false;
                     break;
                  }
               }

               if (doIt && this.tryToFall(world, x, y, z, x + dir.getXOffset(), y, z + dir.getZOffset())) {
                  return;
               }
            }
         }

         int xx = x + random.nextInt(3) - 1;
         int yy = y + random.nextInt(5) - 3;
         int zz = z + random.nextInt(3) - 1;
         if (net.minecraft.world.biome.Biome.getIdForBiome(world.getBiome(new BlockPos(xx, 0, zz))) == Config.biomeTaintID) {
            if (BlockTaintFibres.spreadFibres(world, xx, yy, zz)) {
            }

            if (md == 0) {
               if (Config.spawnTaintSpore && world.isAirBlock(new BlockPos(x, y + 1, z)) && random.nextInt(200) == 0) {
                  List<Entity> targets = world.getEntitiesWithinAABB(EntityTaintSporeSwarmer.class, new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(16.0F, 16.0F, 16.0F));
                  if (targets.isEmpty()) {
                     world.setBlockToAir(new BlockPos(x, y, z));
                     EntityTaintSporeSwarmer spore = new EntityTaintSporeSwarmer(world);
                     spore.setLocationAndAngles((float)x + 0.5F, y, (float)z + 0.5F, 0.0F, 0.0F);
                     world.spawnEntity(spore);
                     world.playSound(null, spore.posX, spore.posY, spore.posZ,
                           new SoundEvent(new ResourceLocation("thaumcraft", "roots")),
                           SoundCategory.NEUTRAL, 0.1F, 0.9F + world.rand.nextFloat() * 0.2F);
                  }
               } else {
                  boolean doIt = world.getBlockState(new BlockPos(x, y + 1, z)).getBlock() == this;
                  if (doIt) {
                     for (int a = 2; a < 6; ++a) {
                        EnumFacing dir = EnumFacing.byIndex(a);
                        if (world.getBlockState(new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset())).getBlock() != this) {
                           doIt = false;
                           break;
                        }
                     }
                  }

                  if (doIt) {
                     world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockFluxGoo.getStateFromMeta(((BlockFluxGoo)ConfigBlocks.blockFluxGoo).getQuanta()), 3);
                  }
               }
            }
         } else if (md == 0 && random.nextInt(20) == 0) {
            world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockFluxGoo.getStateFromMeta(((BlockFluxGoo)ConfigBlocks.blockFluxGoo).getQuanta()), 3);
         } else if (md == 1 && random.nextInt(10) == 0) {
            world.setBlockState(new BlockPos(x, y, z), Blocks.DIRT.getDefaultState(), 3);
         }
      }
   }

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      int md = state.getBlock().getMetaFromState(state);
      return md == 1 ? Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState(), rand, fortune) : (md == 2 ? Items.ROTTEN_FLESH : Items.AIR);
   }

   @Override
   public int damageDropped(IBlockState state) {
      return 0;
   }

   @Override
   public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
      return new ItemStack(this, 1, state.getBlock().getMetaFromState(state));
   }

   @Override
   public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
      int metadata = state.getBlock().getMetaFromState(state);
      return metadata == 2 || super.canSilkHarvest(world, pos, state, player);
   }

   @Override
   public int quantityDropped(Random random) {
      return 1;
   }

   @Override
   public int quantityDroppedWithBonus(int fortune, Random random) {
      // For flesh (md==2), original code returned 9; check state not available here,
      // so we override getDrops in breakBlock or handle in getItemDropped.
      // quantityDropped is called per state, so just return 1 as base.
      return 1;
   }

   @Override
   public java.util.List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      java.util.List<ItemStack> drops = new java.util.ArrayList<>();
      int md = state.getBlock().getMetaFromState(state);
      Random rand = new Random();
      if (md == 2) {
         drops.add(new ItemStack(Items.ROTTEN_FLESH, 9));
      } else {
         Item dropped = getItemDropped(state, rand, fortune);
         if (dropped != Items.AIR) {
            drops.add(new ItemStack(dropped, 1, damageDropped(state)));
         }
      }
      return drops;
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 0) {
         return 1.75F;
      } else if (md == 1) {
         return 1.5F;
      } else {
         return md == 2 ? 0.2F : super.getBlockHardness(state, world, pos);
      }
   }

   public static boolean canFallBelow(World par0World, int par1, int par2, int par3) {
      BlockPos checkPos = new BlockPos(par1, par2, par3);
      IBlockState checkState = par0World.getBlockState(checkPos);
      Block l = checkState.getBlock();

      for (int xx = -1; xx <= 1; ++xx) {
         for (int zz = -1; zz <= 1; ++zz) {
            for (int yy = -1; yy <= 1; ++yy) {
               if (Utils.isWoodLog(par0World, par1 + xx, par2 + yy, par3 + zz)) {
                  return false;
               }
            }
         }
      }

      if (l.isAir(checkState, par0World, checkPos)) {
         return true;
      } else if (l == ConfigBlocks.blockFluxGoo && checkState.getBlock().getMetaFromState(checkState) >= 4) {
         return false;
      } else if (l != Blocks.FIRE && l != ConfigBlocks.blockTaintFibres) {
         if (l.isReplaceable(par0World, checkPos)) {
            return true;
         } else {
            return l.getMaterial(checkState) == Material.WATER || l.getMaterial(checkState) == Material.LAVA;
         }
      } else {
         return true;
      }
   }

   private boolean tryToFall(World par1World, int x, int y, int z, int x2, int y2, int z2) {
      IBlockState curState = par1World.getBlockState(new BlockPos(x, y, z));
      int md = curState.getBlock().getMetaFromState(curState);
      if (canFallBelow(par1World, x2, y2 - 1, z2) && y2 >= 0) {
         byte b0 = 32;
         BlockPos min = new BlockPos(x2 - b0, y2 - b0, z2 - b0);
         BlockPos max = new BlockPos(x2 + b0, y2 + b0, z2 + b0);
         if (par1World.isAreaLoaded(min, max)) {
            if (!par1World.isRemote) {
               EntityFallingTaint entityfalling = new EntityFallingTaint(par1World, (float)x2 + 0.5F, (float)y2 + 0.5F, (float)z2 + 0.5F, this, md, x, y, z);
               this.onStartFalling(entityfalling);
               par1World.spawnEntity(entityfalling);
               return true;
            }
         } else {
            par1World.setBlockToAir(new BlockPos(x, y, z));

            while (canFallBelow(par1World, x2, y2 - 1, z2) && y2 > 0) {
               --y2;
            }

            if (y2 > 0) {
               par1World.setBlockState(new BlockPos(x2, y2, z2), this.getStateFromMeta(md), 3);
            }
         }
      }

      return false;
   }

   @Override
   public void onEntityWalk(World world, BlockPos pos, Entity entity) {
      int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (md != 2) {
         if (!world.isRemote && entity instanceof EntityLivingBase && !((EntityLivingBase)entity).isEntityUndead()) {
            if (entity instanceof EntityPlayer && world.rand.nextInt(100) == 0) {
               ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.getPotionById(Config.potionTaintPoisonID), 80, 0, false, false));
            } else if (!(entity instanceof EntityPlayer) && world.rand.nextInt(20) == 0) {
               ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.getPotionById(Config.potionTaintPoisonID), 160, 0, false, false));
            }
         }

         super.onEntityWalk(world, pos, entity);
      }
   }

   protected void onStartFalling(EntityFallingTaint entityfalling) {
   }

   public void onFinishFalling(World par1World, int par2, int par3, int par4, int par5) {
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 0 && world.isAirBlock(pos.down()) && random.nextInt(10) == 0) {
         Thaumcraft.proxy.dropletFX(world, (float)pos.getX() + 0.1F + world.rand.nextFloat() * 0.8F, (float)pos.getY(), (float)pos.getZ() + 0.1F + world.rand.nextFloat() * 0.8F, 0.3F, 0.1F, 0.8F);
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
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return true;
   }
}
