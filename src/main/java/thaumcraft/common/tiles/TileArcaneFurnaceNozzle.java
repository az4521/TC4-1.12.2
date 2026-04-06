package thaumcraft.common.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;

public class TileArcaneFurnaceNozzle extends TileThaumcraft implements IEssentiaTransport {
   EnumFacing facing;
   TileArcaneFurnace furnace;
   int drawDelay;

   public TileArcaneFurnaceNozzle() {
      this.facing = EnumFacing.UP; // sentinel: "not yet initialized"
      this.furnace = null;
      this.drawDelay = 0;
   }

   public boolean canUpdate() {
      return this.facing != null;
   }

   public void updateEntity() {
      if (this.facing == EnumFacing.UP && this.furnace == null) {
         this.facing = null;

         for(EnumFacing dir : EnumFacing.values()) {
            TileEntity tile = this.world.getTileEntity(new BlockPos(this.getPos().getX() + dir.getXOffset(), this.getPos().getY() + dir.getYOffset(), this.getPos().getZ() + dir.getZOffset()));
            if (tile instanceof TileArcaneFurnace) {
               this.facing = dir.getOpposite();
               this.furnace = (TileArcaneFurnace)tile;
               break;
            }
         }
      }

      if (!this.world.isRemote) {
         try {
            if (this.furnace != null && this.furnace.speedyTime < 60 && this.drawEssentia()) {
               TileArcaneFurnace var10000 = this.furnace;
               var10000.speedyTime += 600;
            }
         } catch (Exception ignored) {
         }
      }

   }

   boolean drawEssentia() {
      if (++this.drawDelay % 5 != 0) {
         return false;
      } else {
         TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.facing);
         if (te != null) {
            IEssentiaTransport ic = (IEssentiaTransport)te;
            if (!ic.canOutputTo(this.facing.getOpposite())) {
               return false;
            }

             return ic.getSuctionAmount(this.facing.getOpposite()) < this.getSuctionAmount(this.facing) && ic.takeEssentia(Aspect.FIRE, 1, this.facing.getOpposite()) == 1;
         }

         return false;
      }
   }

   public boolean isConnectable(EnumFacing face) {
      return this.facing != null;
   }

   public boolean canInputFrom(EnumFacing face) {
      return this.facing != null;
   }

   public boolean canOutputTo(EnumFacing face) {
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
      return Aspect.FIRE;
   }

   public int getSuctionAmount(EnumFacing face) {
      try {
         if (this.furnace != null && this.furnace.speedyTime < 40) {
            return 128;
         }
      } catch (Exception ignored) {
      }

      return 0;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return 0;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing facing) {
      return 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing facing) {
      return 0;
   }
}
