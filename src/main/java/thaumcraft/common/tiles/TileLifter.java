package thaumcraft.common.tiles;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;

public class TileLifter extends TileEntity implements net.minecraft.util.ITickable {
   private int counter = 0;
   public int rangeAbove = 0;
   public boolean requiresUpdate = true;
   public boolean lastPowerState = false;

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
            ++this.counter;
      if (this.requiresUpdate || this.counter % 100 == 0) {
         this.lastPowerState = this.gettingPower();
         this.requiresUpdate = false;
         int max = 10;

         for(int count = 1; this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - count, this.getPos().getZ())).getBlock() == ConfigBlocks.blockLifter && !this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - count, this.getPos().getZ())); max += 10) {
            ++count;
         }

         for(this.rangeAbove = 0; this.rangeAbove < max && !this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1 + this.rangeAbove, this.getPos().getZ())).isOpaqueCube(); ++this.rangeAbove) {
         }
      }

      if (this.rangeAbove > 0 && !this.gettingPower()) {
         List<Entity> targets = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1 + this.rangeAbove, this.getPos().getZ() + 1));
         if (!targets.isEmpty()) {
            for(Entity e : targets) {
               if (e instanceof EntityItem || e.canBePushed() || e instanceof EntityHorse) {
                  if (Thaumcraft.proxy.isShiftKeyDown()) {
                     if (e.motionY < (double)0.0F) {
                        e.motionY *= 0.9F;
                     }
                  } else if (e.motionY < (double)0.35F) {
                     e.motionY += 0.1F;
                  }

                  e.fallDistance = 0.0F;
               }
            }
         }
      }

   }

   public boolean gettingPower() {
      return this.world.isBlockPowered(this.getPos()) || this.world.isBlockPowered(this.getPos().up());
   }
}
