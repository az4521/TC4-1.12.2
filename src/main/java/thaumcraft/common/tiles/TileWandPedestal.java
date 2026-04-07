package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.nodes.INode;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.baubles.ItemAmuletVis;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.research.ResearchManager;

public class TileWandPedestal extends TileThaumcraft implements ISidedInventory, IAspectContainer {
   private static final int[] slots = new int[]{0};
   private ItemStack[] inventory = new ItemStack[1];
   private String customName;
   int counter = 0;
   boolean somethingChanged = false;
   public boolean draining = false;
   public int drainX = 0;
   public int drainY = 0;
   public int drainZ = 0;
   public int drainColor = 0;
   ArrayList<BlockPos> nodes = null;

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(2.0F, 2.0F, 2.0F);
   }

   public int getSizeInventory() {
      return 1;
   }

   public ItemStack getStackInSlot(int par1) {
      return this.inventory[par1];
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (!this.inventory[par1].isEmpty()) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
          ItemStack itemstack;
          if (this.inventory[par1].getCount() <= par2) {
              itemstack = this.inventory[par1];
            this.inventory[par1] = ItemStack.EMPTY;
          } else {
              itemstack = this.inventory[par1].splitStack(par2);
            if (this.inventory[par1].isEmpty()) {
               this.inventory[par1] = ItemStack.EMPTY;
            }

          }
          this.markDirty();
          return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack getStackInSlotOnClosing(int par1) {
      if (!this.inventory[par1].isEmpty()) {
         ItemStack itemstack = this.inventory[par1];
         this.inventory[par1] = ItemStack.EMPTY;
         this.markDirty();
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.inventory[par1] = par2ItemStack == null ? ItemStack.EMPTY : par2ItemStack;
      if (!this.inventory[par1].isEmpty() && this.inventory[par1].getCount() > this.getInventoryStackLimit()) {
         this.inventory[par1].setCount(this.getInventoryStackLimit());
      }

      this.markDirty();
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
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
         if (!this.inventory[i].isEmpty()) {
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
      if (this.hasCustomName()) {
         nbtCompound.setString("CustomName", this.customName);
      }
      return nbtCompound;
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      if (this.world != null && this.world.isRemote) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      }

   }

   public void updateEntity() {
            if (this.nodes == null) {
         this.findNodes();
      }

      ++this.counter;
      boolean recalc = false;
      if (this.counter % 20 == 0 && this.somethingChanged && this.nodes != null && !this.nodes.isEmpty() && !this.getStackInSlot(0).isEmpty()) {
         this.world.notifyNeighborsOfStateChange(this.getPos(), this.getBlockType(), false);
         this.somethingChanged = false;
      }

      if (this.counter % 5 == 0 && this.nodes != null && !this.nodes.isEmpty() && !this.getStackInSlot(0).isEmpty()) {
         BlockPos abovePos = new BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ());
         boolean hasThingy = this.world.getBlockState(abovePos).getBlock() == ConfigBlocks.blockStoneDevice
               && this.world.getBlockState(abovePos).getBlock().getMetaFromState(this.world.getBlockState(abovePos)) == 8;

          if (this.getStackInSlot(0).getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting)this.getStackInSlot(0).getItem();
            int min = 1;
            if (wand.getCap(this.getStackInSlot(0)).getTag().equals("iron") || wand.getRod(this.getStackInSlot(0)).getTag().equals("wood")) {
               min = 0;
            }

            AspectList as = wand.getAspectsWithRoom(this.getStackInSlot(0));
            this.draining = false;
            if (as != null && as.size() > 0) {
               label152:
               for(BlockPos co : this.nodes) {
                  TileEntity te = this.world.getTileEntity(co);
                  if (te instanceof INode && !(te instanceof TileJarNode)) {
                     INode node = (INode)te;

                     for(Aspect aspect : as.getAspects()) {
                        if (node.getAspects().getAmount(aspect) > min) {
                           wand.addVis(this.getStackInSlot(0), aspect, 1, true);
                           node.takeFromContainer(aspect, 1);
                           this.somethingChanged = true;
                           this.draining = true;
                           if (this.world.isRemote) {
                              this.drainX = co.getX();
                              this.drainY = co.getY();
                              this.drainZ = co.getZ();
                              this.drainColor = aspect.getColor();
                           }
                           break label152;
                        }
                     }

                     if (hasThingy) {
                        for(Aspect aspect : node.getAspects().getAspects()) {
                           if (aspect != null && !aspect.isPrimal()) {
                              AspectList primals = ResearchManager.reduceToPrimals((new AspectList()).add(aspect, 1));

                              for(Aspect aspect2 : as.getAspects()) {
                                 if (primals.getAmount(aspect2) > 0 && node.getAspects().getAmount(aspect) > min) {
                                    wand.addVis(this.getStackInSlot(0), aspect2, 1, true);
                                    node.takeFromContainer(aspect, 1);
                                    this.somethingChanged = true;
                                    this.draining = true;
                                    if (this.world.isRemote) {
                                       this.drainX = co.getX();
                                       this.drainY = co.getY();
                                       this.drainZ = co.getZ();
                                       this.drainColor = aspect.getColor();
                                    }
                                    break label152;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (!this.draining) {
                  recalc = true;
               }
            }
         } else if (this.getStackInSlot(0).getItem() instanceof ItemAmuletVis) {
            ItemAmuletVis amulet = (ItemAmuletVis)this.getStackInSlot(0).getItem();
            int min = 1;
            AspectList as = amulet.getAspectsWithRoom(this.getStackInSlot(0));
            this.draining = false;
            if (as != null && as.size() > 0) {
               label207:
               for(BlockPos co : this.nodes) {
                  TileEntity te = this.world.getTileEntity(co);
                  if (te instanceof INode && !(te instanceof TileJarNode)) {
                     INode node = (INode)te;

                     for(Aspect aspect : as.getAspects()) {
                        if (node.getAspects().getAmount(aspect) > min) {
                           amulet.addVis(this.getStackInSlot(0), aspect, 1, true);
                           node.takeFromContainer(aspect, 1);
                           this.draining = true;
                           if (this.world.isRemote) {
                              this.drainX = co.getX();
                              this.drainY = co.getY();
                              this.drainZ = co.getZ();
                              this.drainColor = aspect.getColor();
                           }
                           break label207;
                        }
                     }

                     if (hasThingy) {
                        for(Aspect aspect : node.getAspects().getAspects()) {
                           if (aspect != null && !aspect.isPrimal()) {
                              AspectList primals = ResearchManager.reduceToPrimals((new AspectList()).add(aspect, 1));

                              for(Aspect aspect2 : as.getAspects()) {
                                 if (primals.getAmount(aspect2) > 0 && node.getAspects().getAmount(aspect) > min) {
                                    amulet.addVis(this.getStackInSlot(0), aspect2, 1, true);
                                    node.takeFromContainer(aspect, 1);
                                    this.draining = true;
                                    if (this.world.isRemote) {
                                       this.drainX = co.getX();
                                       this.drainY = co.getY();
                                       this.drainZ = co.getZ();
                                       this.drainColor = aspect.getColor();
                                    }
                                    break label207;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (!this.draining) {
                  recalc = true;
               }
            }
         }
      }

      if (this.counter % 100 == 0 && (recalc || this.nodes.isEmpty())) {
         this.findNodes();
      }

   }

   private void findNodes() {
      this.nodes = new ArrayList<>();

      for(int xx = -8; xx <= 8; ++xx) {
         for(int yy = -8; yy <= 8; ++yy) {
            for(int zz = -8; zz <= 8; ++zz) {
               TileEntity te = this.world.getTileEntity(new BlockPos(this.getPos().getX() + xx, this.getPos().getY() + yy, this.getPos().getZ() + zz));
               if (te instanceof INode) {
                  this.nodes.add(new BlockPos(this.getPos().getX() + xx, this.getPos().getY() + yy, this.getPos().getZ() + zz));
               }
            }
         }
      }

   }

   @Override
   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.world.getTileEntity(this.getPos()) == this && par1EntityPlayer.getDistanceSq((double) this.getPos().getX() + (double) 0.5F, (double) this.getPos().getY() + (double) 0.5F, (double) this.getPos().getZ() + (double) 0.5F) <= (double) 64.0F;
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack stack : this.inventory) {
         if (!stack.isEmpty()) return false;
      }
      return true;
   }

   @Override
   public ItemStack removeStackFromSlot(int index) {
      if (!this.inventory[index].isEmpty()) {
         ItemStack stack = this.inventory[index];
         this.inventory[index] = ItemStack.EMPTY;
         return stack;
      }
      return ItemStack.EMPTY;
   }

   @Override
   public String getName() {
      return this.hasCustomName() ? this.customName : "container.wandpedestal";
   }

   @Override
   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   @Override
   public int getField(int id) { return 0; }

   @Override
   public void setField(int id, int value) {}

   @Override
   public int getFieldCount() { return 0; }

   @Override
   public void clear() {
      for (int i = 0; i < this.inventory.length; i++) this.inventory[i] = ItemStack.EMPTY;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return !par2ItemStack.isEmpty() && (par2ItemStack.getItem() instanceof ItemWandCasting || par2ItemStack.getItem() instanceof ItemAmuletVis);
   }

   @Override
   public int[] getSlotsForFace(EnumFacing side) {
      return slots;
   }

   @Override
   public boolean canInsertItem(int index, ItemStack stack, EnumFacing side) {
      return this.getStackInSlot(index).isEmpty() && (stack.getItem() instanceof ItemWandCasting || stack.getItem() instanceof ItemAmuletVis);
   }

   @Override
   public boolean canExtractItem(int index, ItemStack stack, EnumFacing side) {
      return true;
   }

   public AspectList getAspects() {
      if (!this.getStackInSlot(0).isEmpty() && this.getStackInSlot(0).getItem() instanceof ItemWandCasting) {
         ItemWandCasting wand = (ItemWandCasting)this.getStackInSlot(0).getItem();
         AspectList al = wand.getAllVis(this.getStackInSlot(0));
         AspectList out = new AspectList();

         for(Aspect a : al.getAspectsSorted()) {
            out.add(a, al.getAmount(a) / 100);
         }

         return out;
      } else if (!this.getStackInSlot(0).isEmpty() && this.getStackInSlot(0).getItem() instanceof ItemAmuletVis) {
         ItemAmuletVis amulet = (ItemAmuletVis)this.getStackInSlot(0).getItem();
         AspectList al = amulet.getAllVis(this.getStackInSlot(0));
         AspectList out = new AspectList();

         for(Aspect a : al.getAspectsSorted()) {
            out.add(a, al.getAmount(a) / 100);
         }

         return out;
      } else {
         return null;
      }
   }

   public void setAspects(AspectList aspects) {
   }

   public int addToContainer(Aspect tag, int amount) {
      return 0;
   }

   public boolean takeFromContainer(Aspect tag, int amount) {
      return false;
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tag, int amount) {
      return false;
   }

   public boolean doesContainerContain(AspectList ot) {
      return false;
   }

   public int containerContains(Aspect tag) {
      return 0;
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }
}
