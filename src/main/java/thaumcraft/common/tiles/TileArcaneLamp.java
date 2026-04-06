package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
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

         if (this.world.isAirBlock(new BlockPos(x, y, z)) && this.world.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockAiry && this.world.getLightFor(EnumSkyBlock.BLOCK, new BlockPos(x, y, z)) < 9) {
            this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y, z), (ConfigBlocks.blockAiry).getStateFromMeta(3), 3);
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
      for(int x = -15; x <= 15; ++x) {
         for(int y = -15; y <= 15; ++y) {
            for(int z = -15; z <= 15; ++z) {
               if (this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z)).getBlock() == ConfigBlocks.blockAiry && this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z))) == 3) {
                  this.world.setBlockToAir(new BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z));
               }
            }
         }
      }

   }
}
