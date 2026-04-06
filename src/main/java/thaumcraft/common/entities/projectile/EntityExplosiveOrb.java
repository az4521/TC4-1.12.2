package thaumcraft.common.entities.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

public class EntityExplosiveOrb extends EntityThrowable {
   public float strength = 1.0F;
   public boolean onFire = false;

   public EntityExplosiveOrb(World par1World) {
      super(par1World);
   }

   public EntityExplosiveOrb(World par1World, EntityLivingBase par2EntityLiving) {
      super(par1World, par2EntityLiving);
   }

   protected float getGravityVelocity() {
      return 0.01F;
   }

   protected void onImpact(RayTraceResult mop) {
      if (!this.world.isRemote) {
         if (mop.entityHit != null) {
            mop.entityHit.attackEntityFrom(causeFireballDamage(this, this.getThrower()), this.strength * 1.5F);
         }

         this.world.newExplosion(null, this.posX, this.posY, this.posZ, this.strength, this.onFire, false);
         this.setDead();
      }

      this.setDead();
   }

   public static DamageSource causeFireballDamage(EntityExplosiveOrb fireball, Entity indirectEntityIn) {
      return indirectEntityIn == null ? (new EntityDamageSourceIndirect("onFire", fireball, fireball)).setFireDamage().setProjectile() : (new EntityDamageSourceIndirect("fireball", fireball, indirectEntityIn)).setFireDamage().setProjectile();
   }

   public float getShadowSize() {
      return 0.1F;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         Thaumcraft.proxy.drawGenericParticles(this.world, this.prevPosX + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F), this.prevPosY + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F), this.prevPosZ + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.3F), 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.8F, false, 151, 9, 1, 7 + this.rand.nextInt(5), 0, 2.0F + this.rand.nextFloat());
      }

      if (this.ticksExisted > 500) {
         this.setDead();
      }

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
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
