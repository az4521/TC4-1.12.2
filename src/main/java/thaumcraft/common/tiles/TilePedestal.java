package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.common.Thaumcraft;
import net.minecraft.util.math.BlockPos;

public class TilePedestal extends TileThaumcraft implements ISidedInventory {
   private static final int[] slots = new int[]{0};
   private ItemStack[] inventory = new ItemStack[1];
   private String customName;

   public TilePedestal() {
      java.util.Arrays.fill(this.inventory, ItemStack.EMPTY);
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 2, this.getPos().getZ() + 1);
   }

   public int getSizeInventory() {
      return 1;
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack stack : inventory) {
         if (stack != null && !stack.isEmpty()) return false;
      }
      return true;
   }

   public ItemStack getStackInSlot(int par1) {
      return this.inventory[par1] == null ? ItemStack.EMPTY : this.inventory[par1];
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (!this.inventory[par1].isEmpty()) {
         if (!this.world.isRemote) {
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         }

          ItemStack itemstack;
          if (this.inventory[par1].getCount() <= par2) {
              itemstack = this.inventory[par1];
            this.inventory[par1] = ItemStack.EMPTY;
          } else {
              itemstack = this.inventory[par1].splitStack(par2);
            if (this.inventory[par1].getCount() == 0) {
               this.inventory[par1] = ItemStack.EMPTY;
            }

          }
          this.markDirty();
          return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeStackFromSlot(int par1) {
      if (!this.inventory[par1].isEmpty()) {
         ItemStack itemstack = this.inventory[par1];
         this.inventory[par1] = ItemStack.EMPTY;
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.inventory[par1] = par2ItemStack == null ? ItemStack.EMPTY : par2ItemStack;
      if (!par2ItemStack.isEmpty() && par2ItemStack.getCount() > this.getInventoryStackLimit()) {
         par2ItemStack.setCount(this.getInventoryStackLimit());
      }

      this.markDirty();
      if (!this.world.isRemote) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      }

   }

   public void setInventorySlotContentsFromInfusion(int par1, ItemStack par2ItemStack) {
      this.inventory[par1] = par2ItemStack == null ? ItemStack.EMPTY : par2ItemStack;
      this.markDirty();
      if (!this.world.isRemote) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      }

   }

   // IWorldNameable
   @Override
   public String getName() {
      return this.hasCustomName() ? this.customName : "container.pedestal";
   }

   @Override
   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   @Override
   public net.minecraft.util.text.ITextComponent getDisplayName() {
      return new net.minecraft.util.text.TextComponentTranslation(this.getName());
   }

   /** @deprecated kept for internal use by old TC4 code */
   public String getInventoryName() {
      return getName();
   }

   /** @deprecated kept for internal use by old TC4 code */
   public boolean hasCustomInventoryName() {
      return hasCustomName();
   }

   public void setGuiDisplayName(String par1Str) {
      this.customName = par1Str;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
      this.inventory = new ItemStack[this.getSizeInventory()];
      java.util.Arrays.fill(this.inventory, ItemStack.EMPTY);

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte b0 = nbttagcompound1.getByte("Slot");
         if (b0 >= 0 && b0 < this.inventory.length) {
            this.inventory[b0] = new ItemStack(nbttagcompound1);
         }
      }

   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.inventory.length; ++i) {
         if (this.inventory[i] != null && !this.inventory[i].isEmpty()) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.inventory[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbttagcompound.setTag("Items", nbttaglist);
   }

   public void readFromNBT(NBTTagCompound nbtCompound) {
      super.readFromNBT(nbtCompound);
      if (nbtCompound.hasKey("CustomName")) {
         this.customName = nbtCompound.getString("CustomName");
      }

   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
      super.writeToNBT(nbtCompound);
      if (this.hasCustomInventoryName()) {
         nbtCompound.setString("CustomName", this.customName);
      }
      return nbtCompound;
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
   }

   public boolean canUpdate() {
      return false;
   }

   @Override
   public boolean isUsableByPlayer(EntityPlayer player) {
      return this.world.getTileEntity(this.getPos()) == this && player.getDistanceSq((double) this.getPos().getX() + 0.5, (double) this.getPos().getY() + 0.5, (double) this.getPos().getZ() + 0.5) <= 64.0;
   }

   @Override
   public void openInventory(EntityPlayer player) {
   }

   @Override
   public void closeInventory(EntityPlayer player) {
   }

   @Override
   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return true;
   }

   @Override
   public int getField(int id) { return 0; }

   @Override
   public void setField(int id, int value) { }

   @Override
   public int getFieldCount() { return 0; }

   @Override
   public void clear() {
      for (int i = 0; i < inventory.length; i++) inventory[i] = ItemStack.EMPTY;
   }

   @Override
   public int[] getSlotsForFace(EnumFacing side) {
      return slots;
   }

   @Override
   public boolean canInsertItem(int index, ItemStack stack, EnumFacing side) {
      return this.getStackInSlot(index).isEmpty();
   }

   @Override
   public boolean canExtractItem(int index, ItemStack stack, EnumFacing side) {
      return true;
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 11) {
         if (this.world.isRemote) {
            for(int a = 0; a < Thaumcraft.proxy.particleCount(5); ++a) {
               Thaumcraft.proxy.blockSparkle(this.world, this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ(), 12583104, 2);
            }
         }

         return true;
      } else if (i != 12) {
         return super.receiveClientEvent(i, j);
      } else {
         if (this.world.isRemote) {
            for(int a = 0; a < Thaumcraft.proxy.particleCount(10); ++a) {
               Thaumcraft.proxy.blockSparkle(this.world, this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ(), -9999, 2);
            }
         }

         return true;
      }
   }
}
