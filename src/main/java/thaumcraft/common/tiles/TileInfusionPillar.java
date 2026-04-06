package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.api.TileThaumcraft;

public class TileInfusionPillar extends TileThaumcraft {
   public byte orientation = 0;

   public boolean canUpdate() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY() - 1, this.getPos().getZ() - 1, this.getPos().getX() + 1, this.getPos().getY() + 2, this.getPos().getZ() + 1);
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.orientation = nbttagcompound.getByte("orientation");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("orientation", this.orientation);
   }
}
