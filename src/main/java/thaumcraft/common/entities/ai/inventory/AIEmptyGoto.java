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
import thaumcraft.common.entities.golems.Marker;

public class AIEmptyGoto extends EntityAIBase {
   private EntityGolemBase theGolem;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private BlockPos dest = null;
   int count = 0;
   int prevX = 0;
   int prevY = 0;
   int prevZ = 0;

   public AIEmptyGoto(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (this.theGolem.itemCarried != null && this.theGolem.ticksExisted % Config.golemDelay <= 0) {
         ArrayList<Byte> matchingColors = this.theGolem.getColorsMatching(this.theGolem.itemCarried);

         for(byte color : matchingColors) {
            ArrayList<IInventory> results = GolemHelper.getContainersWithRoom(this.theGolem.world, this.theGolem, color);
            if (!results.isEmpty()) {
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
                  TileEntity tile = (TileEntity) te;
                  double distance = this.theGolem.getDistanceSq((double)tile.getPos().getX() + 0.5D, (double)tile.getPos().getY() + 0.5D, (double)tile.getPos().getZ() + 0.5D);
                  if (distance < range && distance <= (double)(dmod * dmod) && (tile.getPos().getX() != cX || tile.getPos().getY() != cY || tile.getPos().getZ() != cZ)) {
                     range = distance;
                     tX = tile.getPos().getX();
                     tY = tile.getPos().getY();
                     tZ = tile.getPos().getZ();
                     this.dest = new BlockPos(tX, tY, tZ);
                  }
               }

               if (this.dest != null) {
                  this.movePosX = tX;
                  this.movePosY = tY;
                  this.movePosZ = tZ;
                  return true;
               }
            }
         }

         for(byte color : matchingColors) {
            for(Marker marker : this.theGolem.getMarkers()) {
               if ((marker.color == color || color == -1) && (this.theGolem.world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z)) == null || !(this.theGolem.world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z)) instanceof IInventory))) {
                  this.movePosX = marker.x;
                  this.movePosY = marker.y;
                  this.movePosZ = marker.z;
                  return true;
               }
            }
         }

      }
       return false;
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
