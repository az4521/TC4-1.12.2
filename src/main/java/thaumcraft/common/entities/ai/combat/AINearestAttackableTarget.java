package thaumcraft.common.entities.ai.combat;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class AINearestAttackableTarget extends EntityAITarget {
   EntityGolemBase theGolem;
   EntityLivingBase target;
   int targetChance;
   private final Predicate<Entity> entitySelector;
   private float targetDistance;
   private AINearestAttackableTargetSorter theNearestAttackableTargetSorter;

   public AINearestAttackableTarget(EntityGolemBase par1EntityLiving, int par4, boolean par5) {
      this(par1EntityLiving, 0.0F, par4, par5, false, null);
   }

   public AINearestAttackableTarget(EntityGolemBase par1, float par3, int par4, boolean par5, boolean par6, Predicate<Entity> par7IEntitySelector) {
      super(par1, par5, par6);
      this.targetDistance = 0.0F;
      this.theGolem = par1;
      this.targetDistance = 0.0F;
      this.targetChance = par4;
      this.theNearestAttackableTargetSorter = new AINearestAttackableTargetSorter(this, par1);
      this.entitySelector = par7IEntitySelector;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      this.targetDistance = this.theGolem.getRange();
      if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
         return false;
      } else {
         List<EntityLivingBase> var5 = this.taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class, this.taskOwner.getEntityBoundingBox().expand(this.targetDistance, 4.0F, this.targetDistance), e -> this.entitySelector == null || this.entitySelector.test(e));
         var5.sort(this.theNearestAttackableTargetSorter);

         for(EntityLivingBase var4 : var5) {
            if (this.theGolem.isValidTarget(var4)) {
               this.target = var4;
               return true;
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
