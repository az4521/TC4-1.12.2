package thaumcraft.common.entities.ai.inventory;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.lib.utils.InventoryUtils;

public class AIHomePlace extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int countChest = 0;
   private IInventory inv;

   public AIHomePlace(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      BlockPos home = this.theGolem.getHomePosition();
      if (this.theGolem.getCarried() != null && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.getNavigator().noPath() && !(this.theGolem.getDistanceSq((double)home.getX() + 0.5D, (double)home.getY() + 0.5D, (double)home.getZ() + 0.5D) > 5.0D)) {
         EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
         int cX = home.getX() - facing.getXOffset();
         int cY = home.getY() - facing.getYOffset();
         int cZ = home.getZ() - facing.getZOffset();
         TileEntity tile = this.theGolem.world.getTileEntity(new BlockPos(cX, cY, cZ));
         boolean repeat = true;
         boolean didRepeat = false;

         while(repeat) {
            if (didRepeat) {
               repeat = false;
            }

            if (tile instanceof IInventory) {
               ItemStack result = InventoryUtils.placeItemStackIntoInventory(this.theGolem.getCarried(), (IInventory)tile, facing.ordinal(), false);
               if (!ItemStack.areItemStacksEqual(result, this.theGolem.itemCarried)) {
                  return true;
               }
            }

            if (!didRepeat && InventoryUtils.getDoubleChest(tile) != null) {
               tile = InventoryUtils.getDoubleChest(tile);
               didRepeat = true;
            } else {
               repeat = false;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return this.shouldExecute() || this.countChest > 0;
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
      super.updateTask();
   }

   public void startExecuting() {
      BlockPos home = this.theGolem.getHomePosition();
      EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      TileEntity tile = this.theGolem.world.getTileEntity(new BlockPos(cX, cY, cZ));
      boolean repeat = true;
      boolean didRepeat = false;

      while(repeat) {
         if (didRepeat) {
            repeat = false;
         }

         if (tile instanceof IInventory) {
            ItemStack result = InventoryUtils.placeItemStackIntoInventory(this.theGolem.getCarried(), (IInventory)tile, facing.ordinal(), true);
            if (!ItemStack.areItemStacksEqual(result, this.theGolem.itemCarried)) {
               this.theGolem.setCarried(result);

               try {
                  if (Config.golemChestInteract) {
                     ((IInventory)tile).openInventory(null);
                  }
               } catch (Exception ignored) {
               }

               this.countChest = 5;
               this.inv = (IInventory)tile;
               break;
            }
         }

         if (!didRepeat && InventoryUtils.getDoubleChest(tile) != null) {
            tile = InventoryUtils.getDoubleChest(tile);
            didRepeat = true;
         } else {
            repeat = false;
         }
      }

   }
}
