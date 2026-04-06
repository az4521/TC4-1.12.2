package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import net.minecraft.util.math.BlockPos;

public class TileBellows extends TileThaumcraft {
   public float inflation = 1.0F;
   boolean direction = false;
   boolean firstrun = true;
   public byte orientation = 0;
   public boolean onVanillaFurnace = false;
   public int delay = 0;

   public void updateEntity() {
      if (this.world.isRemote) {
         if (!this.gettingPower()) {
            if (this.firstrun) {
               this.inflation = 0.35F + this.world.rand.nextFloat() * 0.55F;
            }

            this.firstrun = false;
            if (this.inflation > 0.35F && !this.direction) {
               this.inflation -= 0.075F;
            }

            if (this.inflation <= 0.35F && !this.direction) {
               this.direction = true;
            }

            if (this.inflation < 1.0F && this.direction) {
               this.inflation += 0.025F;
            }

            if (this.inflation >= 1.0F && this.direction) {
               this.direction = false;
               this.world.playSound(null, this.getPos(), net.minecraft.init.SoundEvents.ENTITY_GHAST_SHOOT, net.minecraft.util.SoundCategory.BLOCKS, 0.01F, 0.5F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F);
            }
         }
      } else if (this.onVanillaFurnace && !this.gettingPower()) {
         ++this.delay;
         if (this.delay >= 2) {
            this.delay = 0;
            EnumFacing dir = EnumFacing.byIndex(this.orientation);
            TileEntity tile = this.world.getTileEntity(this.getPos().offset(dir));
            if (tile instanceof TileEntityFurnace) {
               TileEntityFurnace tf = (TileEntityFurnace)tile;
               int cookTime = tf.getField(2); // 2 is cookTime
               if (cookTime > 0 && cookTime < 199) {
                  tf.setField(2, ++cookTime);
               }
            }
         }
      }

   }

   public boolean gettingPower() {
      return this.world.isBlockPowered(this.getPos());
   }

   public static int getBellows(World world, int x, int y, int z, EnumFacing[] directions) {
      int bellows = 0;

      for(EnumFacing dir : directions) {
         int xx = x + dir.getXOffset();
         int yy = y + dir.getYOffset();
         int zz = z + dir.getZOffset();
         TileEntity tile = world.getTileEntity(new BlockPos(xx, yy, zz));
         if (tile instanceof TileBellows && ((TileBellows) tile).orientation == dir.getOpposite().ordinal() && !world.isBlockPowered(new BlockPos(xx, yy, zz))) {
            ++bellows;
         }
      }

      return bellows;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.orientation = nbttagcompound.getByte("orientation");
      this.onVanillaFurnace = nbttagcompound.getBoolean("onVanillaFurnace");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("orientation", this.orientation);
      nbttagcompound.setBoolean("onVanillaFurnace", this.onVanillaFurnace);
   }
}
