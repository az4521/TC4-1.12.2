package thaumcraft.common.lib.world.biomes;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.EntityPech;
import thaumcraft.common.entities.monster.EntityWisp;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.WorldGenBigMagicTree;
import thaumcraft.common.lib.world.WorldGenGreatwoodTrees;
import thaumcraft.common.lib.world.WorldGenManaPods;
import thaumcraft.common.lib.world.WorldGenSilverwoodTrees;

public class BiomeGenMagicalForest extends Biome {
   protected WorldGenBigMagicTree bigTree = new WorldGenBigMagicTree(false);
   private static final WorldGenBlockBlob blobs;

   public BiomeGenMagicalForest() {
      super(new BiomeProperties("Magical Forest")
              .setTemperature(0.7F)
              .setRainfall(0.6F)
              .setBaseHeight(0.2F)
              .setHeightVariation(0.2F)
              .setWaterColor(30702));
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityWolf.class, 2, 1, 3));
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHorse.class, 2, 1, 3));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWitch.class, 3, 1, 1));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityEnderman.class, 3, 1, 1));
      if (Config.spawnPech) {
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityPech.class, 10, 1, 2));
      }

      if (Config.spawnWisp) {
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWisp.class, 10, 1, 2));
      }

      this.decorator.treesPerChunk = 2;
      this.decorator.flowersPerChunk = 10;
      this.decorator.grassPerChunk = 12;
      this.decorator.waterlilyPerChunk = 6;
      this.decorator.mushroomsPerChunk = 6;
      this.flowers.clear();

      for (BlockFlower.EnumFlowerType type : BlockFlower.EnumFlowerType.getTypes(BlockFlower.EnumFlowerColor.RED)) {
         this.addFlower(Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), type), 10);
      }
   }

   public WorldGenAbstractTree getRandomTreeFeature(Random par1Random) {
      return par1Random.nextInt(14) == 0
              ? new WorldGenSilverwoodTrees(false, 8, 5)
              : (par1Random.nextInt(10) == 0
              ? new WorldGenGreatwoodTrees(false)
              : this.bigTree
      );
   }

   public WorldGenerator getRandomWorldGenForGrass(Random par1Random) {
      return par1Random.nextInt(4) == 0
              ? new WorldGenTallGrass(BlockTallGrass.EnumType.FERN)
              : new WorldGenTallGrass(BlockTallGrass.EnumType.GRASS);
   }

   @SideOnly(Side.CLIENT)
   public int getGrassColorAtPos(BlockPos pos) {
      return Config.blueBiome ? 6728396 : 5635969;
   }

   @SideOnly(Side.CLIENT)
   public int getFoliageColorAtPos(BlockPos pos) {
      return Config.blueBiome ? 7851246 : 6750149;
   }

   public int getWaterColorMultiplier() {
      return 30702;
   }

   public void decorate(World world, Random random, BlockPos chunkPos) {
      int x = chunkPos.getX();
      int z = chunkPos.getZ();
      int k = random.nextInt(3);

      for(int l = 0; l < k; ++l) {
         int i1 = x + random.nextInt(16) + 8;
         int j1 = z + random.nextInt(16) + 8;
         int k1 = world.getHeight(i1, j1);
         blobs.generate(world, random, new BlockPos(i1, k1, j1));
      }

      for(int var16 = 0; var16 < 4; ++var16) {
         for(int var18 = 0; var18 < 4; ++var18) {
            int i1 = x + var16 * 4 + 1 + 8 + random.nextInt(3);
            int j1 = z + var18 * 4 + 1 + 8 + random.nextInt(3);
            int k1 = world.getHeight(i1, j1);
            if (random.nextInt(40) == 0) {
               WorldGenBigMushroom worldgenbigmushroom = new WorldGenBigMushroom();
               worldgenbigmushroom.generate(world, random, new BlockPos(i1, k1, j1));
            }
         }
      }

      super.decorate(world, random, chunkPos);
      WorldGenManaPods worldgenpods = new WorldGenManaPods();

      for(int var17 = 0; var17 < 10; ++var17) {
         int var19 = x + random.nextInt(16) + 8;
         int b0 = 64;
         int i1 = z + random.nextInt(16) + 8;
         worldgenpods.generate(world, random, new BlockPos(var19, b0, i1));
      }

      for(int a = 0; a < 8; ++a) {
         int xx = x + random.nextInt(16);
         int zz = z + random.nextInt(16);

         int yy;
         for(yy = world.getHeight(xx, zz); yy > 50 && world.getBlockState(new BlockPos(xx, yy, zz)).getBlock() != Blocks.GRASS; --yy) {
         }

         Block l1 = world.getBlockState(new BlockPos(xx, yy, zz)).getBlock();
         BlockPos above = new BlockPos(xx, yy + 1, zz);
         if (l1 == Blocks.GRASS
                 && world.getBlockState(above).getBlock().isReplaceable(world, above)
                 && this.isBlockAdjacentToWood(world, xx, yy + 1, zz)) {
            world.setBlockState(above, (ConfigBlocks.blockCustomPlant).getStateFromMeta(5), 2);
         }
      }
   }

   private boolean isBlockAdjacentToWood(IBlockAccess world, int x, int y, int z) {
      for(int xx = -1; xx <= 1; ++xx) {
         for(int yy = -1; yy <= 1; ++yy) {
            for(int zz = -1; zz <= 1; ++zz) {
               if ((xx != 0 || yy != 0 || zz != 0) && Utils.isWoodLog(world, xx + x, yy + y, zz + z)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public Biome createMutation() {
      return null;
   }

   static {
      blobs = new WorldGenBlockBlob(Blocks.MOSSY_COBBLESTONE, 0);
   }
}
