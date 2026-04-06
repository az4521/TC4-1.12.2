package thaumcraft.common.entities.monster.boss;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockLoot;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityGolemOrb;
import thaumcraft.common.lib.utils.EntityUtils;

public class EntityEldritchGolem extends EntityThaumcraftBoss implements IEldritchMob, IRangedAttackMob {
   private static final DataParameter<Byte> HEADLESS = EntityDataManager.createKey(EntityEldritchGolem.class, DataSerializers.BYTE);

   int beamCharge = 0;
   boolean chargingBeam = false;
   int arcing = 0;
   int ax = 0;
   int ay = 0;
   int az = 0;
   private int attackTimer;

   public EntityEldritchGolem(World worldIn) {
      super(worldIn);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityLivingBase.class, 1.1, false));
      this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.8));
      this.tasks.addTask(7, new EntityAIWander(this, 0.8));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.setSize(1.75F, 3.5F);
      this.isImmuneToFire = true;
   }

   public void generateName() {
      int t = (int)this.getEntityAttribute(EntityUtils.CHAMPION_MOD).getAttributeValue();
      if (t >= 0) {
         this.setCustomNameTag(String.format(I18n.translateToLocal("entity.Thaumcraft.EldritchGolem.name"),
                 ChampionModifier.mods[t].getModNameLocalized()));
      }

   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(HEADLESS, (byte)0);
   }

   public boolean isHeadless() {
      return this.dataManager.get(HEADLESS) == 1;
   }

   public void setHeadless(boolean par1) {
      this.dataManager.set(HEADLESS, (byte)(par1 ? 1 : 0));
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setBoolean("headless", this.isHeadless());
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.setHeadless(nbt.getBoolean("headless"));
      if (this.isHeadless()) {
         this.makeHeadless();
      }

   }

   public float getEyeHeight() {
      return this.isHeadless() ? 3.33F : 3.0F;
   }

   public int getTotalArmorValue() {
      return super.getTotalArmorValue() + 6;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(250.0F);
   }

   @Override
   protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) {
      return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.iron_golem.hurt"));
   }

   @Override
   protected net.minecraft.util.SoundEvent getDeathSound() {
      return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.iron_golem.death"));
   }

   @Override
   protected void playStepSound(BlockPos pos, Block blockIn) {
      net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.iron_golem.walk"));
      if (_snd != null) this.playSound(_snd, 1.0F, 1.0F);
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData entityData) {
      this.spawnTimer = 100;
      return super.onInitialSpawn(difficulty, entityData);
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (this.attackTimer > 0) {
         --this.attackTimer;
      }

      if (this.motionX * this.motionX + this.motionZ * this.motionZ > (double)2.5000003E-7F && this.rand.nextInt(5) == 0) {
         int i = MathHelper.floor(this.posX);
         int j = MathHelper.floor(this.posY - 0.2F);
         int k = MathHelper.floor(this.posZ);
         net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(new BlockPos(i, j, k));
         Block block = _bs.getBlock();
         if (this.world.getBlockState(new BlockPos(i, j, k)).getMaterial() != Material.AIR) {
            this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.BLOCK_CRACK,
               this.posX + ((double)this.rand.nextFloat() - (double)0.5F) * (double)this.width,
               this.getEntityBoundingBox().minY + 0.1,
               this.posZ + ((double)this.rand.nextFloat() - (double)0.5F) * (double)this.width,
               (double)4.0F * ((double)this.rand.nextFloat() - (double)0.5F),
               0.5F,
               ((double)this.rand.nextFloat() - (double)0.5F) * (double)4.0F,
               Block.getStateId(this.world.getBlockState(new BlockPos(i, j, k))));
         }

         if (!this.world.isRemote && block instanceof BlockLoot) {
            this.world.destroyBlock(new BlockPos(i, j, k), true);
         }
      }

      if (!this.world.isRemote) {
         int i = MathHelper.floor(this.posX + this.motionX);
         int j = MathHelper.floor(this.getEntityBoundingBox().minY);
         int k = MathHelper.floor(this.posZ + this.motionZ);
         net.minecraft.block.state.IBlockState _s2 = this.world.getBlockState(new BlockPos(i, j, k));
         float h = _s2.getBlockHardness(this.world, new BlockPos(i, j, k));
         if (h >= 0.0F && h <= 0.15F) {
            this.world.destroyBlock(new BlockPos(i, j, k), true);
         }
      }

   }

   public boolean attackEntityFrom(DamageSource source, float damage) {
      if (!this.world.isRemote && damage > this.getHealth() && !this.isHeadless()) {
         this.setHeadless(true);
         this.spawnTimer = 100;
         double xx = MathHelper.cos(this.rotationYaw % 360.0F / 180.0F * (float)Math.PI) * 0.75F;
         double zz = MathHelper.sin(this.rotationYaw % 360.0F / 180.0F * (float)Math.PI) * 0.75F;
         this.world.createExplosion(this, this.posX + xx, this.posY + (double)this.getEyeHeight(), this.posZ + zz, 2.0F, false);
         this.makeHeadless();
         return false;
      } else {
         return super.attackEntityFrom(source, damage);
      }
   }

   void makeHeadless() {
      this.tasks.addTask(2, new AILongRangeAttack(this, 3.0F, 1.0F, 5, 5, 24.0F));
   }

   public boolean attackEntityAsMob(Entity target) {
      if (this.attackTimer > 0) {
         return false;
      } else {
         this.attackTimer = 10;
         this.world.setEntityState(this, (byte)4);
         boolean flag = target.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 0.75F);
         if (flag) {
            target.motionY += 0.2000000059604645;
            if (this.isHeadless()) {
               target.addVelocity(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * 1.5F, 0.1, MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * 1.5F);
            }
         }

         return flag;
      }
   }

   public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f) {
      if (this.canEntityBeSeen(entitylivingbase) && !this.chargingBeam && this.beamCharge > 0) {
         this.beamCharge -= 15 + this.rand.nextInt(5);
         this.getLookHelper().setLookPosition(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY + (double)(entitylivingbase.height / 2.0F), entitylivingbase.posZ, 30.0F, 30.0F);
         Vec3d v = this.getLook(1.0F);
         EntityGolemOrb blast = new EntityGolemOrb(this.world, this, entitylivingbase, false);
         blast.posX += v.x;
         blast.posZ += v.z;
         blast.setPosition(blast.posX, blast.posY, blast.posZ);
         double d0 = entitylivingbase.posX + entitylivingbase.motionX - this.posX;
         double d1 = entitylivingbase.posY - this.posY - (double)(entitylivingbase.height / 2.0F);
         double d2 = entitylivingbase.posZ + entitylivingbase.motionZ - this.posZ;
         blast.shoot(d0, d1, d2, 0.66F, 5.0F);
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egattack")); if (_snd != null) this.playSound(_snd, 1.0F, 1.0F + this.rand.nextFloat() * 0.1F); }
         this.world.spawnEntity(blast);
      }

   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte id) {
      if (id == 4) {
         this.attackTimer = 10;
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.iron_golem.attack")); if (_snd != null) this.playSound(_snd, 1.0F, 1.0F); }
      } else if (id == 18) {
         this.spawnTimer = 150;
      } else if (id == 19) {
         if (this.arcing == 0) {
            float radius = 2.0F + this.rand.nextFloat() * 2.0F;
            double radians = Math.toRadians(this.rand.nextInt(360));
            double deltaX = (double)radius * Math.cos(radians);
            double deltaZ = (double)radius * Math.sin(radians);
            int bx = MathHelper.floor(this.posX + deltaX);
            int by = MathHelper.floor(this.posY);
            int bz = MathHelper.floor(this.posZ + deltaZ);

            for(int c = 0; c < 5 && this.world.isAirBlock(new BlockPos(bx, by, bz)); --by) {
               ++c;
            }

            if (this.world.isAirBlock(new BlockPos(bx, by + 1, bz)) && !this.world.isAirBlock(new BlockPos(bx, by, bz))) {
               this.ax = bx;
               this.ay = by;
               this.az = bz;
               this.arcing = 8 + this.rand.nextInt(5);
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:jacobs")); if (_snd != null) this.world.playSound(null, this.posX, this.posY, this.posZ, _snd, SoundCategory.BLOCKS, 0.8F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F); }
            }
         }
      } else {
         super.handleStatusUpdate(id);
      }

   }

   public void onUpdate() {
      if (this.getSpawnTimer() == 150) {
         this.world.setEntityState(this, (byte)18);
      }

      if (this.getSpawnTimer() > 0) {
         this.heal(2.0F);
      }

      super.onUpdate();
      if (this.world.isRemote) {
         if (this.isHeadless()) {
            this.rotationPitch = 0.0F;
            float f1 = MathHelper.cos(-this.renderYawOffset * ((float)Math.PI / 180F) - (float)Math.PI);
            float f2 = MathHelper.sin(-this.renderYawOffset * ((float)Math.PI / 180F) - (float)Math.PI);
            float f3 = -MathHelper.cos(-this.rotationPitch * ((float)Math.PI / 180F));
            float f4 = MathHelper.sin(-this.rotationPitch * ((float)Math.PI / 180F));
            Vec3d v = new Vec3d(f2 * f3, f4, f1 * f3);
            if (this.rand.nextInt(20) == 0) {
               float a = (this.rand.nextFloat() - this.rand.nextFloat()) / 2.0F;
               float b = (this.rand.nextFloat() - this.rand.nextFloat()) / 2.0F;
               Thaumcraft.proxy.spark((float)(this.posX + v.x + (double)a), (float)this.posY + this.getEyeHeight() - 0.25F, (float)(this.posZ + v.z + (double)b), 0.3F, 0.65F + this.rand.nextFloat() * 0.1F, 1.0F, 1.0F, 0.8F);
            }

            Thaumcraft.proxy.drawVentParticles(this.world, (double)((float)this.posX) + v.x * 0.66, (float)this.posY + this.getEyeHeight() - 0.75F, (double)((float)this.posZ) + v.z * 0.66, 0.0F, 0.001, 0.0F, 5592405, 4.0F);
            if (this.arcing > 0) {
               Thaumcraft.proxy.arcLightning(this.world, this.posX, this.posY + (double)(this.height / 2.0F), this.posZ, (double)this.ax + (double)0.5F, this.ay + 1, (double)this.az + (double)0.5F, 0.65F + this.rand.nextFloat() * 0.1F, 1.0F, 1.0F, 1.0F - (float)this.arcing / 10.0F);
               --this.arcing;
            }
         }
      } else {
         if (this.isHeadless() && this.beamCharge <= 0) {
            this.chargingBeam = true;
         }

         if (this.isHeadless() && this.chargingBeam) {
            ++this.beamCharge;
            this.world.setEntityState(this, (byte)19);
            if (this.beamCharge == 150) {
               this.chargingBeam = false;
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public int getAttackTimer() {
      return this.attackTimer;
   }

   public void setSwingingArms(boolean b) {}
}
