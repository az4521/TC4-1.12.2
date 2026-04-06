package thaumcraft.common.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class TileEtherealBloom extends TileEntity implements net.minecraft.util.ITickable {
   public int counter = 0;
   public int growthCounter = 0;

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
            if (this.counter == 0) {
         this.counter = this.world.rand.nextInt(100);
      }

      ++this.counter;
      if (!this.world.isRemote && this.counter % 20 == 0) {
         int x = this.world.rand.nextInt(8) - this.world.rand.nextInt(8);
         int z = this.world.rand.nextInt(8) - this.world.rand.nextInt(8);
         int bx = x + this.getPos().getX();
         int bz = z + this.getPos().getZ();
         int currentBiomeId = Biome.getIdForBiome(this.world.getBiome(new BlockPos(bx, 0, bz)));
         if ((currentBiomeId == Config.biomeTaintID || currentBiomeId == Config.biomeEerieID || currentBiomeId == Config.biomeMagicalForestID)
                 && this.getDistanceSq(
                         (double)bx + (double)0.5F, this.getPos().getY(), (double)bz + (double)0.5F) <= (double)81.0F) {
            Biome[] biomesForGeneration = null;
            biomesForGeneration = this.world.getBiomeProvider().getBiomes(biomesForGeneration, bx, bz, 1, 1);
            if (biomesForGeneration != null && biomesForGeneration[0] != null) {
               Biome biome = biomesForGeneration[0];
               if (biome == ThaumcraftWorldGenerator.biomeTaint) {
                  biome = net.minecraft.init.Biomes.PLAINS;
               }

               Utils.setBiomeAt(this.world, bx, bz, biome);
            }
         }
      }

      if (this.world.isRemote && this.growthCounter == 0) {
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:roots")); if (_snd != null) this.world.playSound(null, (double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 0.6F); };
      }

      ++this.growthCounter;
   }
}
