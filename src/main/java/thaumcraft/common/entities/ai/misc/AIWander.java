package thaumcraft.common.entities.ai.misc;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

public class AIWander extends EntityAIBase {
   private EntityCreature entity;
   private double xPosition;
   private double yPosition;
   private double zPosition;
   private double speed;
   private boolean field_179482_g;

   public AIWander(EntityCreature creatureIn, double speedIn) {
      this.entity = creatureIn;
      this.speed = speedIn;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (!this.field_179482_g) {
         if (this.entity.ticksExisted >= 100) {
            return false;
         }

         if (this.entity.getRNG().nextInt(120) != 0) {
            return false;
         }
      }

      Vec3d vec3 = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
      if (vec3 == null) {
         return false;
      } else {
         this.xPosition = vec3.x;
         this.yPosition = vec3.y;
         this.zPosition = vec3.z;
         this.field_179482_g = false;
         return true;
      }
   }

   public boolean continueExecuting() {
      return !this.entity.getNavigator().noPath();
   }

   public void setWander() {
      this.field_179482_g = true;
   }

   public void startExecuting() {
      this.entity.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
   }
}
