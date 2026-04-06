package thaumcraft.common.entities.ai.misc;

import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import thaumcraft.common.entities.golems.EntityGolemBase;

public abstract class AIDoorInteract extends EntityAIBase {
   protected EntityGolemBase theEntity;
   protected int entityPosX;
   protected int entityPosY;
   protected int entityPosZ;
   protected Block targetDoor;
   boolean hasStoppedDoorInteraction;
   float entityPositionX;
   float entityPositionZ;
   int count = 0;

   public AIDoorInteract(EntityGolemBase par1EntityLiving) {
      this.theEntity = par1EntityLiving;
   }

   public boolean shouldExecute() {
      if (!this.theEntity.collidedHorizontally) {
         return false;
      } else {
         PathNavigate var1 = this.theEntity.getNavigator();
         Path var2 = var1.getPath();
         if (var2 != null && !var2.isFinished()) {
            for(int var3 = 0; var3 < Math.min(var2.getCurrentPathIndex() + 2, var2.getCurrentPathLength()); ++var3) {
               PathPoint var4 = var2.getPathPointFromIndex(var3);
               this.entityPosX = var4.x;
               this.entityPosY = var4.y;
               this.entityPosZ = var4.z;
               if (this.theEntity.getDistanceSq((double)this.entityPosX, this.theEntity.posY, (double)this.entityPosZ) <= 2.25) {
                  this.targetDoor = this.findUsableDoor(this.entityPosX, this.entityPosY, this.entityPosZ);
                  if (this.targetDoor != null && this.targetDoor != Blocks.AIR) {
                     this.count = 200;
                     return true;
                  }
               }
            }

            this.entityPosX = MathHelper.floor(this.theEntity.posX);
            this.entityPosY = MathHelper.floor(this.theEntity.posY);
            this.entityPosZ = MathHelper.floor(this.theEntity.posZ);
            this.targetDoor = this.findUsableDoor(this.entityPosX, this.entityPosY, this.entityPosZ);
            if (this.targetDoor != null && this.targetDoor != Blocks.AIR) {
               this.count = 200;
            }

            return this.targetDoor != null && this.targetDoor != Blocks.AIR;
         } else {
            return false;
         }
      }
   }

   public boolean continueExecuting() {
      return this.count > 0 && !this.hasStoppedDoorInteraction;
   }

   public void startExecuting() {
      this.count = 100;
      this.hasStoppedDoorInteraction = false;
      this.entityPositionX = (float)((double)((float)this.entityPosX + 0.5F) - this.theEntity.posX);
      this.entityPositionZ = (float)((double)((float)this.entityPosZ + 0.5F) - this.theEntity.posZ);
   }

   public void updateTask() {
      --this.count;
      float var1 = (float)((double)((float)this.entityPosX + 0.5F) - this.theEntity.posX);
      float var2 = (float)((double)((float)this.entityPosZ + 0.5F) - this.theEntity.posZ);
      float var3 = this.entityPositionX * var1 + this.entityPositionZ * var2;
      if (var3 < 0.0F) {
         this.hasStoppedDoorInteraction = true;
      }
   }

   private Block findUsableDoor(int par1, int par2, int par3) {
      Block var4 = this.theEntity.world.getBlockState(new BlockPos(par1, par2, par3)).getBlock();
      return var4 != Blocks.OAK_DOOR && var4 != Blocks.OAK_FENCE_GATE ? Blocks.AIR : var4;
   }
}
