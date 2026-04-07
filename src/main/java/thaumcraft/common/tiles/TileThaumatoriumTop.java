package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import net.minecraft.util.math.BlockPos;

public class TileThaumatoriumTop extends TileThaumcraft implements IAspectContainer, IEssentiaTransport, ISidedInventory {
   public TileThaumatorium thaumatorium = null;

   public void updateEntity() {
      if (this.thaumatorium == null) {
         TileEntity tile = this.world.getTileEntity(this.getPos().down());
         if (tile instanceof TileThaumatorium) {
            this.thaumatorium = (TileThaumatorium)tile;
            this.world.notifyBlockUpdate(this.getPos(), this.world.getBlockState(this.getPos()), this.world.getBlockState(this.getPos()), 3);
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
         } else {
            this.world.setBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), this.world.getBlockState(this.getPos()).getBlock().getStateFromMeta(9), 3);
         }
      }

   }

   public int addToContainer(Aspect tt, int am) {
      return this.thaumatorium == null ? am : this.thaumatorium.addToContainer(tt, am);
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      return this.thaumatorium != null && this.thaumatorium.takeFromContainer(tt, am);
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContain(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tt, int am) {
      return this.thaumatorium != null && this.thaumatorium.doesContainerContainAmount(tt, am);
   }

   public int containerContains(Aspect tt) {
      return this.thaumatorium == null ? 0 : this.thaumatorium.containerContains(tt);
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }

   public boolean isConnectable(EnumFacing face) {
      return this.thaumatorium != null && this.thaumatorium.isConnectable(face);
   }

   public boolean canInputFrom(EnumFacing face) {
      return this.thaumatorium != null && this.thaumatorium.canInputFrom(face);
   }

   public boolean canOutputTo(EnumFacing face) {
      return false;
   }

   public void setSuction(Aspect aspect, int amount) {
      if (this.thaumatorium != null) {
         this.thaumatorium.setSuction(aspect, amount);
      }
   }

   public Aspect getSuctionType(EnumFacing loc) {
      return this.thaumatorium == null ? null : this.thaumatorium.getSuctionType(loc);
   }

   public int getSuctionAmount(EnumFacing loc) {
      return this.thaumatorium == null ? 0 : this.thaumatorium.getSuctionAmount(loc);
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return 0;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.thaumatorium == null ? 0 : this.thaumatorium.takeEssentia(aspect, amount, face);
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.thaumatorium == null ? 0 : this.thaumatorium.addEssentia(aspect, amount, face);
   }

   public int getMinimumSuction() {
      return 0;
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public AspectList getAspects() {
      return this.thaumatorium == null ? null : this.thaumatorium.essentia;
   }

   public void setAspects(AspectList aspects) {
      if (this.thaumatorium != null) {
         this.thaumatorium.setAspects(aspects);
      }
   }

   public int getSizeInventory() {
      return 1;
   }

   public ItemStack getStackInSlot(int par1) {
      return this.thaumatorium == null ? ItemStack.EMPTY : this.thaumatorium.getStackInSlot(par1);
   }

   public ItemStack decrStackSize(int par1, int par2) {
      return this.thaumatorium == null ? ItemStack.EMPTY : this.thaumatorium.decrStackSize(par1, par2);
   }

   public ItemStack removeStackFromSlot(int par1) {
      return this.thaumatorium == null ? ItemStack.EMPTY : this.thaumatorium.removeStackFromSlot(par1);
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      if (this.thaumatorium != null) {
         this.thaumatorium.setInventorySlotContents(par1, par2ItemStack);
      }
   }

   public String getName() {
      return "container.alchemyfurnace";
   }

   public boolean hasCustomName() {
      return false;
   }

   public boolean isEmpty() {
      return this.thaumatorium == null || this.thaumatorium.getStackInSlot(0).isEmpty();
   }

   public void clear() {
      if (this.thaumatorium != null) this.thaumatorium.clear();
   }

   public int getField(int id) { return 0; }
   public void setField(int id, int value) {}
   public int getFieldCount() { return 0; }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.world.getTileEntity(this.getPos()) == this && par1EntityPlayer.getDistanceSq((double) this.getPos().getX() + (double) 0.5F, (double) this.getPos().getY() + (double) 0.5F, (double) this.getPos().getZ() + (double) 0.5F) <= (double) 64.0F;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return true;
   }

   @Override
   public int[] getSlotsForFace(EnumFacing side) {
      return new int[]{0};
   }

   @Override
   public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
      return true;
   }

   @Override
   public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
      return true;
   }
}
