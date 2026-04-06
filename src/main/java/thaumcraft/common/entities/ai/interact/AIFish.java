package thaumcraft.common.entities.ai.interact;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishFood.FishType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.EntityGolemBobber;

public class AIFish extends EntityAIBase {
   private EntityGolemBase theGolem;
   private float quality;
   private float distance;
   private World theWorld;
   private int maxDelay = 1;
   private int mod = 1;
   private int count = 0;
   private Vec3d target = null;
   private EntityGolemBobber bobber = null;

   // Simple weighted fishing item entry
   private static class FishingEntry extends WeightedRandom.Item {
      private final ItemStack stack;
      FishingEntry(ItemStack stack, int weight) {
         super(weight);
         this.stack = stack;
      }
      public ItemStack getStack() {
         return stack.copy();
      }
   }

   private static final List<FishingEntry> LOOTCRAP;
   private static final List<FishingEntry> LOOTRARE;
   private static final List<FishingEntry> LOOTFISH;

   public AIFish(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.theWorld = par1EntityCreature.world;
      this.setMutexBits(3);
      this.distance = (float)MathHelper.ceil(this.theGolem.getRange() / 2.0F);
   }

   public boolean shouldExecute() {
      if (this.target == null && this.count <= 0 && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.getNavigator().noPath()) {
         if (this.bobber != null) {
            this.bobber.setDead();
         }

         Vec3d vv = this.findWater();
         if (vv == null) {
            return false;
         } else {
            this.target = new Vec3d(vv.x, vv.y, vv.z);
            this.quality = 0.0F;
            int x = (int)this.target.x;
            int y = (int)this.target.y;
            int z = (int)this.target.z;

            for(int a = 2; a <= 5; ++a) {
               EnumFacing dir = EnumFacing.byIndex(a);
               IBlockState adjState = this.theWorld.getBlockState(new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset()));
               if (adjState.getMaterial() == Material.WATER && this.theWorld.isAirBlock(new BlockPos(x + dir.getXOffset(), y + 1 + dir.getYOffset(), z + dir.getZOffset()))) {
                  this.quality += 3.0E-5F;
                  if (this.theWorld.canBlockSeeSky(new BlockPos(x + dir.getXOffset(), y + 1 + dir.getYOffset(), z + dir.getZOffset()))) {
                     this.quality += 3.0E-5F;
                  }

                  for(int depth = 1; depth <= 3; ++depth) {
                     IBlockState depthState = this.theWorld.getBlockState(new BlockPos(x + dir.getXOffset(), y - depth + dir.getYOffset(), z + dir.getZOffset()));
                     if (depthState.getMaterial() == Material.WATER) {
                        this.quality += 1.5E-5F;
                     }
                  }
               }
            }

            this.theWorld.playSound(null, this.theGolem.getPosition(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (this.theWorld.rand.nextFloat() * 0.4F + 0.8F));
            this.bobber = new EntityGolemBobber(this.theWorld, this.theGolem, x, y, z);
            return this.theWorld.spawnEntity(this.bobber);
         }
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return this.bobber != null && !this.bobber.isDead && this.target != null && this.count-- > 0;
   }

   public void updateTask() {
      if (this.target != null) {
         this.theGolem.getLookHelper().setLookPosition(this.target.x + (double)0.5F, this.target.y + (double)1.0F, this.target.z + (double)0.5F, 30.0F, 30.0F);
         float chance = this.quality + (float)this.theGolem.getGolemStrength() * 1.5E-4F;
         if (this.theWorld.rand.nextFloat() < chance) {
            this.theGolem.startRightArmTimer();
            int qq = 1;
            if (this.theGolem.getUpgradeAmount(0) > 0 && this.theWorld.rand.nextInt(10) < this.theGolem.getUpgradeAmount(0)) {
               ++qq;
            }

            for(int a = 0; a < qq; ++a) {
               ItemStack fs = this.getFishingResult();
               if (this.theGolem.getUpgradeAmount(2) > 0) {
                  ItemStack sr = FurnaceRecipes.instance().getSmeltingResult(fs);
                  if (!sr.isEmpty()) {
                     fs = sr.copy();
                  }
               }

               EntityItem entityitem = new EntityItem(this.theWorld, this.target.x + (double)0.5F, this.target.y + (double)1.0F, this.target.z + (double)0.5F, fs);
               if (this.theGolem.getUpgradeAmount(2) > 0) {
                  entityitem.setFire(2);
               }

               entityitem.setPickupDelay(20);
               double d1 = this.theGolem.posX + (double)this.theWorld.rand.nextFloat() - (double)this.theWorld.rand.nextFloat() - this.target.x + (double)0.5F;
               double d3 = this.theGolem.posY - this.target.y + (double)1.0F;
               double d5 = this.theGolem.posZ + (double)this.theWorld.rand.nextFloat() - (double)this.theWorld.rand.nextFloat() - this.target.z + (double)0.5F;
               double d7 = MathHelper.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
               double d9 = 0.1;
               entityitem.motionX = d1 * d9;
               entityitem.motionY = d3 * d9 + (double)MathHelper.sqrt(d7) * 0.08;
               entityitem.motionZ = d5 * d9;
               this.theWorld.spawnEntity(entityitem);
            }

            if (this.bobber != null) {
               this.bobber.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.15F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.4F);
               ((WorldServer)this.theWorld).spawnParticle(EnumParticleTypes.WATER_SPLASH, false, this.bobber.posX, this.bobber.posY + 0.5, this.bobber.posZ, 20 + this.theWorld.rand.nextInt(20), 0.1, 0.0, 0.1, 0.0);
               this.bobber.setDead();
            }

            this.target = null;
         }
      }

   }

   public void resetTask() {
      if (this.bobber != null) {
         this.bobber.setDead();
      }

      this.target = null;
      this.count = -1;
   }

   public void startExecuting() {
      this.count = 300 + this.theWorld.rand.nextInt(200);
      this.theGolem.startRightArmTimer();
   }

   private Vec3d findWater() {
      Random rand = this.theGolem.getRNG();
      BlockPos homePos = this.theGolem.getHomePosition();

      for(int var2 = 0; (float)var2 < this.distance * 2.0F; ++var2) {
         int x = (int)((float)(homePos.getX() + rand.nextInt((int)(1.0F + this.distance * 2.0F))) - this.distance);
         int y = (int)((float)(homePos.getY() + rand.nextInt((int)(1.0F + this.distance))) - this.distance / 2.0F);
         int z = (int)((float)(homePos.getZ() + rand.nextInt((int)(1.0F + this.distance * 2.0F))) - this.distance);
         IBlockState state = this.theWorld.getBlockState(new BlockPos(x, y, z));
         if (state.getMaterial() == Material.WATER && this.theWorld.isAirBlock(new BlockPos(x, y + 1, z))) {
            return new Vec3d(x, y, z);
         }
      }

      return null;
   }

   private ItemStack getFishingResult() {
      float f = this.theWorld.rand.nextFloat();
      float f1 = 0.1F - (float)this.theGolem.getUpgradeAmount(5) * 0.025F;
      float f2 = 0.05F + (float)this.theGolem.getUpgradeAmount(4) * 0.0125F;
      int x = (int)this.target.x;
      int y = (int)this.target.y;
      int z = (int)this.target.z;

      for(int a = 2; a <= 5; ++a) {
         EnumFacing dir = EnumFacing.byIndex(a);
         IBlockState adjState = this.theWorld.getBlockState(new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset()));
         if (adjState.getMaterial() == Material.WATER && this.theWorld.isAirBlock(new BlockPos(x + dir.getXOffset(), y + 1 + dir.getYOffset(), z + dir.getZOffset()))) {
            f1 -= 0.005F;
            f2 += 0.00125F;
            if (this.theWorld.canBlockSeeSky(new BlockPos(x + dir.getXOffset(), y + 1 + dir.getYOffset(), z + dir.getZOffset()))) {
               f1 -= 0.005F;
               f2 += 0.00125F;
            }

            for(int depth = 1; depth <= 3; ++depth) {
               IBlockState depthState = this.theWorld.getBlockState(new BlockPos(x + dir.getXOffset(), y - depth + dir.getYOffset(), z + dir.getZOffset()));
               if (depthState.getMaterial() == Material.WATER) {
                  f2 += 0.001F;
               }
            }
         }
      }

      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      f2 = MathHelper.clamp(f2, 0.0F, 1.0F);
      if (f < f1) {
         return ((FishingEntry)WeightedRandom.getRandomItem(this.theWorld.rand, LOOTCRAP)).getStack();
      } else {
         f -= f1;
         if (f < f2) {
            return ((FishingEntry)WeightedRandom.getRandomItem(this.theWorld.rand, LOOTRARE)).getStack();
         } else {
            return ((FishingEntry)WeightedRandom.getRandomItem(this.theWorld.rand, LOOTFISH)).getStack();
         }
      }
   }

   static {
      LOOTCRAP = Arrays.asList(
         new FishingEntry(new ItemStack(Items.LEATHER_BOOTS), 10),
         new FishingEntry(new ItemStack(Items.LEATHER), 10),
         new FishingEntry(new ItemStack(Items.BONE), 10),
         new FishingEntry(new ItemStack(Items.POTIONITEM), 10),
         new FishingEntry(new ItemStack(Items.STRING), 5),
         new FishingEntry(new ItemStack(Items.FISHING_ROD), 2),
         new FishingEntry(new ItemStack(Items.BOWL), 10),
         new FishingEntry(new ItemStack(Items.STICK), 5),
         new FishingEntry(new ItemStack(Items.DYE, 10, 0), 5),
         new FishingEntry(new ItemStack(Blocks.TRIPWIRE_HOOK), 10),
         new FishingEntry(new ItemStack(Items.ROTTEN_FLESH), 10)
      );
      LOOTRARE = Arrays.asList(
         new FishingEntry(new ItemStack(Blocks.WATERLILY), 1),
         new FishingEntry(new ItemStack(Items.NAME_TAG), 1),
         new FishingEntry(new ItemStack(Items.SADDLE), 1),
         new FishingEntry(new ItemStack(Items.BOW), 1),
         new FishingEntry(new ItemStack(Items.FISHING_ROD), 1),
         new FishingEntry(new ItemStack(Items.BOOK), 1)
      );
      LOOTFISH = Arrays.asList(
         new FishingEntry(new ItemStack(Items.FISH, 1, FishType.COD.getMetadata()), 60),
         new FishingEntry(new ItemStack(Items.FISH, 1, FishType.SALMON.getMetadata()), 25),
         new FishingEntry(new ItemStack(Items.FISH, 1, FishType.CLOWNFISH.getMetadata()), 2),
         new FishingEntry(new ItemStack(Items.FISH, 1, FishType.PUFFERFISH.getMetadata()), 13)
      );
   }
}
