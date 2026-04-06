package thaumcraft.common.lib.network.playerdata;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.client.lib.PlayerNotifications;

public class PacketWarpMessage implements IMessage, IMessageHandler<PacketWarpMessage,IMessage> {
   protected int data = 0;
   protected byte type = 0;

   public PacketWarpMessage() {
   }

   public PacketWarpMessage(EntityPlayer player, byte type, int change) {
      this.data = change;
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
   public IMessage onMessage(PacketWarpMessage message, MessageContext ctx) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
      if (message.data != 0) {
         if (message.type == 0 && message.data > 0) {
            String text = I18n.translateToLocal("tc.addwarp");
            if (message.data < 0) {
               text = I18n.translateToLocal("tc.removewarp");
            } else {
               { net.minecraft.entity.player.EntityPlayer player = Minecraft.getMinecraft().player; net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:whispers")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 1.0F); }
            }

            PlayerNotifications.addNotification(text);
         } else if (message.type == 1) {
            String text = I18n.translateToLocal("tc.addwarpsticky");
            if (message.data < 0) {
               text = I18n.translateToLocal("tc.removewarpsticky");
            } else {
               { net.minecraft.entity.player.EntityPlayer player = Minecraft.getMinecraft().player; net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:whispers")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 1.0F); }
            }

            PlayerNotifications.addNotification(text);
         } else if (message.data > 0) {
            String text = I18n.translateToLocal("tc.addwarptemp");
            if (message.data < 0) {
               text = I18n.translateToLocal("tc.removewarptemp");
            }

            PlayerNotifications.addNotification(text);
         }
      }

            });
      return null;
   }
}
