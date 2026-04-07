package thaumcraft.common.container;

import java.util.Arrays;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.blocks.ItemJarFilled;

public class InventoryHoverHarness implements IInventory {
   private ItemStack[] stackList = new ItemStack[1];
   private Container eventHandler;

   public InventoryHoverHarness(Container par1Container) {
      this.eventHandler = par1Container;
      Arrays.fill(this.stackList, ItemStack.EMPTY);
   }

   public int getSizeInventory() {
      return this.stackList.length;
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack stack : this.stackList) {
         if (!stack.isEmpty()) return false;
      }
      return true;
   }

   public ItemStack getStackInSlot(int par1) {
      return par1 >= this.getSizeInventory() ? ItemStack.EMPTY : this.stackList[par1];
   }

   @Override
   public ItemStack removeStackFromSlot(int par1) {
      if (!this.stackList[par1].isEmpty()) {
         ItemStack var2 = this.stackList[par1];
         this.stackList[par1] = ItemStack.EMPTY;
         return var2;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (!this.stackList[par1].isEmpty()) {
          ItemStack var3;
          if (this.stackList[par1].getCount() <= par2) {
              var3 = this.stackList[par1];
            this.stackList[par1] = ItemStack.EMPTY;
          } else {
              var3 = this.stackList[par1].splitStack(par2);
            if (this.stackList[par1].getCount() == 0) {
               this.stackList[par1] = ItemStack.EMPTY;
            }

          }
          this.eventHandler.onCraftMatrixChanged(this);
          return var3;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.stackList[par1] = par2ItemStack == null ? ItemStack.EMPTY : par2ItemStack;
      this.eventHandler.onCraftMatrixChanged(this);
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   @Override
   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return true;
   }

   public boolean isItemValidForSlot(int i, ItemStack jar) {
      if (!jar.isEmpty() && jar.getItem() instanceof ItemJarFilled && jar.hasTagCompound()) {
         AspectList aspects = ((ItemJarFilled)jar.getItem()).getAspects(jar);
          return aspects != null && aspects.size() > 0 && aspects.getAmount(Aspect.ENERGY) > 0;
      }

      return false;
   }

   @Override
   public String getName() {
      return "container.hoverharness";
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
