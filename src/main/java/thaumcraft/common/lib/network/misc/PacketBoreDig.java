package thaumcraft.common.lib.network.misc;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileArcaneBore;

public class PacketBoreDig implements IMessage, IMessageHandler<PacketBoreDig,IMessage> {
   private int x;
   private int y;
   private int z;
   private int digloc;

   public PacketBoreDig() {
   }

   public PacketBoreDig(int x, int y, int z, int digloc) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.digloc = digloc;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeInt(this.x);
      buffer.writeInt(this.y);
      buffer.writeInt(this.z);
      buffer.writeInt(this.digloc);
   }

   public void fromBytes(ByteBuf buffer) {
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
      this.digloc = buffer.readInt();
   }

   public IMessage onMessage(PacketBoreDig message, MessageContext ctx) {
      TileEntity tile = Thaumcraft.proxy.getClientWorld().getTileEntity(new net.minecraft.util.math.BlockPos(message.x, message.y, message.z));
      if (tile instanceof TileArcaneBore) {
         ((TileArcaneBore)tile).getDigEvent(message.digloc);
      }

      return null;
   }
}
