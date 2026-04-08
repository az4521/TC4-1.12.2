package thaumcraft.common.lib.world.dim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import java.util.HashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import thaumcraft.common.config.ConfigBlocks;

public class TeleporterThaumcraft extends Teleporter {
    private final WorldServer worldServerInstance;
    private final Random random;
    private static final HashMap<Long, Teleporter.PortalPosition> destinationCoordinateCache = new HashMap<>();
    private static final List<Long> destinationCoordinateKeys = new ArrayList<>();

    public TeleporterThaumcraft(WorldServer par1WorldServer) {
        super(par1WorldServer);
        this.worldServerInstance = par1WorldServer;
        this.random = new Random(par1WorldServer.getSeed());
    }

    @Override
    public void placeEntity(World world, Entity par1Entity, float yaw) {
        if (this.worldServerInstance.provider.getDimension() != 1/*TheEnd*/) {
            if (!this.placeInExistingPortal(par1Entity, yaw)) {
                this.makePortal(par1Entity);
                this.placeInExistingPortal(par1Entity, yaw);
            }
        } else if (!this.placeInExistingPortal(par1Entity, yaw)) {
            int i = MathHelper.floor(par1Entity.posX);
            int k = MathHelper.floor(par1Entity.posZ);
            int j = this.worldServerInstance.getHeight(i, k);
            par1Entity.setLocationAndAngles(i, (double) j + (double) 4.0F, k, par1Entity.rotationYaw, 0.0F);
            par1Entity.motionX = par1Entity.motionY = par1Entity.motionZ = 0.0F;
        }
    }

    @Override
    public boolean placeInExistingPortal(Entity par1Entity, float yaw) {
        short short1 = 128;
        double d3 = -1.0F;
        int i = 0;
        int j = 0;
        int k = 0;
        int l = MathHelper.floor(par1Entity.posX);
        int i1 = MathHelper.floor(par1Entity.posZ);
        int chunkX = l >> 4;
        int chunkZ = i1 >> 4;
        String hs = chunkX + ":" + chunkZ + ":" + this.worldServerInstance.provider.getDimension();
        long j1 = hs.hashCode();
        boolean flag = true;
        if (destinationCoordinateCache.containsKey(j1)) {
            Teleporter.PortalPosition portalposition = (Teleporter.PortalPosition) destinationCoordinateCache.get(j1);
            d3 = 0.0F;
            i = portalposition.getX();
            j = portalposition.getY();
            k = portalposition.getZ();
            portalposition.lastUpdateTime = this.worldServerInstance.getTotalWorldTime();
            flag = false;
        } else {
            for (int k1 = l - short1; k1 <= l + short1; ++k1) {
                double d5 = (double) k1 + (double) 0.5F - par1Entity.posX;

                for (int l1 = i1 - short1; l1 <= i1 + short1; ++l1) {
                    double d6 = (double) l1 + (double) 0.5F - par1Entity.posZ;

                    for (int i2 = this.worldServerInstance.getActualHeight() - 1; i2 >= 0; --i2) {
                        if (this.worldServerInstance.getBlockState(new BlockPos(k1, i2, l1)).getBlock() == ConfigBlocks.blockEldritchPortal) {
                            double d4 = (double) i2 + (double) 0.5F - par1Entity.posY;
                            double d7 = d5 * d5 + d4 * d4 + d6 * d6;
                            if (d3 < (double) 0.0F || d7 < d3) {
                                d3 = d7;
                                i = k1;
                                j = i2;
                                k = l1;
                            }
                        }
                    }
                }
            }
        }

        if (d3 >= 0.0) {
            if (flag) {
                destinationCoordinateCache.put(j1, new Teleporter.PortalPosition(new BlockPos(i, j, k), this.worldServerInstance.getTotalWorldTime()));
                destinationCoordinateKeys.add(j1);
            }

            double d8 = (double) i + 0.5D;
            double d4 = (double) k + 0.5D;
            par1Entity.motionX = par1Entity.motionY = par1Entity.motionZ = 0.0F;
            par1Entity.setLocationAndAngles(d8, j, d4, par1Entity.rotationYaw, par1Entity.rotationPitch);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean makePortal(Entity par1Entity) {
        if (this.worldServerInstance.provider.getDimension() == thaumcraft.common.config.Config.dimensionOuterId) {
            int chunkX = MathHelper.floor(par1Entity.posX) >> 4;
            int chunkZ = MathHelper.floor(par1Entity.posZ) >> 4;
            int radius = 32;
            CellLoc portalCell = this.findPortalCell(chunkX, chunkZ, radius);
            if (portalCell == null && !MazeHandler.mazesInRange(chunkX, chunkZ, radius, radius)) {
                (new MazeThread(chunkX, chunkZ, 31, 31, this.random.nextLong())).run();
                portalCell = this.findPortalCell(chunkX, chunkZ, radius);
            }

            if (portalCell == null) {
                portalCell = new CellLoc(chunkX, chunkZ);
            }

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int gx = portalCell.x + dx;
                    int gz = portalCell.z + dz;
                    if (MazeHandler.getFromHashMap(new CellLoc(gx, gz)) == null) {
                        continue;
                    }

                    this.worldServerInstance.getChunk(gx, gz);
                    MazeHandler.generateEldritch(this.worldServerInstance, this.random, gx, gz);
                    this.worldServerInstance.getChunk(gx, gz).markDirty();
                }
            }
        }

        return true;
    }

    private CellLoc findPortalCell(int chunkX, int chunkZ, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Cell cell = MazeHandler.getFromHashMap(new CellLoc(chunkX + dx, chunkZ + dz));
                if (cell != null && cell.feature == 1) {
                    return new CellLoc(chunkX + dx, chunkZ + dz);
                }
            }
        }

        return null;
    }

    @Override
    public void removeStalePortalLocations(long par1) {
        if (par1 % 100L == 0L) {
            Iterator<Long> iterator = destinationCoordinateKeys.iterator();
            long j = par1 - 600L;

            while (iterator.hasNext()) {
                Long olong = iterator.next();
                Teleporter.PortalPosition portalposition = (Teleporter.PortalPosition) destinationCoordinateCache.get(olong);
                if (portalposition == null || portalposition.lastUpdateTime < j) {
                    iterator.remove();
                    destinationCoordinateCache.remove(olong);
                }
            }
        }
    }
}
