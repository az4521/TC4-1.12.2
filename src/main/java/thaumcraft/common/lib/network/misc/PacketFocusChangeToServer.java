package thaumcraft.common.lib.network.misc;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;

public class PacketFocusChangeToServer implements IMessage, IMessageHandler<PacketFocusChangeToServer,IMessage> {
   private int dim;
   private int playerid;
   private String focus;

   public PacketFocusChangeToServer() {
   }

   public PacketFocusChangeToServer(EntityPlayer player, String focus) {
      this.dim = player.world.provider.getDimension();
      this.playerid = player.getEntityId();
      this.focus = focus;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeInt(this.dim);
      buffer.writeInt(this.playerid);
      ByteBufUtils.writeUTF8String(buffer, this.focus);
   }

   public void fromBytes(ByteBuf buffer) {
      this.dim = buffer.readInt();
      this.playerid = buffer.readInt();
      this.focus = ByteBufUtils.readUTF8String(buffer);
   }

   public IMessage onMessage(PacketFocusChangeToServer message, MessageContext ctx) {
      World world = DimensionManager.getWorld(message.dim);
      if (world != null && (ctx.getServerHandler().player == null || ctx.getServerHandler().player.getEntityId() == message.playerid)) {
         Entity player = world.getEntityByID(message.playerid);
         if (player instanceof EntityPlayer && ((EntityPlayer) player).getHeldItemMainhand() != null && ((EntityPlayer) player).getHeldItemMainhand().getItem() instanceof ItemWandCasting && !((ItemWandCasting) ((EntityPlayer) player).getHeldItemMainhand().getItem()).isSceptre(((EntityPlayer) player).getHeldItemMainhand())) {
            WandManager.changeFocus(((EntityPlayer)player).getHeldItemMainhand(), world, (EntityPlayer)player, message.focus);
         }

         return null;
      } else {
         return null;
      }
   }
}
