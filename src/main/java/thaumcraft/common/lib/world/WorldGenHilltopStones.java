package thaumcraft.common.lib.world;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.storage.loot.LootTableList;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class WorldGenHilltopStones extends WorldGenerator {
   protected Block[] GetValidSpawnBlocks() {
      return new Block[]{Blocks.STONE, Blocks.GRASS, Blocks.DIRT};
   }

   public boolean LocationIsValidSpawn(World world, int i, int j, int k) {
       if (j >= 85) {
           int distanceToAir = 0;

           for (Block checkID = world.getBlockState(new BlockPos(i, j, k)).getBlock(); checkID != Blocks.AIR; checkID = world.getBlockState(new net.minecraft.util.math.BlockPos(i, j + distanceToAir, k)).getBlock()) {
               ++distanceToAir;
           }

           if (distanceToAir <= 2) {
               j += distanceToAir - 1;
               Block blockID = world.getBlockState(new BlockPos(i, j, k)).getBlock();
               Block blockIDAbove = world.getBlockState(new net.minecraft.util.math.BlockPos(i, j + 1, k)).getBlock();
               Block blockIDBelow = world.getBlockState(new net.minecraft.util.math.BlockPos(i, j - 1, k)).getBlock();

               for (Block x : this.GetValidSpawnBlocks()) {
                   if (blockIDAbove != Blocks.AIR) {
                       return false;
                   }

                   if (blockID == x) {
                       return true;
                   }

                   if ((blockID == Blocks.SNOW_LAYER || blockID == Blocks.TALLGRASS) && blockIDBelow == x) {
                       return true;
                   }
               }

           }
       }
       return false;
   }

   @Override
   public boolean generate(World world, Random rand, BlockPos pos) {
      return generate(world, rand, pos.getX(), pos.getY(), pos.getZ());
   }

   private boolean generate(World world, Random rand, int i, int j, int k) {
      if (this.LocationIsValidSpawn(world, i - 2, j, k - 2) && this.LocationIsValidSpawn(world, i, j, k) && this.LocationIsValidSpawn(world, i + 2, j, k) && this.LocationIsValidSpawn(world, i + 2, j, k + 2) && this.LocationIsValidSpawn(world, i, j, k + 2)) {
         IBlockState replaceBlock = world.getBiome(new BlockPos(i, 0, k)).topBlock;
         boolean genVines = !world.getBiome(new BlockPos(i, 0, k)).getEnableSnow();

         for(int x = i - 3; x <= i + 3; ++x) {
            for(int z = k - 3; z <= k + 3; ++z) {
               if (x != i - 3 && x != i + 3 || z != k - 3 && z != k + 3) {
                  if (rand.nextBoolean()) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j, z), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(1), 3);
                  } else {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j, z), (Blocks.OBSIDIAN).getStateFromMeta(0), 3);
                  }

                  boolean stop = false;

                  for(int y = 1; y < 5; ++y) {
                     if (j - y >= 0) {
                        Block blockID = world.getBlockState(new net.minecraft.util.math.BlockPos(x, j - y, z)).getBlock();
                        if (replaceBlock != null && blockID == Blocks.SNOW_LAYER || blockID == Blocks.RED_FLOWER || blockID == Blocks.YELLOW_FLOWER || blockID == Blocks.TALLGRASS || blockID == Blocks.AIR) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j - y, z), replaceBlock, 3);
                        }

                        if (x == i && z == k && y == 1) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + y, z), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(1), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + y + 1, z), (Blocks.CHEST).getStateFromMeta(0), 3);
                           TileEntityChest chest = (TileEntityChest)world.getTileEntity(new BlockPos(x, j + y + 1, z));
                           if (chest != null) {
                              chest.setLootTable(LootTableList.CHESTS_SIMPLE_DUNGEON, rand.nextLong());
                           }
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + y - 1, z), (Blocks.MOB_SPAWNER).getStateFromMeta(0), 3);
                           TileEntityMobSpawner var12 = (TileEntityMobSpawner)world.getTileEntity(new BlockPos(x, j + y - 1, z));
                           if (var12 != null) {
                              var12.getSpawnerBaseLogic().setEntityId(new ResourceLocation("thaumcraft", "wisp"));
                           }
                        }

                        if (!stop && ((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1 || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1)) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + y, z), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(0), 3);
                           if (y >= 2 && rand.nextBoolean()) {
                              stop = true;
                              if (genVines) {
                                 if (rand.nextInt(3) == 0 && world.isAirBlock(new BlockPos(x - 1, j + y, z))) {
                                    this.growVines(world, x - 1, j + y, z, 8);
                                 }

                                 if (rand.nextInt(3) == 0 && world.isAirBlock(new BlockPos(x + 1, j + y, z))) {
                                    this.growVines(world, x + 1, j + y, z, 2);
                                 }

                                 if (rand.nextInt(3) == 0 && world.isAirBlock(new BlockPos(x, j + y, z - 1))) {
                                    this.growVines(world, x, j + y, z - 1, 1);
                                 }

                                 if (rand.nextInt(3) == 0 && world.isAirBlock(new BlockPos(x, j + y, z + 1))) {
                                    this.growVines(world, x, j + y, z + 1, 4);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void growVines(World par1World, int par2, int par3, int par4, int par5) {
      this.setBlockAndNotifyAdequately(par1World, new BlockPos(par2, par3, par4), Blocks.VINE.getStateFromMeta(par5));
      int var6 = 4;

      while(true) {
         --par3;
         if (!par1World.isAirBlock(new BlockPos(par2, par3, par4)) || var6 <= 0) {
            return;
         }

         this.setBlockAndNotifyAdequately(par1World, new BlockPos(par2, par3, par4), Blocks.VINE.getStateFromMeta(par5));
         --var6;
      }
   }
}
