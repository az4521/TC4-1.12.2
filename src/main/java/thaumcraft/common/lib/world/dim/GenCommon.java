package thaumcraft.common.lib.world.dim;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.tiles.TileCrystal;
import thaumcraft.common.tiles.TileEldritchCrabSpawner;

public class GenCommon {
   static ArrayList<BlockPos> decoCommon = new ArrayList<>();
   static ArrayList<BlockPos> crabSpawner = new ArrayList<>();
   static ArrayList<BlockPos> decoUrn = new ArrayList<>();
   static final int BEDROCK = 1;
   static final int BEDROCK_REPL = 99;
   static final int STONE = 2;
   static final int VOID = 8;
   static final int AIR_REPL = 9;
   static final int STAIR_DIRECTIONAL = 10;
   static final int STAIR_DIRECTIONAL_INV = 11;
   static final int SLAB = 12;
   static final int DOOR_BLOCK = 15;
   static final int DOOR_LOCK = 16;
   static final int VOID_DOOR = 17;
   static final int ROCK = 18;
   static final int STONE_NOSPAWN = 19;
   static final int STONE_TRAPPED = 20;
   static final int CRUST = 21;
   static final int[][] PAT_CONNECT = new int[][]{
           {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
           {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
           {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
           {1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1},
           {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
           {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
           {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
           {1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1},
           {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
           {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
           {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
   };

   static void placeBlock(World world, int i, int j, int k, int l, Cell cell) {
      placeBlock(world, i, j, k, l, EnumFacing.UP, cell);
   }

   static void placeBlock(World world, int x, int y, int z, int b, EnumFacing dir, Cell cell) {
      Block block;
      int meta;
      label233: {
         block = null;
         meta = 0;
         switch (b) {
            case 1:
               if (world.isAirBlock(new BlockPos(x, y, z))) {
                  block = Blocks.BEDROCK;
               }
               break label233;
            case 2:
               if (cell.feature == 7 && world.rand.nextInt(3) == 0) {
                  break;
               }

               if (world.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockEldritchNothing) {
                  if (world.rand.nextInt(25) == 0) {
                     boolean crab = cell.feature == 7 || world.rand.nextInt(50) == 0;
                     if ((!crab || cell.feature != 0) && (!crab || cell.feature != 7)) {
                        decoCommon.add(new BlockPos(x, y, z));
                     } else {
                        crabSpawner.add(new BlockPos(x, y, z));
                     }
                  }

                  block = ConfigBlocks.blockCosmeticSolid;
                  meta = 11;
               }
               break label233;
            case 3:
               if ((double)world.rand.nextFloat() < 0.005) {
                  decoUrn.add(new BlockPos(x, y, z));
               }

               block = ConfigBlocks.blockStairsEldritch;
               switch (dir.ordinal()) {
                  case 2:
                  case 3:
                     meta = 1;
                     break label233;
                  case 4:
                  case 5:
                     meta = 3;
                  default:
                     break label233;
               }
            case 4:
               if ((double)world.rand.nextFloat() < 0.005) {
                  decoUrn.add(new BlockPos(x, y, z));
               }

               block = ConfigBlocks.blockStairsEldritch;
               switch (dir.ordinal()) {
                  case 2:
                  case 3:
                     meta = 0;
                     break label233;
                  case 4:
                  case 5:
                     meta = 2;
                  default:
                     break label233;
               }
            case 5:
               block = ConfigBlocks.blockStairsEldritch;
               switch (dir.ordinal()) {
                  case 2:
                  case 3:
                     meta = 5;
                     break label233;
                  case 4:
                  case 5:
                     meta = 7;
                  default:
                     break label233;
               }
            case 6:
               block = ConfigBlocks.blockStairsEldritch;
               switch (dir.ordinal()) {
                  case 2:
                  case 3:
                     meta = 4;
                     break label233;
                  case 4:
                  case 5:
                     meta = 6;
                  default:
                     break label233;
               }
            case 7:
               block = ConfigBlocks.blockEldritch;
               meta = 4;
               break label233;
            case 8:
               block = ConfigBlocks.blockEldritchNothing;
               break label233;
            case 9:
               block = Blocks.AIR;
               decoCommon.remove(new BlockPos(x, y, z));
               crabSpawner.remove(new BlockPos(x, y, z));
               decoUrn.remove(new BlockPos(x, y, z));
               break label233;
            case 10:
               block = ConfigBlocks.blockStairsEldritch;
               switch (dir) {
                  case NORTH:
                     meta = 3;
                     break label233;
                  case SOUTH:
                     meta = 2;
                     break label233;
                  case EAST:
                     meta = 0;
                     break label233;
                  case WEST:
                     meta = 1;
                  default:
                     break label233;
               }
            case 11:
               block = ConfigBlocks.blockStairsEldritch;
               switch (dir) {
                  case NORTH:
                     meta = 7;
                     break label233;
                  case SOUTH:
                     meta = 6;
                     break label233;
                  case EAST:
                     meta = 4;
                     break label233;
                  case WEST:
                     meta = 5;
                  default:
                     break label233;
               }
            case 15:
               block = ConfigBlocks.blockEldritch;
               meta = 7;
               decoCommon.remove(new BlockPos(x, y, z));
               crabSpawner.remove(new BlockPos(x, y, z));
               decoUrn.remove(new BlockPos(x, y, z));
               break label233;
            case 16:
               block = ConfigBlocks.blockEldritch;
               meta = 8;
               decoCommon.remove(new BlockPos(x, y, z));
               crabSpawner.remove(new BlockPos(x, y, z));
               decoUrn.remove(new BlockPos(x, y, z));
               break label233;
            case 17:
               block = ConfigBlocks.blockAiry;
               meta = 12;
               break label233;
            case 18:
               if (world.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockEldritchNothing) {
                  block = ConfigBlocks.blockCosmeticSolid;
                  meta = 12;
               }
               break label233;
            case 19:
               if (world.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockEldritchNothing) {
                  block = ConfigBlocks.blockCosmeticSolid;
                  meta = 13;
               }
               break label233;
            case 20:
               if (world.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockEldritchNothing) {
                  block = ConfigBlocks.blockEldritch;
                  meta = 10;
               }
               break label233;
            case 21:
               break;
            case 99:
               block = Blocks.BEDROCK;
            default:
               break label233;
         }

         if (world.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockEldritchNothing) {
            block = ConfigBlocks.blockCosmeticSolid;
            meta = 14;
            if (world.rand.nextInt(25) == 0) {
               block = ConfigBlocks.blockEldritch;
               meta = 4;
            } else if (world.rand.nextInt(25) == 0) {
               boolean crab = cell.feature == 7 || (cell.feature == 12 && world.rand.nextBoolean() || world.rand.nextInt(25) == 0);
               if (crab && cell.feature == 0 || crab && cell.feature == 7 || crab && cell.feature == 12) {
                  crabSpawner.add(new BlockPos(x, y, z));
               }
            }
         }
      }

      if (block != null) {
        BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
        world.setBlockState(pos, (block).getStateFromMeta(meta), block != ConfigBlocks.blockEldritchNothing && block != Blocks.BEDROCK && block != Blocks.AIR ? 3 : 0);
      }

   }

   public static void genObelisk(World world, int x, int y, int z) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y, z), (ConfigBlocks.blockEldritch).getStateFromMeta(1), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 2, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 3, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 4, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
   }

   static void processDecorations(World world) {
      for(BlockPos cc : decoUrn) {
         if (world.isAirBlock(new BlockPos(cc.getX(), cc.getY() + 1, cc.getZ()))) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(cc.getX(), cc.getY(), cc.getZ()), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(15), 3);
            float rr = world.rand.nextFloat();
            int meta = rr < 0.025F ? 2 : (rr < 0.1F ? 1 : 0);
        world.setBlockState(new net.minecraft.util.math.BlockPos(cc.getX(), cc.getY() + 1, cc.getZ()), (ConfigBlocks.blockLootUrn).getStateFromMeta(meta), 3);
         }
      }

      for(BlockPos cc : decoCommon) {
         int exp = BlockUtils.countExposedSides(world, cc.getX(), cc.getY(), cc.getZ());
         if (exp > 0 && (exp == 1 || !isBedrockShowing(world, cc.getX(), cc.getY(), cc.getZ())) && !BlockUtils.isBlockAdjacentToAtleast(world, cc.getX(), cc.getY(), cc.getZ(), ConfigBlocks.blockEldritch, 32767, 1)) {
            int meta = world.rand.nextInt(3) != 0 ? 4 : (world.rand.nextInt(8) != 0 ? 5 : 10);
        world.setBlockState(new net.minecraft.util.math.BlockPos(cc.getX(), cc.getY(), cc.getZ()), (ConfigBlocks.blockEldritch).getStateFromMeta(meta), 3);
            if (meta == 4 && world.rand.nextInt(12) == 0) {
               for(EnumFacing dir : EnumFacing.values()) {
                  if (world.isAirBlock(new BlockPos(cc.getX() + dir.getXOffset(), cc.getY() + dir.getYOffset(), cc.getZ() + dir.getZOffset()))) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(cc.getX() + dir.getXOffset(), cc.getY() + dir.getYOffset(), cc.getZ() + dir.getZOffset()), (ConfigBlocks.blockCrystal).getStateFromMeta(7), 3);
                     TileCrystal te = (TileCrystal)world.getTileEntity(new BlockPos(cc.getX() + dir.getXOffset(), cc.getY() + dir.getYOffset(), cc.getZ() + dir.getZOffset()));
                     te.orientation = (short)dir.ordinal();
                     break;
                  }
               }
            }
         }
      }

      for(BlockPos cc : crabSpawner) {
         int exp = BlockUtils.countExposedSides(world, cc.getX(), cc.getY(), cc.getZ());
         if (exp == 1 && !BlockUtils.isBlockAdjacentToAtleast(world, cc.getX(), cc.getY(), cc.getZ(), ConfigBlocks.blockEldritch, 32767, 1)) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(cc.getX(), cc.getY(), cc.getZ()), (ConfigBlocks.blockEldritch).getStateFromMeta(9), 3);
            TileEntity te = world.getTileEntity(new BlockPos(cc.getX(), cc.getY(), cc.getZ()));
            if (te instanceof TileEldritchCrabSpawner) {
               for(EnumFacing dir : EnumFacing.values()) {
                  if (world.isAirBlock(new BlockPos(cc.getX() + dir.getXOffset(), cc.getY() + dir.getYOffset(), cc.getZ() + dir.getZOffset()))) {
                     ((TileEldritchCrabSpawner)te).setFacing((byte)dir.ordinal());
                     break;
                  }
               }
            }
         }
      }

      decoCommon.clear();
      crabSpawner.clear();
      decoUrn.clear();
   }

   static boolean isBedrockShowing(World world, int x, int y, int z) {
      for(EnumFacing dir : EnumFacing.values()) {
         if (!world.getBlockState(new net.minecraft.util.math.BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset())).isOpaqueCube() && (world.getBlockState(new net.minecraft.util.math.BlockPos(x + dir.getOpposite().getXOffset(), y + dir.getOpposite().getYOffset(), z + dir.getOpposite().getZOffset())).getBlock() == Blocks.BEDROCK || world.getBlockState(new net.minecraft.util.math.BlockPos(x + dir.getOpposite().getXOffset(), y + dir.getOpposite().getYOffset(), z + dir.getOpposite().getZOffset())).getBlock() == ConfigBlocks.blockEldritchNothing)) {
            return true;
         }
      }

      return false;
   }

   static void generateConnections(World world, Random random, int cx, int cz, int y, Cell cell, int depth, boolean justthetip) {
      int x = cx * 16;
      int z = cz * 16;
      if (cell.north) {
         for(int d = 0; d <= depth; ++d) {
            for(int w = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); w < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++w) {
               for(int h = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); h < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++h) {
                  if (d != depth || !justthetip || PAT_CONNECT[h][w] != 8) {
                     placeBlock(world, x + 3 + w, y + 10 - h, z + d, PAT_CONNECT[h][w], EnumFacing.NORTH, cell);
                  }
               }
            }
         }
      }

      if (cell.south) {
         for(int d = 0; d <= depth; ++d) {
            for(int w = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); w < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++w) {
               for(int h = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); h < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++h) {
                  if (d != depth || !justthetip || PAT_CONNECT[h][w] != 8) {
                     placeBlock(world, x + 3 + w, y + 10 - h, z + 16 - d, PAT_CONNECT[h][w], EnumFacing.SOUTH, cell);
                  }
               }
            }
         }
      }

      if (cell.east) {
         for(int d = 0; d <= depth; ++d) {
            for(int w = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); w < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++w) {
               for(int h = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); h < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++h) {
                  if (d != depth || !justthetip || PAT_CONNECT[h][w] != 8) {
                     placeBlock(world, x + 16 - d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], EnumFacing.EAST, cell);
                  }
               }
            }
         }
      }

      if (cell.west) {
         for(int d = 0; d <= depth; ++d) {
            for(int w = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); w < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++w) {
               for(int h = d == depth && justthetip ? 2 : (d == depth - 1 && justthetip ? 1 : 0); h < (d == depth && justthetip ? 9 : (d == depth - 1 && justthetip ? 10 : 11)); ++h) {
                  if (d != depth || !justthetip || PAT_CONNECT[h][w] != 8) {
                     placeBlock(world, x + d, y + 10 - h, z + 3 + w, PAT_CONNECT[h][w], EnumFacing.WEST, cell);
                  }
               }
            }
         }
      }

   }
}
