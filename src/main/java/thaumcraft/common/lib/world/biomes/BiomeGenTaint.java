package thaumcraft.common.lib.world.biomes;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.EntityTaintacle;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.lib.world.WorldGenBigMagicTree;

public class BiomeGenTaint extends Biome {
   public static WorldGenBlockBlob blobs = null;
   protected WorldGenBigMagicTree bigTree = new WorldGenBigMagicTree(false);

   public BiomeGenTaint() {
      super(new BiomeProperties("Tainted Land").setTemperature(0.7F).setRainfall(0.5F).setWaterColor(13373832));
      this.decorator.treesPerChunk = 2;
      this.decorator.flowersPerChunk = -999;
      this.decorator.grassPerChunk = 2;
      this.decorator.reedsPerChunk = -999;
      this.spawnableCreatureList.clear();
      this.spawnableMonsterList.clear();
      this.spawnableWaterCreatureList.clear();
      if (Config.spawnTaintacle) {
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityTaintacle.class, 1, 1, 1));
      }
   }

   public void decorate(World world, Random random, BlockPos pos) {
      super.decorate(world, random, pos);
      this.decorateSpecial(world, random, pos.getX(), pos.getZ());
   }

   public WorldGenAbstractTree getRandomTreeFeature(Random par1Random) {
      return par1Random.nextInt(8) == 0 ? this.bigTree : super.getRandomTreeFeature(par1Random);
   }

   public void decorateSpecial(World world, Random random, int x, int z) {
      int k = random.nextInt(3);

      for(int l = 0; l < k; ++l) {
         int i1 = x + random.nextInt(16) + 8;
         int j1 = z + random.nextInt(16) + 8;
         int k1 = world.getHeight(i1, j1);
         blobs.generate(world, random, new BlockPos(i1, k1, j1));
      }

      for(int a = 0; a < 10; ++a) {
         int xx = x + random.nextInt(16);
         int zz = z + random.nextInt(16);
         int yy = world.getHeight(xx, zz) - 1;
         Block l1 = world.getBlockState(new BlockPos(xx, yy, zz)).getBlock();
         if (l1 != Blocks.AIR) {
            if (l1 == Blocks.GRASS) {
               world.setBlockState(new BlockPos(xx, yy + 1, zz), (ConfigBlocks.blockTaintFibres).getStateFromMeta(0), 2);
            } else {
               BlockPos bp = new BlockPos(xx, yy, zz);
               if (l1.isReplaceable(world, bp) && world.getBlockState(new BlockPos(xx, yy - 1, zz)).getBlock() == Blocks.GRASS) {
                  world.setBlockState(bp, (ConfigBlocks.blockTaintFibres).getStateFromMeta(0), 2);
               }
            }
         }
      }

      for(int a = 0; a < 8; ++a) {
         int xx = x + random.nextInt(16);
         int zz = z + random.nextInt(16);
         int yy = Utils.getFirstUncoveredBlockHeight(world, xx, zz) + 1;
         if (world.getBiome(new BlockPos(xx, 0, zz)) != this) {
            Utils.setBiomeAt(world, xx, zz, ThaumcraftWorldGenerator.biomeTaint);
         }

         BlockPos bp = new BlockPos(xx, yy, zz);
         if (world.isAirBlock(bp) && BlockUtils.isAdjacentToSolidBlock(world, xx, yy, zz)) {
            world.setBlockState(bp, (ConfigBlocks.blockTaintFibres).getStateFromMeta(0), 2);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public int getGrassColorAtPos(BlockPos pos) {
      return 7160201;
   }

   @SideOnly(Side.CLIENT)
   public int getFoliageColorAtPos(BlockPos pos) {
      return 8154503;
   }

   public int getSkyColorByTemp(float par1) {
      return 8144127;
   }

   public int getWaterColorMultiplier() {
      return 13373832;
   }

   public Biome createMutation() {
      return null;
   }
}
