package thaumcraft.common.entities.projectile;

import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class EntityEmber extends EntityThrowable implements IEntityAdditionalSpawnData {
   public int duration = 20;
   public int firey = 0;
   public float damage = 1.0F;

   public EntityEmber(World par1World) {
      super(par1World);
   }

   public EntityEmber(World par1World, EntityLivingBase par2EntityLiving, float scatter) {
      super(par1World, par2EntityLiving);
      this.shoot(this.motionX, this.motionY, this.motionZ, this.getVelocity(), scatter);
   }

   protected float getGravityVelocity() {
      return 0.0F;
   }

   protected float getVelocity() {
      return 1.0F;
   }

   public void onUpdate() {
      if (this.ticksExisted > this.duration) {
         this.setDead();
      }

      if (this.duration <= 20) {
         this.motionX *= 0.95;
         this.motionY *= 0.95;
         this.motionZ *= 0.95;
      } else {
         this.motionX *= 0.975;
         this.motionY *= 0.975;
         this.motionZ *= 0.975;
      }

      if (this.onGround) {
         this.motionX *= 0.66;
         this.motionY *= 0.66;
         this.motionZ *= 0.66;
      }

      super.onUpdate();
   }

   public void writeSpawnData(ByteBuf data) {
      data.writeByte(this.duration);
   }

   public void readSpawnData(ByteBuf data) {
      this.duration = data.readByte();
   }

   protected void onImpact(RayTraceResult mop) {
      if (!this.world.isRemote) {
         if (mop.entityHit != null) {
            if (!mop.entityHit.isImmuneToFire() && mop.entityHit.attackEntityFrom((new EntityDamageSourceIndirect("fireball", this, this.getThrower())).setFireDamage(), this.damage)) {
               mop.entityHit.setFire(3 + this.firey);
            }
         } else if (this.rand.nextFloat() < 0.025F * (float)this.firey) {
            int i = mop.getBlockPos().getX();
            int j = mop.getBlockPos().getY();
            int k = mop.getBlockPos().getZ();
            switch (mop.sideHit) {
               case DOWN:
                  --j;
                  break;
               case UP:
                  ++j;
                  break;
               case NORTH:
                  --k;
                  break;
               case SOUTH:
                  ++k;
                  break;
               case WEST:
                  --i;
                  break;
               case EAST:
                  ++i;
                  break;
            }

            if (this.world.isAirBlock(new BlockPos(i, j, k))) {
               this.world.setBlockState(new BlockPos(i, j, k), Blocks.FIRE.getDefaultState());
            }
         }
      }

      this.setDead();
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public float getShadowSize() {
      return 0.0F;
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setFloat("damage", this.damage);
      par1NBTTagCompound.setInteger("firey", this.firey);
      par1NBTTagCompound.setInteger("duration", this.duration);
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.damage = par1NBTTagCompound.getFloat("damage");
      this.firey = par1NBTTagCompound.getInteger("firey");
      this.duration = par1NBTTagCompound.getInteger("duration");
   }

   public boolean canBeCollidedWith() {
       return super.canBeCollidedWith();
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      return false;
   }
}
