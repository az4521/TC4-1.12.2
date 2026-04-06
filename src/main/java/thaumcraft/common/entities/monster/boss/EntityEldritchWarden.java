package thaumcraft.common.entities.monster.boss;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.EntityCultist;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityEldritchOrb;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockArc;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.lib.network.fx.PacketFXSonic;
import thaumcraft.common.lib.utils.EntityUtils;

public class EntityEldritchWarden extends EntityThaumcraftBoss implements IRangedAttackMob, IEldritchMob {
   private static final DataParameter<Byte> TITLE_IDX = EntityDataManager.createKey(EntityEldritchWarden.class, DataSerializers.BYTE);

   String[] titles = new String[]{"Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn", "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon"};
   boolean fieldFrenzy = false;
   int fieldFrenzyCounter = 0;
   boolean lastBlast = false;
   public float armLiftL = 0.0F;
   public float armLiftR = 0.0F;

   public EntityEldritchWarden(World worldIn) {
      super(worldIn);
      ((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new AILongRangeAttack(this, 3.0F, 1.0F, 20, 40, 24.0F));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityLivingBase.class, 1.1, false));
      this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.8));
      this.tasks.addTask(7, new EntityAIWander(this, 1.0F));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityCultist.class, true));
      this.setSize(1.5F, 3.5F);
   }

   public void generateName() {
      int t = (int)this.getEntityAttribute(EntityUtils.CHAMPION_MOD).getAttributeValue();
      if (t >= 0) {
         this.setCustomNameTag(String.format(I18n.translateToLocal("entity.Thaumcraft.EldritchWarden.name"), this.getTitle(), ChampionModifier.mods[t].getModNameLocalized()));
      }

   }

   private String getTitle() {
      return this.titles[this.dataManager.get(TITLE_IDX)];
   }

   private void setTitle(int title) {
      this.dataManager.set(TITLE_IDX, (byte)title);
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(TITLE_IDX, (byte)0);
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setByte("title", this.dataManager.get(TITLE_IDX));
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.setTitle(nbt.getByte("title"));
   }

   public int getTotalArmorValue() {
      return super.getTotalArmorValue() + 4;
   }

   protected void updateAITasks() {
      if (this.fieldFrenzyCounter == 0) {
         super.updateAITasks();
      }

      if (this.hurtResistantTime <= 0 && this.ticksExisted % 25 == 0) {
         int bh = (int)(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 0.66);
         if (this.getAbsorptionAmount() < (float)bh) {
            this.setAbsorptionAmount(this.getAbsorptionAmount() + 1.0F);
         }
      }

   }

   public void onUpdate() {
      if (this.getSpawnTimer() == 150) {
         this.world.setEntityState(this, (byte)18);
      }

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
         Thaumcraft.proxy.wispFXEG(this.world, x, (float)(this.posY + (double)0.25F * (double)this.height), z, this);
         if (this.spawnTimer > 0) {
            float he = Math.max(1.0F, this.height * ((float)(150 - this.spawnTimer) / 150.0F));

            for(int a = 0; a < 33; ++a) {
               Thaumcraft.proxy.smokeSpiral(this.world, this.posX, this.getEntityBoundingBox().minY + (double)(he / 2.0F), this.posZ, he, this.rand.nextInt(360), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, 2232623);
            }
         }
      }

   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posY);
      int k = MathHelper.floor(this.posZ);

      for(int l = 0; l < 4; ++l) {
         i = MathHelper.floor(this.posX + (double)((float)(l % 2 * 2 - 1) * 0.25F));
         j = MathHelper.floor(this.posY);
         k = MathHelper.floor(this.posZ + (double)((float)(l / 2 % 2 * 2 - 1) * 0.25F));
         if (this.world.isAirBlock(new BlockPos(i, j, k))) {
            this.world.setBlockState(new BlockPos(i, j, k), ConfigBlocks.blockAiry.getStateFromMeta(11), 3);
         }
      }

      if (!this.world.isRemote && this.fieldFrenzyCounter > 0) {
         if (this.fieldFrenzyCounter == 150) {
            this.teleportHome();
         }

         this.performFieldFrenzy();
      }

   }

   private void performFieldFrenzy() {
      if (this.fieldFrenzyCounter < 121 && this.fieldFrenzyCounter % 10 == 0) {
         this.world.setEntityState(this, (byte)17);
         double radius = (double)(150 - this.fieldFrenzyCounter) / (double)8.0F;
         int d = 1 + this.fieldFrenzyCounter / 8;
         int i = MathHelper.floor(this.posX);
         int j = MathHelper.floor(this.posY);
         int k = MathHelper.floor(this.posZ);

         for(int q = 0; q < 180 / d; ++q) {
            double radians = Math.toRadians(q * 2 * d);
            int deltaX = (int)(radius * Math.cos(radians));
            int deltaZ = (int)(radius * Math.sin(radians));
            if (this.world.isAirBlock(new BlockPos(i + deltaX, j, k + deltaZ)) && this.world.getBlockState(new BlockPos(i + deltaX, j - 1, k + deltaZ)).isNormalCube()) {
               this.world.setBlockState(new BlockPos(i + deltaX, j, k + deltaZ), ConfigBlocks.blockAiry.getStateFromMeta(11), 3);
               this.world.scheduleUpdate(new BlockPos(i + deltaX, j, k + deltaZ), ConfigBlocks.blockAiry, 250 + this.rand.nextInt(150));
               if (this.rand.nextFloat() < 0.3F) {
                  PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockArc(i + deltaX, j, k + deltaZ, this.getEntityId()), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), i + deltaX, j, k + deltaZ, 32.0F));
               } else {
                  PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(i + deltaX, j, k + deltaZ, 8388736), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), i + deltaX, j, k + deltaZ, 32.0F));
               }
            }
         }

         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); if (_snd != null) this.world.playSound(null, this.getPosition(), _snd, SoundCategory.BLOCKS, 1.0F, 0.9F + this.rand.nextFloat() * 0.1F); }
      }

      --this.fieldFrenzyCounter;
   }

   protected void teleportHome() {
      EnderTeleportEvent event = new EnderTeleportEvent(this, this.getHomePosition().getX(), this.getHomePosition().getY(), this.getHomePosition().getZ(), 0.0F);
      if (!MinecraftForge.EVENT_BUS.post(event)) {
         double d3 = this.posX;
         double d4 = this.posY;
         double d5 = this.posZ;
         this.posX = event.getTargetX();
         this.posY = event.getTargetY();
         this.posZ = event.getTargetZ();
         boolean flag = false;
         int i = MathHelper.floor(this.posX);
         int j = MathHelper.floor(this.posY);
         int k = MathHelper.floor(this.posZ);
         if (this.world.isBlockLoaded(new BlockPos(i, j, k))) {
            boolean flag1 = false;
            int tries = 20;

            while(!flag1 && tries > 0) {
               Block block = this.world.getBlockState(new BlockPos(i, j - 1, k)).getBlock();
               Block block2 = this.world.getBlockState(new BlockPos(i, j, k)).getBlock();
               if (this.world.getBlockState(new BlockPos(i, j - 1, k)).getMaterial().blocksMovement() && !this.world.getBlockState(new BlockPos(i, j, k)).getMaterial().blocksMovement()) {
                  flag1 = true;
               } else {
                  i = MathHelper.floor(this.posX) + this.rand.nextInt(8) - this.rand.nextInt(8);
                  k = MathHelper.floor(this.posZ) + this.rand.nextInt(8) - this.rand.nextInt(8);
                  --tries;
               }
            }

            if (flag1) {
               this.setPosition((double)i + (double)0.5F, (double)j + 0.1, (double)k + (double)0.5F);
               if (this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()) {
                  flag = true;
               }
            }
         }

         if (!flag) {
            this.setPosition(d3, d4, d5);
         } else {
            short short1 = 128;

            for(int l = 0; l < short1; ++l) {
               double d6 = (double)l / ((double)short1 - (double)1.0F);
               float f = (this.rand.nextFloat() - 0.5F) * 0.2F;
               float f1 = (this.rand.nextFloat() - 0.5F) * 0.2F;
               float f2 = (this.rand.nextFloat() - 0.5F) * 0.2F;
               double d7 = d3 + (this.posX - d3) * d6 + (this.rand.nextDouble() - (double)0.5F) * (double)this.width * (double)2.0F;
               double d8 = d4 + (this.posY - d4) * d6 + this.rand.nextDouble() * (double)this.height;
               double d9 = d5 + (this.posZ - d5) * d6 + (this.rand.nextDouble() - (double)0.5F) * (double)this.width * (double)2.0F;
               this.world.spawnParticle(EnumParticleTypes.PORTAL, d7, d8, d9, f, f1, f2);
            }

            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.endermen.teleport")); if (_snd != null) this.world.playSound(null, this.getPosition(), _snd, SoundCategory.BLOCKS, 1.0F, 1.0F); }
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.endermen.teleport")); if (_snd != null) this.playSound(_snd, 1.0F, 1.0F); }
         }
      }
   }

   @Override
   public boolean isEntityInvulnerable(DamageSource source) {
      return this.fieldFrenzyCounter > 0 || super.isEntityInvulnerable(source);
   }

   public boolean attackEntityFrom(DamageSource source, float damage) {
      if (!this.isEntityInvulnerable(source) && source != DamageSource.DROWN && source != DamageSource.WITHER) {
         boolean aef = super.attackEntityFrom(source, damage);
         if (!this.world.isRemote && aef && !this.fieldFrenzy && this.getAbsorptionAmount() <= 0.0F) {
            this.fieldFrenzy = true;
            this.fieldFrenzyCounter = 150;
         }

         return aef;
      } else {
         return false;
      }
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData entityData) {
      this.spawnTimer = 150;
      this.setTitle(this.rand.nextInt(this.titles.length));
      this.setAbsorptionAmount((float)((double)this.getAbsorptionAmount() + this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 0.66));
      return super.onInitialSpawn(difficulty, entityData);
   }

   public float getEyeHeight() {
      return 3.1F;
   }

   public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f) {
      if (this.rand.nextFloat() > 0.2F) {
         EntityEldritchOrb blast = new EntityEldritchOrb(this.world, this);
         this.lastBlast = !this.lastBlast;
         this.world.setEntityState(this, (byte)(this.lastBlast ? 16 : 15));
         int rr = this.lastBlast ? 90 : 180;
         double xx = MathHelper.cos((this.rotationYaw + (float)rr) % 360.0F / 180.0F * (float)Math.PI) * 0.5F;
         double yy = 0.13;
         double zz = MathHelper.sin((this.rotationYaw + (float)rr) % 360.0F / 180.0F * (float)Math.PI) * 0.5F;
         blast.setPosition(blast.posX - xx, blast.posY - yy, blast.posZ - zz);
         double d0 = entitylivingbase.posX + entitylivingbase.motionX - this.posX;
         double d1 = entitylivingbase.posY - this.posY - (double)(entitylivingbase.height / 2.0F);
         double d2 = entitylivingbase.posZ + entitylivingbase.motionZ - this.posZ;
         blast.shoot(d0, d1, d2, 1.0F, 2.0F);
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egattack")); if (_snd != null) this.playSound(_snd, 2.0F, 1.0F + this.rand.nextFloat() * 0.1F); }
         this.world.spawnEntity(blast);
      } else if (this.canEntityBeSeen(entitylivingbase)) {
         PacketHandler.INSTANCE.sendToAllAround(new PacketFXSonic(this.getEntityId()), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 32.0F));
         entitylivingbase.addVelocity(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * 1.5F, 0.1, MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * 1.5F);

         try {
            entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.WITHER, 400, 0));
            entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 400, 0));
         } catch (Exception ignored) {
         }

         if (entitylivingbase instanceof EntityPlayer) {
            Thaumcraft.addWarpToPlayer((EntityPlayer)entitylivingbase, 3 + this.world.rand.nextInt(3), true);
         }

         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egscreech")); if (_snd != null) this.playSound(_snd, 4.0F, 1.0F + this.rand.nextFloat() * 0.1F); }
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
      } else if (id == 18) {
         this.spawnTimer = 150;
      } else {
         super.handleStatusUpdate(id);
      }

   }

   public boolean canAttackClass(Class clazz) {
      return clazz != EntityEldritchGuardian.class && super.canAttackClass(clazz);
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

   public void setSwingingArms(boolean b) {}
}
