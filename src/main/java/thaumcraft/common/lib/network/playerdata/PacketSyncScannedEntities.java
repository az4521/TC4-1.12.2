package thaumcraft.common.lib.network.playerdata;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.common.Thaumcraft;

public class PacketSyncScannedEntities implements IMessage, IMessageHandler<PacketSyncScannedEntities,IMessage> {
   protected ArrayList<String> data = new ArrayList<>();

   public PacketSyncScannedEntities() {
   }

   public PacketSyncScannedEntities(EntityPlayer player) {
      this.data = Thaumcraft.proxy.getScannedEntities().get(player.getName());
   }

   public void toBytes(ByteBuf buffer) {
      if (this.data != null && !this.data.isEmpty()) {
         buffer.writeShort(this.data.size());

         for(String s : this.data) {
            if (s != null) {
               ByteBufUtils.writeUTF8String(buffer, s);
            }
         }
      } else {
         buffer.writeShort(0);
      }

   }

   public void fromBytes(ByteBuf buffer) {
      short size = buffer.readShort();
      this.data = new ArrayList<>();

      for(int a = 0; a < size; ++a) {
         this.data.add(ByteBufUtils.readUTF8String(buffer));
      }

   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketSyncScannedEntities message, MessageContext ctx) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
      for(String key : message.data) {
         Thaumcraft.proxy.getResearchManager().completeScannedEntity(Minecraft.getMinecraft().player, key);
      }

            });
      return null;
   }
}
