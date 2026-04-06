package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;

public class EntityFireBat extends EntityMob {
   private static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(EntityFireBat.class, DataSerializers.BYTE);
   private BlockPos currentFlightTarget;
   public EntityPlayer owner = null;
   public int damBonus = 0;
   private int attackTime = 0;

   public EntityFireBat(World par1World) {
      super(par1World);
      this.setSize(0.5F, 0.9F);
      this.setIsBatHanging(true);
      this.isImmuneToFire = true;
   }

   public void entityInit() {
      super.entityInit();
      this.dataManager.register(FLAGS, (byte) 0);
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender() {
      return 15728880;
   }

   public float getBrightness(float par1) {
      return 1.0F;
   }

   protected float getSoundVolume() {
      return 0.1F;
   }

   protected float getSoundPitch() {
      return super.getSoundPitch() * 0.95F;
   }

   @Override
   protected net.minecraft.util.SoundEvent getAmbientSound() { return null; }
   @Override
   protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) { return null; }
   @Override
   protected net.minecraft.util.SoundEvent getDeathSound() { return null; }

   public boolean canBePushed() {
      return false;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getIsDevil() ? (double)15.0F : (double)5.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.getIsSummoned() ? (double)((this.getIsDevil() ? 3 : 2) + this.damBonus) : (double)1.0F);
   }

   public boolean getIsBatHanging() {
      return Utils.getBit(this.dataManager.get(FLAGS), 0);
   }

   public void setIsBatHanging(boolean par1) {
      byte var2 = this.dataManager.get(FLAGS);
      if (par1) {
         this.dataManager.set(FLAGS, (byte)Utils.setBit(var2, 0));
      } else {
         this.dataManager.set(FLAGS, (byte)Utils.clearBit(var2, 0));
      }
   }

   public boolean getIsSummoned() {
      return Utils.getBit(this.dataManager.get(FLAGS), 1);
   }

   public void setIsSummoned(boolean par1) {
      byte var2 = this.dataManager.get(FLAGS);
      if (par1) {
         this.dataManager.set(FLAGS, (byte)Utils.setBit(var2, 1));
      } else {
         this.dataManager.set(FLAGS, (byte)Utils.clearBit(var2, 1));
      }
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(par1 ? (double)((this.getIsDevil() ? 3 : 2) + this.damBonus) : (double)1.0F);
   }

   public boolean getIsExplosive() {
      return Utils.getBit(this.dataManager.get(FLAGS), 2);
   }

   public void setIsExplosive(boolean par1) {
      byte var2 = this.dataManager.get(FLAGS);
      if (par1) {
         this.dataManager.set(FLAGS, (byte)Utils.setBit(var2, 2));
      } else {
         this.dataManager.set(FLAGS, (byte)Utils.clearBit(var2, 2));
      }
   }

   public boolean getIsDevil() {
      return Utils.getBit(this.dataManager.get(FLAGS), 3);
   }

   public void setIsDevil(boolean par1) {
      byte var2 = this.dataManager.get(FLAGS);
      if (par1) {
         this.dataManager.set(FLAGS, (byte)Utils.setBit(var2, 3));
      } else {
         this.dataManager.set(FLAGS, (byte)Utils.clearBit(var2, 3));
      }
      if (par1) {
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.getIsSummoned() ? (double)((par1 ? 3 : 2) + this.damBonus) : (double)1.0F);
      }
   }

   public boolean getIsVampire() {
      return Utils.getBit(this.dataManager.get(FLAGS), 4);
   }

   public void setIsVampire(boolean par1) {
      byte var2 = this.dataManager.get(FLAGS);
      if (par1) {
         this.dataManager.set(FLAGS, (byte)Utils.setBit(var2, 4));
      } else {
         this.dataManager.set(FLAGS, (byte)Utils.clearBit(var2, 4));
      }
   }

   public void onLivingUpdate() {
      if (this.isWet()) {
         this.attackEntityFrom(DamageSource.DROWN, 1.0F);
      }
      if (this.attackTime > 0) {
         this.attackTime--;
      }
      super.onLivingUpdate();
      // Flight AI (was in updateEntityActionState which can't be overridden cross-package in 1.12.2)
      if (this.getIsBatHanging()) {
         if (!this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), (int)this.posY + 1, MathHelper.floor(this.posZ))).isNormalCube()) {
            this.setIsBatHanging(false);
         } else {
            if (this.rand.nextInt(200) == 0) {
               this.rotationYawHead = (float)this.rand.nextInt(360);
            }
            if (this.world.getClosestPlayerToEntity(this, 4.0F) != null) {
               this.setIsBatHanging(false);
            }
         }
      } else {
         if (this.getAttackTarget() == null) {
            if (this.getIsSummoned()) {
               this.attackEntityFrom(DamageSource.GENERIC, 2.0F);
            }
            if (this.currentFlightTarget != null && (!this.world.isAirBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < 1)) {
               this.currentFlightTarget = null;
            }
            if (this.currentFlightTarget == null || this.rand.nextInt(30) == 0 || this.currentFlightTarget.distanceSq(this.posX, this.posY, this.posZ) < 4.0) {
               this.currentFlightTarget = new BlockPos((int)this.posX + this.rand.nextInt(7) - this.rand.nextInt(7), (int)this.posY + this.rand.nextInt(6) - 2, (int)this.posZ + this.rand.nextInt(7) - this.rand.nextInt(7));
            }
            double var1 = this.currentFlightTarget.getX() + 0.5 - this.posX;
            double var3 = this.currentFlightTarget.getY() + 0.1 - this.posY;
            double var5 = this.currentFlightTarget.getZ() + 0.5 - this.posZ;
            this.motionX += (Math.signum(var1) * 0.5 - this.motionX) * 0.1;
            this.motionY += (Math.signum(var3) * 0.7 - this.motionY) * 0.1;
            this.motionZ += (Math.signum(var5) * 0.5 - this.motionZ) * 0.1;
            float var7 = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0 / Math.PI) - 90.0F;
            float var8 = MathHelper.wrapDegrees(var7 - this.rotationYaw);
            this.moveForward = 0.5F;
            this.rotationYaw += var8;
            if (this.rand.nextInt(100) == 0 && this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), (int)this.posY + 1, MathHelper.floor(this.posZ))).isNormalCube()) {
               this.setIsBatHanging(true);
            }
         } else {
            Entity target = this.getAttackTarget();
            double var1 = target.posX - this.posX;
            double var3 = target.posY + (double)(target.getEyeHeight() * 0.66F) - this.posY;
            double var5 = target.posZ - this.posZ;
            this.motionX += (Math.signum(var1) * 0.5 - this.motionX) * 0.1;
            this.motionY += (Math.signum(var3) * 0.7 - this.motionY) * 0.1;
            this.motionZ += (Math.signum(var5) * 0.5 - this.motionZ) * 0.1;
            float var7 = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0 / Math.PI) - 90.0F;
            float var8 = MathHelper.wrapDegrees(var7 - this.rotationYaw);
            this.moveForward = 0.5F;
            this.rotationYaw += var8;
         }
         if (this.getAttackTarget() instanceof EntityPlayer && ((EntityPlayer)this.getAttackTarget()).capabilities.disableDamage) {
            this.setAttackTarget(null);
         }
         if (this.getAttackTarget() != null) {
            float dist = (float)this.getDistance(this.getAttackTarget());
            this.attackEntity(this.getAttackTarget(), dist);
         }
      }
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote && this.getIsExplosive()) {
         Thaumcraft.proxy.drawGenericParticles(this.world, this.prevPosX + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), this.prevPosY + (double)(this.height / 2.0F) + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), this.prevPosZ + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.8F, false, 151, 9, 1, 7 + this.rand.nextInt(5), 0, 1.0F + this.rand.nextFloat() * 0.5F);
      }
      if (this.getIsBatHanging()) {
         this.motionX = this.motionY = this.motionZ = 0.0F;
         this.posY = (double)MathHelper.floor(this.posY) + (double)1.0F - (double)this.height;
      } else {
         this.motionY *= 0.6F;
      }
      if (this.world.isRemote) {
         this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
         this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
      }
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void fall(float par1) {
   }

   protected void updateFallState(double par1, boolean par3) {
   }

   public boolean doesEntityNotTriggerPressurePlate() {
      return true;
   }

   public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
      if (!this.isEntityInvulnerable(par1DamageSource) && !par1DamageSource.isFireDamage() && !par1DamageSource.isExplosion()) {
         if (!this.world.isRemote && this.getIsBatHanging()) {
            this.setIsBatHanging(false);
         }
         return super.attackEntityFrom(par1DamageSource, par2);
      } else {
         return false;
      }
   }

   protected void attackEntity(Entity par1Entity, float par2) {
      if (this.attackTime <= 0 && par2 < Math.max(2.5F, par1Entity.width * 1.1F)
            && par1Entity.getEntityBoundingBox().maxY > this.getEntityBoundingBox().minY
            && par1Entity.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
         if (this.getIsSummoned()) {
            EntityUtils.setRecentlyHit((EntityLivingBase)par1Entity, 100);
         }
         if (this.getIsVampire()) {
            if (this.owner != null && !this.owner.isPotionActive(MobEffects.REGENERATION)) {
               this.owner.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 26, 1));
            }
            this.heal(1.0F);
         }
         this.attackTime = 20;
         if ((this.getIsExplosive() || this.world.rand.nextInt(10) == 0) && !this.world.isRemote && !this.getIsDevil()) {
            par1Entity.hurtResistantTime = 0;
            this.world.newExplosion(this, this.posX, this.posY, this.posZ, 1.5F + (this.getIsExplosive() ? (float)this.damBonus * 0.33F : 0.0F), false, false);
            this.setDead();
         } else if (!this.getIsVampire() && !this.world.rand.nextBoolean()) {
            par1Entity.setFire(this.getIsSummoned() ? 4 : 2);
         } else {
            double mx = par1Entity.motionX;
            double my = par1Entity.motionY;
            double mz = par1Entity.motionZ;
            this.attackEntityAsMob(par1Entity);
            par1Entity.isAirBorne = false;
            par1Entity.motionX = mx;
            par1Entity.motionY = my;
            par1Entity.motionZ = mz;
         }
         this.playSound(net.minecraft.init.SoundEvents.ENTITY_BAT_HURT, 1.0F, 1.0F);
      }
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.dataManager.set(FLAGS, par1NBTTagCompound.getByte("BatFlags"));
      this.damBonus = par1NBTTagCompound.getByte("damBonus");
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setByte("BatFlags", this.dataManager.get(FLAGS));
      par1NBTTagCompound.setByte("damBonus", (byte)this.damBonus);
   }

   public boolean getCanSpawnHere() {
      int var1 = MathHelper.floor(this.getEntityBoundingBox().minY);
      int var2 = MathHelper.floor(this.posX);
      int var3 = MathHelper.floor(this.posZ);
      int var4 = this.world.getLightFor(EnumSkyBlock.BLOCK, new BlockPos(var2, var1, var3));
      byte var5 = 7;
      return var4 <= this.rand.nextInt(var5) && super.getCanSpawnHere();
   }

   protected Item getDropItem() {
      return !this.getIsSummoned() ? Items.GUNPOWDER : Item.getItemById(0);
   }

   protected boolean isValidLightLevel() {
      return true;
   }
}
