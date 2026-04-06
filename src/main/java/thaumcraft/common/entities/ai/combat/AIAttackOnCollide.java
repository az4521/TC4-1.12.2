package thaumcraft.common.entities.ai.combat;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AIAttackOnCollide extends EntityAIBase {
   World world;
   EntityCreature attacker;
   int attackTick;
   double speedTowardsTarget;
   boolean longMemory;
   Path entityPathEntity;
   Class<?> classTarget;
   private int delayCounter;
   private double targetX;
   private double targetY;
   private double targetZ;
   private int failedPathFindingPenalty;

   public AIAttackOnCollide(EntityCreature attacker, Class<?> classTarget, double speedTowardsTarget, boolean longMemory) {
      this(attacker, speedTowardsTarget, longMemory);
      this.classTarget = classTarget;
   }

   public AIAttackOnCollide(EntityCreature attacker, double speedTowardsTarget, boolean longMemory) {
      this.attacker = attacker;
      this.world = attacker.world;
      this.speedTowardsTarget = speedTowardsTarget;
      this.longMemory = longMemory;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
      if (entitylivingbase == null) {
         return false;
      } else if (!entitylivingbase.isEntityAlive()) {
         return false;
      } else if (this.classTarget != null && !this.classTarget.isAssignableFrom(entitylivingbase.getClass())) {
         return false;
      } else if (--this.delayCounter <= 0) {
         this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
         this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
         return this.entityPathEntity != null;
      } else {
         return true;
      }
   }

   public boolean continueExecuting() {
      EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
      return entitylivingbase != null
              && (entitylivingbase.isEntityAlive()
              && (
                      !this.longMemory
                              ? !this.attacker.getNavigator().noPath()
                              : this.attacker.isWithinHomeDistanceFromPosition(
                                      new net.minecraft.util.math.BlockPos(entitylivingbase))
      ));
   }

   public void startExecuting() {
      this.attacker.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
      this.delayCounter = 0;
   }

   public void resetTask() {
      this.attacker.getNavigator().clearPath();
   }

   public void updateTask() {
      EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
      this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
      double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
      double d1 = this.attacker.width * 2.0F * this.attacker.width * 2.0F + entitylivingbase.width;
      --this.delayCounter;
      if (this.attackTick > 0) {
         --this.attackTick;
      }

      if ((this.longMemory || this.attacker.getEntitySenses().canSee(entitylivingbase)) && this.delayCounter <= 0 && (this.targetX == (double)0.0F && this.targetY == (double)0.0F && this.targetZ == (double)0.0F || entitylivingbase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= (double)1.0F || this.attacker.getRNG().nextFloat() < 0.05F)) {
         this.targetX = entitylivingbase.posX;
         this.targetY = entitylivingbase.getEntityBoundingBox().minY;
         this.targetZ = entitylivingbase.posZ;
         this.delayCounter = this.failedPathFindingPenalty + 4 + this.attacker.getRNG().nextInt(7);
         if (this.attacker.getNavigator().getPath() != null) {
            PathPoint finalPathPoint = this.attacker.getNavigator().getPath().getFinalPathPoint();
            if (finalPathPoint != null && entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < (double)1.0F) {
               this.failedPathFindingPenalty = 0;
            } else {
               this.failedPathFindingPenalty += 10;
            }
         } else {
            this.failedPathFindingPenalty += 10;
         }

         if (d0 > (double)1024.0F) {
            this.delayCounter += 10;
         } else if (d0 > (double)256.0F) {
            this.delayCounter += 5;
         }

         if (!this.attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget)) {
            this.delayCounter += 15;
         }
      }

      if (d0 <= d1 && this.attackTick <= 0) {
         this.attackTick = 10;
         if (!this.attacker.getHeldItemMainhand().isEmpty()) {
            this.attacker.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
         }

         this.attacker.attackEntityAsMob(entitylivingbase);
      }

   }
}
