package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.entities.EntityItemGrate;

public class TileGrate extends TileEntity implements ISidedInventory {
   public int getSizeInventory() {
      return 1;
   }

   public ItemStack getStackInSlot(int par1) {
      return null;
   }

   public ItemStack decrStackSize(int par1, int par2) {
      return null;
   }


   public void setInventorySlotContents(int par1, ItemStack stack) {
      if (!this.world.isRemote) {
         EntityItemGrate ei = new EntityItemGrate(this.world, (double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + 0.6, (double)this.getPos().getZ() + (double)0.5F, stack.copy());
         ei.motionY = -0.1;
         ei.motionX = 0.0F;
         ei.motionZ = 0.0F;
         this.world.spawnEntity(ei);
      }

   }

   public int getInventoryStackLimit() {
      return 64;
   }


   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()))) == 5;
   }

   @Override
   public int[] getSlotsForFace(EnumFacing side) {
      return this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()))) == 5 && side == EnumFacing.UP ? new int[]{0} : new int[0];
   }

   @Override
   public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
      return this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()))) == 5 && direction == EnumFacing.UP;
   }

   @Override
   public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
      return true;
   }

   public String getName() {
      return "thaumcraft.grate";
   }

   public boolean hasCustomName() {
      return false;
   }

   public boolean isEmpty() {
      return true;
   }

   public ItemStack removeStackFromSlot(int index) {
      return ItemStack.EMPTY;
   }

   public void clear() {
   }

   public int getField(int id) { return 0; }
   public void setField(int id, int value) {}
   public int getFieldCount() { return 0; }

   public void openInventory(EntityPlayer player) {}
   public void closeInventory(EntityPlayer player) {}
   public boolean isUsableByPlayer(EntityPlayer player) { return false; }

   public boolean canUpdate() {
      return false;
   }
}
