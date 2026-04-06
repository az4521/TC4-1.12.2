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
import net.minecraft.util.text.translation.I18n;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.client.gui.GuiResearchBrowser;
import thaumcraft.client.lib.ClientTickEventsFML;
import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.common.Thaumcraft;

public class PacketResearchComplete implements IMessage, IMessageHandler<PacketResearchComplete,IMessage> {
   private String key;

   public PacketResearchComplete() {
   }

   public PacketResearchComplete(String key) {
      this.key = key;
   }

   public void toBytes(ByteBuf buffer) {
      ByteBufUtils.writeUTF8String(buffer, this.key);
   }

   public void fromBytes(ByteBuf buffer) {
      this.key = ByteBufUtils.readUTF8String(buffer);
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketResearchComplete message, MessageContext ctx) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
      if (message.key != null && !message.key.isEmpty()) {
         Thaumcraft.proxy.getResearchManager().completeResearch(Minecraft.getMinecraft().player, message.key);
         if (message.key.startsWith("@")) {
            String text = I18n.translateToLocal("tc.addclue");
            PlayerNotifications.addNotification("§a" + text);
            { net.minecraft.entity.player.EntityPlayer player = Minecraft.getMinecraft().player; net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:learn")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, 1.0F + player.world.rand.nextFloat() * 0.1F); }
         } else if (!ResearchCategories.getResearch(message.key).isVirtual()) {
            ClientTickEventsFML.researchPopup.queueResearchInformation(ResearchCategories.getResearch(message.key));
            GuiResearchBrowser.highlightedItem.add(message.key);
            GuiResearchBrowser.highlightedItem.add(ResearchCategories.getResearch(message.key).category);
         }

         if (Minecraft.getMinecraft().currentScreen instanceof GuiResearchBrowser) {
            ArrayList<String> al = GuiResearchBrowser.completedResearch.get(Minecraft.getMinecraft().player.getName());
            if (al == null) {
               al = new ArrayList<>();
            }

            al.add(message.key);
            GuiResearchBrowser.completedResearch.put(Minecraft.getMinecraft().player.getName(), al);
            ((GuiResearchBrowser)Minecraft.getMinecraft().currentScreen).updateResearch();
         }
      }

            });
      return null;
   }
}
