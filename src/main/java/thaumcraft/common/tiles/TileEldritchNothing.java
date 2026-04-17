package thaumcraft.common.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEldritchNothing extends TileEntity {
   @Override
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(pos, pos.add(1, 1, 1));
   }

   @Override
   public double getMaxRenderDistanceSquared() {
      return 4096.0D;
   }
}
