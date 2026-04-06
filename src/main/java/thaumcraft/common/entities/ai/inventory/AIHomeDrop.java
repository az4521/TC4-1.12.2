package thaumcraft.common.entities.ai.inventory;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class AIHomeDrop extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int countChest = 0;
   private IInventory inv;
   int count = 0;

   public AIHomeDrop(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      BlockPos home = this.theGolem.getHomePosition();
      if (this.theGolem.getCarried() != null && this.theGolem.getNavigator().noPath() && !(this.theGolem.getDistanceSq((double)home.getX() + 0.5D, (double)home.getY() + 0.5D, (double)home.getZ() + 0.5D) > 5.0D)) {
         EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
         int cX = home.getX() - facing.getXOffset();
         int cY = home.getY() - facing.getYOffset();
         int cZ = home.getZ() - facing.getZOffset();
         TileEntity tile = this.theGolem.world.getTileEntity(new BlockPos(cX, cY, cZ));
         return !(tile instanceof IInventory);
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return this.count > 0 && (this.shouldExecute() || this.countChest > 0);
   }

   public void resetTask() {
      try {
         if (this.inv != null && Config.golemChestInteract) {
            this.inv.closeInventory(null);
         }
      } catch (Exception ignored) {
      }

   }

   public void updateTask() {
      --this.countChest;
      --this.count;
      super.updateTask();
   }

   public void startExecuting() {
      this.count = 200;
      BlockPos home = this.theGolem.getHomePosition();
      EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      ItemStack carried = this.theGolem.itemCarried;
      if (carried != null && !carried.isEmpty()) {
         EntityItem item = new EntityItem(this.theGolem.world, this.theGolem.posX, this.theGolem.posY + (double)(this.theGolem.height / 2.0F), this.theGolem.posZ, carried.copy());
         double distance = this.theGolem.getDistance((double)cX + 0.5D, (double)cY + 0.5D, (double)cZ + 0.5D);
         item.motionX = ((double)cX + 0.5D - this.theGolem.posX) * (distance / 3.0D);
         item.motionY = 0.1D + ((double)cY + 0.5D - (this.theGolem.posY + (double)(this.theGolem.height / 2.0F))) * (distance / 3.0D);
         item.motionZ = ((double)cZ + 0.5D - this.theGolem.posZ) * (distance / 3.0D);
         item.setPickupDelay(10);
         this.theGolem.world.spawnEntity(item);
         this.theGolem.itemCarried = ItemStack.EMPTY;
         this.theGolem.startActionTimer();
         this.theGolem.updateCarried();
      }
   }
}
