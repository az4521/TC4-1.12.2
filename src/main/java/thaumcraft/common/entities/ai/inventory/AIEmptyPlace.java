package thaumcraft.common.entities.ai.inventory;

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

public class AIEmptyPlace extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int countChest = 0;
   private IInventory inv;
   private int xx;
   private int yy;
   private int zz;
   int count = 0;

   public AIEmptyPlace(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (this.theGolem.itemCarried != null && this.theGolem.getNavigator().noPath()) {
         BlockPos home = this.theGolem.getHomePosition();
         EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
         int cX = home.getX() - facing.getXOffset();
         int cY = home.getY() - facing.getYOffset();
         int cZ = home.getZ() - facing.getZOffset();

         for(IInventory te : GolemHelper.getMarkedContainersAdjacentToGolem(this.theGolem.world, this.theGolem)) {
            TileEntity tile = (TileEntity)te;
            if (tile != null && (tile.getPos().getX() != cX || tile.getPos().getY() != cY || tile.getPos().getZ() != cZ)) {
               for(byte color : this.theGolem.getColorsMatching(this.theGolem.itemCarried)) {
                  for(Integer side : GolemHelper.getMarkedSides(this.theGolem, tile, color)) {
                     ItemStack is = InventoryUtils.placeItemStackIntoInventory(this.theGolem.itemCarried, te, side, false);
                     if (!ItemStack.areItemStacksEqual(is, this.theGolem.itemCarried)) {
                        this.xx = tile.getPos().getX();
                        this.yy = tile.getPos().getY();
                        this.zz = tile.getPos().getZ();
                        return true;
                     }
                  }

                  if (InventoryUtils.getDoubleChest(tile) != null) {
                     for(Integer side : GolemHelper.getMarkedSides(this.theGolem, tile, color)) {
                        ItemStack is = InventoryUtils.placeItemStackIntoInventory(this.theGolem.itemCarried, InventoryUtils.getDoubleChest(tile), side, false);
                        if (ItemStack.areItemStacksEqual(is, this.theGolem.itemCarried)) {
                           this.xx = tile.getPos().getX();
                           this.yy = tile.getPos().getY();
                           this.zz = tile.getPos().getZ();
                           return true;
                        }
                     }
                  }
               }
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
      TileEntity tile = this.theGolem.world.getTileEntity(new BlockPos(this.xx, this.yy, this.zz));
      if (tile != null && (tile.getPos().getX() != cX || tile.getPos().getY() != cY || tile.getPos().getZ() != cZ)) {
         IInventory te = (IInventory)tile;

         for(byte color : this.theGolem.getColorsMatching(this.theGolem.itemCarried)) {
            for(Integer side : GolemHelper.getMarkedSides(this.theGolem, tile, color)) {
               this.theGolem.itemCarried = InventoryUtils.placeItemStackIntoInventory(this.theGolem.itemCarried, te, side, true);
               this.countChest = 5;
               this.inv = (IInventory)tile;
               if (this.theGolem.itemCarried == null || this.theGolem.itemCarried.isEmpty()) {
                  break;
               }
            }

            if (InventoryUtils.getDoubleChest(tile) != null && this.theGolem.itemCarried != null && !this.theGolem.itemCarried.isEmpty()) {
               for(Integer side : GolemHelper.getMarkedSides(this.theGolem, tile, color)) {
                  ItemStack is = InventoryUtils.placeItemStackIntoInventory(this.theGolem.itemCarried, InventoryUtils.getDoubleChest(tile), side, false);
                  if (!ItemStack.areItemStacksEqual(is, this.theGolem.itemCarried)) {
                     this.theGolem.itemCarried = InventoryUtils.placeItemStackIntoInventory(this.theGolem.itemCarried, InventoryUtils.getDoubleChest(tile), side, true);
                     this.countChest = 5;
                     this.inv = InventoryUtils.getDoubleChest(tile);
                     if (this.theGolem.itemCarried == null || this.theGolem.itemCarried.isEmpty()) {
                        break;
                     }
                  }
               }
            }

            if (this.countChest == 5) {
               try {
                  if (Config.golemChestInteract) {
                     ((IInventory)tile).openInventory(null);
                  }
               } catch (Exception ignored) {
               }
               break;
            }
         }
      }

      this.theGolem.updateCarried();
   }
}
