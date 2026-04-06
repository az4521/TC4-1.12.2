package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.common.items.wands.ItemWandCasting;

import static tc4tweak.ClientUtils.lastUpdate;
import static tc4tweak.ClientUtils.postponed;
import net.minecraft.util.math.BlockPos;

public class TileMagicWorkbench extends TileThaumcraft implements IInventory, ISidedInventory {
   public ItemStack[] stackList = new ItemStack[11];
   public Container eventHandler;
   protected int count;

   public int getSizeInventory() {
      return this.stackList.length;
   }

   public ItemStack getStackInSlot(int par1) {
      if (par1 >= this.getSizeInventory()) return ItemStack.EMPTY;
      ItemStack s = this.stackList[par1]; return s != null ? s : ItemStack.EMPTY;
   }

   public ItemStack getStackInRowAndColumn(int par1, int par2) {
      if (par1 >= 0 && par1 < 3) {
         int var3 = par1 + par2 * 3;
         return this.getStackInSlot(var3);
      } else {
         return null;
      }
   }

   public void clear() {
      java.util.Arrays.fill(this.stackList, null);
   }

   public ItemStack getStackInSlotOnClosing(int par1) {
      if (this.stackList[par1] != null) {
         ItemStack var2 = this.stackList[par1];
         this.stackList[par1] = null;
         this.markDirty();
         return var2;
      } else {
         return null;
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
          if (this.eventHandler != null) {
             this.eventHandler.onCraftMatrixChanged(this);
          }
          this.markDirty();
          { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
          return var3;
      } else {
         return null;
      }
   }
   public static void updateCraftingMatrix(TileMagicWorkbench self) {
      if (!self.getWorld().isRemote) {
         self.eventHandler.onCraftMatrixChanged(self);
         return;
      }
      long oldUpdate = lastUpdate;
      lastUpdate = System.currentTimeMillis();
      if (lastUpdate - oldUpdate > 1000 / 5) {
         self.eventHandler.onCraftMatrixChanged(self); // 5 times per second at max
      } else {
         synchronized (postponed) {
            postponed.put(self, null);
         }
      }
   }
   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.stackList[par1] = par2ItemStack;
      this.markDirty();
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      if (this.eventHandler != null) {
         this.eventHandler.onCraftMatrixChanged(this);
         updateCraftingMatrix(this);
      }

   }

   public void setInventorySlotContentsSoftly(int par1, ItemStack par2ItemStack) {
      this.stackList[par1] = par2ItemStack;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return true;
   }

   public void readCustomNBT(NBTTagCompound par1NBTTagCompound) {
      NBTTagList var2 = par1NBTTagCompound.getTagList("Inventory", 10);
      this.stackList = new ItemStack[this.getSizeInventory()];

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         int var5 = var4.getByte("Slot") & 255;
         if (var5 >= 0 && var5 < this.stackList.length) {
            this.stackList[var5] = new ItemStack(var4);
         }
      }

   }

   public void writeCustomNBT(NBTTagCompound par1NBTTagCompound) {
      NBTTagList var2 = new NBTTagList();

      for(int var3 = 0; var3 < this.stackList.length; ++var3) {
         if (this.stackList[var3] != null) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setByte("Slot", (byte)var3);
            this.stackList[var3].writeToNBT(var4);
            var2.appendTag(var4);
         }
      }

      par1NBTTagCompound.setTag("Inventory", var2);
   }

   @Override
   public String getName() {
      return I18n.translateToLocal("tile.blockTable.15.name");
   }

   public String getInventoryName() {
      return getName();
   }

   public boolean isEmpty() {
      for (ItemStack stack : this.stackList) {
         if (stack != null && !stack.isEmpty()) return false;
      }
      return true;
   }

   public int getField(int id) { return 0; }
   public void setField(int id, int value) {}
   public int getFieldCount() { return 0; }
   public ItemStack removeStackFromSlot(int index) {
      ItemStack stack = this.stackList[index];
      this.stackList[index] = null;
      return stack;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean hasCustomInventoryName() {
      return false;
   }

   @Override
   public boolean hasCustomName() {
      return false;
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      if (i == 10 && itemstack != null) {
         if (!(itemstack.getItem() instanceof ItemWandCasting)) {
            return false;
         } else {
            ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
            return !wand.isStaff(itemstack);
         }
      } else {
         return true;
      }
   }

   @Override
   public int[] getSlotsForFace(EnumFacing side) {
      return new int[]{10};
   }

   @Override
   public boolean canInsertItem(int index, ItemStack itemstack, EnumFacing facing) {
      if (index == 10 && itemstack != null && itemstack.getItem() instanceof ItemWandCasting) {
         ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
         return !wand.isStaff(itemstack);
      } else {
         return false;
      }
   }

   @Override
   public boolean canExtractItem(int index, ItemStack itemstack, EnumFacing facing) {
      return index == 10;
   }
}
