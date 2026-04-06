package thaumcraft.common.entities.ai.fluid;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillable;

public class AIEssentiaEmpty extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int jarX;
   private int jarY;
   private int jarZ;
   private EnumFacing markerOrientation;
   private World theWorld;

   public AIEssentiaEmpty(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.theWorld = par1EntityCreature.world;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      BlockPos home = this.theGolem.getHomePosition();
      if (this.theGolem.getNavigator().noPath() && this.theGolem.essentia != null && this.theGolem.essentiaAmount != 0) {
         BlockPos jarloc = GolemHelper.findJarWithRoom(this.theGolem);
         if (jarloc == null) {
            return false;
         } else if (this.theGolem.getDistanceSq((double)jarloc.getX() + (double)0.5F, (double)jarloc.getY() + (double)0.5F, (double)jarloc.getZ() + (double)0.5F) > (double)4.0F) {
            return false;
         } else {
            this.jarX = jarloc.getX();
            this.jarY = jarloc.getY();
            this.jarZ = jarloc.getZ();
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return false;
   }

   public void startExecuting() {
      TileEntity tile = this.theWorld.getTileEntity(new BlockPos(this.jarX, this.jarY, this.jarZ));
      if (tile instanceof TileJarFillable) {
         TileJarFillable jar = (TileJarFillable)tile;
         this.theGolem.essentiaAmount = jar.addToContainer(this.theGolem.essentia, this.theGolem.essentiaAmount);
         if (this.theGolem.essentiaAmount == 0) {
            this.theGolem.essentia = null;
         }

         this.theWorld.playSound(null, this.theGolem.getPosition(), SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.NEUTRAL, 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
         this.theGolem.updateCarried();
         { BlockPos _jp = new BlockPos(this.jarX, this.jarY, this.jarZ); net.minecraft.block.state.IBlockState _bs = this.theWorld.getBlockState(_jp); this.theWorld.notifyBlockUpdate(_jp, _bs, _bs, 3); }
      } else if (tile instanceof TileEssentiaReservoir) {
         TileEssentiaReservoir trans = (TileEssentiaReservoir)tile;
         if (trans.getSuctionAmount(trans.facing) > 0 && (trans.getSuctionType(trans.facing) == null || trans.getSuctionType(trans.facing) == this.theGolem.essentia)) {
            int added = trans.addEssentia(this.theGolem.essentia, this.theGolem.essentiaAmount, trans.facing);
            if (added > 0) {
               EntityGolemBase var9 = this.theGolem;
               var9.essentiaAmount -= added;
               if (this.theGolem.essentiaAmount == 0) {
                  this.theGolem.essentia = null;
               }

               this.theWorld.playSound(null, this.theGolem.getPosition(), SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.NEUTRAL, 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
               this.theGolem.updateCarried();
               { BlockPos _jp = new BlockPos(this.jarX, this.jarY, this.jarZ); net.minecraft.block.state.IBlockState _bs = this.theWorld.getBlockState(_jp); this.theWorld.notifyBlockUpdate(_jp, _bs, _bs, 3); }
            }
         }
      } else if (tile instanceof IEssentiaTransport) {
         for(Integer side : GolemHelper.getMarkedSides(this.theGolem, tile, (byte)-1)) {
            IEssentiaTransport trans = (IEssentiaTransport)tile;
            if (trans.canInputFrom(EnumFacing.byIndex(side)) && trans.getSuctionAmount(EnumFacing.byIndex(side)) > 0 && (trans.getSuctionType(EnumFacing.byIndex(side)) == null || trans.getSuctionType(EnumFacing.byIndex(side)) == this.theGolem.essentia)) {
               int added = trans.addEssentia(this.theGolem.essentia, this.theGolem.essentiaAmount, EnumFacing.byIndex(side));
               if (added > 0) {
                  EntityGolemBase var10000 = this.theGolem;
                  var10000.essentiaAmount -= added;
                  if (this.theGolem.essentiaAmount == 0) {
                     this.theGolem.essentia = null;
                  }

                  this.theWorld.playSound(null, this.theGolem.getPosition(), SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.NEUTRAL, 0.2F, 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
                  this.theGolem.updateCarried();
                  { BlockPos _jp = new BlockPos(this.jarX, this.jarY, this.jarZ); net.minecraft.block.state.IBlockState _bs = this.theWorld.getBlockState(_jp); this.theWorld.notifyBlockUpdate(_jp, _bs, _bs, 3); }
                  break;
               }
            }
         }
      }

   }
}
