package thaumcraft.common.tiles;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileMemory extends TileEntity {
   public Block oldblock;
   public int oldmeta;
   public NBTTagCompound tileEntityCompound;

   public TileMemory() {
   }

   public TileMemory(Block bi, int md, TileEntity te) {
      this.oldblock = bi;
      this.oldmeta = md;
      if (te != null) {
         this.tileEntityCompound = new NBTTagCompound();
         te.writeToNBT(this.tileEntityCompound);
      }

   }

   public boolean canUpdate() {
      return false;
   }

   public void recreateTileEntity() {
      if (this.tileEntityCompound != null && this.world.getTileEntity(this.getPos()) != null) {
         this.world.setBlockState(this.getPos(), this.oldblock.getStateFromMeta(this.oldmeta), 0);
         this.tileEntityCompound.setInteger("x", this.getPos().getX());
         this.tileEntityCompound.setInteger("y", this.getPos().getY());
         this.tileEntityCompound.setInteger("z", this.getPos().getZ());
         this.world.getTileEntity(this.getPos()).readFromNBT(this.tileEntityCompound);
      }

      this.markDirty();
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.oldblock = Block.getBlockById(nbttagcompound.getInteger("oldblock"));
      this.oldmeta = nbttagcompound.getInteger("oldmeta");
      if (nbttagcompound.hasKey("TileEntity")) {
         this.tileEntityCompound = nbttagcompound.getCompoundTag("TileEntity");
      }

   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setInteger("oldblock", Block.getIdFromBlock(this.oldblock));
      nbttagcompound.setInteger("oldmeta", this.oldmeta);
      if (this.tileEntityCompound != null) {
         nbttagcompound.setTag("TileEntity", this.tileEntityCompound);
      }
      return nbttagcompound;
   }
}
