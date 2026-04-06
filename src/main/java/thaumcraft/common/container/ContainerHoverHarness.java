package thaumcraft.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ContainerHoverHarness extends Container {
   private World world;
   private int posX;
   private int posY;
   private int posZ;
   public IInventory input = new InventoryHoverHarness(this);
   ItemStack armor = ItemStack.EMPTY;
   EntityPlayer player = null;
   private int blockSlot;

   public ContainerHoverHarness(InventoryPlayer iinventory, World par2World, int par3, int par4, int par5) {
      this.world = par2World;
      this.posX = par3;
      this.posY = par4;
      this.posZ = par5;
      this.player = iinventory.player;
      this.armor = iinventory.getCurrentItem();
      this.blockSlot = iinventory.currentItem + 28;
      this.addSlotToContainer(new Slot(this.input, 0, 80, 32));
      this.bindPlayerInventory(iinventory);
      if (!par2World.isRemote) {
         try {
            ItemStack jar = new ItemStack(this.armor.getTagCompound().getCompoundTag("jar"));
            this.input.setInventorySlotContents(0, jar);
         } catch (Exception ignored) {
         }
      }

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

   public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot) {
      if (slot == this.blockSlot) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = ItemStack.EMPTY;
         Slot slotObject = (Slot)this.inventorySlots.get(slot);
         if (slotObject != null && slotObject.getHasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();
            if (slot == 0) {
               if (!this.input.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 1, this.inventorySlots.size(), true, 64)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.input.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 0, 1, false, 1)) {
               return ItemStack.EMPTY;
            }

            if (stackInSlot.getCount() == 0) {
               slotObject.putStack(ItemStack.EMPTY);
            } else {
               slotObject.onSlotChanged();
            }
         }

         return stack;
      }
   }

   public ItemStack slotClick(int par1, int par2, ClickType par3, EntityPlayer par4EntityPlayer) {
      if (par1 == this.blockSlot) {
         return ItemStack.EMPTY;
      } else {
         InventoryPlayer inventoryplayer = par4EntityPlayer.inventory;
         return par1 == 0 && !this.input.isItemValidForSlot(par1, inventoryplayer.getItemStack()) && (par1 != 0 || !inventoryplayer.getItemStack().isEmpty()) ? ItemStack.EMPTY : super.slotClick(par1, par2, par3, par4EntityPlayer);
      }
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return true;
   }

   public void putStackInSlot(int par1, ItemStack par2ItemStack) {
      if (this.input.isItemValidForSlot(par1, par2ItemStack)) {
         super.putStackInSlot(par1, par2ItemStack);
      }

   }

   public void onContainerClosed(EntityPlayer par1EntityPlayer) {
      if (!this.world.isRemote) {
         ItemStack var3 = this.input.removeStackFromSlot(0);
         if (!var3.isEmpty()) {
            NBTTagCompound var4 = new NBTTagCompound();
            var3.writeToNBT(var4);
            this.armor.setTagInfo("jar", var4);
         } else {
            this.armor.setTagInfo("jar", new NBTTagCompound());
         }

         if (this.player == null) {
            return;
         }

         if (!this.player.getHeldItemMainhand().isEmpty() && this.player.getHeldItemMainhand().isItemEqual(this.armor)) {
            this.player.setHeldItem(EnumHand.MAIN_HAND, this.armor);
         }

         this.player.inventory.markDirty();
      }

   }

   protected boolean mergeItemStack(ItemStack par1ItemStack, int par2, int par3, boolean par4, int limit) {
      boolean var5 = false;
      int var6 = par2;
      if (par4) {
         var6 = par3 - 1;
      }

      if (par1ItemStack.isStackable()) {
         while(par1ItemStack.getCount() > 0 && (!par4 && var6 < par3 || par4 && var6 >= par2)) {
            Slot var7 = (Slot)this.inventorySlots.get(var6);
            ItemStack var8 = var7.getStack();
            if (!var8.isEmpty() && var8.getItem() == par1ItemStack.getItem() && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == var8.getItemDamage()) && ItemStack.areItemStackTagsEqual(par1ItemStack, var8)) {
               int var9 = var8.getCount() + par1ItemStack.getCount();
               if (var9 <= Math.min(par1ItemStack.getMaxStackSize(), limit)) {
                  par1ItemStack.setCount(0);
                  var8.setCount(var9);
                  var7.onSlotChanged();
                  var5 = true;
               } else if (var8.getCount() < Math.min(par1ItemStack.getMaxStackSize(), limit)) {
                  par1ItemStack.shrink(Math.min(par1ItemStack.getMaxStackSize(), limit) - var8.getCount());
                  var8.setCount(Math.min(par1ItemStack.getMaxStackSize(), limit));
                  var7.onSlotChanged();
                  var5 = true;
               }
            }

            if (par4) {
               --var6;
            } else {
               ++var6;
            }
         }
      }

      if (par1ItemStack.getCount() > 0) {
         if (par4) {
            var6 = par3 - 1;
         } else {
            var6 = par2;
         }

         while(!par4 && var6 < par3 || par4 && var6 >= par2) {
            Slot var7 = (Slot)this.inventorySlots.get(var6);
            ItemStack var8 = var7.getStack();
            if (var8.isEmpty()) {
               ItemStack res = par1ItemStack.copy();
               res.setCount(Math.min(res.getCount(), limit));
               var7.putStack(res);
               var7.onSlotChanged();
               par1ItemStack.shrink(res.getCount());
               var5 = true;
               break;
            }

            if (par4) {
               --var6;
            } else {
               ++var6;
            }
         }
      }

      return var5;
   }
}
