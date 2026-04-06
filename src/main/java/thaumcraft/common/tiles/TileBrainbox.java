package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;

public class TileBrainbox extends TileThaumcraft {
   public EnumFacing facing;

   public TileBrainbox() {
      this.facing = EnumFacing.UP;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = EnumFacing.byIndex(nbttagcompound.getByte("facing"));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("facing", (byte)this.facing.ordinal());
   }

   public boolean canUpdate() {
      return false;
   }
}
