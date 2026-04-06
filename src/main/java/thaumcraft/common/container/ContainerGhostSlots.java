package thaumcraft.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGhostSlots extends Container {
   public boolean canInteractWith(EntityPlayer entityplayer) {
      return false;
   }

   public ItemStack slotClick(int slotClicked, int button, ClickType mod, EntityPlayer player) {
      ItemStack itemstack = ItemStack.EMPTY;
      InventoryPlayer inventoryplayer = player.inventory;
      if ((mod == ClickType.PICKUP || mod == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
         if (slotClicked == -999) {
            if (!inventoryplayer.getItemStack().isEmpty() && slotClicked == -999) {
               if (button == 0) {
                  player.dropItem(inventoryplayer.getItemStack(), false);
                  inventoryplayer.setItemStack(ItemStack.EMPTY);
               }

               if (button == 1) {
                  player.dropItem(inventoryplayer.getItemStack().splitStack(1), false);
                  if (inventoryplayer.getItemStack().getCount() == 0) {
                     inventoryplayer.setItemStack(ItemStack.EMPTY);
                  }
               }
            }
         } else if (mod == ClickType.QUICK_MOVE) {
            if (slotClicked < 0) {
               return ItemStack.EMPTY;
            }

            Slot slot2 = (Slot)this.inventorySlots.get(slotClicked);
            if (slot2 != null && !slot2.getStack().isEmpty() && slot2 instanceof SlotGhost) {
               if (button == 0) {
                  slot2.putStack(ItemStack.EMPTY);
               } else if (button == 1) {
                  ItemStack slotStack = slot2.getStack();
                  slotStack.grow(16);
                  if (slotStack.getCount() > slot2.getSlotStackLimit()) {
                     slotStack.setCount(slot2.getSlotStackLimit());
                  }
               }
            }
         } else {
            if (slotClicked < 0) {
               return ItemStack.EMPTY;
            }

            Slot slot2 = (Slot)this.inventorySlots.get(slotClicked);
            if (slot2 != null) {
               ItemStack slotStack = slot2.getStack();
               ItemStack playerStack = inventoryplayer.getItemStack();
               if (!slotStack.isEmpty()) {
                  itemstack = slotStack.copy();
               }

               if (slotStack.isEmpty()) {
                  if (!playerStack.isEmpty() && slot2.isItemValid(playerStack)) {
                     int k1 = button == 0 ? playerStack.getCount() : 1;
                     if (k1 > slot2.getSlotStackLimit()) {
                        k1 = slot2.getSlotStackLimit();
                     }

                     if (playerStack.getCount() >= k1) {
                        if (slot2 instanceof SlotGhost) {
                           ItemStack ic = playerStack.copy();
                           ic.setCount(k1);
                           slot2.putStack(ic);
                        } else {
                           slot2.putStack(playerStack.splitStack(k1));
                        }
                     }

                     if (!(slot2 instanceof SlotGhost) && playerStack.getCount() == 0) {
                        inventoryplayer.setItemStack(ItemStack.EMPTY);
                     }
                  }
               } else if (slot2.canTakeStack(player) || slot2 instanceof SlotGhost) {
                  if (playerStack.isEmpty()) {
                     if (slot2 instanceof SlotGhost) {
                        int k1 = button == 0 ? 1 : -1;
                        if (slotStack.getCount() - k1 <= slot2.getSlotStackLimit()) {
                           slot2.decrStackSize(k1);
                        }

                        if (slotStack.getCount() == 0) {
                           slot2.putStack(ItemStack.EMPTY);
                        }
                     } else {
                        int k1 = button == 0 ? slotStack.getCount() : (slotStack.getCount() + 1) / 2;
                        ItemStack itemstack3 = slot2.decrStackSize(k1);
                        inventoryplayer.setItemStack(itemstack3);
                        if (slotStack.getCount() == 0) {
                           slot2.putStack(ItemStack.EMPTY);
                        }

                        slot2.onTake(player, inventoryplayer.getItemStack());
                     }
                  } else if (slot2.isItemValid(playerStack)) {
                     if (slotStack.getItem() == playerStack.getItem() && slotStack.getItemDamage() == playerStack.getItemDamage() && ItemStack.areItemStackTagsEqual(slotStack, playerStack)) {
                        int k1 = button == 0 ? playerStack.getCount() : 1;
                        if (k1 > slot2.getSlotStackLimit() - slotStack.getCount()) {
                           k1 = slot2.getSlotStackLimit() - slotStack.getCount();
                        }

                        if (k1 > playerStack.getMaxStackSize() - slotStack.getCount()) {
                           k1 = playerStack.getMaxStackSize() - slotStack.getCount();
                        }

                        if (!(slot2 instanceof SlotGhost)) {
                           playerStack.splitStack(k1);
                           if (playerStack.getCount() == 0) {
                              inventoryplayer.setItemStack(ItemStack.EMPTY);
                           }
                        }

                        slotStack.grow(k1);
                     } else if (playerStack.getCount() <= slot2.getSlotStackLimit()) {
                        slot2.putStack(playerStack.copy());
                        if (!(slot2 instanceof SlotGhost)) {
                           inventoryplayer.setItemStack(slotStack);
                        }
                     }
                  } else if (slotStack.getItem() == playerStack.getItem() && playerStack.getMaxStackSize() > 1 && (!slotStack.getHasSubtypes() || slotStack.getItemDamage() == playerStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(slotStack, playerStack)) {
                     int k1 = slotStack.getCount();
                     if (k1 > 0 && k1 + playerStack.getCount() <= playerStack.getMaxStackSize()) {
                        if (!(slot2 instanceof SlotGhost)) {
                           playerStack.grow(k1);
                        }

                        slotStack = slot2.decrStackSize(k1);
                        if (slotStack.getCount() == 0) {
                           slot2.putStack(ItemStack.EMPTY);
                        }

                        if (!(slot2 instanceof SlotGhost)) {
                           slot2.onTake(player, inventoryplayer.getItemStack());
                        }
                     }
                  }
               }

               slot2.onSlotChanged();
            }
         }
      } else if (mod == ClickType.SWAP && button >= 0 && button < 9) {
         Slot slot2 = (Slot)this.inventorySlots.get(slotClicked);
         if (slot2.canTakeStack(player)) {
            ItemStack slotStack = inventoryplayer.getStackInSlot(button);
            boolean flag = slotStack.isEmpty() || slot2.inventory == inventoryplayer && slot2.isItemValid(slotStack);
            int k1 = -1;
            if (!flag) {
               k1 = inventoryplayer.getFirstEmptyStack();
               flag |= k1 > -1;
            }

            if (slot2.getHasStack() && flag) {
               ItemStack itemstack3 = slot2.getStack();
               if (!(slot2 instanceof SlotGhost)) {
                  inventoryplayer.setInventorySlotContents(button, itemstack3.copy());
               }

               if ((slot2.inventory != inventoryplayer || !slot2.isItemValid(slotStack)) && !slotStack.isEmpty()) {
                  if (k1 > -1) {
                     if (!(slot2 instanceof SlotGhost)) {
                        inventoryplayer.addItemStackToInventory(slotStack);
                     }

                     slot2.decrStackSize(itemstack3.getCount());
                     slot2.putStack(ItemStack.EMPTY);
                     if (!(slot2 instanceof SlotGhost)) {
                        slot2.onTake(player, itemstack3);
                     }
                  }
               } else {
                  slot2.decrStackSize(itemstack3.getCount());
                  slot2.putStack(slotStack);
                  if (!(slot2 instanceof SlotGhost)) {
                     slot2.onTake(player, itemstack3);
                  }
               }
            } else if (!slot2.getHasStack() && !slotStack.isEmpty() && slot2.isItemValid(slotStack)) {
               if (!(slot2 instanceof SlotGhost)) {
                  inventoryplayer.setInventorySlotContents(button, ItemStack.EMPTY);
               }

               slot2.putStack(slotStack);
            }
         }
      } else if (mod == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack().isEmpty() && slotClicked >= 0) {
         Slot slot2 = (Slot)this.inventorySlots.get(slotClicked);
         if (slot2 != null && slot2.getHasStack()) {
            ItemStack slotStack = slot2.getStack().copy();
            slotStack.setCount(slotStack.getMaxStackSize());
            if (!(slot2 instanceof SlotGhost)) {
               inventoryplayer.setItemStack(slotStack);
            }
         }
      } else if (mod == ClickType.CLONE) {
         Slot slot2 = (Slot)this.inventorySlots.get(slotClicked);
         if (slot2 instanceof SlotGhost && button == 3) {
            slot2.putStack(ItemStack.EMPTY);
         }
      } else if (mod == ClickType.THROW && inventoryplayer.getItemStack().isEmpty() && slotClicked >= 0) {
         Slot slot2 = (Slot)this.inventorySlots.get(slotClicked);
         if (slot2 != null && slot2.getHasStack() && (slot2.canTakeStack(player) || slot2 instanceof SlotGhost)) {
            ItemStack slotStack = slot2.decrStackSize(button == 0 ? 1 : slot2.getStack().getCount());
            if (!(slot2 instanceof SlotGhost)) {
               slot2.onTake(player, slotStack);
               player.dropItem(slotStack, false);
            }
         }
      } else if (mod == ClickType.PICKUP_ALL && slotClicked >= 0) {
         Slot slot2 = (Slot)this.inventorySlots.get(slotClicked);
         ItemStack slotStack = inventoryplayer.getItemStack();
         if (!slotStack.isEmpty() && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player))) {
            int l = button == 0 ? 0 : this.inventorySlots.size() - 1;
            int k1 = button == 0 ? 1 : -1;

            for(int l1 = 0; l1 < 2; ++l1) {
               for(int i2 = l; i2 >= 0 && i2 < this.inventorySlots.size() && slotStack.getCount() < slotStack.getMaxStackSize(); i2 += k1) {
                  Slot slot3 = (Slot)this.inventorySlots.get(i2);
                  if (!(slot3 instanceof SlotGhost) && !(slot3 instanceof SlotGhostFluid) && slot3.getHasStack() && canAddItemToSlot(slot3, slotStack, true) && slot3.canTakeStack(player) && this.canMergeSlot(slotStack, slot3) && (l1 != 0 || slot3.getStack().getCount() != slot3.getStack().getMaxStackSize())) {
                     int j2 = Math.min(slotStack.getMaxStackSize() - slotStack.getCount(), slot3.getStack().getCount());
                     ItemStack itemstack5 = slot3.decrStackSize(j2);
                     if (!(slot2 instanceof SlotGhost)) {
                        slotStack.grow(j2);
                     }

                     if (itemstack5.getCount() <= 0) {
                        slot3.putStack(ItemStack.EMPTY);
                     }

                     if (!(slot2 instanceof SlotGhost)) {
                        slot3.onTake(player, itemstack5);
                     }
                  }
               }
            }
         }

         this.detectAndSendChanges();
      }

      return itemstack;
   }
}
