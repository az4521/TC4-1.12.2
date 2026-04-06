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

public class AIHomeReplace extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int countChest = 0;
   private IInventory inv;

   public AIHomeReplace(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      BlockPos home = this.theGolem.getHomePosition();
      if (!this.theGolem.getCarried().isEmpty() && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.getNavigator().noPath() && !(this.theGolem.getDistanceSq((float)home.getX() + 0.5F, (float)home.getY() + 0.5F, (float)home.getZ() + 0.5F) > (double)5.0F)) {
         EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
         int cX = home.getX() - facing.getXOffset();
         int cY = home.getY() - facing.getYOffset();
         int cZ = home.getZ() - facing.getZOffset();
         if (GolemHelper.isOnTimeOut(this.theGolem, this.theGolem.getCarried())) {
            return true;
         } else {
            switch (this.theGolem.getCore()) {
               case 1:
                  return !GolemHelper.findSomethingEmptyCore(this.theGolem, this.theGolem.getCarried());
               case 8:
                  return !GolemHelper.findSomethingUseCore(this.theGolem, this.theGolem.getCarried());
               case 10:
                  return !GolemHelper.findSomethingSortCore(this.theGolem, this.theGolem.getCarried());
               default:
                  this.theGolem.world.getTileEntity(new BlockPos(cX, cY, cZ));
                  ArrayList<ItemStack> neededList = GolemHelper.getItemsNeeded(this.theGolem, this.theGolem.getUpgradeAmount(5) > 0);
                  if (neededList != null && !neededList.isEmpty()) {
                     for(ItemStack stack : neededList) {
                        if (InventoryUtils.areItemStacksEqual(stack, this.theGolem.itemCarried, this.theGolem.checkOreDict(), this.theGolem.ignoreDamage(), this.theGolem.ignoreNBT())) {
                           return false;
                        }
                     }

                     return true;
                  } else {
                     return false;
                  }
            }
         }
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
