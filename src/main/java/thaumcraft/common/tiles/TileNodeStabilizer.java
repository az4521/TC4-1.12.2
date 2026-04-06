package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.common.config.ConfigBlocks;

public class TileNodeStabilizer extends TileEntity implements net.minecraft.util.ITickable {
   public int count = 0;
   public int lock = 0;

   public TileNodeStabilizer(int metadata) {
      this.lock = metadata == 9 ? 1 : 2;
   }

   public TileNodeStabilizer() {
   }

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
      if (this.world.isRemote && this.getPos().getY() < this.world.provider.getHeight() - 1) {
         net.minecraft.util.math.BlockPos above = this.getPos().up();
         net.minecraft.block.state.IBlockState aboveState = this.world.getBlockState(above);
         int md = aboveState.getBlock().getMetaFromState(aboveState);
         if (aboveState.getBlock() == ConfigBlocks.blockAiry && (md == 0 || md == 5) && !this.world.isBlockPowered(this.getPos())) {
            if (this.count < 37) {
               ++this.count;
            }
         } else if (this.count > 0) {
            --this.count;
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 2, this.getPos().getZ() + 1);
   }
}
