package thaumcraft.common.tiles;

import net.minecraft.tileentity.TileEntity;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class TileWardingStoneFence extends TileEntity implements net.minecraft.util.ITickable {
   int count = 0;

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
      if (!this.world.isRemote) {
         if (this.count == 0) {
            this.count = this.world.rand.nextInt(100);
         }

         if (++this.count % 100 == 0 && (this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock() != ConfigBlocks.blockCosmeticSolid || this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ()))) != 3) && (this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 2, this.getPos().getZ())).getBlock() != ConfigBlocks.blockCosmeticSolid || this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 2, this.getPos().getZ())).getBlock().getMetaFromState(this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 2, this.getPos().getZ()))) != 3)) {
            this.world.setBlockToAir(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()));
         }
      }

   }
}
