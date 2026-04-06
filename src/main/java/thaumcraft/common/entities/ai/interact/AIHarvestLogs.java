package thaumcraft.common.entities.ai.interact;

import com.mojang.authlib.GameProfile;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;

public class AIHarvestLogs extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int xx;
   private int yy;
   private int zz;
   private float distance;
   private World theWorld;
   private Block block;
   private int blockMd;
   private int delay;
   private int maxDelay;
   private int mod;
   FakePlayer player;
   private int count;

   public AIHarvestLogs(EntityGolemBase par1EntityCreature) {
      this.block = Blocks.AIR;
      this.blockMd = 0;
      this.delay = -1;
      this.maxDelay = 1;
      this.mod = 1;
      this.count = 0;
      this.theGolem = par1EntityCreature;
      this.theWorld = par1EntityCreature.world;
      this.setMutexBits(3);
      this.distance = (float)MathHelper.ceil(this.theGolem.getRange() / 3.0F);
      if (this.theWorld instanceof WorldServer) {
         this.player = FakePlayerFactory.get((WorldServer)this.theWorld, new GameProfile(null, "FakeThaumcraftGolem"));
      }

   }

   public boolean shouldExecute() {
      if (this.delay < 0 && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.getNavigator().noPath()) {
         Vec3d var1 = this.findLog();
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
      return state.getBlock() == this.block && this.block.getMetaFromState(state) == this.blockMd && this.count-- > 0 && (this.delay > 0 || Utils.isWoodLog(this.theWorld, this.xx, this.yy, this.zz) || !this.theGolem.getNavigator().noPath());
   }

   public void updateTask() {
      double dist = this.theGolem.getDistanceSq((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F);
      this.theGolem.getLookHelper().setLookPosition((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F, 30.0F, 30.0F);
      if (dist <= (double)4.0F) {
         if (this.delay < 0) {
            IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
            this.delay = (int)Math.max(5.0F, (20.0F - (float)this.theGolem.getGolemStrength() * 3.0F) * this.block.getBlockHardness(state, this.theWorld, new BlockPos(this.xx, this.yy, this.zz)));
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
               if (Utils.isWoodLog(this.theWorld, this.xx, this.yy, this.zz)) {
                  this.delay = -1;
                  IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
                  this.block = state.getBlock();
                  this.blockMd = this.block.getMetaFromState(state);
                  this.startExecuting();
               } else {
                  this.checkAdjacent();
               }
            }
         }
      }

   }

   private void checkAdjacent() {
      BlockPos homePos = this.theGolem.getHomePosition();
      for(int x2 = -1; x2 <= 1; ++x2) {
         for(int z2 = -1; z2 <= 1; ++z2) {
            for(int y2 = -1; y2 <= 1; ++y2) {
               int x = this.xx + x2;
               int y = this.yy + y2;
               int z = this.zz + z2;
               if (!((float)Math.abs(homePos.getX() - x) > this.distance) && !((float)Math.abs(homePos.getY() - y) > this.distance) && !((float)Math.abs(homePos.getZ() - z) > this.distance) && Utils.isWoodLog(this.theWorld, x, y, z)) {
                  this.xx = x;
                  this.yy = y;
                  this.zz = z;
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

   void harvest() {
      this.count = 200;
      IBlockState state = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
      this.theWorld.playEvent(2001, new BlockPos(this.xx, this.yy, this.zz), Block.getStateId(state));
      BlockUtils.breakFurthestBlock(this.theWorld, this.xx, this.yy, this.zz, this.block, this.player);
      this.theGolem.startActionTimer();
   }

   private Vec3d findLog() {
      Random rand = this.theGolem.getRNG();
      BlockPos homePos = this.theGolem.getHomePosition();

      for(int var2 = 0; (float)var2 < this.distance * 4.0F; ++var2) {
         int x = (int)((float)(homePos.getX() + rand.nextInt((int)(1.0F + this.distance * 2.0F))) - this.distance);
         int y = (int)((float)(homePos.getY() + rand.nextInt((int)(1.0F + this.distance))) - this.distance / 2.0F);
         int z = (int)((float)(homePos.getZ() + rand.nextInt((int)(1.0F + this.distance * 2.0F))) - this.distance);
         if (Utils.isWoodLog(this.theWorld, x, y, z)) {
            Vec3d v = new Vec3d(x, y, z);
            double dist = this.theGolem.getDistanceSq((double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F);

            for(int yy = 1; Utils.isWoodLog(this.theWorld, x, y - yy, z) && this.theGolem.getDistanceSq((double)x + (double)0.5F, (double)(y - yy) + (double)0.5F, (double)z + (double)0.5F) < dist; ++yy) {
               v = new Vec3d(x, y - yy, z);
               dist = this.theGolem.getDistanceSq((double)x + (double)0.5F, (double)(y - yy) + (double)0.5F, (double)z + (double)0.5F);
            }

            return v;
         }
      }

      return null;
   }
}
