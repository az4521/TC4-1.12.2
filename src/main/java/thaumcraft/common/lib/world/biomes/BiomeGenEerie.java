package thaumcraft.common.lib.world.biomes;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.monster.EntityBrainyZombie;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.EntityGiantBrainyZombie;
import thaumcraft.common.entities.monster.EntityWisp;

public class BiomeGenEerie extends Biome {
   public BiomeGenEerie() {
      super(new BiomeProperties("Eerie").setTemperature(0.5F).setRainfall(0.5F).setWaterColor(3035999));
      this.spawnableCreatureList.clear();
      this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBat.class, 3, 1, 1));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWitch.class, 8, 1, 1));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityEnderman.class, 4, 1, 1));
      if (Config.spawnAngryZombie) {
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityBrainyZombie.class, 32, 1, 1));
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityGiantBrainyZombie.class, 8, 1, 1));
      }

      if (Config.spawnWisp) {
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWisp.class, 3, 1, 1));
      }

      if (Config.spawnElder) {
         this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityEldritchGuardian.class, 1, 1, 1));
      }

      this.decorator.treesPerChunk = 2;
      this.decorator.flowersPerChunk = 1;
      this.decorator.grassPerChunk = 2;
   }

   @SideOnly(Side.CLIENT)
   public int getGrassColorAtPos(BlockPos pos) {
      return 4212800;
   }

   @SideOnly(Side.CLIENT)
   public int getFoliageColorAtPos(BlockPos pos) {
      return 4215616;
   }

   public int getSkyColorByTemp(float par1) {
      return 2237081;
   }

   public int getWaterColorMultiplier() {
      return 3035999;
   }

   public Biome createMutation() {
      return null;
   }
}
