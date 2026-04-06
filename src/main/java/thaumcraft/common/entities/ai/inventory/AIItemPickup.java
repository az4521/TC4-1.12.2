package thaumcraft.common.entities.ai.inventory;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.lib.utils.InventoryUtils;

public class AIItemPickup extends EntityAIBase {
   private EntityGolemBase theGolem;
   private Entity targetEntity;
   int count = 0;

   public AIItemPickup(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      return this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.findItem();
   }

   private boolean findItem() {
      double range = Double.MAX_VALUE;
      float dmod = this.theGolem.getRange();
      List<Entity> targets = this.theGolem.world.getEntitiesWithinAABBExcludingEntity(this.theGolem, new AxisAlignedBB(this.theGolem.getHomePosition().getX(), this.theGolem.getHomePosition().getY(), this.theGolem.getHomePosition().getZ(), this.theGolem.getHomePosition().getX() + 1, this.theGolem.getHomePosition().getY() + 1, this.theGolem.getHomePosition().getZ() + 1).expand(dmod, dmod, dmod));
      if (targets.isEmpty()) {
         return false;
      } else {
         for(Entity e : targets) {
            if (e instanceof EntityItem && !((EntityItem)e).cannotPickup() && (this.theGolem.inventory.allEmpty() || this.theGolem.inventory.getAmountNeededSmart(((EntityItem)e).getItem(), this.theGolem.getUpgradeAmount(5) > 0) > 0) && (this.theGolem.getCarried().isEmpty() || InventoryUtils.areItemStacksEqualStrict(this.theGolem.getCarried(), ((EntityItem)e).getItem()) && ((EntityItem)e).getItem().getCount() <= this.theGolem.getCarrySpace())) {
               double distance = e.getDistanceSq((float)this.theGolem.getHomePosition().getX() + 0.5F, (float)this.theGolem.getHomePosition().getY() + 0.5F, (float)this.theGolem.getHomePosition().getZ() + 0.5F);
               double distance2 = e.getDistanceSq(this.theGolem.posX, this.theGolem.posY, this.theGolem.posZ);
               if (distance2 < range && distance <= (double)(dmod * dmod)) {
                  range = distance2;
                   if (e.isEntityAlive()) {
                       this.targetEntity = e;
                   }
               }
            }
         }

          return this.targetEntity != null;
      }
   }

   public boolean continueExecuting() {
      return this.count-- > 0 && !this.theGolem.getNavigator().noPath() && this.targetEntity.isEntityAlive();
   }

   public void resetTask() {
      this.count = 0;
      this.targetEntity = null;
      this.theGolem.getNavigator().clearPath();
   }

   public void updateTask() {
      this.theGolem.getLookHelper().setLookPositionWithEntity(this.targetEntity, 30.0F, 30.0F);
      double dist = this.theGolem.getDistanceSq(this.targetEntity);
      if (dist <= (double)2.0F) {
         this.pickUp();
      }

   }

   private void pickUp() {
      int amount = 0;
      if (this.targetEntity instanceof EntityItem) {
         ItemStack stack = ((EntityItem)this.targetEntity).getItem().copy();
         if (((EntityItem)this.targetEntity).getItem().getCount() < this.theGolem.getCarrySpace()) {
            amount = ((EntityItem)this.targetEntity).getItem().getCount();
         } else {
            amount = this.theGolem.getCarrySpace();
         }

         stack.setCount(amount);
         ((EntityItem)this.targetEntity).getItem().shrink(amount);
         if (this.theGolem.getCarried().isEmpty()) {
            this.theGolem.setCarried(stack);
         } else {
            this.theGolem.getCarried().grow(amount);
         }
      }

      if (amount != 0) {
         this.targetEntity.world.playSound(null, this.targetEntity.getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.2F, ((this.targetEntity.world.rand.nextFloat() - this.targetEntity.world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
      }
   }

   public void startExecuting() {
      this.count = 200;
      this.theGolem.getNavigator().tryMoveToEntityLiving(this.targetEntity, this.theGolem.getAIMoveSpeed());
   }
}
