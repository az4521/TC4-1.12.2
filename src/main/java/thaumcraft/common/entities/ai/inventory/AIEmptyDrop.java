package thaumcraft.common.entities.ai.inventory;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;

public class AIEmptyDrop extends EntityAIBase {
   private EntityGolemBase theGolem;
   int count = 0;

   public AIEmptyDrop(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (this.theGolem.itemCarried != null && this.theGolem.getNavigator().noPath()) {
         BlockPos home = this.theGolem.getHomePosition();

         for(byte color : this.theGolem.getColorsMatching(this.theGolem.itemCarried)) {
            for(BlockPos cc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.theGolem.world, this.theGolem, color)) {
               if (!cc.equals(home)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return this.count > 0 && this.shouldExecute();
   }

   public void resetTask() {
       super.resetTask();
   }

   public void updateTask() {
      --this.count;
      super.updateTask();
   }

   public void startExecuting() {
      this.count = 200;
      BlockPos home = this.theGolem.getHomePosition();

      label24:
      for(byte color : this.theGolem.getColorsMatching(this.theGolem.itemCarried)) {
         for(BlockPos cc : GolemHelper.getMarkedBlocksAdjacentToGolem(this.theGolem.world, this.theGolem, color)) {
            if (!cc.equals(home)) {
               ItemStack carried = this.theGolem.itemCarried;
               if (carried != null && !carried.isEmpty()) {
                  EntityItem item = new EntityItem(this.theGolem.world, this.theGolem.posX, this.theGolem.posY + (double)(this.theGolem.height / 2.0F), this.theGolem.posZ, carried.copy());
                  double distance = this.theGolem.getDistance((double)cc.getX() + 0.5D, (double)cc.getY() + 0.5D, (double)cc.getZ() + 0.5D);
                  item.motionX = ((double)cc.getX() + 0.5D - this.theGolem.posX) * (distance / 3.0D);
                  item.motionY = 0.1D + ((double)cc.getY() + 0.5D - (this.theGolem.posY + (double)(this.theGolem.height / 2.0F))) * (distance / 3.0D);
                  item.motionZ = ((double)cc.getZ() + 0.5D - this.theGolem.posZ) * (distance / 3.0D);
                  item.setPickupDelay(10);
                  this.theGolem.world.spawnEntity(item);
                  this.theGolem.itemCarried = ItemStack.EMPTY;
                  this.theGolem.startActionTimer();
                  break label24;
               }
            }
         }
      }

      this.theGolem.updateCarried();
   }
}
