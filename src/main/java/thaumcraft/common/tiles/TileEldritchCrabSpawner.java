package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.monster.EntityEldritchCrab;
import net.minecraft.util.math.BlockPos;

public class TileEldritchCrabSpawner extends TileThaumcraft {
   public int count = 150;
   public int ticks = 0;
   int venting = 0;
   byte facing = 0;

   public void updateEntity() {
            if (this.ticks == 0) {
         this.ticks = this.world.rand.nextInt(500);
      }

      ++this.ticks;
      if (!this.world.isRemote) {
         --this.count;
         if (this.count < 0) {
            this.count = 50 + this.world.rand.nextInt(50);
         } else {
            if (this.count == 15 && this.isActivated() && !this.maxEntitiesReached()) {
               this.world.addBlockEvent(this.getPos(), this.getBlockType(), 1, 0);
               this.world.playSound(null, this.getPos(), thaumcraft.common.lib.SoundsTC.get("random.fizz"), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F);
            }

            if (this.count <= 0 && this.isActivated() && !this.maxEntitiesReached()) {
               this.count = 150 + this.world.rand.nextInt(100);
               this.spawnCrab();
               this.world.playSound(null, this.getPos(), thaumcraft.common.lib.SoundsTC.get("thaumcraft:gore"), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F);
            }
         }
      } else if (this.venting > 0) {
         --this.venting;

         for(int a = 0; a < 3; ++a) {
            this.drawVent();
         }
      } else if (this.world.rand.nextInt(20) == 0) {
         this.drawVent();
      }

   }

   void drawVent() {
      EnumFacing dir = EnumFacing.byIndex(this.facing);
      float fx = 0.15F - this.world.rand.nextFloat() * 0.3F;
      float fz = 0.15F - this.world.rand.nextFloat() * 0.3F;
      float fy = 0.15F - this.world.rand.nextFloat() * 0.3F;
      float fx2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
      float fz2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
      float fy2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
      Thaumcraft.proxy.drawVentParticles(this.world, (float)this.getPos().getX() + 0.5F + fx + (float)dir.getXOffset() / 2.1F, (float)this.getPos().getY() + 0.5F + fy + (float)dir.getYOffset() / 2.1F, (float)this.getPos().getZ() + 0.5F + fz + (float)dir.getZOffset() / 2.1F, (float)dir.getXOffset() / 3.0F + fx2, (float)dir.getYOffset() / 3.0F + fy2, (float)dir.getZOffset() / 3.0F + fz2, 10061994, 2.0F);
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 1) {
         this.venting = 20;
         return true;
      } else {
         return super.receiveClientEvent(i, j);
      }
   }

   private boolean maxEntitiesReached() {
      List ents = this.world.getEntitiesWithinAABB(EntityEldritchCrab.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), (double)this.getPos().getX() + (double)1.0F, (double)this.getPos().getY() + (double)1.0F, (double)this.getPos().getZ() + (double)1.0F).expand(32.0F, 32.0F, 32.0F));
      return ents.size() > 5;
   }

   public boolean isActivated() {
      return this.world.getClosestPlayer((double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F, 16.0F, false) != null;
   }

   private void spawnCrab() {
      EnumFacing dir = EnumFacing.byIndex(this.facing);
      EntityEldritchCrab crab = new EntityEldritchCrab(this.world);
      double x = this.getPos().getX() + dir.getXOffset();
      double y = this.getPos().getY() + dir.getYOffset();
      double z = this.getPos().getZ() + dir.getZOffset();
      crab.setLocationAndAngles(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F, 0.0F, 0.0F);
      crab.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos((int)x, (int)y, (int)z)), null);
      crab.setHelm(false);
      crab.motionX = (float)dir.getXOffset() * 0.2F;
      crab.motionY = (float)dir.getYOffset() * 0.2F;
      crab.motionZ = (float)dir.getZOffset() * 0.2F;
      this.world.spawnEntity(crab);
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY() - 1, this.getPos().getZ() - 1, this.getPos().getX() + 2, this.getPos().getY() + 2, this.getPos().getZ() + 2);
   }

   public byte getFacing() {
      return this.facing;
   }

   public void setFacing(byte face) {
      this.facing = face;
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      this.markDirty();
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = nbttagcompound.getByte("facing");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("facing", this.facing);
   }
}
