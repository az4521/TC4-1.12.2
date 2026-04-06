package thaumcraft.common.entities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import thaumcraft.common.entities.monster.EntityPech;

public class InventoryPech implements IInventory {
   private final EntityPech theMerchant;
   private ItemStack[] theInventory = new ItemStack[5];
   private final EntityPlayer thePlayer;
   private Container eventHandler;

   public InventoryPech(EntityPlayer par1EntityPlayer, EntityPech par2IMerchant, Container par1Container) {
      this.thePlayer = par1EntityPlayer;
      this.theMerchant = par2IMerchant;
      this.eventHandler = par1Container;
      for (int i = 0; i < theInventory.length; i++) {
         theInventory[i] = ItemStack.EMPTY;
      }
   }

   public int getSizeInventory() {
      return this.theInventory.length;
   }

   public boolean isEmpty() {
      for (ItemStack stack : this.theInventory) {
         if (!stack.isEmpty()) return false;
      }
      return true;
   }

   public ItemStack getStackInSlot(int par1) {
      ItemStack stack = this.theInventory[par1];
      return stack != null ? stack : ItemStack.EMPTY;
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (!this.theInventory[par1].isEmpty()) {
         ItemStack var3;
         if (this.theInventory[par1].getCount() <= par2) {
            var3 = this.theInventory[par1];
            this.theInventory[par1] = ItemStack.EMPTY;
         } else {
            var3 = this.theInventory[par1].splitStack(par2);
            if (this.theInventory[par1].isEmpty()) {
               this.theInventory[par1] = ItemStack.EMPTY;
            }
         }
         this.eventHandler.onCraftMatrixChanged(this);
         return var3;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeStackFromSlot(int index) {
      if (!this.theInventory[index].isEmpty()) {
         ItemStack stack = this.theInventory[index];
         this.theInventory[index] = ItemStack.EMPTY;
         return stack;
      }
      return ItemStack.EMPTY;
   }

   public ItemStack getStackInSlotOnClosing(int par1) {
      if (!this.theInventory[par1].isEmpty()) {
         ItemStack itemstack = this.theInventory[par1];
         this.theInventory[par1] = ItemStack.EMPTY;
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.theInventory[par1] = par2ItemStack == null ? ItemStack.EMPTY : par2ItemStack;
      if (!par2ItemStack.isEmpty() && par2ItemStack.getCount() > this.getInventoryStackLimit()) {
         par2ItemStack.setCount(this.getInventoryStackLimit());
      }
      this.eventHandler.onCraftMatrixChanged(this);
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.theMerchant.isTamed();
   }

   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return par1 == 0;
   }

   public String getName() {
      return "entity.Pech.name";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public void markDirty() {
      this.eventHandler.onCraftMatrixChanged(this);
   }

   public void openInventory(EntityPlayer player) {}

   public void closeInventory(EntityPlayer player) {}

   public int getField(int id) { return 0; }

   public void setField(int id, int value) {}

   public int getFieldCount() { return 0; }

   public void clear() {
      for (int i = 0; i < this.theInventory.length; i++) {
         this.theInventory[i] = ItemStack.EMPTY;
      }
   }
}
