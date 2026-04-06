package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import static thaumcraft.common.config.ConfigBlocks.blockChestHungry;
import net.minecraft.util.math.BlockPos;

public class TileChestHungry extends TileEntity implements IInventory, net.minecraft.util.ITickable {
   @Override
   public void clear() {
      for (int i = 0; i < this.chestContents.length; ++i) {
         this.chestContents[i] = ItemStack.EMPTY;
      }
   }
   private ItemStack[] chestContents = new ItemStack[36];
   public float lidAngle;
   public float prevLidAngle;
   public int numUsingPlayers;
   private int ticksSinceSync;

   public boolean isEmpty() {
      for (ItemStack stack : this.chestContents) {
         if (stack != null && !stack.isEmpty()) return false;
      }
      return true;
   }

   public int getSizeInventory() {
      return 27;
   }

   public ItemStack getStackInSlot(int par1) {
      ItemStack s = this.chestContents[par1]; return s != null ? s : ItemStack.EMPTY;
   }

   public ItemStack decrStackSize(int itemIndexInChest, int extractAtMostCount) {
      ItemStack result = this.chestContents[itemIndexInChest];
      if (result != null) {
         //directly output if not greater than extractAtMostCount
         if (result.getCount() <= extractAtMostCount){
            this.chestContents[itemIndexInChest] = null;
            this.markDirty();
            return result.getCount() == 0 ? null : result;
         }

         //tc4 vanilla
         result = result.splitStack(extractAtMostCount);
         if (result.getCount() == 0) {
            this.chestContents[itemIndexInChest] = null;
         }
         this.markDirty();
         return result;
      } else {
         return null;
      }

   }

   public ItemStack removeStackFromSlot(int par1) {
      if (this.chestContents[par1] != null) {
         ItemStack var2 = this.chestContents[par1];
         this.chestContents[par1] = null;
         return var2;
      } else {
         return null;
      }
   }

   public ItemStack getStackInSlotOnClosing(int par1) {
      if (this.chestContents[par1] != null) {
         ItemStack var2 = this.chestContents[par1];
         this.chestContents[par1] = null;
         return var2;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.chestContents[par1] = par2ItemStack;
      if (par2ItemStack != null && par2ItemStack.getCount() > this.getInventoryStackLimit()) {
         par2ItemStack.setCount(this.getInventoryStackLimit());
      }

      this.markDirty();
   }

   public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readFromNBT(par1NBTTagCompound);
      NBTTagList var2 = par1NBTTagCompound.getTagList("Items", 10);
      this.chestContents = new ItemStack[this.getSizeInventory()];

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         int var5 = var4.getByte("Slot") & 255;
         if (var5 < this.chestContents.length) {
            this.chestContents[var5] = new ItemStack(var4);
         }
      }

   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeToNBT(par1NBTTagCompound);
      NBTTagList var2 = new NBTTagList();

      for(int var3 = 0; var3 < this.chestContents.length; ++var3) {
         if (this.chestContents[var3] != null) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setByte("Slot", (byte)var3);
            this.chestContents[var3].writeToNBT(var4);
            var2.appendTag(var4);
         }
      }

      par1NBTTagCompound.setTag("Items", var2);
      return par1NBTTagCompound;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.world.getTileEntity(this.getPos()) == this && par1EntityPlayer.getDistanceSq((double) this.getPos().getX() + (double) 0.5F, (double) this.getPos().getY() + (double) 0.5F, (double) this.getPos().getZ() + (double) 0.5F) <= (double) 64.0F;
   }

   public void updateContainingBlockInfo() {
      super.updateContainingBlockInfo();
   }

   @Override
   public void update() {
      if (++this.ticksSinceSync % 20 * 4 == 0) {
      }

      this.prevLidAngle = this.lidAngle;
      float var1 = 0.1F;
      if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F) {
         this.world.playSound(null, this.getPos(), net.minecraft.init.SoundEvents.BLOCK_CHEST_OPEN, net.minecraft.util.SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
      }

      if (this.numUsingPlayers == 0 && this.lidAngle > 0.0F || this.numUsingPlayers > 0 && this.lidAngle < 1.0F) {
         float var8 = this.lidAngle;
         if (this.numUsingPlayers > 0) {
            this.lidAngle += var1;
         } else {
            this.lidAngle -= var1;
         }

         if (this.lidAngle > 1.0F) {
            this.lidAngle = 1.0F;
         }

         float var3 = 0.5F;
         if (this.lidAngle < var3 && var8 >= var3) {
            this.world.playSound(null, this.getPos(), net.minecraft.init.SoundEvents.BLOCK_CHEST_CLOSE, net.minecraft.util.SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
         }

         if (this.lidAngle < 0.0F) {
            this.lidAngle = 0.0F;
         }
      }

   }

   public boolean receiveClientEvent(int par1, int par2) {
      if (par1 == 1) {
         this.numUsingPlayers = par2;
         return true;
      } else if (par1 == 2) {
         if (this.lidAngle < (float)par2 / 10.0F) {
            this.lidAngle = (float)par2 / 10.0F;
         }

         return true;
      } else {
         return this.tileEntityInvalid;
      }
   }

   @Override
   public void openInventory(EntityPlayer player) {
      ++this.numUsingPlayers;
      this.world.addBlockEvent(this.getPos(), blockChestHungry, 1, this.numUsingPlayers);
   }

   @Override
   public void closeInventory(EntityPlayer player) {
      --this.numUsingPlayers;
      this.world.addBlockEvent(this.getPos(), blockChestHungry, 1, this.numUsingPlayers);
   }

   public void invalidate() {
      this.updateContainingBlockInfo();
      super.invalidate();
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   @Override
   public int getField(int id) { return 0; }
   @Override
   public void setField(int id, int value) {}
   @Override
   public int getFieldCount() { return 0; }
   @Override
   public String getName() { return blockChestHungry != null ? blockChestHungry.getLocalizedName() : "chest"; }
   @Override
   public boolean hasCustomName() { return false; }
}
