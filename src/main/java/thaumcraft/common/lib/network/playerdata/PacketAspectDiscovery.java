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
import thaumcraft.client.gui.GuiResearchBrowser;
import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.common.Thaumcraft;

public class PacketAspectDiscovery implements IMessage, IMessageHandler<PacketAspectDiscovery,IMessage> {
   private String key;

   public PacketAspectDiscovery() {
   }

   public PacketAspectDiscovery(String key) {
      this.key = key;
   }

   public void toBytes(ByteBuf buffer) {
      ByteBufUtils.writeUTF8String(buffer, this.key);
   }

   public void fromBytes(ByteBuf buffer) {
      this.key = ByteBufUtils.readUTF8String(buffer);
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketAspectDiscovery message, MessageContext ctx) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
      if (Aspect.getAspect(message.key) != null) {
         Thaumcraft.proxy.getPlayerKnowledge().addDiscoveredAspect(Minecraft.getMinecraft().player.getName(), Aspect.getAspect(message.key));
         String text = I18n.translateToLocal("tc.addaspectdiscovery");
         text = text.replaceAll("%n", Aspect.getAspect(message.key).getName());
         PlayerNotifications.addNotification("§6" + text, Aspect.getAspect(message.key));
         { net.minecraft.entity.player.EntityPlayer player = Minecraft.getMinecraft().player; net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.orb")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, 0.5F + Thaumcraft.proxy.getClientWorld().rand.nextFloat() * 0.2F); }
         GuiResearchBrowser.highlightedItem.add("ASPECTS");
         GuiResearchBrowser.highlightedItem.add("BASICS");
      }

            });
      return null;
   }
}
