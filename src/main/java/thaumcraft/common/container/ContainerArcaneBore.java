package thaumcraft.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.wands.foci.ItemFocusExcavation;
import thaumcraft.common.tiles.TileArcaneBore;

public class ContainerArcaneBore extends Container {
   private TileArcaneBore tileEntity;

   public ContainerArcaneBore(InventoryPlayer iinventory, TileArcaneBore e) {
      this.tileEntity = e;
      this.addSlotToContainer(new SlotLimitedByClass(ItemFocusExcavation.class, e, 0, 26, 18));
      this.addSlotToContainer(new SlotLimitedByClass(ItemPickaxe.class, e, 1, 74, 18));
      this.bindPlayerInventory(iinventory);
   }

   protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 59 + i * 18));
         }
      }

      for(int i = 0; i < 9; ++i) {
         this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 117));
      }

   }

   public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
      return this.tileEntity.getWorld().getTileEntity(this.tileEntity.getPos()) == this.tileEntity && par1EntityPlayer.getDistanceSq((double) this.tileEntity.getPos().getX() + 0.5D, (double) this.tileEntity.getPos().getY() + 0.5D, (double) this.tileEntity.getPos().getZ() + 0.5D) <= 64.0D;
   }

   public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot) {
      ItemStack stack = ItemStack.EMPTY;
      Slot slotObject = (Slot)this.inventorySlots.get(slot);
      if (slotObject != null && slotObject.getHasStack()) {
         ItemStack stackInSlot = slotObject.getStack();
         stack = stackInSlot.copy();
         if (slot <= 1) {
            if (!this.mergeItemStack(stackInSlot, 2, this.inventorySlots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (slot > 1) {
            if (stackInSlot.getItem() instanceof ItemFocusExcavation) {
               if (!this.mergeItemStack(stackInSlot, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (stackInSlot.getItem() instanceof ItemPickaxe && !this.mergeItemStack(stackInSlot, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(stackInSlot, 2, 38, false)) {
            return ItemStack.EMPTY;
         }

         if (stackInSlot.isEmpty()) {
            slotObject.putStack(ItemStack.EMPTY);
         } else {
            slotObject.onSlotChanged();
         }

         if (stackInSlot.getCount() == stack.getCount()) {
            return ItemStack.EMPTY;
         }
      }

      return stack;
   }
}
