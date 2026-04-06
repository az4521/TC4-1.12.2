package thaumcraft.common.entities.ai.interact;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLog;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.CropUtils;
import thaumcraft.common.lib.utils.EntityUtils;

public class AIHarvestCrops extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int xx;
   private int yy;
   private int zz;
   private float movementSpeed;
   private float distance;
   private World theWorld;
   private Block block;
   private int blockMd;
   private int delay;
   private int maxDelay;
   private int mod;
   private int count;
   ArrayList checklist;

   public AIHarvestCrops(EntityGolemBase par1EntityCreature) {
      this.block = Blocks.AIR;
      this.blockMd = 0;
      this.delay = -1;
      this.maxDelay = 1;
      this.mod = 1;
      this.count = 0;
      this.checklist = new ArrayList<>();
      this.theGolem = par1EntityCreature;
      this.theWorld = par1EntityCreature.world;
      this.setMutexBits(3);
      this.distance = (float)MathHelper.ceil(this.theGolem.getRange() / 4.0F);
   }

   public boolean shouldExecute() {
      if (this.delay < 0 && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.getNavigator().noPath()) {
         Vec3d var1 = this.findGrownCrop();
         if (var1 == null) {
            return false;
         } else {
            this.xx = (int)var1.x;
            this.yy = (int)var1.y;
            this.zz = (int)var1.z;
            IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
            this.block = state.getBlock();
            this.blockMd = this.block.getMetaFromState(state);
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
      return state.getBlock() == this.block && this.block.getMetaFromState(state) == this.blockMd && this.count-- > 0 && (this.delay > 0 || !this.theGolem.getNavigator().noPath());
   }

   public void updateTask() {
      double dist = this.theGolem.getDistanceSq((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F);
      this.theGolem.getLookHelper().setLookPosition((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F, 30.0F, 30.0F);
      if (dist <= (double)4.0F) {
         if (this.delay < 0) {
            IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
            this.delay = (int)Math.max(10.0F, (20.0F - (float)this.theGolem.getGolemStrength() * 2.0F) * this.block.getBlockHardness(state, this.theWorld, new BlockPos(this.xx, this.yy, this.zz)));
            this.maxDelay = this.delay;
            this.mod = this.delay / Math.round((float)this.delay / 6.0F);
         }

         if (this.delay > 0) {
            if (--this.delay > 0 && this.delay % this.mod == 0 && this.theGolem.getNavigator().noPath()) {
               this.theGolem.startActionTimer();
               SoundType snd = this.block.getSoundType();
               this.theWorld.playSound(null, new BlockPos(this.xx, this.yy, this.zz), snd.getBreakSound(), SoundCategory.BLOCKS, (snd.getVolume() + 0.7F) / 8.0F, snd.getPitch() * 0.5F);
               BlockUtils.destroyBlockPartially(this.theWorld, this.theGolem.getEntityId(), this.xx, this.yy, this.zz, (int)(9.0F * (1.0F - (float)this.delay / (float)this.maxDelay)));
            }

            if (this.delay == 0) {
               this.harvest();
               this.checkAdjacent();
            }
         }
      }

   }

   private void checkAdjacent() {
      BlockPos homePos = this.theGolem.getHomePosition();
      for(int x2 = -2; x2 <= 2; ++x2) {
         for(int z2 = -2; z2 <= 2; ++z2) {
            for(int y2 = -1; y2 <= 1; ++y2) {
               int x = this.xx + x2;
               int y = this.yy + y2;
               int z = this.zz + z2;
               if (!((float)Math.abs(homePos.getX() - x) > this.distance) && !((float)Math.abs(homePos.getY() - y) > this.distance) && !((float)Math.abs(homePos.getZ() - z) > this.distance) && CropUtils.isGrownCrop(this.theWorld, x, y, z)) {
                  Vec3d var1 = new Vec3d(x, y, z);
                  this.xx = (int)var1.x;
                  this.yy = (int)var1.y;
                  this.zz = (int)var1.z;
                  IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
                  this.block = state.getBlock();
                  this.blockMd = this.block.getMetaFromState(state);
                  this.delay = -1;
                  this.startExecuting();
                  return;
               }
            }
         }
      }

   }

   public void resetTask() {
      BlockUtils.destroyBlockPartially(this.theWorld, this.theGolem.getEntityId(), this.xx, this.yy, this.zz, -1);
      this.delay = -1;
   }

   public void startExecuting() {
      this.count = 200;
      this.theGolem.getNavigator().tryMoveToXYZ((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F, this.theGolem.getAIMoveSpeed());
   }

   private Vec3d findGrownCrop() {
      Random rand = this.theGolem.getRNG();
      BlockPos homePos = this.theGolem.getHomePosition();
      if (this.checklist.isEmpty()) {
         for(int a = (int)(-this.distance); (float)a <= this.distance; ++a) {
            for(int b = (int)(-this.distance); (float)b <= this.distance; ++b) {
               this.checklist.add(new BlockCoordinates(homePos.getX() + a, 0, homePos.getZ() + b));
            }
         }

         Collections.shuffle(this.checklist, rand);
      }

      int x = ((BlockCoordinates)this.checklist.get(0)).x;
      int z = ((BlockCoordinates)this.checklist.get(0)).z;
      this.checklist.remove(0);

      for(int y = homePos.getY() - 3; y <= homePos.getY() + 3; ++y) {
         if (CropUtils.isGrownCrop(this.theWorld, x, y, z)) {
            return new Vec3d(x, y, z);
         }
      }

      return null;
   }

   void harvest() {
      this.count = 200;
      FakePlayer fp = FakePlayerFactory.get((WorldServer)this.theWorld, new GameProfile(null, "FakeThaumcraftGolem"));
      fp.setPosition(this.theGolem.posX, this.theGolem.posY, this.theGolem.posZ);
      if (CropUtils.clickableCrops.contains(this.block.getTranslationKey() + this.blockMd)) {
         IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
         this.block.onBlockActivated(this.theWorld, new BlockPos(this.xx, this.yy, this.zz), state, fp, EnumHand.MAIN_HAND, EnumFacing.UP, 0.0F, 0.0F, 0.0F);
      } else {
         this.theWorld.destroyBlock(new BlockPos(this.xx, this.yy, this.zz), true);
         if (this.theGolem.getUpgradeAmount(4) > 0) {
            ArrayList<Entity> drops = EntityUtils.getEntitiesInRange(this.theWorld, this.theGolem.posX, this.theGolem.posY, this.theGolem.posZ, this.theGolem, EntityItem.class, 6.0F);
            if (!drops.isEmpty()) {
               for(Entity e : drops) {
                  if (e instanceof EntityItem) {
                     if (e.ticksExisted < 2) {
                        Vec3d v = new Vec3d(e.posX - this.theGolem.posX, e.posY - this.theGolem.posY, e.posZ - this.theGolem.posZ);
                        v = v.normalize();
                        e.motionX = -v.x / (double)4.0F;
                        e.motionY = 0.075;
                        e.motionZ = -v.z / (double)4.0F;
                     }

                     boolean done = false;
                     EntityItem item = (EntityItem)e;
                     ItemStack st = item.getItem();
                     if (st.getItem() != null && st.getItem() == Items.DYE && st.getItemDamage() == 3) {
                        // Cocoa bean: place on adjacent jungle log
                        IBlockState cocoaState = Blocks.COCOA.getStateFromMeta(this.blockMd);
                        EnumFacing facing = (EnumFacing)cocoaState.getValue(BlockHorizontal.FACING);
                        int par2 = this.xx + facing.getXOffset();
                        int par4 = this.zz + facing.getZOffset();
                        IBlockState logState = this.theWorld.getBlockState(new BlockPos(par2, this.yy, par4));
                        if (logState.getBlock() == Blocks.LOG && logState.getValue(BlockLog.LOG_AXIS) == BlockLog.EnumAxis.NONE) {
                           st.shrink(1);
                           IBlockState newCocoa = Blocks.COCOA.getDefaultState()
                              .withProperty(BlockHorizontal.FACING, facing)
                              .withProperty(BlockCocoa.AGE, 0);
                           this.theWorld.setBlockState(new BlockPos(this.xx, this.yy, this.zz), newCocoa, 3);
                        }

                        done = true;
                     } else if (st.getItem() != null && st.getItem() == ConfigItems.itemManaBean) {
                        if (this.block.canPlaceBlockOnSide(this.theWorld, new BlockPos(this.xx, this.yy, this.zz), EnumFacing.DOWN)) {
                           st.shrink(1);
                           if (st.getItem().onItemUse(fp, this.theWorld, new BlockPos(this.xx, this.yy + 1, this.zz), EnumHand.MAIN_HAND, EnumFacing.DOWN, 0.5F, 0.5F, 0.5F) != EnumActionResult.SUCCESS) {
                              this.theWorld.setBlockState(new BlockPos(this.xx, this.yy, this.zz), ConfigBlocks.blockManaPod.getDefaultState(), 3);
                           }
                        }

                        done = true;
                     } else {
                        int[] xm = new int[]{0, 0, 1, 1, -1, 0, -1, -1, 1};
                        int[] zm = new int[]{0, 1, 0, 1, 0, -1, -1, 1, -1};

                        for(int count = 0; !st.isEmpty() && count < 9; ++count) {
                           if (st.getItem() != null && (st.getItem() instanceof IPlantable || st.getItem() instanceof ItemSeedFood) && st.getItem().onItemUse(fp, this.theWorld, new BlockPos(this.xx + xm[count], this.yy - 1, this.zz + zm[count]), EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F) == EnumActionResult.SUCCESS) {
                              st.shrink(1);
                           }
                        }
                     }

                     if (st.isEmpty()) {
                        item.setDead();
                     } else {
                        item.setItem(st);
                     }

                     if (done) {
                        break;
                     }
                  }
               }
            }
         }
      }

      fp.setDead();
      this.theGolem.startActionTimer();
   }
}
