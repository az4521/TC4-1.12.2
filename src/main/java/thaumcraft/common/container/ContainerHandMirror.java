package thaumcraft.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.common.items.relics.ItemHandMirror;

public class ContainerHandMirror extends Container {
   private World world;
   private int posX;
   private int posY;
   private int posZ;
   public IInventory input = new InventoryHandMirror(this);
   ItemStack mirror = ItemStack.EMPTY;
   EntityPlayer player = null;

   public ContainerHandMirror(InventoryPlayer iinventory, World par2World, int par3, int par4, int par5) {
      this.world = par2World;
      this.posX = par3;
      this.posY = par4;
      this.posZ = par5;
      this.player = iinventory.player;
      this.mirror = iinventory.getCurrentItem();
      this.addSlotToContainer(new Slot(this.input, 0, 80, 24));
      this.bindPlayerInventory(iinventory);
      this.onCraftMatrixChanged(this.input);
   }

   protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int i = 0; i < 9; ++i) {
         this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
      }

   }

   public void onCraftMatrixChanged(IInventory par1IInventory) {
      if (!this.input.getStackInSlot(0).isEmpty() && ItemStack.areItemStacksEqual(this.input.getStackInSlot(0), this.mirror)) {
         this.player.openContainer = this.player.inventoryContainer;
      } else if (!this.world.isRemote && !this.input.getStackInSlot(0).isEmpty() && this.player != null && ItemHandMirror.transport(this.mirror, this.input.getStackInSlot(0), this.player, this.world)) {
         this.input.setInventorySlotContents(0, ItemStack.EMPTY);

          for (IContainerListener listener : this.listeners) {
              listener.sendSlotContents(this, 0, ItemStack.EMPTY);
          }
      }

   }

   public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot) {
      ItemStack stack = ItemStack.EMPTY;
      Slot slotObject = (Slot)this.inventorySlots.get(slot);
      if (slotObject != null && slotObject.getHasStack() && !(slotObject.getStack().getItem() instanceof ItemHandMirror)) {
         ItemStack stackInSlot = slotObject.getStack();
         stack = stackInSlot.copy();
         if (slot == 0) {
            if (!this.mergeItemStack(stackInSlot, 1, this.inventorySlots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(stackInSlot, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (stackInSlot.isEmpty()) {
            slotObject.putStack(ItemStack.EMPTY);
         } else {
            slotObject.onSlotChanged();
         }
      }

      return stack;
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return true;
   }

   public void onContainerClosed(EntityPlayer par1EntityPlayer) {
      super.onContainerClosed(par1EntityPlayer);
      if (!this.world.isRemote) {
         for(int var2 = 0; var2 < 1; ++var2) {
            ItemStack var3 = this.input.removeStackFromSlot(var2);
            if (!var3.isEmpty()) {
               par1EntityPlayer.dropItem(var3, false);
            }
         }
      }

   }
}
