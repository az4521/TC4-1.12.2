package thaumcraft.common.entities.projectile;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

public class EntityEldritchOrb extends EntityThrowable {
   public EntityEldritchOrb(World par1World) {
      super(par1World);
   }

   public EntityEldritchOrb(World par1World, EntityLivingBase par2EntityLiving) {
      super(par1World, par2EntityLiving);
   }

   public void setThrowableHeading(double dx, double dy, double dz, float speed, float inaccuracy) {
      double length = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
      if (length > 0) {
         dx /= length;
         dy /= length;
         dz /= length;
      }
      dx += this.rand.nextGaussian() * 0.0075 * (double)inaccuracy;
      dy += this.rand.nextGaussian() * 0.0075 * (double)inaccuracy;
      dz += this.rand.nextGaussian() * 0.0075 * (double)inaccuracy;
      this.motionX = dx * (double)speed;
      this.motionY = dy * (double)speed;
      this.motionZ = dz * (double)speed;
   }

   protected float getGravityVelocity() {
      return 0.0F;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.ticksExisted > 100) {
         this.setDead();
      }

   }

   public void handleStatusUpdate(byte b) {
      if (b == 16) {
         if (this.world.isRemote) {
            for(int a = 0; a < 30; ++a) {
               float fx = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
               float fy = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
               float fz = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
               Thaumcraft.proxy.wispFX3(this.world, this.posX + (double)fx, this.posY + (double)fy, this.posZ + (double)fz, this.posX + (double)(fx * 8.0F), this.posY + (double)(fy * 8.0F), this.posZ + (double)(fz * 8.0F), 0.3F, 5, true, 0.02F);
            }
         }
      } else {
         super.handleStatusUpdate(b);
      }

   }

   protected void onImpact(RayTraceResult mop) {
      if (!this.world.isRemote && this.getThrower() != null) {
         List list = this.world.getEntitiesWithinAABBExcludingEntity(this.getThrower(), this.getEntityBoundingBox().grow(2.0, 2.0, 2.0));

          for (Object o : list) {
              Entity entity1 = (Entity) o;
              if (entity1 instanceof EntityLivingBase && !((EntityLivingBase) entity1).isEntityUndead()) {
                  entity1.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getThrower()), (float) this.getThrower().getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 0.666F);

                  try {
                      ((EntityLivingBase) entity1).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 160, 0));
                  } catch (Exception ignored) {
                  }
              }
          }

         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.generic.extinguish_fire")); if (_snd != null) this.world.playSound(null, this.posX, this.posY, this.posZ, _snd, SoundCategory.NEUTRAL, 0.5F, 2.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.8F); }
         this.ticksExisted = 100;
         this.world.setEntityState(this, (byte)16);
      }

   }

   public float getShadowSize() {
      return 0.1F;
   }
}
