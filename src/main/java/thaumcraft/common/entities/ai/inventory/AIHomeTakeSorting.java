package thaumcraft.common.entities.ai.inventory;

import java.util.ArrayList;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;
import thaumcraft.common.lib.utils.InventoryUtils;

public class AIHomeTakeSorting extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int countChest = 0;
   private IInventory inv;
   int count = 0;

   public AIHomeTakeSorting(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      BlockPos home = this.theGolem.getHomePosition();
      if (this.theGolem.getCarried().isEmpty() && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.getNavigator().noPath() && !(this.theGolem.getDistanceSq((float)home.getX() + 0.5F, (float)home.getY() + 0.5F, (float)home.getZ() + 0.5F) > (double)5.0F)) {
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
               ArrayList<ItemStack> neededList = GolemHelper.getItemsNeeded(this.theGolem, this.theGolem.getUpgradeAmount(5) > 0);
               if (neededList != null && !neededList.isEmpty()) {
                  for(ItemStack stack : neededList) {
                     ItemStack needed = stack.copy();
                     needed.setCount(this.theGolem.getCarrySpace());
                     if (!InventoryUtils.extractStack((IInventory)tile, needed, facing.ordinal(), this.theGolem.checkOreDict(), this.theGolem.ignoreDamage(), this.theGolem.ignoreNBT(), false).isEmpty()) {
                        return true;
                     }
                  }
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
      TileEntity tile = this.theGolem.world.getTileEntity(new BlockPos(cX, cY, cZ));
      boolean repeat = true;
      boolean didRepeat = false;

      while(repeat) {
         if (didRepeat) {
            repeat = false;
         }

         if (tile instanceof IInventory) {
            ArrayList<ItemStack> neededList = GolemHelper.getItemsNeeded(this.theGolem, this.theGolem.getUpgradeAmount(5) > 0);
            if (neededList != null && !neededList.isEmpty()) {
               for(ItemStack stack : neededList) {
                  ItemStack needed = stack.copy();
                  needed.setCount(this.theGolem.getCarrySpace());
                  ItemStack result = InventoryUtils.extractStack((IInventory)tile, needed, facing.ordinal(), this.theGolem.checkOreDict(), this.theGolem.ignoreDamage(), this.theGolem.ignoreNBT(), true);
                  if (!result.isEmpty()) {
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
            }

            if (!this.theGolem.getCarried().isEmpty()) {
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
