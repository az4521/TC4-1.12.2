package thaumcraft.common.lib.world.biomes;

import java.util.*;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import thaumcraft.api.aspects.Aspect;

public class BiomeHandler {

   public static Map<BiomeDictionary.Type,BiomeInfo> biomeInfo = new HashMap<>();

   public static void registerBiomeInfo(BiomeDictionary.Type type, int auraLevel, Aspect tag, boolean greatwood, float greatwoodchance) {
      biomeInfo.put(type, new BiomeInfo(auraLevel, tag, greatwood, greatwoodchance));
   }

   public static void registerBiomeInfo(BiomeDictionary.Type type, BiomeInfo info) {
      biomeInfo.put(type, info);
   }

   public static int getBiomeAura(Biome biome) {
      Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
      int average = 0;
      int count = 0;

      for(BiomeDictionary.Type type : types) {
         BiomeInfo info = biomeInfo.get(type);
         if (info == null) {
            continue;
         }
         average += info.getAuraLevel();
         ++count;
      }
      if (count == 0){return 100;}
      return average / count;
   }

   public static Aspect getRandomBiomeTag(int biomeId, Random random) {
      Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(Biome.getBiome(biomeId));
      if (types.isEmpty()) return null;
      List<BiomeDictionary.Type> typeList = new ArrayList<>(types);
      BiomeDictionary.Type type = typeList.get(random.nextInt(typeList.size()));
      BiomeInfo info = biomeInfo.get(type);
      if (info == null) {
         return null;
      }
      return info.getTag();
   }

   public static float getBiomeSupportsGreatwood(int biomeId) {
      Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(Biome.getBiome(biomeId));

      for(BiomeDictionary.Type type : types) {
         BiomeInfo info = biomeInfo.get(type);
         if (info == null) {
            continue;
         }
         if (info.isGreatwood()) {
            return info.getGreatwoodchance();
         }
      }

      return 0.0F;
   }
}
