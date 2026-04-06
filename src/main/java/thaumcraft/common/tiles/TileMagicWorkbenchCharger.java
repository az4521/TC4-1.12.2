package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;

public class TileMagicWorkbenchCharger extends TileVisRelay {
   public short orientation = 0;

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
   }

   public boolean isSource() {
       return super.isSource();
   }

   public void updateEntity() {
            if (!this.world.isRemote) {
         TileEntity te = this.world.getTileEntity(this.getPos().down());
         if (te instanceof TileMagicWorkbench) {
            TileMagicWorkbench tm = (TileMagicWorkbench)te;
            ItemStack wand = tm.getStackInSlot(10);
            if (wand != null && wand.getItem() instanceof ItemWandCasting) {
               AspectList al = ((ItemWandCasting)wand.getItem()).getAspectsWithRoom(wand);
               if (al.size() > 0) {
                  for(Aspect aspect : al.getAspects()) {
                     int drain = Math.min(5, ((ItemWandCasting)wand.getItem()).getMaxVis(tm.getStackInSlot(10)) - ((ItemWandCasting)wand.getItem()).getVis(tm.getStackInSlot(10), aspect));
                     if (drain > 0) {
                        ((ItemWandCasting)wand.getItem()).addRealVis(tm.getStackInSlot(10), aspect, this.consumeVis(aspect, drain), true);
                     }
                  }
               }
            }
         }
      }

   }
}
