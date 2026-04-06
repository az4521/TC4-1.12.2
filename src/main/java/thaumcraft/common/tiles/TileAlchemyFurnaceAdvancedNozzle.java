package thaumcraft.common.tiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import net.minecraft.util.math.BlockPos;

public class TileAlchemyFurnaceAdvancedNozzle extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {
   EnumFacing facing;
   public TileAlchemyFurnaceAdvanced furnace;

   public TileAlchemyFurnaceAdvancedNozzle() {
      this.facing = EnumFacing.UP; // sentinel: "not yet initialized"
      this.furnace = null;
   }

   public boolean canUpdate() {
      return this.facing != null;
   }

   public void updateEntity() {
      if (this.facing == EnumFacing.UP && this.furnace == null) {
         this.facing = null;

         for(EnumFacing dir : EnumFacing.values()) {
            TileEntity tile = this.world.getTileEntity(new BlockPos(this.getPos().getX() + dir.getXOffset(), this.getPos().getY() + dir.getYOffset(), this.getPos().getZ() + dir.getZOffset()));
            if (tile instanceof TileAlchemyFurnaceAdvanced) {
               this.facing = dir.getOpposite();
               this.furnace = (TileAlchemyFurnaceAdvanced)tile;
               break;
            }
         }
      }

   }

   public AspectList getAspects() {
      return this.furnace != null ? this.furnace.aspects : null;
   }

   public void setAspects(AspectList aspects) {
   }

   public int addToContainer(Aspect tt, int am) {
      return am;
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.furnace == null) {
         return false;
      } else if (this.furnace.aspects.getAmount(tt) >= am) {
         this.furnace.aspects.remove(tt, am);
         this.furnace.markDirty();
         this.furnace.vis = this.furnace.aspects.visSize();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.furnace.getPos()); this.world.notifyBlockUpdate(this.furnace.getPos(), _bs, _bs, 3); }
         return true;
      } else {
         return false;
      }
   }

   public boolean doesContainerContain(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tt, int am) {
      if (this.furnace == null) {
         return false;
      } else {
         return this.furnace.aspects.getAmount(tt) >= am;
      }
   }

   public int containerContains(Aspect tt) {
      return this.furnace == null ? 0 : this.furnace.aspects.getAmount(tt);
   }

   public boolean doesContainerAccept(Aspect tag) {
      return false;
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean isConnectable(EnumFacing face) {
      return face == this.facing;
   }

   public boolean canInputFrom(EnumFacing face) {
      return false;
   }

   public boolean canOutputTo(EnumFacing face) {
      return face == this.facing;
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
      return this.furnace != null ? this.furnace.aspects.getAspects()[0] : null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.furnace != null ? this.furnace.aspects.getAmount(this.furnace.aspects.getAspects()[0]) : null;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing facing) {
      return this.canOutputTo(facing) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing facing) {
      return 0;
   }
}
