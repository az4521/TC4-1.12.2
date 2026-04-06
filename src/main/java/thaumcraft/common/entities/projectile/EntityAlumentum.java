package thaumcraft.common.entities.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

public class EntityAlumentum extends EntityThrowable {
   public EntityAlumentum(World par1World) {
      super(par1World);
   }

   public EntityAlumentum(World par1World, EntityLivingBase par2EntityLiving) {
      super(par1World, par2EntityLiving);
   }

   public EntityAlumentum(World par1World, double par2, double par4, double par6) {
      super(par1World, par2, par4, par6);
   }

   protected float getVelocity() {
      return 0.75F;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         for(int a = 0; a < 3; ++a) {
            Thaumcraft.proxy.wispFX2(this.world, this.posX + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F), this.posY + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F), this.posZ + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F), 0.3F, 5, true, true, 0.02F);
            double x2 = (this.posX + this.prevPosX) / (double)2.0F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F);
            double y2 = (this.posY + this.prevPosY) / (double)2.0F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F);
            double z2 = (this.posZ + this.prevPosZ) / (double)2.0F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F);
            Thaumcraft.proxy.wispFX2(this.world, x2, y2, z2, 0.3F, 5, true, true, 0.02F);
            Thaumcraft.proxy.sparkle((float)this.posX + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.1F, (float)this.posY + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.1F, (float)this.posZ + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.1F, 6);
         }
      }

   }

   protected void onImpact(RayTraceResult par1MovingObjectPosition) {
      if (!this.world.isRemote) {
         boolean var2 = this.world.getGameRules().getBoolean("mobGriefing");
         this.world.createExplosion(null, this.posX, this.posY, this.posZ, 1.66F, var2);
         this.setDead();
      }

   }

   public float getShadowSize() {
      return 0.1F;
   }
}
