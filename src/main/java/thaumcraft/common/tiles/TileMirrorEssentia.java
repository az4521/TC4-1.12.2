package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.lib.events.EssentiaHandler;
import thaumcraft.common.lib.utils.Utils;
import net.minecraft.util.math.BlockPos;

public class TileMirrorEssentia extends TileThaumcraft implements IAspectSource {
   public boolean linked = false;
   public int linkX;
   public int linkY;
   public int linkZ;
   public int linkDim;
   public EnumFacing linkedFacing;
   int count;
   int inc;

   public TileMirrorEssentia() {
      this.linkedFacing = null;
      this.count = 0;
      this.inc = 40;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.linked = nbttagcompound.getBoolean("linked");
      this.linkX = nbttagcompound.getInteger("linkX");
      this.linkY = nbttagcompound.getInteger("linkY");
      this.linkZ = nbttagcompound.getInteger("linkZ");
      this.linkDim = nbttagcompound.getInteger("linkDim");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setBoolean("linked", this.linked);
      nbttagcompound.setInteger("linkX", this.linkX);
      nbttagcompound.setInteger("linkY", this.linkY);
      nbttagcompound.setInteger("linkZ", this.linkZ);
      nbttagcompound.setInteger("linkDim", this.linkDim);
   }

   public void restoreLink() {
      if (this.isDestinationValid()) {
         World targetWorld = DimensionManager.getWorld(this.linkDim);
         if (targetWorld == null) {
            return;
         }

         TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
         if (te instanceof TileMirrorEssentia) {
            TileMirrorEssentia tm = (TileMirrorEssentia)te;
            tm.linked = true;
            tm.linkX = this.getPos().getX();
            tm.linkY = this.getPos().getY();
            tm.linkZ = this.getPos().getZ();
            tm.linkDim = this.world.provider.getDimension();
            { net.minecraft.block.state.IBlockState _bs = targetWorld.getBlockState(tm.getPos()); targetWorld.notifyBlockUpdate(tm.getPos(), _bs, _bs, 3); }
            this.linkedFacing = EnumFacing.byIndex(
        targetWorld.getBlockState(new BlockPos(this.linkX, this.linkY, this.linkZ)).getBlock()
            .getMetaFromState(targetWorld.getBlockState(new BlockPos(this.linkX, this.linkY, this.linkZ))));
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
            if (te instanceof TileMirrorEssentia) {
               TileMirrorEssentia tm = (TileMirrorEssentia)te;
               tm.linked = false;
               tm.linkedFacing = null;
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
            if (te instanceof TileMirrorEssentia) {
               TileMirrorEssentia tm = (TileMirrorEssentia)te;
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
            if (te instanceof TileMirrorEssentia) {
               TileMirrorEssentia tm = (TileMirrorEssentia)te;
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
         if (te instanceof TileMirrorEssentia) {
            TileMirrorEssentia tm = (TileMirrorEssentia)te;
            return !tm.isLinkValid();
         } else {
            this.linked = false;
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            return false;
         }
      }
   }

   public AspectList getAspects() {
      return null;
   }

   public void setAspects(AspectList aspects) {
   }

   public boolean doesContainerAccept(Aspect tag) {
      return false;
   }

   public int addToContainer(Aspect tag, int amount) {
      return 0;
   }

   public boolean takeFromContainer(Aspect tag, int amount) {
      if (this.isLinkValid() && amount <= 1) {
         World targetWorld = DimensionManager.getWorld(this.linkDim);
         if (this.linkedFacing == null && targetWorld != null) {
            this.linkedFacing = EnumFacing.byIndex(
        targetWorld.getBlockState(new BlockPos(this.linkX, this.linkY, this.linkZ)).getBlock()
            .getMetaFromState(targetWorld.getBlockState(new BlockPos(this.linkX, this.linkY, this.linkZ))) % 6);
         }

         TileEntity te = targetWorld.getTileEntity(new BlockPos(this.linkX, this.linkY, this.linkZ));
         return te instanceof TileMirrorEssentia && EssentiaHandler.drainEssentia(te, tag, this.linkedFacing, 8, true);
      } else {
         return false;
      }
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

   public void updateEntity() {
      if (!this.world.isRemote && this.count++ % this.inc == 0) {
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
