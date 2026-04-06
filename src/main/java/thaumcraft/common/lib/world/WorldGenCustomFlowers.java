package thaumcraft.common.lib.world;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.util.math.BlockPos;

public class WorldGenCustomFlowers extends WorldGenerator {
   private Block plantBlock;
   private int plantBlockMeta;

   public WorldGenCustomFlowers(Block bi, int md) {
      this.plantBlock = bi;
      this.plantBlockMeta = md;
   }

   @Override
   public boolean generate(World world, Random rand, BlockPos pos) {
      return generate(world, rand, pos.getX(), pos.getY(), pos.getZ());
   }

   private boolean generate(World world, Random par2Random, int par3, int par4, int par5) {
      for(int var6 = 0; var6 < 18; ++var6) {
         int var7 = par3 + par2Random.nextInt(8) - par2Random.nextInt(8);
         int var8 = par4 + par2Random.nextInt(4) - par2Random.nextInt(4);
         int var9 = par5 + par2Random.nextInt(8) - par2Random.nextInt(8);
         if (world.isAirBlock(new BlockPos(var7, var8, var9)) && (world.getBlockState(new net.minecraft.util.math.BlockPos(var7, var8 - 1, var9)).getBlock() == Blocks.GRASS || world.getBlockState(new net.minecraft.util.math.BlockPos(var7, var8 - 1, var9)).getBlock() == Blocks.SAND)) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(var7, var8, var9), (this.plantBlock).getStateFromMeta(this.plantBlockMeta), 3);
         }
      }

      return true;
   }
}
