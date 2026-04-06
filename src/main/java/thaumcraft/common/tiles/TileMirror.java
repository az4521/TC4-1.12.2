package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import thaumcraft.client.fx.ParticleSpellCustom;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.Utils;
import net.minecraft.util.math.BlockPos;

public class TileMirror extends TileThaumcraft implements IInventory {
   public boolean linked = false;
   public int linkX;
   public int linkY;
   public int linkZ;
   public int linkDim;
   public int instability;
   int count = 0;
   int inc = 40;
   private ArrayList outputStacks = new ArrayList<>();

   public void restoreLink() {
      if (this.isDestinationValid()) {
         World targetWorld = DimensionManager.getWorld(this.linkDim);
         if (targetWorld == null) {
            return;
         }

         TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
         if (te instanceof TileMirror) {
            TileMirror tm = (TileMirror)te;
            tm.linked = true;
            tm.linkX = this.getPos().getX();
            tm.linkY = this.getPos().getY();
            tm.linkZ = this.getPos().getZ();
            tm.linkDim = this.world.provider.getDimension();
            { net.minecraft.block.state.IBlockState _bs = targetWorld.getBlockState(tm.getPos()); targetWorld.notifyBlockUpdate(tm.getPos(), _bs, _bs, 3); }
            this.linked = true;
            this.markDirty();
            tm.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         }
      }

   }

   public void invalidateLink() {
      World targetWorld = DimensionManager.getWorld(this.linkDim);
      if (targetWorld != null) {
         if (Utils.isChunkLoaded(targetWorld, this.linkX, this.linkZ)) {
            TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
            if (te instanceof TileMirror) {
               TileMirror tm = (TileMirror)te;
               tm.linked = false;
               this.markDirty();
               tm.markDirty();
               { BlockPos _lp = new BlockPos(this.linkX, this.linkY, this.linkZ); net.minecraft.block.state.IBlockState _bs = targetWorld.getBlockState(_lp); targetWorld.notifyBlockUpdate(_lp, _bs, _bs, 3); }
            }

         }
      }
   }

   public boolean isLinkValid() {
      if (!this.linked) {
         return false;
      } else {
         World targetWorld = DimensionManager.getWorld(this.linkDim);
         if (targetWorld == null) {
            return false;
         } else {
            TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
            if (te instanceof TileMirror) {
               TileMirror tm = (TileMirror)te;
               if (!tm.linked) {
                  this.linked = false;
                  this.markDirty();
                  { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                  return false;
               } else if (tm.linkX == this.getPos().getX() && tm.linkY == this.getPos().getY() && tm.linkZ == this.getPos().getZ() && tm.linkDim == this.world.provider.getDimension()) {
                  return true;
               } else {
                  this.linked = false;
                  this.markDirty();
                  { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                  return false;
               }
            } else {
               this.linked = false;
               this.markDirty();
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
               return false;
            }
         }
      }
   }

   public boolean isLinkValidSimple() {
      if (!this.linked) {
         return false;
      } else {
         World targetWorld = DimensionManager.getWorld(this.linkDim);
         if (targetWorld == null) {
            return false;
         } else {
            TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
            if (te instanceof TileMirror) {
               TileMirror tm = (TileMirror)te;
               if (!tm.linked) {
                  return false;
               } else {
                  return tm.linkX == this.getPos().getX() && tm.linkY == this.getPos().getY() && tm.linkZ == this.getPos().getZ() && tm.linkDim == this.world.provider.getDimension();
               }
            } else {
               return false;
            }
         }
      }
   }

   public boolean isDestinationValid() {
      World targetWorld = DimensionManager.getWorld(this.linkDim);
      if (targetWorld == null) {
         return false;
      } else {
         TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
         if (te instanceof TileMirror) {
            TileMirror tm = (TileMirror)te;
            return !tm.isLinkValid();
         } else {
            this.linked = false;
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            return false;
         }
      }
   }

   public boolean transport(EntityItem ie) {
      ItemStack items = ie.getItem();
      if (this.linked && this.isLinkValid()) {
         World world = DimensionManager.getWorld(this.linkDim);
         TileEntity target = world.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
         if (target instanceof TileMirror) {
            ((TileMirror)target).addStack(items);
            this.addInstability(null, items.getCount());
            ie.setDead();
            this.markDirty();
            target.markDirty();
            this.world.addBlockEvent(this.getPos(), ConfigBlocks.blockMirror, 1, 0);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void eject() {
      if (!this.outputStacks.isEmpty() && this.count > 20) {
         int i = this.world.rand.nextInt(this.outputStacks.size());
         if (this.outputStacks.get(i) != null) {
            ItemStack outItem = ((ItemStack)this.outputStacks.get(i)).copy();
            outItem.setCount(1);
            if (this.spawnItem(outItem)) {
               ((ItemStack)this.outputStacks.get(i)).shrink(1);
               this.addInstability(null, 1);
               this.world.addBlockEvent(this.getPos(), ConfigBlocks.blockMirror, 1, 0);
               if (((ItemStack)this.outputStacks.get(i)).isEmpty()) {
                  this.outputStacks.remove(i);
               }

               this.markDirty();
            }
         }
      }

   }

   public boolean spawnItem(ItemStack stack) {
      try {
         EnumFacing face = EnumFacing.byIndex(
        this.getBlockMetadata());
         EntityItem ie2 = new EntityItem(this.world, (double)this.getPos().getX() + (double)0.5F - (double)face.getXOffset() * 0.3, (double)this.getPos().getY() + (double)0.5F - (double)face.getYOffset() * 0.3, (double)this.getPos().getZ() + (double)0.5F - (double)face.getZOffset() * 0.3, stack);
         ie2.motionX = (float)face.getXOffset() * 0.15F;
         ie2.motionY = (float)face.getYOffset() * 0.15F;
         ie2.motionZ = (float)face.getZOffset() * 0.15F;
         ie2.timeUntilPortal = 20;
         this.world.spawnEntity(ie2);
         return true;
      } catch (Exception var4) {
         return false;
      }
   }

   protected void addInstability(World targetWorld, int amt) {
      this.instability += amt;
      if (targetWorld != null) {
         TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
         if (te instanceof TileMirror) {
            ((TileMirror)te).instability += amt;
            if (((TileMirror)te).instability < 0) {
               ((TileMirror)te).instability = 0;
            }

            te.markDirty();
         }
      }

   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      super.readCustomNBT(nbttagcompound);
      this.linked = nbttagcompound.getBoolean("linked");
      this.linkX = nbttagcompound.getInteger("linkX");
      this.linkY = nbttagcompound.getInteger("linkY");
      this.linkZ = nbttagcompound.getInteger("linkZ");
      this.linkDim = nbttagcompound.getInteger("linkDim");
      this.instability = nbttagcompound.getInteger("instability");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      super.writeCustomNBT(nbttagcompound);
      nbttagcompound.setBoolean("linked", this.linked);
      nbttagcompound.setInteger("linkX", this.linkX);
      nbttagcompound.setInteger("linkY", this.linkY);
      nbttagcompound.setInteger("linkZ", this.linkZ);
      nbttagcompound.setInteger("linkDim", this.linkDim);
      nbttagcompound.setInteger("instability", this.instability);
   }

   @SideOnly(Side.CLIENT)
   public boolean receiveClientEvent(int i, int j) {
      if (i != 1) {
         return super.receiveClientEvent(i, j);
      } else {
         if (this.world.isRemote) {
            EnumFacing face = EnumFacing.byIndex(
        this.getBlockMetadata());

            for(int q = 0; q < Thaumcraft.proxy.particleCount(1); ++q) {
               double xx = (double)this.getPos().getX() + 0.33 + (double)(this.world.rand.nextFloat() * 0.33F) - (double)face.getXOffset() / (double)2.0F;
               double yy = (double)this.getPos().getY() + 0.33 + (double)(this.world.rand.nextFloat() * 0.33F) - (double)face.getYOffset() / (double)2.0F;
               double zz = (double)this.getPos().getZ() + 0.33 + (double)(this.world.rand.nextFloat() * 0.33F) - (double)face.getZOffset() / (double)2.0F;
               ParticleSpellCustom var21 = new ParticleSpellCustom(this.world, xx, yy, zz,
                     (double)face.getXOffset() * 0.05, (double)face.getYOffset() * 0.05, (double)face.getZOffset() * 0.05);
               var21.setAlphaF(0.5F);
               var21.setRBGColorF(0.0F, 0.0F, 0.0F);
               thaumcraft.client.fx.ParticleEngine.instance.addEffect(this.world, var21);
            }
         }

         return true;
      }
   }

   public void updateEntity() {
            if (!this.world.isRemote) {
         int tickrate = this.instability / 50;
         if (tickrate == 0 || this.count % (tickrate * tickrate) == 0) {
            this.eject();
         }

         this.checkInstability();
         if (this.count++ % this.inc == 0) {
            if (!this.isLinkValidSimple()) {
               if (this.inc < 600) {
                  this.inc += 20;
               }

               this.restoreLink();
            } else {
               this.inc = 40;
            }
         }
      }

   }

   public void checkInstability() {
      if (this.instability > 0 && this.count % 20 == 0) {
         --this.instability;
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      }

      if (this.instability > 0) {
         int amt = VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.ORDER, Math.min(this.instability, 1));
         if (amt > 0) {
            World targetWorld = DimensionManager.getWorld(this.linkDim);
            this.addInstability(targetWorld, -amt);
         }
      }

   }

   public void readFromNBT(NBTTagCompound nbtCompound) {
      super.readFromNBT(nbtCompound);
      NBTTagList nbttaglist = nbtCompound.getTagList("Items", 10);
      this.outputStacks = new ArrayList<>();

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte b0 = nbttagcompound1.getByte("Slot");
         this.outputStacks.add(new ItemStack(nbttagcompound1));
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
      super.writeToNBT(nbtCompound);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.outputStacks.size(); ++i) {
         if (this.outputStacks.get(i) != null && !((ItemStack)this.outputStacks.get(i)).isEmpty()) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            ((ItemStack)this.outputStacks.get(i)).writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbtCompound.setTag("Items", nbttaglist);
      return nbtCompound;
   }

   public int getSizeInventory() {
      return 1;
   }

   public boolean isEmpty() {
      return true;
   }

   public ItemStack getStackInSlot(int par1) {
      return null;
   }

   public ItemStack decrStackSize(int par1, int par2) {
      return null;
   }

   public ItemStack removeStackFromSlot(int par1) {
      return null;
   }

   public void addStack(ItemStack stack) {
      this.outputStacks.add(stack);
      this.markDirty();
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      World world = DimensionManager.getWorld(this.linkDim);
      TileEntity target = world.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
      if (target instanceof TileMirror) {
         ((TileMirror)target).addStack(par2ItemStack.copy());
         this.addInstability(null, par2ItemStack.getCount());
         this.world.addBlockEvent(this.getPos(), ConfigBlocks.blockMirror, 1, 0);
      } else {
         this.spawnItem(par2ItemStack.copy());
      }

   }

   public String getName() {
      return "container.mirror";
   }

   public boolean hasCustomName() {
      return false;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return false;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   @Override
   public void clear() {
      this.outputStacks.clear();
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      World world = DimensionManager.getWorld(this.linkDim);
      TileEntity target = world.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
      return target instanceof TileMirror;
   }
}
