package thaumcraft.common.entities.ai.combat;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class AINearestButcherTarget extends EntityAITarget {
   EntityGolemBase theGolem;
   EntityLivingBase target;
   int targetChance;
   private float targetDistance;
   private AIOldestAttackableTargetSorter theOldestAttackableTargetSorter;

   public AINearestButcherTarget(EntityGolemBase par1EntityLiving, int par4, boolean par5) {
      this(par1EntityLiving, 0.0F, par4, par5, false);
   }

   public AINearestButcherTarget(EntityGolemBase par1, float par3, int par4, boolean par5, boolean par6) {
      super(par1, par5, par6);
      this.targetDistance = 0.0F;
      this.theGolem = par1;
      this.targetDistance = 0.0F;
      this.targetChance = par4;
      this.theOldestAttackableTargetSorter = new AIOldestAttackableTargetSorter(this, par1);
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      this.targetDistance = this.theGolem.getRange();
      if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
         return false;
      } else {
         List<EntityLivingBase> var5 = this.taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class, this.taskOwner.getEntityBoundingBox().expand(this.targetDistance, 4.0F, this.targetDistance));
         var5.sort(this.theOldestAttackableTargetSorter);

         for(EntityLivingBase var4 : var5) {
            if (this.theGolem.isValidTarget(var4)) {
               this.target = var4;
               @SuppressWarnings("unchecked")
               List<EntityLivingBase> var55 = this.taskOwner.world.getEntitiesWithinAABB((Class<EntityLivingBase>)this.target.getClass(), this.taskOwner.getEntityBoundingBox().expand(this.targetDistance, 4.0F, this.targetDistance));
               Iterator<EntityLivingBase> var22 = var55.iterator();
               int count = 0;

               while(var22.hasNext()) {
                  Entity var33 = var22.next();
                  if (this.theGolem.isValidTarget(var33)) {
                     ++count;
                  }
               }

               if (count > 2) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void startExecuting() {
      this.taskOwner.setAttackTarget(this.target);
      super.startExecuting();
   }
}
