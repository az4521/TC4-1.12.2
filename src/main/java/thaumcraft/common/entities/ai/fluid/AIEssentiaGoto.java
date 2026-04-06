package thaumcraft.common.entities.ai.fluid;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;

public class AIEssentiaGoto extends EntityAIBase {
   private EntityGolemBase theGolem;
   private double jarX;
   private double jarY;
   private double jarZ;
   private World theWorld;
   int count = 0;
   int prevX = 0;
   int prevY = 0;
   int prevZ = 0;

   public AIEssentiaGoto(EntityGolemBase par1EntityCreature) {
      this.theGolem = par1EntityCreature;
      this.theWorld = par1EntityCreature.world;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.essentia != null && this.theGolem.essentiaAmount != 0) {
         BlockPos jarloc = GolemHelper.findJarWithRoom(this.theGolem);
         if (jarloc == null) {
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
      return this.count > 0 && !this.theGolem.getNavigator().noPath();
   }

   public void resetTask() {
      this.count = 0;
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

   public void startExecuting() {
      this.count = 200;
      this.prevX = MathHelper.floor(this.theGolem.posX);
      this.prevY = MathHelper.floor(this.theGolem.posY);
      this.prevZ = MathHelper.floor(this.theGolem.posZ);
      this.theGolem.getNavigator().tryMoveToXYZ(this.jarX, this.jarY, this.jarZ, this.theGolem.getAIMoveSpeed());
   }
}
