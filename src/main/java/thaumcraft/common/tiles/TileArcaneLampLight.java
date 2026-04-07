package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileArcaneLampLight extends TileEntity implements net.minecraft.util.ITickable {
   int x = Integer.MAX_VALUE;
   int y = Integer.MAX_VALUE;
   int z = Integer.MAX_VALUE;
   int count = 0;

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
      if (!this.world.isRemote) {
         BlockPos sourcePos = new BlockPos(this.x, this.y, this.z);
         if (!(this.world.getTileEntity(sourcePos) instanceof TileArcaneLamp)) {
            this.world.setBlockToAir(this.getPos());
            return;
         }

         if (this.count == 0) {
            this.count = this.world.rand.nextInt(100);
         }

         ++this.count;
      }

   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.x = nbttagcompound.getInteger("sourceX");
      this.y = nbttagcompound.getInteger("sourceY");
      this.z = nbttagcompound.getInteger("sourceZ");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setInteger("sourceX", this.x);
      nbttagcompound.setInteger("sourceY", this.y);
      nbttagcompound.setInteger("sourceZ", this.z);
      return nbttagcompound;
   }
}
