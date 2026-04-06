package thaumcraft.common.lib.world;

import java.util.Random;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileManaPod;
import net.minecraft.util.math.BlockPos;

public class WorldGenManaPods extends WorldGenerator {
   @Override
   public boolean generate(World world, Random rand, BlockPos pos) {
      return generate(world, rand, pos.getX(), pos.getY(), pos.getZ());
   }

   private boolean generate(World par1World, Random par2Random, int x, int y, int z) {
      int l = x;

      for(int i1 = z; y < Math.min(128, par1World.getHeight(x, z)); ++y) {
         if (par1World.isAirBlock(new BlockPos(x, y, z)) && par1World.isAirBlock(new BlockPos(x, y - 1, z))) {
            if (ConfigBlocks.blockManaPod.canPlaceBlockOnSide(par1World, new BlockPos(x, y, z), EnumFacing.DOWN)) {
        par1World.setBlockState(new net.minecraft.util.math.BlockPos(x, y, z), (ConfigBlocks.blockManaPod).getStateFromMeta(2 + par2Random.nextInt(5)), 2);
               TileEntity tile = par1World.getTileEntity(new BlockPos(x, y, z));
               if (tile instanceof TileManaPod) {
                  ((TileManaPod)tile).checkGrowth();
               }
               break;
            }
         } else {
            x = l + par2Random.nextInt(4) - par2Random.nextInt(4);
            z = i1 + par2Random.nextInt(4) - par2Random.nextInt(4);
         }
      }

      return true;
   }
}
