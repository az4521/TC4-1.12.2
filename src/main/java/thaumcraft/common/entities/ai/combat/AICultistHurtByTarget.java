package thaumcraft.common.entities.ai.combat;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.common.entities.monster.EntityCultist;
import thaumcraft.common.entities.monster.EntityCultistCleric;

import java.util.List;

public class AICultistHurtByTarget extends EntityAITarget {
   boolean entityCallsForHelp;
   private int revengeTimerOld;

   public AICultistHurtByTarget(EntityCreature p_i1660_1_, boolean p_i1660_2_) {
      super(p_i1660_1_, false);
      this.entityCallsForHelp = p_i1660_2_;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      int i = this.taskOwner.getRevengeTimer();
      return i != this.revengeTimerOld && this.isSuitableTarget(this.taskOwner.getRevengeTarget(), false);
   }

   public void startExecuting() {
      this.taskOwner.setAttackTarget(this.taskOwner.getRevengeTarget());
      this.revengeTimerOld = this.taskOwner.getRevengeTimer();
      if (this.entityCallsForHelp) {
         double d0 = this.getTargetDistance();

         for(EntityCreature entitycreature : (List<EntityCreature>)(List<?>)this.taskOwner.world.getEntitiesWithinAABB(EntityCultist.class, new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + (double)1.0F, this.taskOwner.posY + (double)1.0F, this.taskOwner.posZ + (double)1.0F).expand(d0, 10.0F, d0))) {
            if (this.taskOwner != entitycreature && entitycreature.getAttackTarget() == null && !entitycreature.isOnSameTeam(this.taskOwner.getRevengeTarget())) {
               if (entitycreature instanceof EntityCultistCleric && ((EntityCultistCleric)entitycreature).getIsRitualist()) {
                  if (this.taskOwner.world.rand.nextInt(3) == 0) {
                     ((EntityCultistCleric)entitycreature).setIsRitualist(false);
                     entitycreature.setAttackTarget(this.taskOwner.getRevengeTarget());
                  }
               } else {
                  entitycreature.setAttackTarget(this.taskOwner.getRevengeTarget());
               }
            }
         }
      }

      super.startExecuting();
   }
}
