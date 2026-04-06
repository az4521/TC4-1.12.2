package thaumcraft.common.tiles;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.common.container.ContainerMagicBox;
import net.minecraft.util.math.BlockPos;

public class TileMagicBox extends TileThaumcraft implements IInventory {
   ArrayList boxContents = new ArrayList<>();
   WorldCoordinates master = null;
   byte sorting = -1;
   short linkedBoxes = -1;
   public static ContainerMagicBox tc;

   public int getSizeInventory() {
      return 27 * (this.getInventory().linkedBoxes + 1);
   }

   public boolean isEmpty() {
      for (Object obj : this.getContents()) {
         if (obj != null && !((ItemStack) obj).isEmpty()) {
            return false;
         }
      }
      return true;
   }

   private ArrayList getContents() {
      return this.master != null ? this.getInventory().boxContents : this.boxContents;
   }

   private TileMagicBox getInventory() {
      TileEntity tile = null;
      if (this.master != null) {
         tile = this.world.getTileEntity(new BlockPos(this.master.x, this.master.y, this.master.z));
      }

      return tile instanceof TileMagicBox ? (TileMagicBox)tile : this;
   }

   public ItemStack getStackInSlot(int par1) {
      return par1 >= this.getContents().size() ? ItemStack.EMPTY : (ItemStack)this.getContents().get(par1);
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (par1 < this.getContents().size() && this.getContents().get(par1) != null) {
         if (((ItemStack)this.getContents().get(par1)).getCount() <= par2) {
            ItemStack var3 = (ItemStack)this.getContents().get(par1);
            this.getContents().remove(par1);
            this.getInventory().markDirty();
            return var3;
         } else {
            ItemStack var3 = ((ItemStack)this.getContents().get(par1)).splitStack(par2);
            if (((ItemStack)this.getContents().get(par1)).getCount() == 0) {
               this.getContents().remove(par1);
            }

            this.getInventory().markDirty();
            return var3;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeStackFromSlot(int par1) {
      if (par1 < this.getContents().size() && this.getContents().get(par1) != null) {
         ItemStack var2 = (ItemStack)this.getContents().get(par1);
         this.getContents().remove(par1);
         return var2;
      } else {
         return ItemStack.EMPTY;
      }
   }

   /** @deprecated kept for internal use during container close */
   public ItemStack getStackInSlotOnClosing(int par1) {
      return removeStackFromSlot(par1);
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      if (par2ItemStack == null) par2ItemStack = ItemStack.EMPTY;
      if (par1 >= this.getContents().size() && !par2ItemStack.isEmpty()) {
         this.getContents().add(par2ItemStack);
      } else if (!par2ItemStack.isEmpty()) {
         this.getContents().set(par1, par2ItemStack);
      } else if (par1 < this.getContents().size()) {
         this.getContents().remove(par1);
      }

      if (!par2ItemStack.isEmpty() && par2ItemStack.getCount() > this.getInventoryStackLimit()) {
         par2ItemStack.setCount(this.getInventoryStackLimit());
      }

      this.getInventory().markDirty();
   }

   public void markDirty() {
      super.markDirty();
      this.sort();
   }

   public String getName() {
      return "Magic Box";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readFromNBT(par1NBTTagCompound);
      NBTTagList var2 = par1NBTTagCompound.getTagList("Items", 10);
      this.boxContents = new ArrayList<>();

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         this.boxContents.add(new ItemStack(var4));
      }

      this.sort();
   }

   public void readCustomNBT(NBTTagCompound par1NBTTagCompound) {
      this.sorting = par1NBTTagCompound.getByte("sort");
      this.master = null;
      if (par1NBTTagCompound.hasKey("w_x")) {
         this.master = new WorldCoordinates();
         this.master.readNBT(par1NBTTagCompound);
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeToNBT(par1NBTTagCompound);
      NBTTagList var2 = new NBTTagList();

       for (Object boxContent : this.boxContents) {
           if (boxContent != null) {
               NBTTagCompound var4 = new NBTTagCompound();
               ((ItemStack) boxContent).writeToNBT(var4);
               var2.appendTag(var4);
           }
       }

      par1NBTTagCompound.setTag("Items", var2);
      return par1NBTTagCompound;
   }

   public void writeCustomNBT(NBTTagCompound par1NBTTagCompound) {
      par1NBTTagCompound.setByte("sort", this.sorting);
      if (this.master != null) {
         this.master.writeNBT(par1NBTTagCompound);
      }

   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.world.getTileEntity(this.getPos()) == this && par1EntityPlayer.getDistanceSq((double) this.getPos().getX() + (double) 0.5F, (double) this.getPos().getY() + (double) 0.5F, (double) this.getPos().getZ() + (double) 0.5F) <= (double) 64.0F;
   }

   public void updateEntity() {
      if (this.getInventory() == this && this.linkedBoxes < 0) {
         this.refreshLinks();
      }
   }

   public boolean receiveClientEvent(int par1, int par2) {
      if (par1 == 1) {
         return true;
      } else {
         return par1 == 2 || this.isInvalid();
      }
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public void invalidate() {
      super.invalidate();
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      this.getContents().clear();
   }

   public void sort() {
      if (this.getWorld() != null && this.sorting >= 0) {
         boolean done = false;

         while(!done) {
            done = true;

            for(int i = 0; i < this.getContents().size() - 1; ++i) {
               done = this.swopSlots(i, i + 1);
               if (((ItemStack)this.getContents().get(i)).getCount() < ((ItemStack)this.getContents().get(i)).getMaxStackSize() && ((ItemStack)this.getContents().get(i)).isItemEqual((ItemStack)this.getContents().get(i + 1)) && ItemStack.areItemStackTagsEqual((ItemStack)this.getContents().get(i), (ItemStack)this.getContents().get(i + 1))) {
                  ItemStack is1 = ((ItemStack)this.getContents().get(i)).copy();
                  ItemStack is2 = ((ItemStack)this.getContents().get(i + 1)).copy();
                  int c = Math.min(is1.getMaxStackSize() - is1.getCount(), is2.getCount());
                  is1.grow(c);
                  is2.shrink(c);
                  this.getContents().set(i, is1);
                  done = false;
                  if (is2.getCount() <= 0) {
                     this.getContents().remove(i + 1);
                     break;
                  }

                  this.getContents().set(i + 1, is2);
               }
            }
         }

      }
   }

   private boolean swopSlots(int i, int j) {
      if (this.sorting != 0 && this.sorting != 1) {
         if (this.sorting == 2 && ((ItemStack)this.getContents().get(i)).getDisplayName() != null && ((ItemStack)this.getContents().get(j)).getDisplayName() != null) {
            String s1 = "";
            String s2 = "";
            Item itemI = ((ItemStack)this.getContents().get(i)).getItem();
            Item itemJ = ((ItemStack)this.getContents().get(j)).getItem();
            if (itemI.getRegistryName() != null) {
               s1 = s1 + itemI.getRegistryName().getNamespace();
            } else if (Block.getBlockFromItem(itemI).getRegistryName() != null) {
               s1 = s1 + Block.getBlockFromItem(itemI).getRegistryName().getNamespace();
            }

            if (itemJ.getRegistryName() != null) {
               s2 = s2 + itemJ.getRegistryName().getNamespace();
            } else if (Block.getBlockFromItem(itemJ).getRegistryName() != null) {
               s2 = s2 + Block.getBlockFromItem(itemJ).getRegistryName().getNamespace();
            }

            s1 = s1 + ((ItemStack)this.getContents().get(i)).getDisplayName();
            s2 = s2 + ((ItemStack)this.getContents().get(j)).getDisplayName();
            if (((ItemStack)this.getContents().get(i)).hasTagCompound()) {
               s1 = s1 + ((ItemStack)this.getContents().get(i)).getTagCompound().hashCode();
            }

            if (((ItemStack)this.getContents().get(j)).hasTagCompound()) {
               s2 = s2 + ((ItemStack)this.getContents().get(j)).getTagCompound().hashCode();
            }

            int r = s1.compareToIgnoreCase(s2);
            if (r > 0 && this.sorting == 2) {
               ItemStack is1 = ((ItemStack)this.getContents().get(i)).copy();
               ItemStack is2 = ((ItemStack)this.getContents().get(j)).copy();
               this.getContents().set(i, is2);
               this.getContents().set(j, is1);
               return false;
            }
         }
      } else if (((ItemStack)this.getContents().get(i)).getDisplayName() != null && ((ItemStack)this.getContents().get(j)).getDisplayName() != null) {
         String s1 = "";
         String s2 = "";
         s1 = s1 + ((ItemStack)this.getContents().get(i)).getDisplayName();
         s2 = s2 + ((ItemStack)this.getContents().get(j)).getDisplayName();
         if (((ItemStack)this.getContents().get(i)).hasTagCompound()) {
            s1 = s1 + ((ItemStack)this.getContents().get(i)).getTagCompound().hashCode();
         }

         if (((ItemStack)this.getContents().get(j)).hasTagCompound()) {
            s2 = s2 + ((ItemStack)this.getContents().get(j)).getTagCompound().hashCode();
         }

         int r = s1.compareToIgnoreCase(s2);
         if (r > 0 && this.sorting == 0 || r < 0 && this.sorting == 1) {
            ItemStack is1 = ((ItemStack)this.getContents().get(i)).copy();
            ItemStack is2 = ((ItemStack)this.getContents().get(j)).copy();
            this.getContents().set(i, is2);
            this.getContents().set(j, is1);
            return false;
         }
      }

      return true;
   }

   public void refreshLinks() {
      if (this.getInventory() == this) {
         this.linkedBoxes = 0;
         ArrayList<WorldCoordinates> list = new ArrayList<>();
         this.findBoxes(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), list);
         this.linkedBoxes = (short)list.size();
      }
   }

   private void findBoxes(int x, int y, int z, ArrayList list) {
      if (list.size() < 1024) {
         for(int a = 0; a < 6; ++a) {
            EnumFacing dir = EnumFacing.byIndex(a);
            TileEntity tile = this.world.getTileEntity(new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset()));
            if (tile instanceof TileMagicBox) {
               WorldCoordinates wc = new WorldCoordinates(tile);
               if (!list.contains(wc)) {
                  list.add(wc);
                  this.findBoxes(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset(), list);
               }
            }
         }

      }
   }
}
