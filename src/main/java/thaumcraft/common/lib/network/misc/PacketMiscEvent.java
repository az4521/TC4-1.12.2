package thaumcraft.common.lib.network.misc;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.client.lib.ClientTickEventsFML;
import thaumcraft.client.lib.RenderEventHandler;

public class PacketMiscEvent implements IMessage, IMessageHandler<PacketMiscEvent,IMessage> {
   private short type;
   public static final short WARP_EVENT = 0;
   public static final short MIST_EVENT = 1;
   public static final short MIST_EVENT_SHORT = 2;

   public PacketMiscEvent() {
   }

   public PacketMiscEvent(short type) {
      this.type = type;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeShort(this.type);
   }

   public void fromBytes(ByteBuf buffer) {
      this.type = buffer.readShort();
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketMiscEvent message, MessageContext ctx) {
      EntityPlayer p = Minecraft.getMinecraft().player;
      switch (message.type) {
         case 0:
            ClientTickEventsFML.warpVignette = 100;
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:heartbeat")); if (_snd != null) p.world.playSound(null, p.posX, p.posY, p.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F); };
            break;
         case 1:
            RenderEventHandler.fogFiddled = true;
            RenderEventHandler.fogDuration = 2400;
            break;
         case 2:
            RenderEventHandler.fogFiddled = true;
            if (RenderEventHandler.fogDuration < 200) {
               RenderEventHandler.fogDuration = 200;
            }
         case 3:
      }

      return null;
   }
}
