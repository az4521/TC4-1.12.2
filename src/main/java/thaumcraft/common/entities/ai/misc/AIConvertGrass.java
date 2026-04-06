package thaumcraft.common.entities.ai.misc;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class AIConvertGrass extends EntityAIBase {
   private EntityLiving entity;
   private World world;
   int field_48399_a = 0;

   public AIConvertGrass(EntityLiving par1EntityLiving) {
      this.entity = par1EntityLiving;
      this.world = par1EntityLiving.world;
      this.setMutexBits(7);
   }

   public boolean shouldExecute() {
      if (this.entity.getRNG().nextInt(250) != 0) {
         return false;
      } else {
         int var1 = MathHelper.floor(this.entity.posX);
         int var2 = MathHelper.floor(this.entity.posY);
         int var3 = MathHelper.floor(this.entity.posZ);
         BlockPos pos = new BlockPos(var1, var2, var3);
         BlockPos posBelow = new BlockPos(var1, var2 - 1, var3);
         return (this.world.getBlockState(pos).getBlock() == Blocks.TALLGRASS
               && this.world.getBlockState(pos).getBlock().getMetaFromState(this.world.getBlockState(pos)) == 1)
               || this.world.getBlockState(posBelow).getBlock() == Blocks.GRASS;
      }
   }

   public void startExecuting() {
      this.field_48399_a = 40;
      this.world.setEntityState(this.entity, (byte)10);
      this.entity.getNavigator().clearPath();
   }

   public void resetTask() {
      this.field_48399_a = 0;
   }

   public boolean continueExecuting() {
      return this.field_48399_a > 0;
   }

   public int func_48396_h() {
      return this.field_48399_a;
   }

   public void updateTask() {
      this.field_48399_a = Math.max(0, this.field_48399_a - 1);
      if (this.field_48399_a == 4) {
         int var1 = MathHelper.floor(this.entity.posX);
         int var2 = MathHelper.floor(this.entity.posY);
         int var3 = MathHelper.floor(this.entity.posZ);
         BlockPos pos = new BlockPos(var1, var2, var3);
         BlockPos posBelow = new BlockPos(var1, var2 - 1, var3);
         if (this.world.getBlockState(pos).getBlock() == Blocks.TALLGRASS) {
            this.world.playEvent(2001, pos, Block.getIdFromBlock(Blocks.GRASS) + 4096);
            this.world.setBlockToAir(pos);
            this.world.setBlockState(pos, ConfigBlocks.blockTaintFibres.getDefaultState(), 3);
            Utils.setBiomeAt(this.world, var1, var3, ThaumcraftWorldGenerator.biomeTaint);
            this.entity.eatGrassBonus();
         } else if (this.world.getBlockState(posBelow).getBlock() == Blocks.GRASS) {
            this.world.playEvent(2001, posBelow, Block.getIdFromBlock(Blocks.GRASS));
            this.world.setBlockState(pos, ConfigBlocks.blockTaintFibres.getDefaultState(), 3);
            Utils.setBiomeAt(this.world, var1, var3, ThaumcraftWorldGenerator.biomeTaint);
            this.entity.eatGrassBonus();
         }
      }
   }
}
