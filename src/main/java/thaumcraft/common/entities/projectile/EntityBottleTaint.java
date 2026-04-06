package thaumcraft.common.entities.projectile;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import net.minecraft.util.math.BlockPos;

public class EntityBottleTaint extends EntityThrowable {
   public EntityBottleTaint(World worldIn) {
      super(worldIn);
   }

   public EntityBottleTaint(World worldIn, EntityLivingBase p_i1790_2) {
      super(worldIn, p_i1790_2);
   }

   protected float getGravityVelocity() {
      return 0.05F;
   }

   protected float getVelocity() {
      return 0.5F;
   }

   protected float getInaccuracy() {
      return -20.0F;
   }

   protected void onImpact(RayTraceResult result) {
      if (!this.world.isRemote) {
         List ents = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).expand(5.0F, 5.0F, 5.0F));
         if (!ents.isEmpty()) {
            for(Object ent : ents) {
               EntityLivingBase el = (EntityLivingBase)ent;
               if (!(el instanceof ITaintedMob) && !el.isEntityUndead()) {
                  el.addPotionEffect(new PotionEffect(net.minecraft.potion.Potion.getPotionById(Config.potionTaintPoisonID), 100, 0));
               }
            }
         }

         int x = (int)this.posX;
         int y = (int)this.posY;
         int z = (int)this.posZ;

         for(int a = 0; a < 10; ++a) {
            int xx = x + (int)((this.rand.nextFloat() - this.rand.nextFloat()) * 5.0F);
            int zz = z + (int)((this.rand.nextFloat() - this.rand.nextFloat()) * 5.0F);
            if (this.world.rand.nextBoolean() && this.world.getBiome(new BlockPos(xx, 0, zz)) != ThaumcraftWorldGenerator.biomeTaint) {
               Utils.setBiomeAt(this.world, xx, zz, ThaumcraftWorldGenerator.biomeTaint);
               if (this.world.getBlockState(new BlockPos(xx, y - 1, zz)).isNormalCube() && this.world.getBlockState(new BlockPos(xx, y, zz)).getBlock().isReplaceable(this.world, new BlockPos(xx, y, zz))) {
                  this.world.setBlockState(new BlockPos(xx, y, zz), ConfigBlocks.blockTaintFibres.getDefaultState(), 3);
               }
            }
         }

         this.setDead();
      } else {
         for(int a = 0; a < Thaumcraft.proxy.particleCount(100); ++a) {
            Thaumcraft.proxy.taintsplosionFX(this);
         }

         Thaumcraft.proxy.bottleTaintBreak(this.world, this.posX, this.posY, this.posZ);
      }

   }
}
