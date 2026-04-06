package thaumcraft.common.lib.world;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class WorldGenSilverwoodTrees extends WorldGenAbstractTree {
   private final int minTreeHeight;
   private final int randomTreeHeight;
   boolean worldgen;

   public WorldGenSilverwoodTrees(boolean doBlockNotify, int minTreeHeight, int randomTreeHeight) {
      super(doBlockNotify);
      this.worldgen = !doBlockNotify;
      this.minTreeHeight = minTreeHeight;
      this.randomTreeHeight = randomTreeHeight;
   }

   private boolean doGenerate(World world, Random random, int x, int y, int z) {
      int height = random.nextInt(this.randomTreeHeight) + this.minTreeHeight;
      boolean flag = true;
      if (y >= 1 && y + height + 1 <= 256) {
         for(int i1 = y; i1 <= y + 1 + height; ++i1) {
            byte spread = 1;
            if (i1 == y) {
               spread = 0;
            }

            if (i1 >= y + 1 + height - 2) {
               spread = 3;
            }

            for(int j1 = x - spread; j1 <= x + spread && flag; ++j1) {
               for(int k1 = z - spread; k1 <= z + spread && flag; ++k1) {
                  if (i1 >= 0 && i1 < 256) {
                     BlockPos checkPos = new BlockPos(j1, i1, k1);
                     Block block = world.getBlockState(checkPos).getBlock();
                     if (!world.isAirBlock(checkPos)
                             && !block.isLeaves(world.getBlockState(checkPos), world, checkPos)
                             && !world.getBlockState(checkPos).getMaterial().isReplaceable()
                             && i1 > y) {
                        flag = false;
                     }
                  } else {
                     flag = false;
                  }
               }
            }
         }

         if (!flag) {
            return false;
         } else {
            BlockPos soilPos = new BlockPos(x, y - 1, z);
            Block block1 = world.getBlockState(soilPos).getBlock();
            boolean isSoil = block1.canSustainPlant(world.getBlockState(soilPos), world, soilPos, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);
            if (isSoil && y < 256 - height - 1) {
               block1.onPlantGrow(world.getBlockState(soilPos), world, soilPos, new BlockPos(x, y, z));
               int start = y + height - 5;
               int end = y + height + 3 + random.nextInt(3);

               for(int k2 = start; k2 <= end; ++k2) {
                  int cty = MathHelper.clamp(k2, y + height - 3, y + height);

                  for(int xx = x - 5; xx <= x + 5; ++xx) {
                     for(int zz = z - 5; zz <= z + 5; ++zz) {
                        double d3 = xx - x;
                        double d4 = k2 - cty;
                        double d5 = zz - z;
                        double dist = d3 * d3 + d4 * d4 + d5 * d5;
                        BlockPos leafPos = new BlockPos(xx, k2, zz);
                        if (dist < (double)(10 + random.nextInt(8)) && world.getBlockState(leafPos).getBlock().canBeReplacedByLeaves(world.getBlockState(leafPos), world, leafPos)) {
                           this.setBlockAndNotifyAdequately(world, leafPos, ConfigBlocks.blockMagicalLeaves.getStateFromMeta(1));
                        }
                     }
                  }
               }

               int chance = (int)((double)height * (double)1.5F);
               boolean lastblock = false;

               int var29;
               for(var29 = 0; var29 < height; ++var29) {
                  BlockPos trunkPos = new BlockPos(x, y + var29, z);
                  Block block2 = world.getBlockState(trunkPos).getBlock();
                  if (world.isAirBlock(trunkPos)
                          || block2.isLeaves(world.getBlockState(trunkPos), world, trunkPos)
                          || world.getBlockState(trunkPos).getMaterial().isReplaceable()) {
                     if (var29 > 0 && !lastblock && random.nextInt(chance) == 0) {
                        this.setBlockAndNotifyAdequately(world, trunkPos, ConfigBlocks.blockMagicalLog.getStateFromMeta(2));
                        ThaumcraftWorldGenerator.createRandomNodeAt(world, x, y + var29, z, random, true, false, false);
                        chance += height;
                        lastblock = true;
                     } else {
                        this.setBlockAndNotifyAdequately(world, trunkPos, ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
                        lastblock = false;
                     }

                     this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + var29, z), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
                     this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + var29, z), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
                     this.setBlockAndNotifyAdequately(world, new BlockPos(x, y + var29, z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
                     this.setBlockAndNotifyAdequately(world, new BlockPos(x, y + var29, z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
                  }
               }

               this.setBlockAndNotifyAdequately(world, new BlockPos(x, y + var29, z), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y, z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y, z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y, z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y, z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               if (random.nextInt(3) != 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + 1, z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               if (random.nextInt(3) != 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + 1, z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               if (random.nextInt(3) != 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + 1, z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               if (random.nextInt(3) != 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + 1, z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               this.setBlockAndNotifyAdequately(world, new BlockPos(x - 2, y, z), ConfigBlocks.blockMagicalLog.getStateFromMeta(5));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x + 2, y, z), ConfigBlocks.blockMagicalLog.getStateFromMeta(5));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x, y, z - 2), ConfigBlocks.blockMagicalLog.getStateFromMeta(9));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x, y, z + 2), ConfigBlocks.blockMagicalLog.getStateFromMeta(9));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x - 2, y - 1, z), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x + 2, y - 1, z), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x, y - 1, z - 2), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x, y - 1, z + 2), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 4), z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 4), z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 4), z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 4), z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               if (random.nextInt(3) == 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 5), z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               if (random.nextInt(3) == 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 5), z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               if (random.nextInt(3) == 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x - 1, y + (height - 5), z + 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               if (random.nextInt(3) == 0) {
                  this.setBlockAndNotifyAdequately(world, new BlockPos(x + 1, y + (height - 5), z - 1), ConfigBlocks.blockMagicalLog.getStateFromMeta(1));
               }

               this.setBlockAndNotifyAdequately(world, new BlockPos(x - 2, y + (height - 4), z), ConfigBlocks.blockMagicalLog.getStateFromMeta(5));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x + 2, y + (height - 4), z), ConfigBlocks.blockMagicalLog.getStateFromMeta(5));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x, y + (height - 4), z - 2), ConfigBlocks.blockMagicalLog.getStateFromMeta(9));
               this.setBlockAndNotifyAdequately(world, new BlockPos(x, y + (height - 4), z + 2), ConfigBlocks.blockMagicalLog.getStateFromMeta(9));
               if (this.worldgen) {
                  WorldGenCustomFlowers flowers = new WorldGenCustomFlowers(ConfigBlocks.blockCustomPlant, 2);
                  flowers.generate(world, random, new BlockPos(x, y, z));
               }

               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean generate(World world, Random random, BlockPos pos) {
      return this.doGenerate(world, random, pos.getX(), pos.getY(), pos.getZ());
   }
}
