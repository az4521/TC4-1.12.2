package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import net.minecraft.util.math.BlockPos;

public class TileFluxScrubber extends TileThaumcraft implements IEssentiaTransport {
   public int essentia = 0;
   public int charges = 0;
   public int power = 0;
   public EnumFacing facing = EnumFacing.byIndex(0);
   public int count = 0;
   ArrayList<BlockCoordinates> checklist = new ArrayList<>();

   public void updateEntity() {
      if (this.count == 0) {
         this.count = this.world.rand.nextInt(1000);
      }

      if (!this.world.isRemote) {
         if (this.charges >= 4) {
            this.charges -= 4;
            if (this.world.rand.nextInt(4) == 0) {
               ++this.essentia;
               if (this.essentia > 4) {
                  this.essentia = 4;
               }

               this.markDirty();
            }
         }

         if (this.power < 5) {
            this.power += VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.AIR, 10);
         }

         if (this.power >= 5) {
            this.checkFlux();
         }
      }

   }

   boolean isFlux(int x, int y, int z) {
      Material mat = this.world.getBlockState(new BlockPos(x, y, z)).getMaterial();
      return mat == Config.fluxGoomaterial;
   }

   private void checkFlux() {
      int distance = 16;
      if (this.checklist.isEmpty()) {
         for(int a = -distance; a <= distance; ++a) {
            for(int c = -distance; c <= distance; ++c) {
               for(int b = -distance; b <= distance; ++b) {
                  this.checklist.add(new BlockCoordinates(this.getPos().getX() + a, this.getPos().getY() + c, this.getPos().getZ() + b));
               }
            }
         }

         Collections.shuffle(this.checklist, this.world.rand);
      }

      int x = 0;
      int y = 0;
      int z = 0;
      int cc = 0;

      while(cc < 16 && !this.checklist.isEmpty()) {
         ++cc;
         x = this.checklist.get(0).x;
         y = this.checklist.get(0).y;
         z = this.checklist.get(0).z;
         this.checklist.remove(0);
         if (!this.world.isAirBlock(new BlockPos(x, y, z)) && this.isFlux(x, y, z) && this.getDistanceSq((double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F) < (double)(distance * distance)) {
            this.power -= 5;
            int lmd = this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
            if (lmd > 0) {
               this.world.setBlockState(new net.minecraft.util.math.BlockPos(x, y, z), this.world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getStateFromMeta(lmd - 1), 3);
            } else {
               this.world.setBlockToAir(new BlockPos(x, y, z));
            }

            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(x, y, z, 14483711), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), x, y, z, 32.0F));
            ++this.charges;
            this.markDirty();
            return;
         }
      }

   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = EnumFacing.byIndex(nbttagcompound.getInteger("facing"));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("facing", this.facing.ordinal());
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.charges = nbttagcompound.getInteger("charges");
      this.power = nbttagcompound.getInteger("power");
      this.essentia = nbttagcompound.getInteger("essentia");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setInteger("charges", this.charges);
      nbttagcompound.setInteger("power", this.power);
      nbttagcompound.setInteger("essentia", this.essentia);
      return nbttagcompound;
   }

   public boolean isConnectable(EnumFacing face) {
      return face == this.facing;
   }

   public boolean canOutputTo(EnumFacing face) {
      return face == this.facing;
   }

   public boolean canInputFrom(EnumFacing face) {
      return false;
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public int getMinimumSuction() {
      return 0;
   }

   public Aspect getSuctionType(EnumFacing face) {
      return null;
   }

   public int getSuctionAmount(EnumFacing face) {
      return 0;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return Aspect.MAGIC;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.essentia;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing loc) {
      int re = Math.min(this.essentia, amount);
      this.essentia -= re;
      this.markDirty();
      return re;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing loc) {
      return 0;
   }
}
