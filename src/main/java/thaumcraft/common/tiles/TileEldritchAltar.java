package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.common.entities.monster.EntityCultist;
import thaumcraft.common.entities.monster.EntityCultistCleric;
import thaumcraft.common.entities.monster.EntityCultistKnight;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.lib.world.dim.MazeHandler;
import thaumcraft.common.lib.world.dim.MazeThread;

public class TileEldritchAltar extends TileThaumcraft {
    private boolean spawner = false;
    private boolean open = false;
    private boolean spawnedClerics = false;
    private byte spawnType = 0;
    private byte eyes = 0;
    private int counter = 0;

    public void readCustomNBT(NBTTagCompound nbttagcompound) {
        this.setEyes(nbttagcompound.getByte("eyes"));
        this.setOpen(nbttagcompound.getBoolean("open"));
    }

    public void writeCustomNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("eyes", this.getEyes());
        nbttagcompound.setBoolean("open", this.isOpen());
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.spawnedClerics = nbttagcompound.getBoolean("spawnedClerics");
        this.spawner = nbttagcompound.getBoolean("spawner");
        this.spawnType = nbttagcompound.getByte("spawntype");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("spawnedClerics", this.spawnedClerics);
        nbttagcompound.setBoolean("spawner", this.spawner);
        nbttagcompound.setByte("spawntype", this.spawnType);
        return nbttagcompound;
    }

    public double getMaxRenderDistanceSquared() {
        return 9216.0F;
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
    }

    public boolean isSpawner() {
        return this.spawner;
    }

    public void setSpawner(boolean spawner) {
        this.spawner = spawner;
    }

    public void updateEntity() {
        if (!this.world.isRemote && this.isSpawner() && this.counter++ >= 80 && this.counter % 40 == 0) {
            switch (this.spawnType) {
                case 0:
                    if (!this.spawnedClerics) {
                        this.spawnClerics();
                    } else {
                        this.spawnGuards();
                    }
                    break;
                case 1:
                    this.spawnGuardian();
            }
        }

    }

    private void spawnGuards() {
        List ents = this.world.getEntitiesWithinAABB(EntityCultistCleric.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(24.0F, 16.0F, 24.0F));
        if (ents.isEmpty()) {
            this.setSpawner(false);
        } else {
            ents = this.world.getEntitiesWithinAABB(EntityCultist.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(24.0F, 16.0F, 24.0F));
            if (ents.size() < 8) {
                EntityCultistKnight eg = new EntityCultistKnight(this.world);
                int i1 = this.getPos().getX() + MathHelper.getInt(this.world.rand, 4, 10) * MathHelper.getInt(this.world.rand, -1, 1);
                int j1 = this.getPos().getY() + MathHelper.getInt(this.world.rand, 0, 3) * MathHelper.getInt(this.world.rand, -1, 1);
                int k1 = this.getPos().getZ() + MathHelper.getInt(this.world.rand, 4, 10) * MathHelper.getInt(this.world.rand, -1, 1);
                if (this.world.isSideSolid(new BlockPos(i1, j1 - 1, k1), EnumFacing.UP)) {
                    eg.setPosition(i1, j1, k1);
                    if (this.world.checkNoEntityCollision(eg.getEntityBoundingBox()) && this.world.getCollisionBoxes(eg, eg.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(eg.getEntityBoundingBox())) {
                        eg.onInitialSpawn(this.world.getDifficultyForLocation(eg.getPosition()), null);
                        eg.spawnExplosionParticle();
                        eg.setHomePosAndDistance(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), 16);
                        this.world.spawnEntity(eg);
                    }
                }
            }

        }
    }

    private void spawnGuardian() {
        EntityEldritchGuardian eg = new EntityEldritchGuardian(this.world);
        int i1 = this.getPos().getX() + MathHelper.getInt(this.world.rand, 4, 10) * MathHelper.getInt(this.world.rand, -1, 1);
        int j1 = this.getPos().getY() + MathHelper.getInt(this.world.rand, 0, 3) * MathHelper.getInt(this.world.rand, -1, 1);
        int k1 = this.getPos().getZ() + MathHelper.getInt(this.world.rand, 4, 10) * MathHelper.getInt(this.world.rand, -1, 1);
        if (this.world.isSideSolid(new BlockPos(i1, j1 - 1, k1), EnumFacing.UP)) {
            eg.setPosition(i1, j1, k1);
            if (eg.getCanSpawnHere()) {
                eg.onInitialSpawn(this.world.getDifficultyForLocation(eg.getPosition()), null);
                eg.spawnExplosionParticle();
                eg.setHomePosAndDistance(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), 16);
                this.world.spawnEntity(eg);
            }
        }

    }

    private void spawnClerics() {
        int success = 0;

        for (int a = 0; a < 4; ++a) {
            int xx = 0;
            int zz = 0;
            switch (a) {
                case 0:
                    xx = -2;
                    zz = -2;
                    break;
                case 1:
                    xx = -2;
                    zz = 2;
                    break;
                case 2:
                    xx = 2;
                    zz = -2;
                    break;
                case 3:
                    xx = 2;
                    zz = 2;
            }

            EntityCultistCleric cleric = new EntityCultistCleric(this.world);
            if (this.world.isSideSolid(new BlockPos(this.getPos().getX() + xx, this.getPos().getY() - 1, this.getPos().getZ() + zz), EnumFacing.UP)) {
                cleric.setPosition((double) this.getPos().getX() + (double) 0.5F + (double) xx, this.getPos().getY(), (double) this.getPos().getZ() + (double) 0.5F + (double) zz);
                if (this.world.checkNoEntityCollision(cleric.getEntityBoundingBox()) && this.world.getCollisionBoxes(cleric, cleric.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(cleric.getEntityBoundingBox())) {
                    cleric.setHomePosAndDistance(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), 8);
                    cleric.onInitialSpawn(this.world.getDifficultyForLocation(cleric.getPosition()), null);
                    cleric.spawnExplosionParticle();
                    if (this.world.spawnEntity(cleric)) {
                        ++success;
                        cleric.setIsRitualist(true);
                    }
                }
            }
        }

        if (success > 2) {
            this.spawnedClerics = true;
            this.markDirty();
        }

    }

    public byte getSpawnType() {
        return this.spawnType;
    }

    public void setSpawnType(byte spawnType) {
        this.spawnType = spawnType;
    }

    public byte getEyes() {
        return this.eyes;
    }

    public void setEyes(byte eyes) {
        this.eyes = eyes;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean checkForMaze() {
        int w = 15 + this.world.rand.nextInt(8) * 2;
        int h = 15 + this.world.rand.nextInt(8) * 2;
        if (!MazeHandler.mazesInRange(this.getPos().getX() >> 4, this.getPos().getZ() >> 4, w, h)) {
            Thread t = new Thread(new MazeThread(this.getPos().getX() >> 4, this.getPos().getZ() >> 4, w, h, this.world.rand.nextLong()));
            t.start();
            return false;
        } else {
            return true;
        }
    }
}
