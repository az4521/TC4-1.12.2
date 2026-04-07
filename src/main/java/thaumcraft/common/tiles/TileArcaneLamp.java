package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class TileArcaneLamp extends TileThaumcraft {
   public EnumFacing facing = EnumFacing.byIndex(0);

   public void updateEntity() {
      if (!this.world.isRemote) {
         int x = this.getPos().getX() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
         int y = this.getPos().getY() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
         int z = this.getPos().getZ() + this.world.rand.nextInt(16) - this.world.rand.nextInt(16);
         if (y > this.world.getHeight(x, z) + 4) {
            y = this.world.getHeight(x, z) + 4;
         }

         if (y < 5) {
            y = 5;
         }

         BlockPos lightPos = new BlockPos(x, y, z);
         if (this.world.isAirBlock(lightPos) && this.world.getBlockState(lightPos).getBlock() != ConfigBlocks.blockAiry && this.world.getLightFor(EnumSkyBlock.BLOCK, lightPos) < 9) {
            this.world.setBlockState(lightPos, ConfigBlocks.blockAiry.getStateFromMeta(3), 3);
            TileEntity te = this.world.getTileEntity(lightPos);
            if (te instanceof TileArcaneLampLight) {
               TileArcaneLampLight lampLight = (TileArcaneLampLight)te;
               lampLight.x = this.getPos().getX();
               lampLight.y = this.getPos().getY();
               lampLight.z = this.getPos().getZ();
               lampLight.markDirty();
            }
         }
      }

   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = EnumFacing.byIndex(nbttagcompound.getInteger("orientation"));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("orientation", this.facing.ordinal());
   }

   public void removeLights() {
      BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
      int sx = this.getPos().getX();
      int sy = this.getPos().getY();
      int sz = this.getPos().getZ();
      for(int x = -15; x <= 15; ++x) {
         for(int y = -15; y <= 15; ++y) {
            for(int z = -15; z <= 15; ++z) {
               checkPos.setPos(sx + x, sy + y, sz + z);
               net.minecraft.block.state.IBlockState state = this.world.getBlockState(checkPos);
               if (state.getBlock() == ConfigBlocks.blockAiry && state.getBlock().getMetaFromState(state) == 3) {
                  this.world.setBlockToAir(checkPos);
               }
            }
         }
      }

   }
}
