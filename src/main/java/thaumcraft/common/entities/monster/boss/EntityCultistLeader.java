package thaumcraft.common.entities.monster.boss;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.combat.AICultistHurtByTarget;
import thaumcraft.common.entities.ai.combat.AILongRangeAttack;
import thaumcraft.common.entities.monster.EntityCultist;
import thaumcraft.common.entities.monster.EntityCultistCleric;
import thaumcraft.common.entities.monster.EntityCultistKnight;
import thaumcraft.common.entities.monster.mods.ChampionModifier;
import thaumcraft.common.entities.projectile.EntityGolemOrb;
import thaumcraft.common.lib.utils.EntityUtils;

public class EntityCultistLeader extends EntityThaumcraftBoss implements IRangedAttackMob {
   private static final DataParameter<Byte> TITLE_IDX = EntityDataManager.createKey(EntityCultistLeader.class, DataSerializers.BYTE);

   String[] titles = new String[]{"Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache", "Chuse", "Dodorol", "Ebardo", "Ferrando", "Fertus", "Guillen", "Larpe", "Obano", "Zelipe"};

   public EntityCultistLeader(World worldIn) {
      super(worldIn);
      this.setSize(0.75F, 2.25F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new AILongRangeAttack(this, 16.0F, 1.0F, 30, 40, 24.0F));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityLivingBase.class, 1.1, false));
      this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.8));
      this.tasks.addTask(7, new EntityAIWander(this, 0.8));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new AICultistHurtByTarget(this, true));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.experienceValue = 40;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.32);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(125.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(TITLE_IDX, (byte)0);
   }

   public void generateName() {
      int t = (int)this.getEntityAttribute(EntityUtils.CHAMPION_MOD).getAttributeValue();
      if (t >= 0) {
         this.setCustomNameTag(String.format(I18n.translateToLocal("entity.Thaumcraft.CultistLeader.name"), this.getTitle(), ChampionModifier.mods[t].getModNameLocalized()));
      }

   }

   private String getTitle() {
      return this.titles[this.dataManager.get(TITLE_IDX)];
   }

   private void setTitle(int title) {
      this.dataManager.set(TITLE_IDX, (byte)title);
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setByte("title", this.dataManager.get(TITLE_IDX));
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.setTitle(nbt.getByte("title"));
   }

   protected void addRandomArmor() {
      this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ConfigItems.itemHelmetCultistLeaderPlate));
      this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ConfigItems.itemChestCultistLeaderPlate));
      this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ConfigItems.itemLegsCultistLeaderPlate));
      this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ConfigItems.itemBootsCultist));
      if (this.world.getDifficulty() == EnumDifficulty.EASY) {
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ConfigItems.itemSwordVoid));
      } else {
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ConfigItems.itemSwordCrimson));
      }

   }

   protected void enchantEquipment() {
      float f = 1.0F; // getTensionFactorForBlock removed in 1.12.2
      if (this.getHeldItemMainhand() != null && !this.getHeldItemMainhand().isEmpty() && this.rand.nextFloat() < 0.5F * f) {
         EnchantmentHelper.addRandomEnchantment(this.rand, this.getHeldItemMainhand(), (int)(7.0F + f * (float)this.rand.nextInt(22)), false);
      }

   }

   public boolean isOnSameTeam(EntityLivingBase el) {
      return el instanceof EntityCultist || el instanceof EntityCultistLeader;
   }

   public boolean canAttackClass(Class clazz) {
      return clazz != EntityCultistCleric.class && clazz != EntityCultistLeader.class && clazz != EntityCultistKnight.class && super.canAttackClass(clazz);
   }

   protected Item getDropItem() {
       return super.getDropItem();
   }

   protected void dropFewItems(boolean flag, int i) {
      this.entityDropItem(new ItemStack(ConfigItems.itemLootbag, 1, 2), 1.5F);
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData entityData) {
      this.addRandomArmor();
      this.enchantEquipment();
      this.setTitle(this.rand.nextInt(this.titles.length));
      return super.onInitialSpawn(difficulty, entityData);
   }

   protected void updateAITasks() {
      super.updateAITasks();

      for(Entity e : EntityUtils.getEntitiesInRange(this.world, this.posX, this.posY, this.posZ, this, EntityCultist.class, 8.0F)) {
         try {
            if (e instanceof EntityCultist && !((EntityCultist)e).isPotionActive(MobEffects.REGENERATION)) {
               ((EntityCultist)e).addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60, 1));
            }
         } catch (Exception ignored) {
         }
      }

   }

   public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f) {
      if (this.canEntityBeSeen(entitylivingbase)) {
         this.swingArm(EnumHand.MAIN_HAND);
         this.getLookHelper().setLookPosition(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY + (double)(entitylivingbase.height / 2.0F), entitylivingbase.posZ, 30.0F, 30.0F);
         EntityGolemOrb blast = new EntityGolemOrb(this.world, this, entitylivingbase, true);
         blast.posX += blast.motionX / (double)2.0F;
         blast.posZ += blast.motionZ / (double)2.0F;
         blast.setPosition(blast.posX, blast.posY, blast.posZ);
         double d0 = entitylivingbase.posX - this.posX;
         double d1 = entitylivingbase.getEntityBoundingBox().minY + (double)(entitylivingbase.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
         double d2 = entitylivingbase.posZ - this.posZ;
         blast.shoot(d0, d1 + (double)2.0F, d2, 0.66F, 3.0F);
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:egattack")); if (_snd != null) this.playSound(_snd, 1.0F, 1.0F + this.rand.nextFloat() * 0.1F); }
         this.world.spawnEntity(blast);
      }

   }

   public void setSwingingArms(boolean b) {}
}
