package thaumcraft.common.entities;

import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.common.entities.golems.EntityTravelingTrunk;

public class InventoryMob implements IInventory {
   public ItemStack[] inventory;
   public Entity ent;
   public boolean inventoryChanged;
   public int slotCount;
   public int stacklimit = 64;

   public InventoryMob(Entity entity, int slots) {
      this.slotCount = slots;
      this.inventory = new ItemStack[this.slotCount];
      this.inventoryChanged = false;
      this.ent = entity;
   }

   public InventoryMob(Entity entity, int slots, int lim) {
      this.slotCount = slots;
      this.inventory = new ItemStack[this.slotCount];
      this.inventoryChanged = false;
      this.stacklimit = lim;
      this.ent = entity;
   }

   public int getInventorySlotContainItem(Item i) {
      for(int j = 0; j < this.inventory.length; ++j) {
         if (this.inventory[j] != null && this.inventory[j].getItem() == i) {
            return j;
         }
      }

      return -1;
   }

   public int storeItemStack(ItemStack itemstack) {
      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] != null && this.inventory[i].getItem() == itemstack.getItem() && this.inventory[i].isStackable() && this.inventory[i].getCount() < this.inventory[i].getMaxStackSize() && this.inventory[i].getCount() < this.getInventoryStackLimit() && (!this.inventory[i].getHasSubtypes() || this.inventory[i].getItemDamage() == itemstack.getItemDamage())) {
            return i;
         }
      }

      return -1;
   }

   public int getFirstEmptyStack() {
      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] == null) {
            return i;
         }
      }

      return -1;
   }

   public int storePartialItemStack(ItemStack itemstack) {
      Item i = itemstack.getItem();
      int j = itemstack.getCount();
      int k = this.storeItemStack(itemstack);
      if (k < 0) {
         k = this.getFirstEmptyStack();
      }

       if (k >= 0) {
           if (this.inventory[k] == null) {
               this.inventory[k] = new ItemStack(i, 0, itemstack.getItemDamage());
           }

           int l = j;
           if (j > this.inventory[k].getMaxStackSize() - this.inventory[k].getCount()) {
               l = this.inventory[k].getMaxStackSize() - this.inventory[k].getCount();
           }

           if (l > this.getInventoryStackLimit() - this.inventory[k].getCount()) {
               l = this.getInventoryStackLimit() - this.inventory[k].getCount();
           }

           if (l != 0) {
               j -= l;
               this.inventory[k].grow(l);
           }
       }
       return j;
   }

   public boolean addItemStackToInventory(ItemStack itemstack) {
      if (itemstack.isItemDamaged()) {
         int j = this.getFirstEmptyStack();
         if (j >= 0) {
            this.inventory[j] = itemstack.copy();
            itemstack.setCount(0);
            return true;
         } else {
            return false;
         }
      } else {
         int i;
         do {
            i = itemstack.getCount();
            itemstack.setCount(this.storePartialItemStack(itemstack));
         } while(itemstack.getCount() > 0 && itemstack.getCount() < i);

         return itemstack.getCount() < i;
      }
   }

   public ItemStack decrStackSize(int i, int j) {
      ItemStack[] aitemstack = this.inventory;
      if (aitemstack[i] != null) {
         if (aitemstack[i].getCount() <= j) {
            ItemStack itemstack = aitemstack[i];
            aitemstack[i] = null;
            return itemstack;
         } else {
            ItemStack itemstack1 = aitemstack[i].splitStack(j);
            if (aitemstack[i].getCount() == 0) {
               aitemstack[i] = null;
            }

            return itemstack1;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int i, ItemStack itemstack) {
      ItemStack[] aitemstack = this.inventory;
      aitemstack[i] = itemstack;
   }

   public NBTTagList writeToNBT(NBTTagList nbttaglist) {
      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.inventory[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      return nbttaglist;
   }

   public void readFromNBT(NBTTagList nbttaglist) {
      this.inventory = new ItemStack[this.slotCount];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         int j = nbttagcompound.getByte("Slot") & 255;
         ItemStack itemstack = new ItemStack(nbttagcompound);
         if (!itemstack.isEmpty() && j >= 0 && j < this.inventory.length) {
            this.inventory[j] = itemstack;
         }
      }

   }

   public int getSizeInventory() {
      return this.inventory.length + 1;
   }

   public ItemStack getStackInSlot(int i) {
      ItemStack stack = this.inventory[i];
      return stack != null ? stack : ItemStack.EMPTY;
   }

   public int getInventoryStackLimit() {
      return this.stacklimit;
   }

   // IInventory 1.12.2: isUsableByPlayer
   @Override
   public boolean isUsableByPlayer(EntityPlayer entityplayer) {
      if (this.ent.isDead) {
         return false;
      } else {
         return this.ent.getDistanceSq(entityplayer.posX, entityplayer.posY, entityplayer.posZ) <= (double)64.0F;
      }
   }

   // Keep old name for any internal callers
   public boolean canInteractWith(EntityPlayer entityplayer) {
      return isUsableByPlayer(entityplayer);
   }

   public boolean func_28018_c(ItemStack itemstack) {
       for (ItemStack itemStack : this.inventory) {
           if (itemStack != null && ItemStack.areItemStacksEqual(itemStack, itemstack)) {
               return true;
           }
       }

      return false;
   }

   public void dropAllItems() {
      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] != null) {
            this.ent.entityDropItem(this.inventory[i], 0.0F);
            this.inventory[i] = null;
         }
      }

   }

   public boolean isUseableByPlayer(EntityPlayer entityplayer) {
      return false;
   }

   public ItemStack getStackInSlotOnClosing(int var1) {
      return ItemStack.EMPTY;
   }

   public boolean hasSomething() {
      for(int a = 0; a < this.slotCount; ++a) {
         if (this.inventory[a] != null) {
            return true;
         }
      }

      return false;
   }

   public boolean allEmpty() {
      for(int a = 0; a < this.slotCount; ++a) {
         if (this.inventory[a] != null) {
            return false;
         }
      }

      return true;
   }

   public int getAmountNeeded(ItemStack stackInSlot) {
      int amt = 0;

      for(int a = 0; a < this.slotCount; ++a) {
         if (this.inventory[a] != null && this.inventory[a].isItemEqual(stackInSlot)) {
            amt += this.inventory[a].getCount();
         }
      }

      return amt;
   }

   public int getAmountNeededSmart(ItemStack stackInSlot, boolean fuzzy) {
      int amt = 0;

      for(int a = 0; a < this.slotCount; ++a) {
         if (this.inventory[a] != null) {
            if (fuzzy) {
               if (this.inventory[a].isItemEqual(stackInSlot)) {
                  amt += this.inventory[a].getCount();
               } else {
                  int[] ods = OreDictionary.getOreIDs(this.inventory[a]);
                  int od = ods.length > 0 ? ods[0] : -1;
                  if (od != -1) {
                     ItemStack[] ores = OreDictionary.getOres(OreDictionary.getOreName(od)).toArray(new ItemStack[0]);
                     if (ThaumcraftApiHelper.containsMatch(false, new ItemStack[]{stackInSlot}, ores)) {
                        amt += this.inventory[a].getCount();
                     }
                  }
               }
            } else if (this.inventory[a].isItemEqual(stackInSlot) && ItemStack.areItemStackTagsEqual(this.inventory[a], stackInSlot)) {
               amt += this.inventory[a].getCount();
            }
         }
      }

      return amt;
   }

   public ArrayList getItemsNeeded(boolean fuzzy) {
      ArrayList<ItemStack> needed = new ArrayList<>();

      for(int a = 0; a < this.slotCount; ++a) {
         if (this.inventory[a] != null) {
            if (fuzzy) {
               int[] ods = OreDictionary.getOreIDs(this.inventory[a]);
               int od = ods.length > 0 ? ods[0] : -1;
               if (od != -1) {
                  ItemStack[] ores = OreDictionary.getOres(OreDictionary.getOreName(od)).toArray(new ItemStack[0]);

                  for(ItemStack ore : ores) {
                     needed.add(ore.copy());
                  }
               } else {
                  needed.add(this.inventory[a].copy());
               }
            } else {
               needed.add(this.inventory[a].copy());
            }
         }
      }

      return needed;
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   // Legacy name kept for backward compat
   public String getInventoryName() {
      return "Inventory";
   }

   // Legacy name kept for backward compat
   public boolean hasCustomInventoryName() {
      return false;
   }

   // IInventory 1.12.2 name methods
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
      return new TextComponentString(getName());
   }

   public void markDirty() {
      this.inventoryChanged = true;
   }

   @Override
   public void openInventory(EntityPlayer player) {
      if (this.ent instanceof EntityTravelingTrunk) {
         ((EntityTravelingTrunk)this.ent).setOpen(true);
      }
   }

   @Override
   public void closeInventory(EntityPlayer player) {
      if (this.ent instanceof EntityTravelingTrunk) {
         ((EntityTravelingTrunk)this.ent).setOpen(false);
      }
   }

   // IInventory 1.12.2 additional required methods
   @Override
   public boolean isEmpty() {
      for (ItemStack stack : this.inventory) {
         if (stack != null && !stack.isEmpty()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public void clear() {
      for (int i = 0; i < this.inventory.length; ++i) {
         this.inventory[i] = null;
      }
   }

   @Override
   public int getField(int id) {
      return 0;
   }

   @Override
   public void setField(int id, int value) {
   }

   @Override
   public int getFieldCount() {
      return 0;
   }

   @Override
   public ItemStack removeStackFromSlot(int i) {
      ItemStack stack = this.inventory[i];
      this.inventory[i] = null;
      return stack;
   }
}
