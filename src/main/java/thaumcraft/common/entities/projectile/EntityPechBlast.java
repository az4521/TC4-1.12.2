package thaumcraft.common.entities.projectile;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.monster.EntityPech;

public class EntityPechBlast extends EntityThrowable {
   int strength = 0;
   int duration = 0;
   boolean nightshade = false;

   public EntityPechBlast(World par1World) {
      super(par1World);
   }

   public EntityPechBlast(World par1World, EntityLivingBase par2EntityLiving, int strength, int duration, boolean nightshade) {
      super(par1World, par2EntityLiving);
      this.strength = strength;
      this.nightshade = nightshade;
      this.duration = duration;
   }

   public EntityPechBlast(World par1World, double par2, double par4, double par6, int strength, int duration, boolean nightshade) {
      super(par1World, par2, par4, par6);
      this.strength = strength;
      this.nightshade = nightshade;
      this.duration = duration;
   }

   protected float getGravityVelocity() {
      return 0.025F;
   }


   public void onUpdate() {
      if (this.world.isRemote) {
         for(int a = 0; a < 3; ++a) {
            Thaumcraft.proxy.wispFX2(this.world, this.posX + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F), this.posY + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F), this.posZ + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F), 0.3F, 3, true, true, 0.02F);
            double x2 = (this.posX + this.prevPosX) / (double)2.0F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F);
            double y2 = (this.posY + this.prevPosY) / (double)2.0F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F);
            double z2 = (this.posZ + this.prevPosZ) / (double)2.0F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F);
            Thaumcraft.proxy.wispFX2(this.world, x2, y2, z2, 0.3F, 2, true, true, 0.02F);
            Thaumcraft.proxy.sparkle((float)this.posX + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.1F, (float)this.posY + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.1F, (float)this.posZ + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.1F, 5);
         }
      }

      super.onUpdate();
      if (this.ticksExisted > 500) {
         this.setDead();
      }

   }

   protected void onImpact(RayTraceResult par1MovingObjectPosition) {
      if (this.world.isRemote) {
         for(int a = 0; a < 9; ++a) {
            float fx = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            float fy = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            float fz = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            Thaumcraft.proxy.wispFX3(this.world, this.posX + (double)fx, this.posY + (double)fy, this.posZ + (double)fz, this.posX + (double)(fx * 8.0F), this.posY + (double)(fy * 8.0F), this.posZ + (double)(fz * 8.0F), 0.3F, 3, true, 0.02F);
            fx = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            fy = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            fz = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            Thaumcraft.proxy.wispFX3(this.world, this.posX + (double)fx, this.posY + (double)fy, this.posZ + (double)fz, this.posX + (double)(fx * 8.0F), this.posY + (double)(fy * 8.0F), this.posZ + (double)(fz * 8.0F), 0.3F, 2, true, 0.02F);
            fx = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            fy = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            fz = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F;
            Thaumcraft.proxy.wispFX3(this.world, this.posX + (double)fx, this.posY + (double)fy, this.posZ + (double)fz, this.posX + (double)(fx * 8.0F), this.posY + (double)(fy * 8.0F), this.posZ + (double)(fz * 8.0F), 0.3F, 0, true, 0.02F);
         }
      }

      if (!this.world.isRemote) {
         List list = this.world.getEntitiesWithinAABBExcludingEntity(this.getThrower(), this.getEntityBoundingBox().grow(2.0F, 2.0F, 2.0F));

          for (Object o : list) {
              Entity entity1 = (Entity) o;
              if (!(entity1 instanceof EntityPech) && entity1 instanceof EntityLivingBase) {
                  entity1.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float) (this.strength + 2));

                  try {
                      if (this.nightshade) {
                          ((EntityLivingBase) entity1).addPotionEffect(new PotionEffect(MobEffects.POISON, 100 + this.duration * 40, this.strength));
                          ((EntityLivingBase) entity1).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100 + this.duration * 40, this.strength + 1));
                          ((EntityLivingBase) entity1).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100 + this.duration * 40, this.strength));
                      } else {
                          switch (this.rand.nextInt(3)) {
                              case 0:
                                  ((EntityLivingBase) entity1).addPotionEffect(new PotionEffect(MobEffects.POISON, 100 + this.duration * 40, this.strength));
                                  break;
                              case 1:
                                  ((EntityLivingBase) entity1).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100 + this.duration * 40, this.strength + 1));
                                  break;
                              case 2:
                                  ((EntityLivingBase) entity1).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100 + this.duration * 40, this.strength));
                          }
                      }
                  } catch (Exception ignored) {
                  }
              }
          }

         this.setDead();
      }

   }

   public float getShadowSize() {
      return 0.1F;
   }
}
