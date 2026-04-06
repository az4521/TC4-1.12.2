package thaumcraft.common.entities.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tc4tweak.ConfigurationHandler;
import net.minecraft.util.math.MathHelper;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.EntityUtils;
import net.minecraft.util.math.BlockPos;

public class EntityShockOrb extends EntityThrowable {
   public int area = 4;
   public int damage = 5;

   public EntityShockOrb(World par1World) {
      super(par1World);
   }

   public EntityShockOrb(World par1World, EntityLivingBase par2EntityLiving) {
      super(par1World, par2EntityLiving);
   }

   protected float getGravityVelocity() {
      return 0.05F;
   }

   public static boolean canEarthShockHurt(Entity entity) {
      switch (ConfigurationHandler.INSTANCE.getEarthShockHarmMode()) {
         case OnlyLiving:
            return entity instanceof EntityLivingBase;
         case ExceptItemXp:
            return !(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb);
         case AllEntity:
         default:
            return true;
      }
   }
   protected void onImpact(RayTraceResult mop) {
      if (!this.world.isRemote) {
         for(Entity e : EntityUtils.getEntitiesInRange(this.world, this.posX, this.posY, this.posZ, this, Entity.class, this.area)) {

            if (EntityUtils.canEntityBeSeen(this, e)
                    && canEarthShockHurt(e)
            ) {
               e.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getThrower()), (float)this.damage);
            }
         }

         for(int a = 0; a < 20; ++a) {
            int xx = MathHelper.floor(this.posX) + this.rand.nextInt(this.area) - this.rand.nextInt(this.area);
            int yy = MathHelper.floor(this.posY) + this.area;

            int zz  = MathHelper.floor(this.posZ)
                    + this.rand.nextInt(this.area)
                    - this.rand.nextInt(this.area);
            while (
                    this.world.isAirBlock(new BlockPos(xx, yy, zz))
                    && (yy > MathHelper.floor(this.posY) - this.area)
            ) {
               yy -= 1;
            }

            if (this.world.isAirBlock(new BlockPos(xx, yy + 1, zz)) && !this.world.isAirBlock(new BlockPos(xx, yy, zz)) && this.world.getBlockState(new BlockPos(xx, yy + 1, zz)).getBlock() != ConfigBlocks.blockAiry && EntityUtils.canEntityBeSeen(this, (double)xx + (double)0.5F, (double)yy + (double)1.5F, (double)zz + (double)0.5F)) {
               this.world.setBlockState(new BlockPos(xx, yy + 1, zz), ConfigBlocks.blockAiry.getDefaultState(), 3);
            }
         }
      }

      Thaumcraft.proxy.burst(this.world, this.posX, this.posY, this.posZ, 3.0F);
      { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:shock")); if (_snd != null) this.world.playSound(null, this.posX, this.posY, this.posZ, _snd, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F); }
      this.setDead();
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.ticksExisted > 500) {
         this.setDead();
      }

   }

   public float getShadowSize() {
      return 0.1F;
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      if (this.isEntityInvulnerable(source)) {
         return false;
      } else {
         if (source.getTrueSource() != null) {
            Vec3d vec3 = source.getTrueSource().getLookVec();
            if (vec3 != null) {
               this.motionX = vec3.x;
               this.motionY = vec3.y;
               this.motionZ = vec3.z;
               this.motionX *= 0.9;
               this.motionY *= 0.9;
               this.motionZ *= 0.9;
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); if (_snd != null) this.world.playSound(null, this.posX, this.posY, this.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F); }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
