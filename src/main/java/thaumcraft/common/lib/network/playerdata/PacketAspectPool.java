package thaumcraft.common.lib.network.playerdata;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.common.Thaumcraft;

public class PacketAspectPool implements IMessage, IMessageHandler<PacketAspectPool,IMessage> {
   private String key;
   private Short amount;
   private Short total;
   private static long lastSound = 0L;

   public PacketAspectPool() {
   }

   public PacketAspectPool(String key, Short amount, Short total) {
      this.key = key;
      this.amount = amount;
      this.total = total;
   }

   public void toBytes(ByteBuf buffer) {
      ByteBufUtils.writeUTF8String(buffer, this.key);
      buffer.writeShort(this.amount);
      buffer.writeShort(this.total);
   }

   public void fromBytes(ByteBuf buffer) {
      this.key = ByteBufUtils.readUTF8String(buffer);
      this.amount = buffer.readShort();
      this.total = buffer.readShort();
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketAspectPool message, MessageContext ctx) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
      if (Aspect.getAspect(message.key) != null) {
         boolean success = Thaumcraft.proxy.getPlayerKnowledge().setAspectPool(Minecraft.getMinecraft().player.getName(), Aspect.getAspect(message.key), message.total);
         if (success && message.amount > 0) {
            String text = I18n.translateToLocal("tc.addaspectpool");
            text = text.replaceAll("%s", message.amount + "");
            text = text.replaceAll("%n", Aspect.getAspect(message.key).getName());
            PlayerNotifications.addNotification(text, Aspect.getAspect(message.key));

            for(int a = 0; a < message.amount; ++a) {
               PlayerNotifications.addAspectNotification(Aspect.getAspect(message.key));
            }

            if (System.currentTimeMillis() > lastSound) {
               { net.minecraft.entity.player.EntityPlayer player = Minecraft.getMinecraft().player; net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.orb")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.1F, 0.9F + player.world.rand.nextFloat() * 0.2F); }
               lastSound = System.currentTimeMillis() + 100L;
            }
         }
      }

            });
      return null;
   }
}
