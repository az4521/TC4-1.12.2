package thaumcraft.common.tiles;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import thaumcraft.common.config.ConfigBlocks;

public class TileWardingStone extends TileEntity implements net.minecraft.util.ITickable {
   int count = 0;

   public boolean gettingPower() {
      return this.world.isBlockPowered(this.getPos());
   }

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
      if (!this.world.isRemote) {
         if (this.count == 0) {
            this.count = this.world.rand.nextInt(100);
         }

         if (this.count % 5 == 0 && !this.gettingPower()) {
            List<EntityLivingBase> targets = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 3, this.getPos().getZ() + 1).expand(0.1, 0.1, 0.1));
            if (!targets.isEmpty()) {
               for(EntityLivingBase e : targets) {
                  if (!e.onGround && !(e instanceof EntityPlayer)) {
                     e.addVelocity(-MathHelper.sin((e.rotationYaw + 180.0F) * (float)Math.PI / 180.0F) * 0.2F, -0.1, MathHelper.cos((e.rotationYaw + 180.0F) * (float)Math.PI / 180.0F) * 0.2F);
                  }
               }
            }
         }

         if (++this.count % 100 == 0) {
            net.minecraft.util.math.BlockPos posAbove1 = new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ());
            net.minecraft.block.state.IBlockState stateAbove1 = this.world.getBlockState(posAbove1);
            if ((stateAbove1.getBlock() != ConfigBlocks.blockAiry || stateAbove1.getBlock().getMetaFromState(stateAbove1) != 3) && stateAbove1.getBlock().isReplaceable(this.world, posAbove1)) {
               this.world.setBlockState(posAbove1, (ConfigBlocks.blockAiry).getStateFromMeta(4), 3);
            }

            net.minecraft.util.math.BlockPos posAbove2 = new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 2, this.getPos().getZ());
            net.minecraft.block.state.IBlockState stateAbove2 = this.world.getBlockState(posAbove2);
            if ((stateAbove2.getBlock() != ConfigBlocks.blockAiry || stateAbove2.getBlock().getMetaFromState(stateAbove2) != 3) && stateAbove2.getBlock().isReplaceable(this.world, posAbove2)) {
               this.world.setBlockState(posAbove2, (ConfigBlocks.blockAiry).getStateFromMeta(4), 3);
            }
         }
      }

   }
}
