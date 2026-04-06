package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.combat.AICultistHurtByTarget;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.ai.misc.AIAltarFocus;
import thaumcraft.common.entities.projectile.EntityGolemOrb;

public class EntityCultistCleric extends EntityCultist implements IRangedAttackMob, IEntityAdditionalSpawnData {

   private static final DataParameter<Byte> CLERIC_FLAGS = EntityDataManager.createKey(EntityCultistCleric.class, DataSerializers.BYTE);

   public EntityCultistCleric(World worldIn) {
      super(worldIn);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new AIAltarFocus(this));
      this.tasks.addTask(2, new AILongRangeAttack(this, 2.0F, 1.0F, 20, 40, 24.0F));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityLivingBase.class, 1.0F, false));
      this.tasks.addTask(4, new EntityAIRestrictOpenDoor(this));
      this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
      this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.8));
      this.tasks.addTask(7, new EntityAIWander(this, 0.8));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new AICultistHurtByTarget(this, true));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0F);
   }

   protected void addRandomArmor() {
      this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ConfigItems.itemHelmetCultistRobe));
      this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ConfigItems.itemChestCultistRobe));
      this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ConfigItems.itemLegsCultistRobe));
      if (this.rand.nextFloat() < (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.3F : 0.1F)) {
         this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ConfigItems.itemBootsCultist));
      }
   }

   public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f) {
      double d0 = entitylivingbase.posX - this.posX;
      double d1 = entitylivingbase.getEntityBoundingBox().minY + (double)(entitylivingbase.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
      double d2 = entitylivingbase.posZ - this.posZ;
      this.swingArm(EnumHand.MAIN_HAND);
      if (this.rand.nextFloat() > 0.66F) {
         EntityGolemOrb blast = new EntityGolemOrb(this.world, this, entitylivingbase, true);
         blast.posX += blast.motionX / (double)2.0F;
         blast.posZ += blast.motionZ / (double)2.0F;
         blast.setPosition(blast.posX, blast.posY, blast.posZ);
         blast.shoot(d0, d1 + (double)2.0F, d2, 0.66F, 3.0F);
         this.playSound(new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "egattack")), 1.0F, 1.0F + this.rand.nextFloat() * 0.1F);
         this.world.spawnEntity(blast);
      } else {
         float f1 = MathHelper.sqrt(f) * 0.5F;
         this.world.playEvent(null, 1009, new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 0);

         for(int i = 0; i < 3; ++i) {
            EntitySmallFireball entitysmallfireball = new EntitySmallFireball(this.world, this, d0 + this.rand.nextGaussian() * (double)f1, d1, d2 + this.rand.nextGaussian() * (double)f1);
            entitysmallfireball.posY = this.posY + (double)(this.height / 2.0F) + (double)0.5F;
            this.world.spawnEntity(entitysmallfireball);
         }
      }
   }

   protected boolean canDespawn() {
      return !this.getIsRitualist();
   }

   public void entityInit() {
      super.entityInit();
      this.dataManager.register(CLERIC_FLAGS, (byte) 0);
   }

   public boolean getIsRitualist() {
      return (this.dataManager.get(CLERIC_FLAGS) & 1) != 0;
   }

   public void setIsRitualist(boolean par1) {
      byte var2 = this.dataManager.get(CLERIC_FLAGS);
      if (par1) {
         this.dataManager.set(CLERIC_FLAGS, (byte)(var2 | 1));
      } else {
         this.dataManager.set(CLERIC_FLAGS, (byte)(var2 & -2));
      }
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      if (this.isEntityInvulnerable(source)) {
         return false;
      } else {
         this.setIsRitualist(false);
         return super.attackEntityFrom(source, amount);
      }
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.dataManager.set(CLERIC_FLAGS, par1NBTTagCompound.getByte("Flags"));
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setByte("Flags", this.dataManager.get(CLERIC_FLAGS));
   }

   public void writeSpawnData(ByteBuf data) {
      data.writeInt(this.getHomePosition().getX());
      data.writeInt(this.getHomePosition().getY());
      data.writeInt(this.getHomePosition().getZ());
   }

   public void readSpawnData(ByteBuf data) {
      this.setHomePosAndDistance(new BlockPos(data.readInt(), data.readInt(), data.readInt()), 8);
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote && this.getIsRitualist()) {
         double d0 = (double)this.getHomePosition().getX() + (double)0.5F - this.posX;
         double d1 = (double)this.getHomePosition().getY() + (double)1.5F - (this.posY + (double)this.getEyeHeight());
         double d2 = (double)this.getHomePosition().getZ() + (double)0.5F - this.posZ;
         double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
         float f = (float)(Math.atan2(d2, d0) * (double)180.0F / Math.PI) - 90.0F;
         float f1 = (float)(-(Math.atan2(d1, d3) * (double)180.0F / Math.PI));
         this.rotationPitch = this.updateRotation(this.rotationPitch, f1, 10.0F);
         this.rotationYawHead = this.updateRotation(this.rotationYawHead, f, (float)this.getVerticalFaceSpeed());
      }
   }

   private float updateRotation(float yaw1, float yaw2, float maxOffset) {
      float f3 = MathHelper.wrapDegrees(yaw2 - yaw1);
      if (f3 > maxOffset) {
         f3 = maxOffset;
      }

      if (f3 < -maxOffset) {
         f3 = -maxOffset;
      }

      return yaw1 + f3;
   }

   protected String getLivingSound() {
      return "thaumcraft:chant";
   }

   public int getTalkInterval() {
      return 500;
   }

   public void setSwingingArms(boolean swinging) {}
}
