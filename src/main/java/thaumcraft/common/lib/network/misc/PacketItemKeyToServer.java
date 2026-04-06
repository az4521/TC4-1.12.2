package thaumcraft.common.lib.network.misc;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.entities.golems.ItemGolemBell;
import thaumcraft.common.items.equipment.ItemElementalShovel;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;

public class PacketItemKeyToServer implements IMessage, IMessageHandler<PacketItemKeyToServer,IMessage> {
   private int dim;
   private int playerid;
   private byte key;

   public PacketItemKeyToServer() {
   }

   public PacketItemKeyToServer(EntityPlayer player, int key) {
      this.dim = player.world.provider.getDimension();
      this.playerid = player.getEntityId();
      this.key = (byte)key;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeInt(this.dim);
      buffer.writeInt(this.playerid);
      buffer.writeByte(this.key);
   }

   public void fromBytes(ByteBuf buffer) {
      this.dim = buffer.readInt();
      this.playerid = buffer.readInt();
      this.key = buffer.readByte();
   }

   public IMessage onMessage(PacketItemKeyToServer message, MessageContext ctx) {
      World world = DimensionManager.getWorld(message.dim);
       if (world != null) {
           Entity player = world.getEntityByID(message.playerid);
           if (player instanceof EntityPlayer && ((EntityPlayer) player).getHeldItemMainhand() != null) {
               if (message.key == 0 && ((EntityPlayer) player).getHeldItemMainhand().getItem() instanceof ItemGolemBell) {
                   ItemGolemBell.resetMarkers(((EntityPlayer) player).getHeldItemMainhand(), world, (EntityPlayer) player);
               }

               if (message.key == 1 && ((EntityPlayer) player).getHeldItemMainhand().getItem() instanceof ItemWandCasting) {
                   WandManager.toggleMisc(((EntityPlayer) player).getHeldItemMainhand(), world, (EntityPlayer) player);
               }

               if (message.key == 1 && ((EntityPlayer) player).getHeldItemMainhand().getItem() instanceof ItemElementalShovel) {
                   ItemElementalShovel var10000 = (ItemElementalShovel) ((EntityPlayer) player).getHeldItemMainhand().getItem();
                   byte b = ItemElementalShovel.getOrientation(((EntityPlayer) player).getHeldItemMainhand());
                   var10000 = (ItemElementalShovel) ((EntityPlayer) player).getHeldItemMainhand().getItem();
                   ItemElementalShovel.setOrientation(((EntityPlayer) player).getHeldItemMainhand(), (byte) (b + 1));
               }
           }

       }
       return null;
   }
}
