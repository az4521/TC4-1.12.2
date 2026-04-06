package thaumcraft.common.lib.world.dim;

import javax.annotation.Nonnull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

public class MapBossData extends WorldSavedData {
    public int bossCount;

    public MapBossData(String mapname) {
        super(mapname);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.bossCount = nbt.getInteger("bossCount");
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("bossCount", this.bossCount);
        return nbt;
    }
}
