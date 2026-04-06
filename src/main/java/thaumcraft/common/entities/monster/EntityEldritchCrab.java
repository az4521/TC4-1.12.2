package thaumcraft.common.entities.monster;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;

public class EntityEldritchCrab extends EntityMob {
   private static final DataParameter<Byte> CRAB_FLAGS = EntityDataManager.createKey(EntityEldritchCrab.class, DataSerializers.BYTE);
   private int attackTime = 0;

   public EntityEldritchCrab(World par1World) {
      super(par1World);
      this.setSize(0.8F, 0.6F);
      this.experienceValue = 6;
      ((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAILeapAtTarget(this, 0.63F));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityLivingBase.class, 1.0F, false));
      this.tasks.addTask(7, new EntityAIWander(this, 0.8));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityCultist.class, true));
   }

   public double getYOffset() {
      return this.isRiding() ? (double)0.5F : (double)0.0F;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.hasHelm() ? 0.275 : 0.3);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(CRAB_FLAGS, (byte) 0);
   }

   public boolean canPickUpLoot() {
      return false;
   }

   public int getTotalArmorValue() {
      return this.hasHelm() ? 5 : 0;
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingData) {
      if (this.world.getDifficulty() == EnumDifficulty.HARD) {
         this.setHelm(true);
      } else {
         this.setHelm(this.rand.nextFloat() < 0.33F);
      }

      if (livingData == null) {
         livingData = new EntitySpider.GroupData();
         if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F) {
            ((EntitySpider.GroupData)livingData).setRandomEffect(this.world.rand);
         }
      }

      return super.onInitialSpawn(difficulty, livingData);
   }

   protected boolean isAIEnabled() {
      return true;
   }

   public boolean hasHelm() {
      return (this.dataManager.get(CRAB_FLAGS) & 1) != 0;
   }

   public void setHelm(boolean par1) {
      byte var2 = this.dataManager.get(CRAB_FLAGS);
      if (par1) {
         this.dataManager.set(CRAB_FLAGS, (byte)(var2 | 1));
      } else {
         this.dataManager.set(CRAB_FLAGS, (byte)(var2 & -2));
      }
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.ticksExisted < 20) {
         this.fallDistance = 0.0F;
      }

      Entity riding = this.getRidingEntity();
      Entity target = this.getAttackTarget();
      if (riding == null && target != null && target.getPassengers().isEmpty() && !this.onGround && !this.hasHelm() && !target.isDead && this.posY - target.posY >= (double)(target.height / 2.0F) && this.getDistanceSq(target) < (double)4.0F) {
         this.startRiding(target);
      }

      if (!this.world.isRemote && this.getRidingEntity() != null && this.attackTime <= 0) {
         this.attackTime = 10 + this.rand.nextInt(10);
         this.attackEntityAsMob(this.getRidingEntity());
         if (this.getRidingEntity() != null && (double)this.rand.nextFloat() < 0.2) {
            this.dismountRidingEntity();
         }
      }
   }

   protected Item getDropItem() {
      return super.getDropItem();
   }

   protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
      super.dropFewItems(wasRecentlyHit, lootingModifier);
      if (wasRecentlyHit && (this.rand.nextInt(3) == 0 || this.rand.nextInt(1 + lootingModifier) > 0)) {
         this.dropItem(Items.ENDER_PEARL, 1);
      }
   }

   public boolean attackEntityAsMob(Entity entityIn) {
      if (super.attackEntityAsMob(entityIn)) {
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:crabclaw")); if (_snd != null) this.playSound(_snd, 1.0F, 0.9F + this.world.rand.nextFloat() * 0.2F); }
         return true;
      } else {
         return false;
      }
   }

   public boolean attackEntityFrom(DamageSource source, float damage) {
      boolean b = super.attackEntityFrom(source, damage);
      if (this.hasHelm() && this.getHealth() / this.getMaxHealth() <= 0.5F) {
         this.setHelm(false);
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
      }
      return b;
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.dataManager.set(CRAB_FLAGS, par1NBTTagCompound.getByte("Flags"));
      if (!this.hasHelm()) {
         this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
      }
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setByte("Flags", this.dataManager.get(CRAB_FLAGS));
   }

   public int getTalkInterval() {
      return 160;
   }

   @Override protected net.minecraft.util.SoundEvent getAmbientSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:crabtalk")); }
   @Override protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) { return super.getHurtSound(source); }
   @Override protected net.minecraft.util.SoundEvent getDeathSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:crabdeath")); }
   @Override protected void playStepSound(BlockPos pos, Block blockIn) { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.spider.step")); if (_snd != null) this.playSound(_snd, 0.15F, 1.0F); }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.ARTHROPOD;
   }

   public boolean isPotionApplicable(PotionEffect potioneffectIn) {
      return potioneffectIn.getPotion() != MobEffects.POISON && super.isPotionApplicable(potioneffectIn);
   }

   public boolean isOnSameTeam(EntityLivingBase el) {
      return el instanceof EntityEldritchCrab;
   }
}
