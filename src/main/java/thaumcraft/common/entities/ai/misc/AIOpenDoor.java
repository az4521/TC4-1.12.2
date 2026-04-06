package thaumcraft.common.entities.ai.misc;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import thaumcraft.common.entities.golems.EntityGolemBase;

public class AIOpenDoor extends AIDoorInteract {
   boolean closeDoor;
   int closeDoorTemporisation;

   public AIOpenDoor(EntityGolemBase par1EntityLiving, boolean par2) {
      super(par1EntityLiving);
      this.theEntity = par1EntityLiving;
      this.closeDoor = par2;
   }

   public boolean continueExecuting() {
      return this.closeDoor && this.closeDoorTemporisation > 0 && super.continueExecuting();
   }

   public void startExecuting() {
      this.closeDoorTemporisation = 20;
      BlockPos pos = new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ);
      IBlockState state = this.theEntity.world.getBlockState(pos);
      if (state.getBlock() instanceof BlockDoor) {
         // Set door open
         this.theEntity.world.setBlockState(pos, state.withProperty(BlockDoor.OPEN, true), 2);
      } else if (state.getBlock() instanceof BlockFenceGate) {
         if (!state.getValue(BlockFenceGate.OPEN)) {
            this.theEntity.world.setBlockState(pos, state.withProperty(BlockFenceGate.OPEN, true), 2);
            this.theEntity.world.playEvent(null, 1003, pos, 0);
         }
      }
   }

   public void resetTask() {
      if (this.closeDoor) {
         BlockPos pos = new BlockPos(this.entityPosX, this.entityPosY, this.entityPosZ);
         IBlockState state = this.theEntity.world.getBlockState(pos);
         if (state.getBlock() instanceof BlockDoor) {
            // Set door closed
            this.theEntity.world.setBlockState(pos, state.withProperty(BlockDoor.OPEN, false), 2);
         } else if (state.getBlock() instanceof BlockFenceGate) {
            if (state.getValue(BlockFenceGate.OPEN)) {
               this.theEntity.world.setBlockState(pos, state.withProperty(BlockFenceGate.OPEN, false), 2);
               this.theEntity.world.playEvent(null, 1003, pos, 0);
            }
         }
      }
   }

   public void updateTask() {
      --this.closeDoorTemporisation;
      super.updateTask();
   }
}
