package thaumcraft.common.lib.network.playerdata;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import thaumcraft.common.Thaumcraft;

public class PacketSyncWipe implements IMessage, IMessageHandler<PacketSyncWipe,IMessage> {
   public void toBytes(ByteBuf buffer) {
   }

   public void fromBytes(ByteBuf buffer) {
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketSyncWipe message, MessageContext ctx) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
         if (Minecraft.getMinecraft().player != null) {
            Thaumcraft.proxy.getPlayerKnowledge().wipePlayerKnowledge(Minecraft.getMinecraft().player.getName());
         }
      });
      return null;
   }
}
