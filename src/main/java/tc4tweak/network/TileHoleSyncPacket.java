package tc4tweak.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileHole;

import java.io.IOException;
import net.minecraft.util.math.BlockPos;

public class TileHoleSyncPacket implements IMessage, IMessageHandler<TileHoleSyncPacket, IMessage> {
    private SPacketUpdateTileEntity origin;

    // used by packet pipeline to reflectively construct this
    @SuppressWarnings("unused")
    public TileHoleSyncPacket() {
    }

    public TileHoleSyncPacket(SPacketUpdateTileEntity origin) {
        this.origin = origin;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        origin = new SPacketUpdateTileEntity();
        try {
            origin.readPacketData(new PacketBuffer(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            origin.writePacketData(new PacketBuffer(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IMessage onMessage(TileHoleSyncPacket message, MessageContext ctx) {
        WorldClient theWorld = Minecraft.getMinecraft().world;
        if (theWorld == null) return null;
        int x = message.origin.getPos().getX();
        int y = message.origin.getPos().getY();
        int z = message.origin.getPos().getZ();
        if (!theWorld.isBlockLoaded(new net.minecraft.util.math.BlockPos(x, y, z))) return null;
        if (theWorld.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockHole) return null;
        TileHole t = new TileHole();
        theWorld.setTileEntity(new net.minecraft.util.math.BlockPos(x, y, z), t);
        ctx.getClientHandler().handleUpdateTileEntity(message.origin);
        return null;
    }
}
