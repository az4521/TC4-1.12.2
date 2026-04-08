package thaumcraft.common.lib.world.dim;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class WorldProviderOuter extends WorldProvider {

   @Override
   public DimensionType getDimensionType() {
      return DimensionManager.getProviderType(Config.dimensionOuterId);
   }

   @Override
   public String getSaveFolder() {
      return "DIM" + getDimension();
   }

   @Override
   public boolean shouldMapSpin(String entity, double x, double z, double rotation) {
      return true;
   }

   @Override
   public boolean canBlockFreeze(BlockPos pos, boolean byWater) {
      return false;
   }

   @Override
   public boolean canSnowAt(BlockPos pos, boolean checkLight) {
      return false;
   }

   @Override
   public boolean canDoLightning(Chunk chunk) {
      return false;
   }

   @Override
   public boolean canDoRainSnowIce(Chunk chunk) {
      return false;
   }

   @Override
   protected void init() {
      this.biomeProvider = new BiomeProviderSingle(ThaumcraftWorldGenerator.biomeEldritchLands);
      this.setDimension(Config.dimensionOuterId);
      this.hasSkyLight = false;
   }

   @Override
   public IChunkGenerator createChunkGenerator() {
      return new ChunkProviderOuter(this.world, this.world.getSeed(), true);
   }

   @Override
   public float calculateCelestialAngle(long worldTime, float partialTicks) {
      return 0.0F;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
      return null;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public Vec3d getFogColor(float timeOfDay, float tickDelta) {
      int i = 10518688;
      float f2 = MathHelper.cos(timeOfDay * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
      if (f2 < 0.0F) {
         f2 = 0.0F;
      }

      if (f2 > 1.0F) {
         f2 = 1.0F;
      }

      float f3 = (float)(i >> 16 & 255) / 255.0F;
      float f4 = (float)(i >> 8 & 255) / 255.0F;
      float f5 = (float)(i & 255) / 255.0F;
      f3 *= f2 * 0.0F + 0.15F;
      f4 *= f2 * 0.0F + 0.15F;
      f5 *= f2 * 0.0F + 0.15F;
      return new Vec3d(f3, f4, f5);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public boolean isSkyColored() {
      return false;
   }

   @Override
   public boolean canRespawnHere() {
      return false;
   }

   @Override
   public boolean isSurfaceWorld() {
      return false;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public float getCloudHeight() {
      return 1.0F;
   }

   @Override
   public boolean canCoordinateBeSpawn(int x, int z) {
      return this.world.getGroundAboveSeaLevel(new BlockPos(x, 0, z)).getMaterial().blocksMovement();
   }

   @Override
   public int getAverageGroundLevel() {
      return 50;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public boolean doesXZShowFog(int x, int z) {
      return true;
   }
}
