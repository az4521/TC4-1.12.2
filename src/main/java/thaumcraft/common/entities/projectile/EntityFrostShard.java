package thaumcraft.common.entities.projectile;

import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;

public class EntityFrostShard extends EntityThrowable implements IEntityAdditionalSpawnData {
   private static final DataParameter<Float> DAMAGE = EntityDataManager.createKey(EntityFrostShard.class, DataSerializers.FLOAT);
   private static final DataParameter<Byte> FROSTY = EntityDataManager.createKey(EntityFrostShard.class, DataSerializers.BYTE);

   public double bounce = 0.5F;
   public int bounceLimit = 3;
   public boolean fragile = false;

   public EntityFrostShard(World par1World) {
      super(par1World);
   }

   public EntityFrostShard(World par1World, EntityLivingBase par2EntityLiving, float scatter) {
      super(par1World, par2EntityLiving);
      this.shoot(this.motionX, this.motionY, this.motionZ, 1.0F, scatter);
   }

   protected float getGravityVelocity() {
      return this.fragile ? 0.015F : 0.05F;
   }

   public void writeSpawnData(ByteBuf data) {
      data.writeDouble(this.bounce);
      data.writeInt(this.bounceLimit);
      data.writeBoolean(this.fragile);
   }

   public void readSpawnData(ByteBuf data) {
      this.bounce = data.readDouble();
      this.bounceLimit = data.readInt();
      this.fragile = data.readBoolean();
   }

   protected void onImpact(RayTraceResult mop) {
      if (mop.entityHit != null) {
         int ox = MathHelper.floor(this.posX) - MathHelper.floor(mop.entityHit.posX);
         int oy = MathHelper.floor(this.posY) - MathHelper.floor(mop.entityHit.posY);
         int oz = MathHelper.floor(this.posZ) - MathHelper.floor(mop.entityHit.posZ);
         if (oz != 0) {
            this.motionZ *= -1.0F;
         }
         if (ox != 0) {
            this.motionX *= -1.0F;
         }
         if (oy != 0) {
            this.motionY *= -0.9;
         }
         this.motionX *= 0.66;
         this.motionY *= 0.66;
         this.motionZ *= 0.66;
      } else if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
         EnumFacing dir = mop.sideHit;
         if (dir.getZOffset() != 0) {
            this.motionZ *= -1.0F;
         }
         if (dir.getXOffset() != 0) {
            this.motionX *= -1.0F;
         }
         if (dir.getYOffset() != 0) {
            this.motionY *= -0.9;
         }
         BlockPos hitPos = mop.getBlockPos();
         Block bhit = this.world.getBlockState(hitPos).getBlock();
         try {
            this.playSound(bhit.getSoundType().getBreakSound(), 0.3F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
         } catch (Exception ignored) {
         }
      }

      this.motionX *= this.bounce;
      this.motionY *= this.bounce;
      this.motionZ *= this.bounce;
      float var20 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
      this.posX -= this.motionX / (double)var20 * (double)0.05F;
      this.posY -= this.motionY / (double)var20 * (double)0.05F;
      this.posZ -= this.motionZ / (double)var20 * (double)0.05F;
      // setBeenAttacked removed in 1.12.2
      if (!this.world.isRemote && mop.entityHit != null) {
         double mx = mop.entityHit.motionX;
         double my = mop.entityHit.motionY;
         double mz = mop.entityHit.motionZ;
         mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), this.getDamage());
         if (mop.entityHit instanceof EntityLivingBase && this.getFrosty() > 0) {
            ((EntityLivingBase)mop.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 200, this.getFrosty() - 1));
         }
         if (this.fragile) {
            mop.entityHit.hurtResistantTime = 0;
            this.setDead();
            this.playSound(Blocks.ICE.getSoundType().getBreakSound(), 0.3F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            mop.entityHit.motionX = mx + (mop.entityHit.motionX - mx) / (double)10.0F;
            mop.entityHit.motionY = my + (mop.entityHit.motionY - my) / (double)10.0F;
            mop.entityHit.motionZ = mz + (mop.entityHit.motionZ - mz) / (double)10.0F;
         }
      }

      if (this.bounceLimit-- <= 0) {
         this.setDead();
         this.playSound(Blocks.ICE.getSoundType().getBreakSound(), 0.3F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
      }
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote && this.getFrosty() > 0) {
         float s = this.getDamage() / 10.0F;
         for (int a = 0; a < this.getFrosty(); ++a) {
            Thaumcraft.proxy.sparkle((float)this.posX - s + this.rand.nextFloat() * s * 2.0F, (float)this.posY - s + this.rand.nextFloat() * s * 2.0F, (float)this.posZ - s + this.rand.nextFloat() * s * 2.0F, 0.4F, 6, 0.005F);
         }
      }

      float var20 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * (double)180.0F / Math.PI);

      for (this.rotationPitch = (float)(Math.atan2(this.motionY, var20) * (double)180.0F / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
      }
      while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
         this.prevRotationPitch += 360.0F;
      }
      while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
         this.prevRotationYaw -= 360.0F;
      }
      while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
         this.prevRotationYaw += 360.0F;
      }
      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setFloat("damage", this.getDamage());
      par1NBTTagCompound.setBoolean("fragile", this.fragile);
      par1NBTTagCompound.setInteger("frost", this.getFrosty());
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.setDamage(par1NBTTagCompound.getFloat("damage"));
      this.fragile = par1NBTTagCompound.getBoolean("fragile");
      this.setFrosty(par1NBTTagCompound.getInteger("frost"));
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public float getShadowSize() {
      return 0.0F;
   }

   public void entityInit() {
      super.entityInit();
      this.dataManager.register(DAMAGE, 0.0F);
      this.dataManager.register(FROSTY, (byte) 0);
   }

   public void setDamage(float par1) {
      this.dataManager.set(DAMAGE, par1);
      this.setSize(0.15F + par1 * 0.15F, 0.15F + par1 * 0.15F);
   }

   public float getDamage() {
      return this.dataManager.get(DAMAGE);
   }

   public void setFrosty(int frosty) {
      this.dataManager.set(FROSTY, (byte)frosty);
   }

   public int getFrosty() {
      return this.dataManager.get(FROSTY) & 0xFF;
   }
}
