package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;

public class TileThaumcraftInventory extends TileThaumcraft implements ISidedInventory {
   protected ItemStack[] itemStacks = new ItemStack[1];
   protected String customName;
   protected int[] syncedSlots = new int[0];

   public int getSizeInventory() {
      return this.itemStacks.length;
   }

   public boolean isEmpty() {
      for (ItemStack s : this.itemStacks) if (s != null && !s.isEmpty()) return false;
      return true;
   }

   public ItemStack getStackInSlot(int par1) {
      ItemStack s = this.itemStacks[par1]; return s != null ? s : ItemStack.EMPTY;
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (this.itemStacks[par1] != null && !this.itemStacks[par1].isEmpty()) {
         ItemStack itemstack;
         if (this.itemStacks[par1].getCount() <= par2) {
            itemstack = this.itemStacks[par1];
            this.itemStacks[par1] = ItemStack.EMPTY;
         } else {
            itemstack = this.itemStacks[par1].splitStack(par2);
            if (this.itemStacks[par1].getCount() == 0) {
               this.itemStacks[par1] = ItemStack.EMPTY;
            }
         }
         this.markDirty();
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeStackFromSlot(int par1) {
      if (this.itemStacks[par1] != null && !this.itemStacks[par1].isEmpty()) {
         ItemStack itemstack = this.itemStacks[par1];
         this.itemStacks[par1] = ItemStack.EMPTY;
         this.markDirty();
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.itemStacks[par1] = par2ItemStack;
      if (!par2ItemStack.isEmpty() && par2ItemStack.getCount() > this.getInventoryStackLimit()) {
         par2ItemStack.setCount(this.getInventoryStackLimit());
      }
      this.markDirty();
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.thaumcraft";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public net.minecraft.util.text.ITextComponent getDisplayName() {
      return new net.minecraft.util.text.TextComponentString(this.getName());
   }

   public void setGuiDisplayName(String par1Str) {
      this.customName = par1Str;
   }

   private boolean isSyncedSlot(int slot) {
      for (int s : this.syncedSlots) {
         if (s == slot) return true;
      }
      return false;
   }

   public void readCustomNBT(NBTTagCompound nbtCompound) {
      NBTTagList nbttaglist = nbtCompound.getTagList("ItemsSynced", 10);
      this.itemStacks = new ItemStack[this.getSizeInventory()];
      for (int i = 0; i < nbttaglist.tagCount(); ++i) {
         if (this.isSyncedSlot(i)) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");
            if (b0 >= 0 && b0 < this.itemStacks.length) {
               this.itemStacks[b0] = new ItemStack(nbttagcompound1);
            }
         }
      }
   }

   public void writeCustomNBT(NBTTagCompound nbtCompound) {
      NBTTagList nbttaglist = new NBTTagList();
      for (int i = 0; i < this.itemStacks.length; ++i) {
         if (this.itemStacks[i] != null && !this.itemStacks[i].isEmpty() && this.isSyncedSlot(i)) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.itemStacks[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }
      nbtCompound.setTag("ItemsSynced", nbttaglist);
   }

   public void readFromNBT(NBTTagCompound nbtCompound) {
      super.readFromNBT(nbtCompound);
      if (nbtCompound.hasKey("CustomName")) {
         this.customName = nbtCompound.getString("CustomName");
      }
      NBTTagList nbttaglist = nbtCompound.getTagList("Items", 10);
      for (int i = 0; i < nbttaglist.tagCount(); ++i) {
         if (!this.isSyncedSlot(i)) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");
            if (b0 >= 0 && b0 < this.itemStacks.length) {
               this.itemStacks[b0] = new ItemStack(nbttagcompound1);
            }
         }
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
      super.writeToNBT(nbtCompound);
      if (this.hasCustomName()) {
         nbtCompound.setString("CustomName", this.customName);
      }
      NBTTagList nbttaglist = new NBTTagList();
      for (int i = 0; i < this.itemStacks.length; ++i) {
         if (this.itemStacks[i] != null && !this.itemStacks[i].isEmpty() && !this.isSyncedSlot(i)) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.itemStacks[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }
      nbtCompound.setTag("Items", nbttaglist);
      return nbtCompound;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.world.getTileEntity(this.getPos()) == this &&
             par1EntityPlayer.getDistanceSq(this.getPos().getX() + 0.5D, this.getPos().getY() + 0.5D, this.getPos().getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer player) {}
   public void closeInventory(EntityPlayer player) {}

   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return true;
   }

   public int[] getSlotsForFace(EnumFacing side) {
      return new int[]{0};
   }

   public boolean canInsertItem(int par1, ItemStack par2ItemStack, EnumFacing side) {
      return this.isItemValidForSlot(par1, par2ItemStack);
   }

   public boolean canExtractItem(int par1, ItemStack par2ItemStack, EnumFacing side) {
      return true;
   }

   public int getField(int id) { return 0; }
   public void setField(int id, int value) {}
   public int getFieldCount() { return 0; }
   public void clear() { for (int i = 0; i < itemStacks.length; i++) itemStacks[i] = ItemStack.EMPTY; }
}
