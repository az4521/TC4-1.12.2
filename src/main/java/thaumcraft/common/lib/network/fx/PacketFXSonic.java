package thaumcraft.common.lib.network.fx;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import thaumcraft.client.fx.other.FXSonic;
import thaumcraft.common.Thaumcraft;

public class PacketFXSonic implements IMessage, IMessageHandler<PacketFXSonic,IMessage> {
   private int source;

   public PacketFXSonic() {
   }

   public PacketFXSonic(int source) {
      this.source = source;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeInt(this.source);
   }

   public void fromBytes(ByteBuf buffer) {
      this.source = buffer.readInt();
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketFXSonic message, MessageContext ctx) {
      Entity p = Thaumcraft.proxy.getClientWorld().getEntityByID(message.source);
      if (p != null) {
         FXSonic fb = new FXSonic(Thaumcraft.proxy.getClientWorld(), p.posX, p.posY, p.posZ, p, 10);
         thaumcraft.client.fx.ParticleEngine.instance.addEffect(Thaumcraft.proxy.getClientWorld(), fb);
      }

      return null;
   }
}
