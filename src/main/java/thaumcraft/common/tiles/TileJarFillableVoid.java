package thaumcraft.common.tiles;

import net.minecraft.util.EnumFacing;
import thaumcraft.api.aspects.Aspect;
import net.minecraft.util.math.BlockPos;

public class TileJarFillableVoid extends TileJarFillable {
   int count = 0;

   public int addToContainer(Aspect tt, int am) {
      boolean up = this.amount < this.maxAmount;
       if (am != 0) {
           if (tt == this.aspect || this.amount == 0) {
               this.aspect = tt;
               this.amount += am;
               am = 0;
               if (this.amount > this.maxAmount) {
                   this.amount = this.maxAmount;
               }
           }

           if (up) {
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
               this.markDirty();
           }

       }
       return am;
   }

   public int getMinimumSuction() {
      return this.aspectFilter != null ? 48 : 32;
   }

   public int getSuctionAmount(EnumFacing loc) {
      return this.aspectFilter != null && this.amount < this.maxAmount ? 48 : 32;
   }

   public void updateEntity() {
      if (!this.world.isRemote && ++this.count % 5 == 0) {
         this.fillJar();
      }

   }
}
