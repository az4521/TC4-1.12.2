package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;

public class TileArcanePressurePlate extends TileOwned {
   public byte setting = 0;

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      super.readCustomNBT(nbttagcompound);
      this.setting = nbttagcompound.getByte("setting");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      super.writeCustomNBT(nbttagcompound);
      nbttagcompound.setByte("setting", this.setting);
   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      if (this.world != null && this.world.isRemote) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      }

   }
}
