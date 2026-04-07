package thaumcraft.common.entities.golems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryTrunk implements IInventory {
   public ItemStack[] inventory;
   public EntityTravelingTrunk ent;
   public boolean inventoryChanged;
   public int slotCount;
   public int stacklimit = 64;

   public InventoryTrunk(EntityTravelingTrunk entity, int slots) {
      this.slotCount = slots;
      this.inventory = new ItemStack[36];
      java.util.Arrays.fill(this.inventory, ItemStack.EMPTY);
      this.inventoryChanged = false;
      this.ent = entity;
   }

   public InventoryTrunk(EntityTravelingTrunk entity, int slots, int lim) {
      this.slotCount = slots;
      this.inventory = new ItemStack[36];
      java.util.Arrays.fill(this.inventory, ItemStack.EMPTY);
      this.inventoryChanged = false;
      this.stacklimit = lim;
      this.ent = entity;
   }

   public int getInventorySlotContainItem(Item i) {
      for(int j = 0; j < this.inventory.length; ++j) {
         if (this.inventory[j] != null && !this.inventory[j].isEmpty() && this.inventory[j].getItem() == i) {
            return j;
         }
      }
      return -1;
   }

   public int getFirstEmptyStack() {
      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] == null || this.inventory[i].isEmpty()) {
            return i;
         }
      }
      return -1;
   }

   public ItemStack decrStackSize(int i, int j) {
      ItemStack[] aitemstack = this.inventory;
      if (aitemstack[i] != null && !aitemstack[i].isEmpty()) {
         if (aitemstack[i].getCount() <= j) {
            ItemStack itemstack = aitemstack[i];
            aitemstack[i] = ItemStack.EMPTY;
            return itemstack;
         } else {
            ItemStack itemstack1 = aitemstack[i].splitStack(j);
            if (aitemstack[i].isEmpty()) {
               aitemstack[i] = ItemStack.EMPTY;
            }
            return itemstack1;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeStackFromSlot(int i) {
      if (this.inventory[i] != null && !this.inventory[i].isEmpty()) {
         ItemStack stack = this.inventory[i];
         this.inventory[i] = ItemStack.EMPTY;
         return stack;
      }
      return ItemStack.EMPTY;
   }

   public void setInventorySlotContents(int i, ItemStack itemstack) {
      this.inventory[i] = itemstack == null ? ItemStack.EMPTY : itemstack;
   }

   public NBTTagList writeToNBT(NBTTagList nbttaglist) {
      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] != null && !this.inventory[i].isEmpty()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.inventory[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }
      return nbttaglist;
   }

   public void readFromNBT(NBTTagList nbttaglist) {
      this.inventory = new ItemStack[this.inventory.length];
      java.util.Arrays.fill(this.inventory, ItemStack.EMPTY);
      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         int j = nbttagcompound.getByte("Slot") & 255;
         ItemStack itemstack = new ItemStack(nbttagcompound);
         if (!itemstack.isEmpty() && j >= 0 && j < this.inventory.length) {
            this.inventory[j] = itemstack;
         }
      }
   }

   @Override
   public int getSizeInventory() {
      return this.slotCount;
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack stack : this.inventory) {
         if (stack != null && !stack.isEmpty()) return false;
      }
      return true;
   }

   @Override
   public ItemStack getStackInSlot(int i) {
      ItemStack stack = this.inventory[i];
      return stack != null ? stack : ItemStack.EMPTY;
   }

   @Override
   public int getInventoryStackLimit() {
      return this.stacklimit;
   }

   @Override
   public boolean isUsableByPlayer(EntityPlayer entityplayer) {
      return false;
   }

   @Override
   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   @Override
   public String getName() {
      return "Inventory";
   }

   @Override
   public boolean hasCustomName() {
      return false;
   }

   @Override
   public ITextComponent getDisplayName() {
      return new TextComponentTranslation(getName());
   }

   @Override
   public int getFieldCount() { return 0; }

   @Override
   public int getField(int id) { return 0; }

   @Override
   public void setField(int id, int value) {}

   @Override
   public void clear() {
      for (int i = 0; i < this.inventory.length; ++i) {
         this.inventory[i] = ItemStack.EMPTY;
      }
   }

   @Override
   public void markDirty() {
      this.inventoryChanged = true;
   }

   @Override
   public void openInventory(EntityPlayer player) {
      if (this.ent instanceof EntityTravelingTrunk) {
         this.ent.setOpen(true);
      }
   }

   @Override
   public void closeInventory(EntityPlayer player) {
      if (this.ent instanceof EntityTravelingTrunk) {
         this.ent.setOpen(false);
      }
   }

   public void dropAllItems() {
      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] != null && !this.inventory[i].isEmpty()) {
            this.ent.entityDropItem(this.inventory[i], 0.0F);
            this.inventory[i] = ItemStack.EMPTY;
         }
      }
   }
}
