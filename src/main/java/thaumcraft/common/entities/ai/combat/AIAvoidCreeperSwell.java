package thaumcraft.common.entities.ai.combat;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class AIAvoidCreeperSwell extends EntityAIBase {
   private EntityGolemBase theGolem;
   private float farSpeed;
   private float nearSpeed;
   private Entity closestLivingEntity;
   private float distanceFromEntity;
   private Path entityPathEntity;
   private PathNavigate entityPathNavigate;
   Vec3d targetBlock;

   public AIAvoidCreeperSwell(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.distanceFromEntity = 5.0F;
      this.entityPathNavigate = par1EntityCreature.getNavigator();
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (this.farSpeed == 0.0F) {
         this.farSpeed = this.theGolem.getAIMoveSpeed() * 1.125F;
         this.nearSpeed = this.theGolem.getAIMoveSpeed() * 1.25F;
      }

      List<Entity> var1 = (List<Entity>)(List<?>)this.theGolem.world.getEntitiesWithinAABB(EntityCreeper.class, this.theGolem.getEntityBoundingBox().expand(this.distanceFromEntity, 3.0F, this.distanceFromEntity));
      if (var1.isEmpty()) {
         return false;
      } else if (((EntityCreeper)var1.get(0)).getCreeperState() != 1) {
         return false;
      } else {
         this.closestLivingEntity = var1.get(0);
         if (!this.theGolem.getEntitySenses().canSee(this.closestLivingEntity)) {
            return false;
         } else {
            Vec3d var2 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.theGolem, 16, 7, new Vec3d(this.closestLivingEntity.posX, this.closestLivingEntity.posY, this.closestLivingEntity.posZ));
            if (var2 == null) {
               return false;
            } else if (this.closestLivingEntity.getDistanceSq(var2.x, var2.y, var2.z) < this.closestLivingEntity.getDistanceSq(this.theGolem)) {
               return false;
            } else {
               this.entityPathEntity = this.entityPathNavigate.getPathToXYZ(var2.x, var2.y, var2.z);
               this.targetBlock = var2;
               return this.entityPathEntity != null;
            }
         }
      }
   }

   public boolean continueExecuting() {
      return !this.entityPathNavigate.noPath();
   }

   public void startExecuting() {
      double var1 = this.targetBlock.x + (double)0.5F - this.theGolem.posX;
      double var3 = this.targetBlock.z + (double)0.5F - this.theGolem.posZ;
      float var5 = MathHelper.sqrt(var1 * var1 + var3 * var3);
      EntityGolemBase golem = this.theGolem;
      golem.motionX += var1 / (double)var5 * (double)1.0F * (double)0.8F + this.theGolem.motionX * (double)0.2F;
      golem.motionZ += var3 / (double)var5 * (double)1.0F * (double)0.8F + this.theGolem.motionZ * (double)0.2F;
      this.theGolem.motionY = 0.3;
      this.entityPathNavigate.setPath(this.entityPathEntity, this.nearSpeed);
   }

   public void resetTask() {
      this.closestLivingEntity = null;
   }

   public void updateTask() {
      if (this.theGolem.getDistanceSq(this.closestLivingEntity) < (double)49.0F) {
         this.theGolem.getNavigator().setSpeed(this.nearSpeed);
      } else {
         this.theGolem.getNavigator().setSpeed(this.farSpeed);
      }

   }
}
