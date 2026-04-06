package thaumcraft.common.lib.world;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.world.dim.MazeHandler;
import thaumcraft.common.tiles.TileBanner;
import thaumcraft.common.tiles.TileEldritchAltar;
import net.minecraft.util.math.BlockPos;

public class WorldGenEldritchRing extends WorldGenerator {
   public int chunkX;
   public int chunkZ;
   public int width;
   public int height = 0;

   protected Block[] GetValidSpawnBlocks() {
      return new Block[]{Blocks.STONE, Blocks.SAND, Blocks.PACKED_ICE, Blocks.GRASS, Blocks.GRAVEL, Blocks.DIRT};
   }

   public boolean LocationIsValidSpawn(World world, int i, int j, int k) {
      int distanceToAir = 0;

      for(Block checkID = world.getBlockState(new BlockPos(i, j, k)).getBlock(); checkID != Blocks.AIR; checkID = world.getBlockState(new net.minecraft.util.math.BlockPos(i, j + distanceToAir, k)).getBlock()) {
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
       return false;
   }

   @Override
   public boolean generate(World world, Random rand, BlockPos pos) {
      return generate(world, rand, pos.getX(), pos.getY(), pos.getZ());
   }

   private boolean generate(World world, Random rand, int i, int j, int k) {
      if (this.LocationIsValidSpawn(world, i - 3, j, k - 3)
              && this.LocationIsValidSpawn(world, i, j, k)
              && this.LocationIsValidSpawn(world, i + 3, j, k)
              && this.LocationIsValidSpawn(world, i + 3, j, k + 3)
              && this.LocationIsValidSpawn(world, i, j, k + 3)
              && !MazeHandler.mazesInRange(this.chunkX, this.chunkZ, this.width, this.height)
      ) {
         for(int x = i - 3; x <= i + 3; ++x) {
            for(int z = k - 3; z <= k + 3; ++z) {
               if (x != i - 3 && x != i + 3 || z != k - 3 && z != k + 3) {
                  for(int q = -4; q < 5; ++q) {
                     Block bb = world.getBlockState(new net.minecraft.util.math.BlockPos(x, j + q, z)).getBlock();
                     if (q <= 0
                             || world.getBlockState(new BlockPos(x, j + q, z)).getBlock().isReplaceable(world, new BlockPos(x, j + q, z))
                             || !world.getBlockState(new BlockPos(x, j + q, z)).getMaterial().blocksMovement()
                             || bb.isFoliage(world, new BlockPos(x, j + q, z))//however,once > 0 it's always air,all tries are useless.
                     ) {
                        if (rand.nextInt(4) == 0) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + q, z), (Blocks.OBSIDIAN).getDefaultState(), 3);
                        } else {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + q, z), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(1), 3);
                        }
                     }

                     if (q > 0) {
                        world.setBlockToAir(new BlockPos(x, j + q, z));
                     }
                  }

                  if (x == i && z == k) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + 1, z), (ConfigBlocks.blockEldritch).getStateFromMeta(0), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j, z), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(1), 3);
                     int r = rand.nextInt(10);
                     TileEntity te = world.getTileEntity(new BlockPos(x, j + 1, z));
                     if (te instanceof TileEldritchAltar) {
                        TileEldritchAltar altar = (TileEldritchAltar) te;
                        switch (r) {
                           case 1:
                           case 2:
                           case 3:
                           case 4:
                              altar.setSpawner(true);
                              altar.setSpawnType((byte)0);

                              for(int a = 2; a < 6; ++a) {
                                 EnumFacing dir = EnumFacing.byIndex(a);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - dir.getXOffset() * 3, j + 1, z + dir.getZOffset() * 3), (ConfigBlocks.blockWoodenDevice).getStateFromMeta(8), 3);
                                 TileEntity probablyBanner = world.getTileEntity(new BlockPos(x - dir.getXOffset() * 3, j + 1, z + dir.getZOffset() * 3));
                                 if (probablyBanner instanceof TileBanner) {
                                    TileBanner banner = (TileBanner) probablyBanner;
                                    banner.setFacing(bannerFaceFromDirection(a));
                                 }
                              }
                           case 5:
                           default:
                              break;
                           case 6:
                           case 7:
                              altar.setSpawner(true);
                              altar.setSpawnType((byte)1);
                        }
                     }
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + 3, z), (ConfigBlocks.blockEldritch).getStateFromMeta(1), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + 4, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + 5, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + 6, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + 7, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
                  } else if (((x == i - 3 || x == i + 3) && Math.abs((z - k) % 2) == 1 || (z == k - 3 || z == k + 3) && Math.abs((x - i) % 2) == 1) && Math.abs(x - i) != Math.abs(z - k)) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j, z), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(1), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, j + 1, z), (ConfigBlocks.blockEldritch).getStateFromMeta(3), 3);
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static byte bannerFaceFromDirection(int a) {
      switch (a) {
         case 2:
            return 8;
         case 3:
            return 0;
         case 4:
            return 12;
         case 5:
            return 4;
         default:
            return 0;
      }
   }
}
