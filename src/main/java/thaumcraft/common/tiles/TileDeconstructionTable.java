package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.research.ResearchManager;

public class TileDeconstructionTable extends TileThaumcraft implements ISidedInventory, net.minecraft.util.ITickable {
   public Aspect aspect;
   public int breaktime;
   private ItemStack[] itemStacks = new ItemStack[1];
   private String customName;
   private static final int[] sides = new int[]{0};

   public TileDeconstructionTable() {
      java.util.Arrays.fill(this.itemStacks, ItemStack.EMPTY);
   }

   public int getSizeInventory() {
      return 1;
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
            if (this.itemStacks[par1].isEmpty()) {
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
      this.itemStacks[par1] = par2ItemStack == null ? ItemStack.EMPTY : par2ItemStack;
      if (!this.itemStacks[par1].isEmpty() && this.itemStacks[par1].getCount() > this.getInventoryStackLimit()) {
         this.itemStacks[par1].setCount(this.getInventoryStackLimit());
      }
      this.markDirty();
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.decontable";
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

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.aspect = Aspect.getAspect(nbttagcompound.getString("Aspect"));
      NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
      this.itemStacks = new ItemStack[this.getSizeInventory()];
      java.util.Arrays.fill(this.itemStacks, ItemStack.EMPTY);
      for (int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte b0 = nbttagcompound1.getByte("Slot");
         if (b0 >= 0 && b0 < this.itemStacks.length) {
            this.itemStacks[b0] = new ItemStack(nbttagcompound1);
         }
      }
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      if (this.aspect != null) {
         nbttagcompound.setString("Aspect", this.aspect.getTag());
      }
      NBTTagList nbttaglist = new NBTTagList();
      for (int i = 0; i < this.itemStacks.length; ++i) {
         if (this.itemStacks[i] != null && !this.itemStacks[i].isEmpty()) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.itemStacks[i].writeToNBT(nbttagcompound1);
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

   public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
      super.writeToNBT(nbtCompound);
      if (this.hasCustomName()) {
         nbtCompound.setString("CustomName", this.customName);
      }
      return nbtCompound;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   @SideOnly(Side.CLIENT)
   public int getBreakTimeScaled(int par1) {
      return this.breaktime * par1 / 40;
   }

   @Override
   public void update() {
      boolean flag1 = false;
      if (!this.world.isRemote) {
         if (this.breaktime == 0 && this.canBreak()) {
            this.breaktime = 40;
            flag1 = true;
         }
         if (this.breaktime > 0 && this.canBreak()) {
            --this.breaktime;
            if (this.breaktime == 0) {
               this.breaktime = 0;
               this.breakItem();
               flag1 = true;
            }
         } else {
            this.breaktime = 0;
         }
      }
      if (flag1) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }
   }

   private boolean canBreak() {
      if (this.itemStacks[0] != null && !this.itemStacks[0].isEmpty() && this.aspect == null) {
         AspectList al = ThaumcraftCraftingManager.getObjectTags(this.itemStacks[0]);
         al = ThaumcraftCraftingManager.getBonusTags(this.itemStacks[0], al);
         return al != null && al.size() != 0;
      } else {
         return false;
      }
   }

   public void breakItem() {
      if (this.canBreak()) {
         AspectList al = ThaumcraftCraftingManager.getObjectTags(this.itemStacks[0]);
         al = ThaumcraftCraftingManager.getBonusTags(this.itemStacks[0], al);
         AspectList primals = ResearchManager.reduceToPrimals(al);
         if (this.world.rand.nextInt(80) < primals.visSize()) {
            this.aspect = primals.getAspects()[this.world.rand.nextInt(primals.getAspects().length)];
         }
         this.itemStacks[0].shrink(1);
         if (this.itemStacks[0].getCount() <= 0) {
            this.itemStacks[0] = ItemStack.EMPTY;
      }
   }
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.world.getTileEntity(this.getPos()) == this &&
             par1EntityPlayer.getDistanceSq(this.getPos().getX() + 0.5D, this.getPos().getY() + 0.5D, this.getPos().getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer player) {}
   public void closeInventory(EntityPlayer player) {}

   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      AspectList al = ThaumcraftCraftingManager.getObjectTags(par2ItemStack);
      al = ThaumcraftCraftingManager.getBonusTags(par2ItemStack, al);
      return al != null && al.size() > 0;
   }

   public int[] getSlotsForFace(EnumFacing side) {
      return side != EnumFacing.UP ? sides : new int[0];
   }

   public boolean canInsertItem(int par1, ItemStack par2ItemStack, EnumFacing side) {
      return side != EnumFacing.UP && this.isItemValidForSlot(par1, par2ItemStack);
   }

   public boolean canExtractItem(int par1, ItemStack par2ItemStack, EnumFacing side) {
      return true;
   }

   public int getField(int id) { return 0; }
   public void setField(int id, int value) {}
   public int getFieldCount() { return 0; }
   public void clear() { for (int i = 0; i < itemStacks.length; i++) itemStacks[i] = null; }
}
