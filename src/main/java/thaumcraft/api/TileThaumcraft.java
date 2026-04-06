package thaumcraft.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * @author azanor
 * <p>
 * Custom tile entity class I use for most of my tile entities. Setup in such a way that only
 * the nbt data within readCustomNBT / writeCustomNBT will be sent to the client when the tile
 * updates. Apart from all the normal TE data that gets sent that is.
 */
public class TileThaumcraft extends TileEntity implements net.minecraft.util.ITickable {

    //NBT stuff

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {

        super.readFromNBT(nbttagcompound);
        readCustomNBT(nbttagcompound);
    }

    public void readCustomNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        writeCustomNBT(nbttagcompound);
        return nbttagcompound;
    }

    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
    }

    // ITickable - delegates to updateEntity() for backward compatibility with 1.7.10 code
    @Override
    public void update() {
        updateEntity();
    }

    public void updateEntity() {
    }

    //Client Packet stuff
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.pos, -999, nbttagcompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

}
