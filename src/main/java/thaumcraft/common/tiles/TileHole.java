package thaumcraft.common.tiles;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import tc4tweak.network.TileHoleSyncPacket;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.foci.ItemFocusPortableHole;

public class TileHole extends TileMemory {
   public short countdown = 0;
   public short countdownmax = 120;
   public byte count = 0;
   public byte direction = 0;

   public TileHole() {
   }

   public TileHole(Block bi, int md, short max, byte count, byte direction, TileEntity te) {
      super(bi, md, te);
      this.count = count;
      this.countdownmax = max;
      this.direction = direction;
   }

   public TileHole(byte count) {
      this.count = count;
   }

   public boolean canUpdate() {
      return true;
   }

   public void updateEntity() {
      if (this.world.isRemote) {
         this.surroundwithsparkles();
      }

      if (this.countdown == 0 && this.count > 1 && this.direction != -1) {
         int ii = this.getPos().getX();
         int jj = this.getPos().getY();
         int kk = this.getPos().getZ();
         switch (this.direction) {
            case 0:
            case 1:
               int a = 0;

               for(; a < 9; ++a) {
                  if (a / 3 != 1 || a % 3 != 1) {
                     ItemFocusPortableHole.createHole(this.world, ii - 1 + a / 3, jj, kk - 1 + a % 3, -1, (byte)1, this.countdownmax);
                  }
               }
               break;
            case 2:
            case 3:
               for(int i = 0; i < 9; ++i) {
                  if (i / 3 != 1 || i % 3 != 1) {
                     ItemFocusPortableHole.createHole(this.world, ii - 1 + i / 3, jj - 1 + i % 3, kk, -1, (byte)1, this.countdownmax);
                  }
               }
               break;
            case 4:
            case 5:
               for(int i = 0; i < 9; ++i) {
                  if (i / 3 != 1 || i % 3 != 1) {
                     ItemFocusPortableHole.createHole(this.world, ii, jj - 1 + i / 3, kk - 1 + i % 3, -1, (byte)1, this.countdownmax);
                  }
               }
         }

         switch (this.direction) {
            case 0:
               ++jj;
               break;
            case 1:
               --jj;
               break;
            case 2:
               ++kk;
               break;
            case 3:
               --kk;
               break;
            case 4:
               ++ii;
               break;
            case 5:
               --ii;
         }

         if (!ItemFocusPortableHole.createHole(this.world, ii, jj, kk, this.direction, (byte)(this.count - 1), this.countdownmax)) {
            this.count = 0;
         }
      }

      ++this.countdown;
      if (this.countdown >= this.countdownmax) {
         if (this.world.isRemote) {
            Thaumcraft.proxy.blockSparkle(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 4194368, 1);
         } else {
            this.world.setBlockState(this.getPos(), this.oldblock.getStateFromMeta(this.oldmeta), 0);
            this.recreateTileEntity();
         }

         this.world.scheduleUpdate(this.getPos(), this.oldblock, 2);
      }

   }

   private void surroundwithsparkles() {
      boolean yp = this.world.getBlockState(this.getPos().up()).isOpaqueCube();
      boolean xp = this.world.getBlockState(this.getPos().east()).isOpaqueCube();
      boolean zp = this.world.getBlockState(this.getPos().south()).isOpaqueCube();
      boolean yn = this.world.getBlockState(this.getPos().down()).isOpaqueCube();
      boolean xn = this.world.getBlockState(this.getPos().west()).isOpaqueCube();
      boolean zn = this.world.getBlockState(this.getPos().north()).isOpaqueCube();
      boolean b1 = this.world.getBlockState(this.getPos().up()).getBlock() != ConfigBlocks.blockHole;
      boolean b2 = this.world.getBlockState(this.getPos().down()).getBlock() != ConfigBlocks.blockHole;
      boolean b3 = this.world.getBlockState(this.getPos().north()).getBlock() != ConfigBlocks.blockHole;
      boolean b4 = this.world.getBlockState(this.getPos().south()).getBlock() != ConfigBlocks.blockHole;
      boolean b5 = this.world.getBlockState(this.getPos().west()).getBlock() != ConfigBlocks.blockHole;
      boolean b6 = this.world.getBlockState(this.getPos().east()).getBlock() != ConfigBlocks.blockHole;
      if (!xp && yp && b6) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!xn && yp && b5) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!zp && yp && b4) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)(this.getPos().getY() + 1), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!zn && yp && b3) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ(), 2);
      }

      if (!xp && yn && b6) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)this.getPos().getY(), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!xn && yn && b5) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)this.getPos().getY(), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!zp && yn && b4) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)this.getPos().getY(), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!zn && yn && b3) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)this.getPos().getY(), (float)this.getPos().getZ(), 2);
      }

      if (!yp && xp && b1) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!yn && xp && b2) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)this.getPos().getY(), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!zp && xp && b4) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!zn && xp && b3) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)this.getPos().getZ(), 2);
      }

      if (!yp && xn && b1) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!yn && xn && b2) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)this.getPos().getY(), (float)this.getPos().getZ() + this.world.rand.nextFloat(), 2);
      }

      if (!zp && xn && b4) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!zn && xn && b3) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)this.getPos().getZ(), 2);
      }

      if (!xp && zp && b6) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!xn && zp && b5) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!yp && zp && b1) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)(this.getPos().getY() + 1), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!yn && zp && b2) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)this.getPos().getY(), (float)(this.getPos().getZ() + 1), 2);
      }

      if (!xp && zn && b6) {
         Thaumcraft.proxy.sparkle((float)(this.getPos().getX() + 1), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)this.getPos().getZ(), 2);
      }

      if (!xn && zn && b5) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX(), (float)this.getPos().getY() + this.world.rand.nextFloat(), (float)this.getPos().getZ(), 2);
      }

      if (!yp && zn && b1) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ(), 2);
      }

      if (!yn && zn && b2) {
         Thaumcraft.proxy.sparkle((float)this.getPos().getX() + this.world.rand.nextFloat(), (float)this.getPos().getY(), (float)this.getPos().getZ(), 2);
      }

   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.readCustomNBT(nbttagcompound);
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.countdown = nbttagcompound.getShort("countdown");
      this.countdownmax = nbttagcompound.getShort("countdownmax");
      this.count = nbttagcompound.getByte("count");
      this.direction = nbttagcompound.getByte("direction");
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      this.writeCustomNBT(nbttagcompound);
      return nbttagcompound;
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setShort("countdown", this.countdown);
      nbttagcompound.setShort("countdownmax", this.countdownmax);
      nbttagcompound.setByte("count", this.count);
      nbttagcompound.setByte("direction", this.direction);
   }

   public Packet getDescriptionPacket() {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      this.writeCustomNBT(nbttagcompound);
      SPacketUpdateTileEntity origin = new SPacketUpdateTileEntity(this.getPos(), -999, nbttagcompound);
      try {
         return Thaumcraft.instance.CHANNEL.getPacketFrom(new TileHoleSyncPacket(origin));
      } catch (Exception ex) {
         // fallback to original packet if anything goes wrong
         return origin;
      }
   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      this.readCustomNBT(pkt.getNbtCompound());
   }
}
