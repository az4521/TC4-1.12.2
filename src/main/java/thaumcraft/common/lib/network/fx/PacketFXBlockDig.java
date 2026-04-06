package thaumcraft.common.lib.network.fx;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import thaumcraft.client.fx.particles.FXBoreParticles;
import thaumcraft.common.Thaumcraft;

public class PacketFXBlockDig implements IMessage, IMessageHandler<PacketFXBlockDig,IMessage> {
   private int x;
   private int y;
   private int z;
   private int bi;
   private int md;
   private byte dx;
   private byte dy;
   private byte dz;

   public PacketFXBlockDig() {
   }

   public PacketFXBlockDig(int x, int y, int z, byte xd, byte xy, byte xz, int bi, int md) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.bi = bi;
      this.md = md;
      this.dx = xd;
      this.dy = xy;
      this.dz = xz;
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeInt(this.x);
      buffer.writeInt(this.y);
      buffer.writeInt(this.z);
      buffer.writeInt(this.bi);
      buffer.writeInt(this.md);
      buffer.writeByte(this.dx);
      buffer.writeByte(this.dy);
      buffer.writeByte(this.dz);
   }

   public void fromBytes(ByteBuf buffer) {
      this.x = buffer.readInt();
      this.y = buffer.readInt();
      this.z = buffer.readInt();
      this.bi = buffer.readInt();
      this.md = buffer.readInt();
      this.dx = buffer.readByte();
      this.dy = buffer.readByte();
      this.dz = buffer.readByte();
   }

   @SideOnly(Side.CLIENT)
   public IMessage onMessage(PacketFXBlockDig message, MessageContext ctx) {
      Item item = Item.getItemById(message.bi);
      if (item instanceof ItemBlock) {
         Block block = Block.getBlockById(message.bi);
         if (block != null) {
            for(int a = 0; a < Thaumcraft.proxy.particleCount(20); ++a) {
               FXBoreParticles fb = (new FXBoreParticles(Thaumcraft.proxy.getClientWorld(), (float)message.dx + Thaumcraft.proxy.getClientWorld().rand.nextFloat(), (float)message.dy + Thaumcraft.proxy.getClientWorld().rand.nextFloat(), (float)message.dz + Thaumcraft.proxy.getClientWorld().rand.nextFloat(), (double)message.x + (double)0.5F, (double)message.y + (double)0.5F, (double)message.z + (double)0.5F, block, Thaumcraft.proxy.getClientWorld().rand.nextInt(6), message.md)).applyColourMultiplier(message.x, message.y, message.z);
               thaumcraft.client.fx.ParticleEngine.instance.addEffect(Thaumcraft.proxy.getClientWorld(), fb);
            }

            net.minecraft.block.SoundType _st = block.getSoundType(block.getDefaultState(), Thaumcraft.proxy.getClientWorld(), new net.minecraft.util.math.BlockPos(message.dx, message.dy, message.dz), null);
            Thaumcraft.proxy.getClientWorld().playSound(message.dx + 0.5, message.dy + 0.5, message.dz + 0.5, _st.getBreakSound(), net.minecraft.util.SoundCategory.BLOCKS, (_st.getVolume() + 1.0F) / 2.0F, _st.getPitch() * 0.8F, false);
         }
      } else {
         for(int a = 0; a < Thaumcraft.proxy.particleCount(20); ++a) {
            FXBoreParticles fb = (new FXBoreParticles(Thaumcraft.proxy.getClientWorld(), (float)message.dx + Thaumcraft.proxy.getClientWorld().rand.nextFloat(), (float)message.dy + Thaumcraft.proxy.getClientWorld().rand.nextFloat(), (float)message.dz + Thaumcraft.proxy.getClientWorld().rand.nextFloat(), (double)message.x + (double)0.5F, (double)message.y + (double)0.5F, (double)message.z + (double)0.5F, item, Thaumcraft.proxy.getClientWorld().rand.nextInt(6), message.md)).applyColourMultiplier(message.x, message.y, message.z);
            thaumcraft.client.fx.ParticleEngine.instance.addEffect(Thaumcraft.proxy.getClientWorld(), fb);
         }

         net.minecraft.block.SoundType _stS = Blocks.STONE.getSoundType(Blocks.STONE.getDefaultState(), Thaumcraft.proxy.getClientWorld(), new net.minecraft.util.math.BlockPos(message.dx, message.dy, message.dz), null);
         Thaumcraft.proxy.getClientWorld().playSound(message.dx + 0.5, message.dy + 0.5, message.dz + 0.5, _stS.getBreakSound(), net.minecraft.util.SoundCategory.BLOCKS, (_stS.getVolume() + 1.0F) / 2.0F, _stS.getPitch() * 0.8F, false);
      }

      return null;
   }
}
