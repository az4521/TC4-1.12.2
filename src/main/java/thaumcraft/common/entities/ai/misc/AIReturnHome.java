package thaumcraft.common.entities.ai.misc;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class AIReturnHome extends EntityAIBase {
   private EntityGolemBase theGolem;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private int pathingDelay = 0;
   private int pathingDelayInc = 5;
   int count = 0;
   int prevX = 0;
   int prevY = 0;
   int prevZ = 0;

   public AIReturnHome(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      BlockPos home = this.theGolem.getHomePosition();
      if (this.pathingDelay > 0) {
         --this.pathingDelay;
      }

      if (this.pathingDelay <= 0 && !(this.theGolem.getDistanceSq((double)home.getX() + 0.5, (double)home.getY() + 0.5, (double)home.getZ() + 0.5) < (double)3.0F)) {
         this.movePosX = (double)home.getX() + 0.5;
         this.movePosY = (double)home.getY() + 0.5;
         this.movePosZ = (double)home.getZ() + 0.5;
         return true;
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      BlockPos home = this.theGolem.getHomePosition();
      return this.pathingDelay <= 0 && this.count > 0 && !this.theGolem.getNavigator().noPath() && this.theGolem.getDistanceSq((double)home.getX() + 0.5, (double)home.getY() + 0.5, (double)home.getZ() + 0.5) >= (double)3.0F;
   }

   public void resetTask() {
      super.resetTask();
   }

   public void updateTask() {
      --this.count;
      if (this.count == 0 && this.prevX == MathHelper.floor(this.theGolem.posX) && this.prevY == MathHelper.floor(this.theGolem.posY) && this.prevZ == MathHelper.floor(this.theGolem.posZ)) {
         Vec3d var2 = RandomPositionGenerator.findRandomTarget(this.theGolem, 2, 1);
         if (var2 != null) {
            this.count = 20;
            boolean path = this.theGolem.getNavigator().tryMoveToXYZ(var2.x + 0.5, var2.y + 0.5, var2.z + 0.5, this.theGolem.getAIMoveSpeed());
            if (!path) {
               this.pathingDelay = this.pathingDelayInc;
               if (this.pathingDelayInc < 50) {
                  this.pathingDelayInc += 5;
               }
            } else {
               this.pathingDelayInc = 5;
            }
         }
      }

      super.updateTask();
   }

   public void startExecuting() {
      this.count = 20;
      this.prevX = MathHelper.floor(this.theGolem.posX);
      this.prevY = MathHelper.floor(this.theGolem.posY);
      this.prevZ = MathHelper.floor(this.theGolem.posZ);
      boolean path = this.theGolem.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.theGolem.getAIMoveSpeed());
      if (!path) {
         this.pathingDelay = this.pathingDelayInc;
         if (this.pathingDelayInc < 50) {
            this.pathingDelayInc += 5;
         }
      } else {
         this.pathingDelayInc = 5;
      }
   }
}
