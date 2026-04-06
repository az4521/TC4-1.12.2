package thaumcraft.common.tiles;

import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.ItemBathSalts;
import thaumcraft.common.lib.utils.BlockUtils;

public class TileSpa extends TileThaumcraft implements ISidedInventory, IFluidHandler, net.minecraft.util.ITickable {
   private ItemStack[] itemStacks = new ItemStack[1];
   private boolean mix = true;
   private String customName;
   private int counter = 0;
   public FluidTank tank = new FluidTank(5000);

   public void toggleMix() {
      this.mix = !this.mix;
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      this.markDirty();
   }

   public boolean getMix() {
      return this.mix;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.mix = nbttagcompound.getBoolean("mix");
      this.tank.setFluid(FluidStack.loadFluidStackFromNBT(nbttagcompound));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setBoolean("mix", this.mix);
      if (this.tank.getFluid() != null) {
         this.tank.getFluid().writeToNBT(nbttagcompound);
      }

   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
      this.itemStacks = new ItemStack[this.getSizeInventory()];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte b0 = nbttagcompound1.getByte("Slot");
         if (b0 >= 0 && b0 < this.itemStacks.length) {
            this.itemStacks[b0] = new ItemStack(nbttagcompound1);
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound = super.writeToNBT(nbttagcompound);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.itemStacks.length; ++i) {
         if (this.itemStacks[i] != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.itemStacks[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbttagcompound.setTag("Items", nbttaglist);
      return nbttagcompound;
   }

   public int getSizeInventory() {
      return 1;
   }

   public boolean isEmpty() {
      return itemStacks[0] == null || itemStacks[0].isEmpty();
   }

   public ItemStack getStackInSlot(int par1) {
      ItemStack s = this.itemStacks[par1]; return s != null ? s : ItemStack.EMPTY;
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (this.itemStacks[par1] != null) {
          ItemStack itemstack;
          if (this.itemStacks[par1].getCount() <= par2) {
              itemstack = this.itemStacks[par1];
            this.itemStacks[par1] = null;
          } else {
              itemstack = this.itemStacks[par1].splitStack(par2);
            if (this.itemStacks[par1].isEmpty()) {
               this.itemStacks[par1] = null;
            }

          }
          return itemstack;
      } else {
         return null;
      }
   }

   public ItemStack removeStackFromSlot(int par1) {
      if (this.itemStacks[par1] != null) {
         ItemStack itemstack = this.itemStacks[par1];
         this.itemStacks[par1] = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.itemStacks[par1] = par2ItemStack;
      if (par2ItemStack != null && par2ItemStack.getCount() > this.getInventoryStackLimit()) {
         par2ItemStack.setCount(this.getInventoryStackLimit());
      }

   }

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
      return par2ItemStack != null && par2ItemStack.getItem() instanceof ItemBathSalts;
   }

   public int[] getSlotsForFace(EnumFacing par1) {
      return par1 != EnumFacing.UP ? new int[]{0} : new int[0];
   }

   public boolean canInsertItem(int par1, ItemStack par2ItemStack, EnumFacing par3) {
      return par3 != EnumFacing.UP;
   }

   public boolean canExtractItem(int par1, ItemStack par2ItemStack, EnumFacing par3) {
      return par3 != EnumFacing.UP;
   }

   public String getName() {
      return "thaumcraft.spa";
   }

   public boolean hasCustomName() {
      return false;
   }

   public net.minecraft.util.text.ITextComponent getDisplayName() {
      return new net.minecraft.util.text.TextComponentTranslation(getName());
   }

   public int getFieldCount() {
      return 0;
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public void clear() {
      Arrays.fill(itemStacks, null);
   }

   @Override
   public void update() {
      if (!this.world.isRemote && this.counter++ % 40 == 0 && !(this.world.getRedstonePowerFromNeighbors(this.getPos()) > 0) && this.hasIngredients()) {
         Block b = this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())).getBlock();
         int m = this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())).getBlock().getMetaFromState(this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())));
         Block tb = null;
         if (this.mix) {
            tb = ConfigBlocks.blockFluidPure;
         } else {
            tb = this.tank.getFluid().getFluid().getBlock();
         }

         if (b == tb && m == 0) {
            for(int xx = -2; xx <= 2; ++xx) {
               for(int zz = -2; zz <= 2; ++zz) {
                  if (this.isValidLocation(this.getPos().getX() + xx, this.getPos().getY() + 1, this.getPos().getZ() + zz, true, tb)) {
                     this.consumeIngredients();
                     this.world.setBlockState(new BlockPos(this.getPos().getX() + xx, this.getPos().getY() + 1, this.getPos().getZ() + zz), tb.getDefaultState());
                     this.checkQuanta(this.getPos().getX() + xx, this.getPos().getY() + 1, this.getPos().getZ() + zz);
                     return;
                  }
               }
            }
         } else if (this.isValidLocation(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ(), false, tb)) {
            this.consumeIngredients();
            this.world.setBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()), tb.getDefaultState());
            this.checkQuanta(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ());
         }
      }

   }

   private void checkQuanta(int i, int j, int k) {
      Block b = this.world.getBlockState(new BlockPos(i, j, k)).getBlock();
      if (b instanceof BlockFluidBase) {
         float p = ((BlockFluidBase)b).getQuantaPercentage(this.world, new BlockPos(i, j, k));
         if (p < 1.0F) {
            int md = (int)(1.0F / p) - 1;
            if (md >= 0 && md < 16) {
               this.world.setBlockState(new BlockPos(i, j, k), this.world.getBlockState(new BlockPos(i, j, k)).getBlock().getStateFromMeta(md), 3);
            }
         }
      }

   }

   private boolean hasIngredients() {
      if (this.mix) {
         if (this.tank.getInfo().fluid == null || !this.tank.getInfo().fluid.containsFluid(new FluidStack(FluidRegistry.WATER, 1000))) {
            return false;
         }

          return this.itemStacks[0] != null && this.itemStacks[0].getItem() instanceof ItemBathSalts;
      } else return this.tank.getInfo().fluid != null && this.tank.getFluid().getFluid().canBePlacedInWorld() && this.tank.getFluidAmount() >= 1000;
   }

   private void consumeIngredients() {
      if (this.mix) {
         this.decrStackSize(0, 1);
      }

      this.drain(1000, true);
   }

   private boolean isValidLocation(int x, int y, int z, boolean mustBeAdjacent, Block target) {
      if ((target == Blocks.WATER || target == Blocks.FLOWING_WATER) && this.world.provider.isNether()) {
         return false;
      } else {
         Block b = this.world.getBlockState(new BlockPos(x, y, z)).getBlock();
         Block bb = this.world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
         int m = this.world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(this.world.getBlockState(new BlockPos(x, y, z)));
         if (bb.isSideSolid(this.world.getBlockState(new BlockPos(x, y - 1, z)), this.world, new BlockPos(x, y - 1, z), EnumFacing.UP) && b.isReplaceable(this.world, new BlockPos(x, y, z)) && (b != target || m != 0)) {
            return !mustBeAdjacent || BlockUtils.isBlockTouching(this.world, x, y, z, target, 0);
         } else {
            return false;
         }
      }
   }

   @Override
   public IFluidTankProperties[] getTankProperties() {
      return this.tank.getTankProperties();
   }

   @Override
   public int fill(FluidStack resource, boolean doFill) {
      int df = this.tank.fill(resource, doFill);
      if (df > 0 && doFill) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }
      return df;
   }

   @Override
   public FluidStack drain(FluidStack resource, boolean doDrain) {
      return resource != null && resource.isFluidEqual(this.tank.getFluid()) ? this.tank.drain(resource.amount, doDrain) : null;
   }

   @Override
   public FluidStack drain(int maxDrain, boolean doDrain) {
      FluidStack fs = this.tank.drain(maxDrain, doDrain);
      if (fs != null && doDrain) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }
      return fs;
   }
}
