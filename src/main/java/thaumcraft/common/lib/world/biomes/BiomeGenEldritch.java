package thaumcraft.common.lib.world.biomes;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.EntityInhabitedZombie;

public class BiomeGenEldritch extends Biome {
   public BiomeGenEldritch() {
      super(new BiomeProperties("Eldritch").setRainDisabled());
      this.spawnableMonsterList.clear();
      this.spawnableCreatureList.clear();
      this.spawnableWaterCreatureList.clear();
      this.spawnableCaveCreatureList.clear();
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityInhabitedZombie.class, 1, 1, 1));
      this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityEldritchGuardian.class, 1, 1, 1));
      this.topBlock = Blocks.DIRT.getDefaultState();
      this.fillerBlock = Blocks.DIRT.getDefaultState();
   }

   @SideOnly(Side.CLIENT)
   public int getSkyColorByTemp(float currentTemperature) {
      return 0;
   }

   public void decorate(World world, Random random, BlockPos pos) {
   }

   public Biome createMutation() {
      return null;
   }
}
