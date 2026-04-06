package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;

public class TileBanner extends TileThaumcraft {
   private byte facing = 0;
   private byte color = -1;
   private Aspect aspect = null;
   private boolean onWall = false;

   public boolean canUpdate() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 2, this.getPos().getZ() + 1);
   }

   public byte getFacing() {
      return this.facing;
   }

   public void setFacing(byte face) {
      this.facing = face;
      this.markDirty();
   }

   public boolean getWall() {
      return this.onWall;
   }

   public void setWall(boolean b) {
      this.onWall = b;
      this.markDirty();
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = nbttagcompound.getByte("facing");
      this.setColor(nbttagcompound.getByte("color"));
      String as = nbttagcompound.getString("aspect");
      if (as != null && !as.isEmpty()) {
         this.setAspect(Aspect.getAspect(as));
      } else {
         this.aspect = null;
      }

      this.onWall = nbttagcompound.getBoolean("wall");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("facing", this.facing);
      nbttagcompound.setByte("color", this.getColor());
      nbttagcompound.setString("aspect", this.getAspect() == null ? "" : this.getAspect().getTag());
      nbttagcompound.setBoolean("wall", this.onWall);
   }

   public Aspect getAspect() {
      return this.aspect;
   }

   public void setAspect(Aspect aspect) {
      this.aspect = aspect;
   }

   public byte getColor() {
      return this.color;
   }

   public void setColor(byte color) {
      this.color = color;
   }
}
