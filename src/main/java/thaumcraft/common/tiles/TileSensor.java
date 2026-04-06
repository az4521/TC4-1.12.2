package thaumcraft.common.tiles;

import java.util.ArrayList;
import java.util.WeakHashMap;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class TileSensor extends TileEntity implements net.minecraft.util.ITickable {
   public byte note = 0;
   public byte tone = 0;
   public int redstoneSignal = 0;
   public static WeakHashMap noteBlockEvents = new WeakHashMap();

   public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setByte("note", this.note);
      par1NBTTagCompound.setByte("tone", this.tone);
      return par1NBTTagCompound;
   }

   public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readFromNBT(par1NBTTagCompound);
      this.note = par1NBTTagCompound.getByte("note");
      this.tone = par1NBTTagCompound.getByte("tone");
      if (this.note < 0) {
         this.note = 0;
      }

      if (this.note > 24) {
         this.note = 24;
      }

   }

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
            if (!this.world.isRemote) {
         if (this.redstoneSignal > 0) {
            --this.redstoneSignal;
            if (this.redstoneSignal == 0) {
               this.world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), ConfigBlocks.blockWoodenDevice, false);
               this.world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ()), ConfigBlocks.blockWoodenDevice, false);
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            }
         }

         ArrayList<Integer[]> nbe = (ArrayList)noteBlockEvents.get(this.world);
         if (nbe != null) {
            for(Integer[] dat : nbe) {
               if (dat[3] == this.tone && dat[4] == this.note && this.getDistanceFrom((double)dat[0] + (double)0.5F, (double)dat[1] + (double)0.5F, (double)dat[2] + (double)0.5F) <= (double)4096.0F) {
                  this.triggerNote(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), false);
                  this.redstoneSignal = 10;
                  this.world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), ConfigBlocks.blockWoodenDevice, false);
                  this.world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ()), ConfigBlocks.blockWoodenDevice, false);
                  { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                  break;
               }
            }
         }
      }

   }

   public double getDistanceFrom(double par1, double par3, double par5) {
      double var7 = (double)this.getPos().getX() + (double)0.5F - par1;
      double var9 = (double)this.getPos().getY() + (double)0.5F - par3;
      double var11 = (double)this.getPos().getZ() + (double)0.5F - par5;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public void updateTone() {
      Material var5 = this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getMaterial();
      this.tone = 0;
      if (var5 == Material.ROCK) {
         this.tone = 1;
      }

      if (var5 == Material.SAND) {
         this.tone = 2;
      }

      if (var5 == Material.GLASS) {
         this.tone = 3;
      }

      if (var5 == Material.WOOD) {
         this.tone = 4;
      }

   }

   public void changePitch() {
      this.note = (byte)((this.note + 1) % 25);
      this.markDirty();
   }

   public void triggerNote(World par1World, int par2, int par3, int par4, boolean sound) {
      if (par1World.getBlockState(new net.minecraft.util.math.BlockPos(par2, par3 + 1, par4)).getMaterial() == Material.AIR) {
         byte var6 = -1;
         if (sound) {
            Material var5 = par1World.getBlockState(new net.minecraft.util.math.BlockPos(par2, par3 - 1, par4)).getMaterial();
            var6 = 0;
            if (var5 == Material.ROCK) {
               var6 = 1;
            }

            if (var5 == Material.SAND) {
               var6 = 2;
            }

            if (var5 == Material.GLASS) {
               var6 = 3;
            }

            if (var5 == Material.WOOD) {
               var6 = 4;
            }
         }

         par1World.addBlockEvent(new net.minecraft.util.math.BlockPos(par2, par3, par4), ConfigBlocks.blockWoodenDevice, var6, this.note);
      }

   }
}
