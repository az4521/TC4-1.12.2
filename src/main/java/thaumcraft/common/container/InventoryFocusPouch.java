package thaumcraft.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import thaumcraft.api.wands.ItemFocusBasic;

public class InventoryFocusPouch implements IInventory {
   public ItemStack[] stackList = new ItemStack[18];
   private Container eventHandler;

   public InventoryFocusPouch(Container par1Container) {
      this.eventHandler = par1Container;
   }

   public int getSizeInventory() {
      return this.stackList.length;
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack stack : this.stackList) {
         if (stack != null && !stack.isEmpty()) return false;
      }
      return true;
   }

   public ItemStack getStackInSlot(int par1) {
      return par1 >= this.getSizeInventory() ? ItemStack.EMPTY : (this.stackList[par1] == null ? ItemStack.EMPTY : this.stackList[par1]);
   }

   @Override
   public ItemStack removeStackFromSlot(int par1) {
      if (this.stackList[par1] != null) {
         ItemStack var2 = this.stackList[par1];
         this.stackList[par1] = null;
         return var2;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (this.stackList[par1] != null) {
          ItemStack var3;
          if (this.stackList[par1].getCount() <= par2) {
              var3 = this.stackList[par1];
            this.stackList[par1] = null;
          } else {
              var3 = this.stackList[par1].splitStack(par2);
            if (this.stackList[par1].getCount() == 0) {
               this.stackList[par1] = null;
            }

          }
          this.eventHandler.onCraftMatrixChanged(this);
          return var3;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.stackList[par1] = par2ItemStack;
      this.eventHandler.onCraftMatrixChanged(this);
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   @Override
   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return true;
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return itemstack != null && !itemstack.isEmpty() && itemstack.getItem() instanceof ItemFocusBasic;
   }

   @Override
   public String getName() {
      return "container.focuspouch";
   }

   @Override
   public boolean hasCustomName() {
      return false;
   }

   @Override
   public ITextComponent getDisplayName() {
      return new TextComponentString(getName());
   }

   public void markDirty() {
   }

   @Override
   public void openInventory(EntityPlayer player) {
   }

   @Override
   public void closeInventory(EntityPlayer player) {
   }

   @Override
   public void clear() {
      for (int i = 0; i < getSizeInventory(); i++) setInventorySlotContents(i, ItemStack.EMPTY);
   }

   @Override
   public int getFieldCount() { return 0; }

   @Override
   public int getField(int id) { return 0; }

   @Override
   public void setField(int id, int value) {}
}
