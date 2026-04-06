package thaumcraft.common.entities.monster;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

public class EntityThaumicSlime extends EntityMob implements IMob, ITaintedMob {
   private static final DataParameter<Byte> SLIME_SIZE = EntityDataManager.createKey(EntityThaumicSlime.class, DataSerializers.BYTE);
   private static final float[] spawnChances = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
   public float squishAmount;
   public float squishFactor;
   public float prevSquishFactor;
   private int slimeJumpDelay = 0;
   int launched = 10;
   int spitCounter = 100;

   public EntityThaumicSlime(World par1World) {
      super(par1World);
      int i = 1 << this.rand.nextInt(3);
      this.slimeJumpDelay = this.rand.nextInt(20) + 10;
      this.setSlimeSize(i);
      this.tasks.addTask(1, new net.minecraft.entity.ai.EntityAIBase() {
         public boolean shouldExecute() { return true; }
         public boolean shouldContinueExecuting() { return true; }
         public void updateTask() { slimeAI(); }
      });
   }

   public EntityThaumicSlime(World par1World, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving) {
      super(par1World);
      this.setSlimeSize(1);
      this.posY = (par2EntityLiving.getEntityBoundingBox().minY + par2EntityLiving.getEntityBoundingBox().maxY) / 2.0D;
      double var6 = par3EntityLiving.posX - par2EntityLiving.posX;
      double var8 = par3EntityLiving.getEntityBoundingBox().minY + (double)(par3EntityLiving.height / 3.0F) - this.posY;
      double var10 = par3EntityLiving.posZ - par2EntityLiving.posZ;
      double var12 = MathHelper.sqrt(var6 * var6 + var10 * var10);
      if (var12 >= 1.0E-7) {
         float var14 = (float)(Math.atan2(var10, var6) * 180.0D / Math.PI) - 90.0F;
         float var15 = (float)(-(Math.atan2(var8, var12) * 180.0D / Math.PI));
         double var16 = var6 / var12;
         double var18 = var10 / var12;
         this.setLocationAndAngles(par2EntityLiving.posX + var16, this.posY, par2EntityLiving.posZ + var18, var14, var15);
         float var20 = (float)var12 * 0.2F;
         this.setThrowableHeading(var6, var8 + (double)var20, var10, 1.5F, 1.0F);
      }
   }

   public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8) {
      float var9 = MathHelper.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
      par1 /= var9;
      par3 /= var9;
      par5 /= var9;
      par1 += this.rand.nextGaussian() * 0.0075D * (double)par8;
      par3 += this.rand.nextGaussian() * 0.0075D * (double)par8;
      par5 += this.rand.nextGaussian() * 0.0075D * (double)par8;
      par1 *= par7;
      par3 *= par7;
      par5 *= par7;
      this.motionX = par1;
      this.motionY = par3;
      this.motionZ = par5;
      float var10 = MathHelper.sqrt(par1 * par1 + par5 * par5);
      this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
      this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, var10) * 180.0D / Math.PI);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SLIME_SIZE, (byte) 1);
   }

   public void setSlimeSize(int par1) {
      this.dataManager.set(SLIME_SIZE, (byte) par1);
      float ss = (float)Math.sqrt(par1);
      this.setSize(0.25F * ss + 0.25F, 0.25F * ss + 0.25F);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(par1);
      if (this.getHealth() > this.getMaxHealth()) {
         this.setHealth(this.getMaxHealth());
      }
      this.experienceValue = (int)ss;
   }

   protected int getAttackStrength() {
      return this.getSlimeSize();
   }

   public int getSlimeSize() {
      return this.dataManager.get(SLIME_SIZE);
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setInteger("Size", this.getSlimeSize() - 1);
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.setSlimeSize(par1NBTTagCompound.getInteger("Size") + 1);
   }

   private SoundEvent getJumpSoundEvent() {
      return this.getSlimeSize() > 3 ? SoundEvents.ENTITY_SLIME_JUMP : SoundEvents.ENTITY_SMALL_SLIME_JUMP;
   }

   public void onUpdate() {
      if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.getSlimeSize() > 0) {
         this.isDead = true;
      }

      this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;
      this.prevSquishFactor = this.squishFactor;
      boolean flag = this.onGround;
      super.onUpdate();
      int i = (int)Math.sqrt(this.getSlimeSize());
      if (this.launched > 0) {
         --this.launched;
         if (this.world.isRemote) {
            for (int j = 0; j < i * (this.launched + 1); ++j) {
               Thaumcraft.proxy.slimeJumpFX(this, i);
            }
         }
      }

      if (this.onGround && !flag) {
         if (this.world.isRemote) {
            for (int j = 0; j < i * 8; ++j) {
               Thaumcraft.proxy.slimeJumpFX(this, i);
            }
         }

         if (this.makesSoundOnLand()) {
            this.playSound(this.getJumpSoundEvent(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         }

         this.squishAmount = -0.5F;
      } else if (!this.onGround && flag) {
         this.squishAmount = 1.0F;
      }

      this.alterSquishAmount();
      if (this.world.isRemote) {
         float ff = (float)Math.sqrt(this.getSlimeSize());
         this.setSize(0.6F * ff, 0.6F * ff);
      }
   }

   protected EntityThaumicSlime getClosestMergableSlime() {
      EntityThaumicSlime closest = null;
      double distance = Double.MAX_VALUE;
      List ents = this.world.getEntitiesWithinAABB(EntityThaumicSlime.class, new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).grow(16.0, 8.0, 16.0));
      if (ents != null && !ents.isEmpty()) {
         for (Object s : ents) {
            EntityThaumicSlime slime = (EntityThaumicSlime)s;
            if (slime.getEntityId() != this.getEntityId() && slime.ticksExisted > 100 && slime.getSlimeSize() < 100 && this.getDistanceSq(slime) < distance) {
               closest = slime;
            }
            distance = this.getDistanceSq(slime);
         }
      }
      return closest;
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.despawnEntity();
      EntityPlayer entityplayer = this.world.getClosestPlayerToEntity(this, 16.0);
      if (entityplayer != null) {
         if (this.spitCounter > 0) {
            --this.spitCounter;
         }

         this.faceEntity(entityplayer, 10.0F, 20.0F);
         if (this.getDistance(entityplayer) > 4.0F && this.spitCounter <= 0 && this.getSlimeSize() > 3) {
            this.spitCounter = 101;
            if (!this.world.isRemote) {
               EntityThaumicSlime flyslime = new EntityThaumicSlime(this.world, this, entityplayer);
               this.world.spawnEntity(flyslime);
            }

            this.world.playSound(null, this.posX, this.posY, this.posZ,
                  SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:gore")),
                  SoundCategory.HOSTILE, 1.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
            this.setSlimeSize(this.getSlimeSize() - 1);
         }
      } else {
         EntityThaumicSlime slime = this.getClosestMergableSlime();
         if (slime != null) {
            this.faceEntity(slime, 10.0F, 20.0F);
            if (this.getDistance(slime) < this.width + slime.width) {
               slime.setSlimeSize(Math.min(100, slime.getSlimeSize() + this.getSlimeSize()));
               this.setDead();
            }
         }
      }

   }

   private void slimeAI() {
      EntityPlayer entityplayer = this.world.getClosestPlayerToEntity(this, 16.0);
      boolean move = entityplayer != null || this.getClosestMergableSlime() != null;
      if (this.onGround && this.slimeJumpDelay-- <= 0 && move) {
         this.slimeJumpDelay = this.getJumpDelay();
         if (entityplayer != null) {
            this.slimeJumpDelay /= 3;
         }
         if (this.makesSoundOnJump()) {
            this.playSound(this.getJumpSoundEvent(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
         }
         this.squishAmount = 1.0F;
         float speed = (float) Math.sqrt(this.getSlimeSize()) * 0.1F;
         float yaw = this.rotationYaw * 0.017453292F;
         this.motionX += -MathHelper.sin(yaw) * speed;
         this.motionZ += MathHelper.cos(yaw) * speed;
         this.motionY = 0.42;
         this.isAirBorne = true;
      }
   }

   protected void alterSquishAmount() {
      this.squishAmount *= 0.6F;
   }

   protected int getJumpDelay() {
      return this.rand.nextInt(16) + 8;
   }

   protected EntityThaumicSlime createInstance() {
      return new EntityThaumicSlime(this.world);
   }

   public void setDead() {
      int i = (int)Math.sqrt(this.getSlimeSize());
      if (!this.world.isRemote && i > 1 && this.getHealth() <= 0.0F) {
         for (int k = 0; k < i; ++k) {
            float f = ((float)(k % 2) - 0.5F) * (float)i / 4.0F;
            float f1 = ((float)(k / 2) - 0.5F) * (float)i / 4.0F;
            EntityThaumicSlime entityslime = this.createInstance();
            entityslime.setSlimeSize(1);
            entityslime.setLocationAndAngles(this.posX + (double)f, this.posY + 0.5D, this.posZ + (double)f1, this.rand.nextFloat() * 360.0F, 0.0F);
            this.world.spawnEntity(entityslime);
         }
      }

      super.setDead();
   }

   public void onCollideWithPlayer(EntityPlayer par1EntityPlayer) {
      if (this.canDamagePlayer()) {
         int i = (int)Math.max(1.0F, Math.sqrt(this.getSlimeSize()));
         if (this.launched > 0 && i == 2) {
            i = 3;
         }

         if (this.canEntityBeSeen(par1EntityPlayer) && this.getDistanceSq(par1EntityPlayer) < 0.8 * (double)i * 0.8 * (double)i && par1EntityPlayer.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getAttackStrength())) {
            this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
         }
      }
   }

   protected boolean canDamagePlayer() {
      return this.getSlimeSize() > 0;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return this.getSlimeSize() > 3 ? SoundEvents.ENTITY_SLIME_HURT : SoundEvents.ENTITY_SMALL_SLIME_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return this.getSlimeSize() > 3 ? SoundEvents.ENTITY_SLIME_DEATH : SoundEvents.ENTITY_SMALL_SLIME_DEATH;
   }

   protected Item getDropItem() {
      return this.getSlimeSize() < 3 ? ConfigItems.itemResource : Item.getItemById(0);
   }

   protected void dropFewItems(boolean flag, int i) {
      if (this.getSlimeSize() < 3 && this.rand.nextInt(3) == 0) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 11), this.height / 2.0F);
      }
   }

   protected float getSoundVolume() {
      return 0.1F * (float)Math.sqrt(this.getSlimeSize());
   }

   public int getVerticalFaceSpeed() {
      return 0;
   }

   protected boolean makesSoundOnJump() {
      return this.getSlimeSize() > 3;
   }

   protected boolean makesSoundOnLand() {
      return this.getSlimeSize() > 5;
   }
}
