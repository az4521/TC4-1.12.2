package thaumcraft.common.entities;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.common.container.SlotOutput;
import thaumcraft.common.entities.monster.EntityPech;

public class ContainerPech extends Container {
   private EntityPech pech;
   private InventoryPech inventory;
   private EntityPlayer player;
   private final World theWorld;

   public ContainerPech(InventoryPlayer par1InventoryPlayer, World par3World, EntityPech par2IMerchant) {
      this.pech = par2IMerchant;
      this.theWorld = par3World;
      this.player = par1InventoryPlayer.player;
      this.inventory = new InventoryPech(par1InventoryPlayer.player, par2IMerchant, this);
      this.pech.trading = true;
      this.addSlotToContainer(new Slot(this.inventory, 0, 36, 29));

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            this.addSlotToContainer(new SlotOutput(this.inventory, 1 + j + i * 2, 106 + 18 * j, 20 + 18 * i));
         }
      }

      for(int var6 = 0; var6 < 3; ++var6) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(par1InventoryPlayer, j + var6 * 9 + 9, 8 + j * 18, 84 + var6 * 18));
         }
      }

      for(int var7 = 0; var7 < 9; ++var7) {
         this.addSlotToContainer(new Slot(par1InventoryPlayer, var7, 8 + var7 * 18, 142));
      }
   }

   public InventoryPech getMerchantInventory() {
      return this.inventory;
   }

   public void addListener(IContainerListener listener) {
      super.addListener(listener);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();
   }

   public boolean enchantItem(EntityPlayer par1EntityPlayer, int par2) {
      if (par2 == 0) {
         this.generateContents();
         return true;
      } else {
         return super.enchantItem(par1EntityPlayer, par2);
      }
   }

   private boolean hasStuffInPack() {
      for(ItemStack stack : this.pech.loot) {
         if (!stack.isEmpty()) {
            return true;
         }
      }
      return false;
   }

   private void generateContents() {
      ItemStack slotZero = this.inventory.getStackInSlot(0);
      if (!this.theWorld.isRemote
            && !slotZero.isEmpty()
            && this.inventory.getStackInSlot(1).isEmpty()
            && this.inventory.getStackInSlot(2).isEmpty()
            && this.inventory.getStackInSlot(3).isEmpty()
            && this.inventory.getStackInSlot(4).isEmpty()
            && this.pech.isValued(slotZero)) {

         int value = this.pech.getValue(slotZero);
         if (this.theWorld.rand.nextInt(100) <= value / 2) {
            this.pech.setTamed(false);
            this.pech.updateAINextTick = true;
            this.pech.playSound(new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "pech_trade")), 0.4F, 1.0F);
         }

         if (this.theWorld.rand.nextInt(5) == 0) {
            value += this.theWorld.rand.nextInt(3);
         } else if (this.theWorld.rand.nextBoolean()) {
            value -= this.theWorld.rand.nextInt(3);
         }

         ArrayList<List> pos = (ArrayList<List>)EntityPech.tradeInventory.get(this.pech.getPechType());

         while(value > 0) {
            int am = Math.min(5, Math.max((value + 1) / 2, this.theWorld.rand.nextInt(value) + 1));
            value -= am;
            if (am == 1 && this.theWorld.rand.nextBoolean() && this.hasStuffInPack()) {
               ArrayList<Integer> loot = new ArrayList<>();

               for(int a = 0; a < this.pech.loot.length; ++a) {
                  if (!this.pech.loot[a].isEmpty()) {
                     loot.add(a);
                  }
               }

               int r = loot.get(this.theWorld.rand.nextInt(loot.size()));
               ItemStack is = this.pech.loot[r].copy();
               is.setCount(1);
               this.mergeItemStack(is, 1, 5, false);
               this.pech.loot[r].shrink(1);
               if (this.pech.loot[r].isEmpty()) {
                  this.pech.loot[r] = ItemStack.EMPTY;
               }
            } else {
               // Loot chest content (WeightedRandomChestContent / ChestGenHooks) removed in 1.12.2.
               // am >= 4 branch skipped — use trade inventory only.
               List it = null;

               do {
                  it = pos.get(this.theWorld.rand.nextInt(pos.size()));
               } while((Integer)it.get(0) != am);

               ItemStack is = ((ItemStack)it.get(1)).copy();
               is.onCrafting(this.theWorld, this.player, 0);
               this.mergeItemStack(is, 1, 5, false);
            }
         }

         this.inventory.decrStackSize(0, 1);
      }
   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int par1, int par2) {
   }

   public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
      return this.pech.isTamed();
   }

   public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.inventorySlots.get(par2);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (par2 == 0) {
            if (!this.mergeItemStack(itemstack1, 5, 41, true)) {
               return ItemStack.EMPTY;
            }
         } else if (par2 >= 1 && par2 < 5) {
            if (!this.mergeItemStack(itemstack1, 5, 41, true)) {
               return ItemStack.EMPTY;
            }
         } else if (par2 != 0 && par2 >= 5 && par2 < 41 && !this.mergeItemStack(itemstack1, 0, 1, true)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(par1EntityPlayer, itemstack1);
      }

      return itemstack;
   }

   public void onContainerClosed(EntityPlayer par1EntityPlayer) {
      super.onContainerClosed(par1EntityPlayer);
      this.pech.trading = false;
      if (!this.theWorld.isRemote) {
         for(int a = 0; a < 5; ++a) {
            ItemStack itemstack = this.inventory.getStackInSlotOnClosing(a);
            if (itemstack != null && !itemstack.isEmpty()) {
               EntityItem ei = par1EntityPlayer.dropItem(itemstack, false);
               if (ei != null) {
                  ei.setThrower("PechDrop");
               }
            }
         }
      }
   }

   protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
      boolean flag1 = false;
      int k = startIndex;
      if (reverseDirection) {
         k = endIndex - 1;
      }

      if (stack.isStackable()) {
         while(stack.getCount() > 0 && (!reverseDirection && k < endIndex || reverseDirection && k >= startIndex)) {
            Slot slot = (Slot)this.inventorySlots.get(k);
            ItemStack itemstack1 = slot.getStack();
            if (!itemstack1.isEmpty() && itemstack1.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
               int l = itemstack1.getCount() + stack.getCount();
               if (l <= stack.getMaxStackSize()) {
                  stack.setCount(0);
                  itemstack1.setCount(l);
                  slot.onSlotChanged();
                  flag1 = true;
               } else if (itemstack1.getCount() < stack.getMaxStackSize()) {
                  stack.shrink(stack.getMaxStackSize() - itemstack1.getCount());
                  itemstack1.setCount(stack.getMaxStackSize());
                  slot.onSlotChanged();
                  flag1 = true;
               }
            }

            if (reverseDirection) {
               --k;
            } else {
               ++k;
            }
         }
      }

      if (stack.getCount() > 0) {
         if (reverseDirection) {
            k = endIndex - 1;
         } else {
            k = startIndex;
         }

         while(!reverseDirection && k < endIndex || reverseDirection && k >= startIndex) {
            Slot slot = (Slot)this.inventorySlots.get(k);
            ItemStack itemstack1 = slot.getStack();
            if (itemstack1.isEmpty()) {
               slot.putStack(stack.copy());
               slot.onSlotChanged();
               stack.setCount(0);
               flag1 = true;
               break;
            }

            if (reverseDirection) {
               --k;
            } else {
               ++k;
            }
         }
      }

      return flag1;
   }
}
