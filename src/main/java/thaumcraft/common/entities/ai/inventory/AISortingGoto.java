package thaumcraft.common.entities.ai.inventory;

import java.util.ArrayList;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;
import thaumcraft.common.lib.utils.InventoryUtils;

public class AISortingGoto extends EntityAIBase {
   private EntityGolemBase theGolem;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private BlockPos dest = null;
   int count = 0;
   int prevX = 0;
   int prevY = 0;
   int prevZ = 0;

   public AISortingGoto(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (!this.theGolem.itemCarried.isEmpty() && this.theGolem.ticksExisted % Config.golemDelay <= 0) {
         ArrayList<IInventory> results = GolemHelper.getContainersWithRoom(this.theGolem.world, this.theGolem, (byte)-1);
         if (results.isEmpty()) {
            return false;
         } else {
            EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
            BlockPos home = this.theGolem.getHomePosition();
            int cX = home.getX() - facing.getXOffset();
            int cY = home.getY() - facing.getYOffset();
            int cZ = home.getZ() - facing.getZOffset();
            int tX = 0;
            int tY = 0;
            int tZ = 0;
            double range = Double.MAX_VALUE;
            float dmod = this.theGolem.getRange();

            for(IInventory te : results) {
               int teX = ((TileEntity)te).getPos().getX();
               int teY = ((TileEntity)te).getPos().getY();
               int teZ = ((TileEntity)te).getPos().getZ();
               double distance = this.theGolem.getDistanceSq((double)teX + 0.5D, (double)teY + 0.5D, (double)teZ + 0.5D);
               if (distance < range && distance <= (double)(dmod * dmod) && (teX != cX || teY != cY || teZ != cZ)) {
                  for(int side : GolemHelper.getMarkedSides(this.theGolem, (TileEntity)te, (byte)-1)) {
                     if (InventoryUtils.inventoryContains(te, this.theGolem.itemCarried, side, this.theGolem.checkOreDict(), this.theGolem.ignoreDamage(), this.theGolem.ignoreNBT())) {
                        tX = teX;
                        tY = teY;
                        tZ = teZ;
                        this.dest = new BlockPos(tX, tY, tZ);
                        range = distance;
                        break;
                     }
                  }
               }
            }

            if (this.dest != null) {
               this.movePosX = tX;
               this.movePosY = tY;
               this.movePosZ = tZ;
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return this.count > 0 && !this.theGolem.getNavigator().noPath();
   }

   public void updateTask() {
      --this.count;
      if (this.count == 0 && this.prevX == MathHelper.floor(this.theGolem.posX) && this.prevY == MathHelper.floor(this.theGolem.posY) && this.prevZ == MathHelper.floor(this.theGolem.posZ)) {
         Vec3d var2 = RandomPositionGenerator.findRandomTarget(this.theGolem, 2, 1);
         if (var2 != null) {
            this.count = 20;
            this.theGolem.getNavigator().tryMoveToXYZ(var2.x, var2.y, var2.z, this.theGolem.getAIMoveSpeed());
         }
      }

      super.updateTask();
   }

   public void resetTask() {
      this.count = 0;
      this.dest = null;
   }

   public void startExecuting() {
      this.count = 200;
      this.prevX = MathHelper.floor(this.theGolem.posX);
      this.prevY = MathHelper.floor(this.theGolem.posY);
      this.prevZ = MathHelper.floor(this.theGolem.posZ);
      this.theGolem.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.theGolem.getAIMoveSpeed());
   }
}
