package thaumcraft.common.lib.network.playerdata;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.common.Thaumcraft;

public class PacketSyncWarp implements IMessage, IMessageHandler<PacketSyncWarp,IMessage> {
   protected int data = 0;
   protected byte type = 0;

   public PacketSyncWarp() {
   }

   public PacketSyncWarp(EntityPlayer player, byte type) {
      if (type == 0) {
         this.data = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(player.getName());
      }

      if (type == 1) {
         this.data = Thaumcraft.proxy.getPlayerKnowledge().getWarpSticky(player.getName());
      }

      if (type == 2) {
         this.data = Thaumcraft.proxy.getPlayerKnowledge().getWarpTemp(player.getName());
      }

      this.type = type;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeInt(this.data);
      buffer.writeByte(this.type);
   }

   public void fromBytes(ByteBuf buffer) {
      this.data = buffer.readInt();
      this.type = buffer.readByte();
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketSyncWarp message, MessageContext ctx) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
      if (message.type == 0) {
         Thaumcraft.proxy.getPlayerKnowledge().setWarpPerm(Minecraft.getMinecraft().player.getName(), message.data);
      } else if (message.type == 1) {
         Thaumcraft.proxy.getPlayerKnowledge().setWarpSticky(Minecraft.getMinecraft().player.getName(), message.data);
      } else {
         Thaumcraft.proxy.getPlayerKnowledge().setWarpTemp(Minecraft.getMinecraft().player.getName(), message.data);
      }

            });
      return null;
   }
}
