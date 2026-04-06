package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;
import thaumcraft.common.items.ItemWispEssence;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXSonic;
import thaumcraft.common.lib.network.misc.PacketMiscEvent;

public class EntityEldritchGuardian extends EntityMob implements IRangedAttackMob, IEldritchMob {
   private static final DataParameter<Byte> DATA_12 = EntityDataManager.createKey(EntityEldritchGuardian.class, DataSerializers.BYTE);
   private static final DataParameter<Byte> DATA_13 = EntityDataManager.createKey(EntityEldritchGuardian.class, DataSerializers.BYTE);
   private static final DataParameter<Byte> DATA_14 = EntityDataManager.createKey(EntityEldritchGuardian.class, DataSerializers.BYTE);

   public float armLiftL = 0.0F;
   public float armLiftR = 0.0F;
   boolean lastBlast = false;

   public EntityEldritchGuardian(World worldIn) {
      super(worldIn);
      ((net.minecraft.pathfinding.PathNavigateGround)this.getNavigator()).setBreakDoors(true);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new AILongRangeAttack(this, 8.0F, 1.0F, 20, 40, 24.0F));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityLivingBase.class, 1.0F, false));
      this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.8));
      this.tasks.addTask(7, new EntityAIWander(this, 1.0F));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityCultist.class, true));
      this.setSize(0.8F, 2.25F);
      this.experienceValue = 20;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0F);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(DATA_12, (byte)0);
      this.dataManager.register(DATA_13, (byte)0);
      this.dataManager.register(DATA_14, (byte)0);
   }

   public int getTotalArmorValue() {
      return 4;
   }

   protected boolean isAIEnabled() {
      return true;
   }

   public boolean canPickUpLoot() {
      return false;
   }

   public boolean attackEntityFrom(DamageSource source, float damage) {
      if (source.isMagicDamage()) {
         damage /= 2.0F;
      }

      return super.attackEntityFrom(source, damage);
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         if (this.armLiftL > 0.0F) {
            this.armLiftL -= 0.05F;
         }

         if (this.armLiftR > 0.0F) {
            this.armLiftR -= 0.05F;
         }

         float x = (float)(this.posX + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F));
         float z = (float)(this.posZ + (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F));
         Thaumcraft.proxy.wispFXEG(this.world, x, (float)(this.posY + 0.22 * (double)this.height), z, this);
      } else if (this.world.provider.getDimension() != Config.dimensionOuterId && (this.ticksExisted == 0 || this.ticksExisted % 100 == 0) && this.world.getDifficulty() != EnumDifficulty.EASY) {
         double d6 = this.world.getDifficulty() == EnumDifficulty.HARD ? (double)576.0F : (double)256.0F;

         for(int i = 0; i < this.world.playerEntities.size(); ++i) {
            EntityPlayer entityplayer1 = (EntityPlayer)this.world.playerEntities.get(i);
            if (entityplayer1.isEntityAlive()) {
               double d5 = entityplayer1.getDistanceSq(this.posX, this.posY, this.posZ);
               if (d5 < d6) {
                  PacketHandler.INSTANCE.sendTo(new PacketMiscEvent((short)2), (EntityPlayerMP)entityplayer1);
               }
            }
         }
      }

   }

   public boolean attackEntityAsMob(Entity entityIn) {
      boolean flag = super.attackEntityAsMob(entityIn);
      if (flag) {
         int i = this.world.getDifficulty().getId();
         if (this.getHeldItemMainhand().isEmpty() && this.isBurning() && this.rand.nextFloat() < (float)i * 0.3F) {
            entityIn.setFire(2 * i);
         }
      }

      return flag;
   }

   @Override
   protected net.minecraft.util.SoundEvent getAmbientSound() {
      return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egidle"));
   }

   @Override
   protected net.minecraft.util.SoundEvent getDeathSound() {
      return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egdeath"));
   }

   public int getTalkInterval() {
      return 500;
   }

   protected Item getDropItem() {
       return super.getDropItem();
   }

   protected void dropFewItems(boolean flag, int i) {
      if (this.rand.nextBoolean()) {
         ItemStack ess = new ItemStack(ConfigItems.itemWispEssence);
         new AspectList();
         ((ItemWispEssence)ess.getItem()).setAspects(ess, (new AspectList()).add(Aspect.UNDEAD, 2));
         this.entityDropItem(ess, 1.0F);
      }

      if (this.rand.nextBoolean()) {
         ItemStack ess = new ItemStack(ConfigItems.itemWispEssence);
         new AspectList();
         ((ItemWispEssence)ess.getItem()).setAspects(ess, (new AspectList()).add(Aspect.ELDRITCH, 2));
         this.entityDropItem(ess, 1.0F);
      }

      this.dropItem(ConfigItems.itemEldritchObject, 1);

      super.dropFewItems(flag, i);
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEAD;
   }

   public void writeEntityToNBT(NBTTagCompound tagCompound) {
      super.writeEntityToNBT(tagCompound);
      if (this.getHomePosition() != null && this.getMaximumHomeDistance() > 0.0F) {
         tagCompound.setInteger("HomeD", (int)this.getMaximumHomeDistance());
         tagCompound.setInteger("HomeX", this.getHomePosition().getX());
         tagCompound.setInteger("HomeY", this.getHomePosition().getY());
         tagCompound.setInteger("HomeZ", this.getHomePosition().getZ());
      }

   }

   public void readEntityFromNBT(NBTTagCompound tagCompund) {
      super.readEntityFromNBT(tagCompund);
      if (tagCompund.hasKey("HomeD")) {
         this.setHomePosAndDistance(new BlockPos(tagCompund.getInteger("HomeX"), tagCompund.getInteger("HomeY"), tagCompund.getInteger("HomeZ")), tagCompund.getInteger("HomeD"));
      }

   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData entityData) {
      IEntityLivingData p_110161_1_1 = super.onInitialSpawn(difficulty, entityData);
      if (this.world.provider.getDimension() == Config.dimensionOuterId) {
         int bh = (int)this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2;
         this.setAbsorptionAmount(this.getAbsorptionAmount() + (float)bh);
      }

      return p_110161_1_1;
   }

   protected void updateAITasks() {
      super.updateAITasks();
      if (this.world.provider.getDimension() == Config.dimensionOuterId && this.hurtResistantTime <= 0 && this.ticksExisted % 25 == 0) {
         int bh = (int)this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() / 2;
         if (this.getAbsorptionAmount() < (float)bh) {
            this.setAbsorptionAmount(this.getAbsorptionAmount() + 1.0F);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte id) {
      if (id == 15) {
         this.armLiftL = 0.5F;
      } else if (id == 16) {
         this.armLiftR = 0.5F;
      } else if (id == 17) {
         this.armLiftL = 0.9F;
         this.armLiftR = 0.9F;
      } else {
         super.handleStatusUpdate(id);
      }

   }

   protected boolean canDespawn() {
      return !this.hasHome();
   }

   public float getEyeHeight() {
      return 2.1F;
   }

   public boolean getCanSpawnHere() {
      List ents = this.world.getEntitiesWithinAABB(EntityEldritchGuardian.class, new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX + (double)1.0F, this.posY + (double)1.0F, this.posZ + (double)1.0F).expand(32.0F, 16.0F, 32.0F));
      return ents.isEmpty() && super.getCanSpawnHere();
   }

   protected boolean isValidLightLevel() {
      return true;
   }

   protected float getSoundVolume() {
      return 1.5F;
   }

   public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f) {
      if (this.rand.nextFloat() > 0.1F) {
         EntityEldritchOrb blast = new EntityEldritchOrb(this.world, this);
         this.lastBlast = !this.lastBlast;
         this.world.setEntityState(this, (byte)(this.lastBlast ? 16 : 15));
         int rr = this.lastBlast ? 90 : 180;
         double xx = MathHelper.cos((this.rotationYaw + (float)rr) % 360.0F / 180.0F * (float)Math.PI) * 0.5F;
         double yy = 0.057777777 * (double)this.height;
         double zz = MathHelper.sin((this.rotationYaw + (float)rr) % 360.0F / 180.0F * (float)Math.PI) * 0.5F;
         blast.setPosition(blast.posX - xx, blast.posY - yy, blast.posZ - zz);
         double d0 = entitylivingbase.posX + entitylivingbase.motionX - this.posX;
         double d1 = entitylivingbase.posY - this.posY - (double)(entitylivingbase.height / 2.0F);
         double d2 = entitylivingbase.posZ + entitylivingbase.motionZ - this.posZ;
         blast.setThrowableHeading(d0, d1, d2, 1.0F, 2.0F);
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egattack")); if (_snd != null) this.playSound(_snd, 2.0F, 1.0F + this.rand.nextFloat() * 0.1F); }
         this.world.spawnEntity(blast);
      } else if (this.canEntityBeSeen(entitylivingbase)) {
         PacketHandler.INSTANCE.sendToAllAround(new PacketFXSonic(this.getEntityId()), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 32.0F));

         try {
            entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.WITHER, 400, 0));
         } catch (Exception ignored) {
         }

         if (entitylivingbase instanceof EntityPlayer) {
            Thaumcraft.addWarpToPlayer((EntityPlayer)entitylivingbase, 1 + this.world.rand.nextInt(3), true);
         }

         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egscreech")); if (_snd != null) this.playSound(_snd, 3.0F, 1.0F + this.rand.nextFloat() * 0.1F); }
      }

   }

   @Override
   public void setSwingingArms(boolean swinging) {}

   public boolean isOnSameTeam(EntityLivingBase el) {
      return el instanceof IEldritchMob;
   }
}
