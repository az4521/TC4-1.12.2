package thaumcraft.common.entities.ai.fluid;

import java.util.ArrayList;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;

public class AILiquidEmpty extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int waterX;
   private int waterY;
   private int waterZ;
   private EnumFacing markerOrientation;
   private World theWorld;

   public AILiquidEmpty(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.theWorld = par1EntityCreature.world;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      BlockPos home = this.theGolem.getHomePosition();
      if (this.theGolem.getNavigator().noPath() && this.theGolem.fluidCarried != null && this.theGolem.fluidCarried.amount != 0 && !(this.theGolem.getDistanceSq((float)home.getX() + 0.5F, (float)home.getY() + 0.5F, (float)home.getZ() + 0.5F) > (double)5.0F)) {
         ArrayList<FluidStack> fluids = GolemHelper.getMissingLiquids(this.theGolem);
          if (fluids != null) {
              for (FluidStack fluid : fluids) {
                  if (fluid.isFluidEqual(this.theGolem.fluidCarried)) {
                      return true;
                  }
              }

          }
      }
       return false;
   }

   public boolean continueExecuting() {
      return false;
   }

   public void startExecuting() {
      EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
      BlockPos home = this.theGolem.getHomePosition();
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      TileEntity tile = this.theWorld.getTileEntity(new BlockPos(cX, cY, cZ));
      if (tile instanceof IFluidHandler) {
         IFluidHandler fh = (IFluidHandler)tile;
         int amt = fh.fill(this.theGolem.fluidCarried, true);
         FluidStack var10000 = this.theGolem.fluidCarried;
         var10000.amount -= amt;
         if (this.theGolem.fluidCarried.amount <= 0) {
            this.theGolem.fluidCarried = null;
         }

         if (amt > 200) {
            this.theWorld.playSound(null, this.theGolem.getPosition(), SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.NEUTRAL, Math.min(0.2F, 0.2F * ((float)amt / (float)this.theGolem.getFluidCarryLimit())), 1.0F + (this.theWorld.rand.nextFloat() - this.theWorld.rand.nextFloat()) * 0.3F);
         }

         this.theGolem.updateCarried();
         { BlockPos _lp = new BlockPos(cX, cY, cZ); net.minecraft.block.state.IBlockState _bs = this.theWorld.getBlockState(_lp); this.theWorld.notifyBlockUpdate(_lp, _bs, _bs, 3); }
         this.theGolem.itemWatched = null;
      }

   }
}
