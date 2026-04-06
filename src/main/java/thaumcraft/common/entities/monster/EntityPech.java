package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.pech.AIPechItemEntityGoto;
import thaumcraft.common.entities.ai.pech.AIPechTradePlayer;
import thaumcraft.common.entities.projectile.EntityPechBlast;
import thaumcraft.common.items.ItemManaBean;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.utils.InventoryUtils;

public class EntityPech extends EntityMob implements IRangedAttackMob {
   private static final net.minecraft.network.datasync.DataParameter<Byte> PECH_TYPE = net.minecraft.network.datasync.EntityDataManager.createKey(EntityPech.class, net.minecraft.network.datasync.DataSerializers.BYTE);
   private static final net.minecraft.network.datasync.DataParameter<Integer> ANGER = net.minecraft.network.datasync.EntityDataManager.createKey(EntityPech.class, net.minecraft.network.datasync.DataSerializers.VARINT);
   private static final net.minecraft.network.datasync.DataParameter<Byte> TAMED = net.minecraft.network.datasync.EntityDataManager.createKey(EntityPech.class, net.minecraft.network.datasync.DataSerializers.BYTE);
   public ItemStack[] loot = new ItemStack[9];
   public boolean trading = false;
   public boolean updateAINextTick = false;
   private EntityAIAttackRanged aiArrowAttack = new EntityAIAttackRanged(this, 0.6, 20, 50, 15.0F);
   private EntityAIAttackRanged aiBlastAttack = new EntityAIAttackRanged(this, 0.6, 20, 30, 15.0F);
   private AIAttackOnCollide aiMeleeAttack = new AIAttackOnCollide(this, EntityLivingBase.class, 0.6, false);
   private EntityAIAvoidEntity aiAvoidPlayer = new EntityAIAvoidEntity(this, EntityPlayer.class, 8.0F, 0.5F, 0.6);
   public float mumble = 0.0F;
   int chargecount = 0;
   static HashMap valuedItems = new HashMap<>();
   public static HashMap tradeInventory = new HashMap<>();

   public String getName() {
      if (this.hasCustomName()) {
         return this.getCustomNameTag();
      } else {
         switch (this.getPechType()) {
             case 1:
               return I18n.translateToLocal("entity.Thaumcraft.Pech.1.name");
            case 2:
               return I18n.translateToLocal("entity.Thaumcraft.Pech.2.name");
            default:
               return I18n.translateToLocal("entity.Thaumcraft.Pech.name");
         }
      }
   }

   public EntityPech(World world) {
      super(world);
      this.setSize(0.6F, 1.8F);

      this.setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER, 8.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new AIPechTradePlayer(this));
      this.tasks.addTask(3, new AIPechItemEntityGoto(this));
      this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
      this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.5F));
      this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0F, false));
      this.tasks.addTask(9, new EntityAIWander(this, 0.6));
      this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
      this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
      this.tasks.addTask(11, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
      if (world != null && !world.isRemote) {
         this.setCombatTask();
      }

      this.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.2F);
   }

   @Override
   public void setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot slot, ItemStack par2ItemStack) {
      super.setItemStackToSlot(slot, par2ItemStack);
      if (!this.world.isRemote && slot == net.minecraft.inventory.EntityEquipmentSlot.MAINHAND) {
         this.updateAINextTick = true;
      }
   }

   @Override
   public void setSwingingArms(boolean swinging) {}

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
      super.setEquipmentBasedOnDifficulty(difficulty);
      switch (this.rand.nextInt(20)) {
         case 0:
         case 12:
            ItemStack wand = new ItemStack(ConfigItems.itemWandCasting);
            ItemStack focus = new ItemStack(ConfigItems.itemFocusPech);
            ((ItemWandCasting)wand.getItem()).setFocus(wand, focus);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.EARTH, 2 + this.rand.nextInt(6), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.ENTROPY, 2 + this.rand.nextInt(6), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.WATER, 2 + this.rand.nextInt(6), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.AIR, this.rand.nextInt(4), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.FIRE, this.rand.nextInt(4), true);
            ((ItemWandCasting)wand.getItem()).addVis(wand, Aspect.ORDER, this.rand.nextInt(4), true);
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, wand);
            break;
         case 1:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            break;
         case 2:
         case 4:
         case 10:
         case 11:
         case 13:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            break;
         case 3:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
            break;
         case 5:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            break;
         case 6:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            break;
         case 7:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            break;
         case 8:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_PICKAXE));
            break;
         case 9:
            this.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
      }

   }

   @Override
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData par1EntityLivingData) {
      par1EntityLivingData = super.onInitialSpawn(difficulty, par1EntityLivingData);
      this.setEquipmentBasedOnDifficulty(difficulty);
      ItemStack itemstack = this.getHeldItemMainhand();
      if (!itemstack.isEmpty() && itemstack.getItem() == ConfigItems.itemWandCasting) {
         this.setPechType(1);
         this.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.1F);
      } else if (!itemstack.isEmpty()) {
         if (itemstack.getItem() == Items.BOW) {
            this.setPechType(2);
         }
         this.setEnchantmentBasedOnDifficulty(difficulty);
      }
      this.setCanPickUpLoot(this.rand.nextFloat() < 0.75F);
      return par1EntityLivingData;
   }

   public boolean getCanSpawnHere() {
      Biome biome = this.world.getBiome(new net.minecraft.util.math.BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ)));
      boolean magicBiome = false;
      if (biome != null) {
         magicBiome = BiomeDictionary.hasType(biome, Type.MAGICAL) && net.minecraft.world.biome.Biome.getIdForBiome(biome) != Config.biomeTaintID;
      }

      int count = 0;

      try {
         List l = this.world.getEntitiesWithinAABB(EntityPech.class, this.getEntityBoundingBox().grow(16.0, 16.0, 16.0));
         if (l != null) {
            count = l.size();
         }
      } catch (Exception ignored) {
      }

      if (this.world.provider.getDimension() != 0 && net.minecraft.world.biome.Biome.getIdForBiome(biome) != Config.biomeMagicalForestID && net.minecraft.world.biome.Biome.getIdForBiome(biome) != Config.biomeEerieID) {
         magicBiome = false;
      }

      return count < 4 && magicBiome && super.getCanSpawnHere();
   }

   public float getEyeHeight() {
      return this.height * 0.66F;
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(PECH_TYPE, (byte) 0);
      this.dataManager.register(ANGER, 0);
      this.dataManager.register(TAMED, (byte) 0);
   }

   public int getPechType() {
      return this.dataManager.get(PECH_TYPE);
   }

   public int getAnger() {
      return this.dataManager.get(ANGER);
   }

   public boolean isTamed() {
      return this.dataManager.get(TAMED) == 1;
   }

   public void setPechType(int par1) {
      this.dataManager.set(PECH_TYPE, (byte) par1);
   }

   public void setAnger(int par1) {
      this.dataManager.set(ANGER, par1);
   }

   public void setTamed(boolean par1) {
      this.dataManager.set(TAMED, (byte) (par1 ? 1 : 0));
   }

   public boolean isAIEnabled() {
      return true;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5F);
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setByte("PechType", (byte)this.getPechType());
      par1NBTTagCompound.setShort("Anger", (short)this.getAnger());
      par1NBTTagCompound.setBoolean("Tamed", this.isTamed());
      NBTTagList nbttaglist = new NBTTagList();

       for (ItemStack itemStack : this.loot) {
           NBTTagCompound nbttagcompound1 = new NBTTagCompound();
           if (itemStack != null) {
               itemStack.writeToNBT(nbttagcompound1);
           }

           nbttaglist.appendTag(nbttagcompound1);
       }

      par1NBTTagCompound.setTag("Loot", nbttaglist);
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      if (par1NBTTagCompound.hasKey("PechType")) {
         byte b0 = par1NBTTagCompound.getByte("PechType");
         this.setPechType(b0);
      }

      this.setAnger(par1NBTTagCompound.getShort("Anger"));
      this.setTamed(par1NBTTagCompound.getBoolean("Tamed"));
      if (par1NBTTagCompound.hasKey("Loot")) {
         NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Loot", 10);

         for(int i = 0; i < this.loot.length; ++i) {
            this.loot[i] = new ItemStack(nbttaglist.getCompoundTagAt(i));
         }
      }

      this.updateAINextTick = true;
   }

   protected boolean canDespawn() {
      try {
         if (this.loot == null) {
            return true;
         } else {
            int q = 0;

            for(ItemStack is : this.loot) {
               if (is != null && is.getCount() > 0) {
                  ++q;
               }
            }

            return q < 5;
         }
      } catch (Exception var6) {
         return true;
      }
   }

   public boolean allowLeashing() {
      return false;
   }

   protected void dropFewItems(boolean flag, int i) {
       for (ItemStack itemStack : this.loot) {
           if (itemStack != null && this.world.rand.nextFloat() < 0.88F) {
               this.entityDropItem(itemStack.copy(), 1.5F);
           }
       }

      Aspect[] aspects = Aspect.getPrimalAspects().toArray(new Aspect[0]);

      for(int a = 0; a < 1 + i; ++a) {
         if (this.rand.nextBoolean()) {
            ItemStack is = new ItemStack(ConfigItems.itemManaBean);
            ((ItemManaBean)is.getItem()).setAspects(is, (new AspectList()).add(aspects[this.rand.nextInt(aspects.length)], 1));
            this.entityDropItem(is, 1.5F);
         }
      }

      if (this.world.rand.nextInt(10) < 1 + i) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 18), 1.5F);
      }

      super.dropFewItems(flag, i);
   }

   protected void dropRareDrop(int par1) {
      this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 9), 1.5F);
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte par1) {
      if (par1 == 16) {
         this.mumble = (float)Math.PI;
      } else if (par1 == 17) {
         this.mumble = ((float)Math.PI * 2F);
      } else if (par1 == 18) {
         for(int i = 0; i < 5; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02;
            double d1 = this.rand.nextGaussian() * 0.02;
            double d2 = this.rand.nextGaussian() * 0.02;
            this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)0.5F + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
         }
      }

      if (par1 == 19) {
         for(int i = 0; i < 5; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02;
            double d1 = this.rand.nextGaussian() * 0.02;
            double d2 = this.rand.nextGaussian() * 0.02;
            this.world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)0.5F + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
         }

         this.mumble = ((float)Math.PI * 2F);
      } else {
         super.handleStatusUpdate(par1);
      }

   }

   private void playTCSound(String soundName, float volume, float pitch) {
      SoundEvent snd = SoundEvent.REGISTRY.getObject(new ResourceLocation(soundName));
      if (snd != null) {
         this.world.playSound(null, this.getPosition(), snd, SoundCategory.NEUTRAL, volume, pitch);
      }
   }

   public void playLivingSound() {
      if (!this.world.isRemote) {
         if (this.rand.nextInt(3) == 0) {
            List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(4.0, 2.0, 4.0));

             for (Object o : list) {
                 Entity entity1 = (Entity) o;
                 if (entity1 instanceof EntityPech) {
                     this.world.setEntityState(this, (byte) 17);
                     this.playTCSound("thaumcraft:pech_trade", this.getSoundVolume(), this.getSoundPitch());
                     return;
                 }
             }
         }

         this.world.setEntityState(this, (byte)16);
      }

      super.playLivingSound();
   }

   public int getTalkInterval() {
      return 120;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   protected SoundEvent getLivingSound() {
      return SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:pech_idle"));
   }

   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:pech_hit"));
   }

   protected SoundEvent getDeathSound() {
      return SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:pech_death"));
   }

   public void setCombatTask() {
      this.tasks.removeTask(this.aiMeleeAttack);
      this.tasks.removeTask(this.aiArrowAttack);
      this.tasks.removeTask(this.aiBlastAttack);
      ItemStack itemstack = this.getHeldItemMainhand();
      if (itemstack != null && itemstack.getItem() == Items.BOW) {
         this.tasks.addTask(2, this.aiArrowAttack);
      } else if (itemstack != null && itemstack.getItem() == ConfigItems.itemWandCasting) {
         this.tasks.addTask(2, this.aiBlastAttack);
      } else {
         this.tasks.addTask(2, this.aiMeleeAttack);
      }

      if (this.isTamed()) {
         this.tasks.removeTask(this.aiAvoidPlayer);
      } else {
         this.tasks.addTask(4, this.aiAvoidPlayer);
      }

   }

   public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f) {
      if (this.getPechType() == 2) {
         EntityTippedArrow entityarrow = new EntityTippedArrow(this.world, this);
         double d0 = entitylivingbase.posX - this.posX;
         double d1 = entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - 1.1 - (this.posY + (double)this.getEyeHeight());
         double d2 = entitylivingbase.posZ - this.posZ;
         float inaccuracy = (float)(14 - this.world.getDifficulty().getId() * 4);
         entityarrow.shoot(d0, d1 + MathHelper.sqrt((float)(d0 * d0 + d2 * d2)) * 0.2F, d2, 1.6F, inaccuracy);
         int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, this.getHeldItemMainhand());
         int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, this.getHeldItemMainhand());
         entityarrow.setDamage((double)(f * 2.0F) + this.rand.nextGaussian() * (double)0.25F + (double)((float)this.world.getDifficulty().getId() * 0.11F));
         if (i > 0) {
            entityarrow.setDamage(entityarrow.getDamage() + (double)i * (double)0.5F + (double)0.5F);
         }

         if (j > 0) {
            entityarrow.setKnockbackStrength(j);
         }

         this.playTCSound("minecraft:entity.arrow.shoot", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
         this.world.spawnEntity(entityarrow);
      } else if (this.getPechType() == 1) {
         EntityPechBlast blast = new EntityPechBlast(this.world, this, 1, 0, this.rand.nextFloat() < 0.1F);
         double d0 = entitylivingbase.posX + entitylivingbase.motionX - this.posX;
         double d1 = entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - 1.500000023841858 - this.posY;
         double d2 = entitylivingbase.posZ + entitylivingbase.motionZ - this.posZ;
         float f1 = MathHelper.sqrt(d0 * d0 + d2 * d2);
         blast.shoot(d0, d1 + (double)(f1 * 0.1F), d2, 1.5F, 4.0F);
         this.playTCSound("thaumcraft:ice", 0.4F, 1.0F + this.rand.nextFloat() * 0.1F);
         this.world.spawnEntity(blast);
      }

      this.swingArm(EnumHand.MAIN_HAND);
   }

   private void becomeAngryAt(Entity par1Entity) {
      if (this.getAnger() <= 0) {
         this.world.setEntityState(this, (byte)19);
         this.playTCSound("thaumcraft:pech_charge", this.getSoundVolume(), this.getSoundPitch());
      }

      this.setAttackTarget((EntityLivingBase)par1Entity);
      this.setAnger(400 + this.rand.nextInt(400));
      this.setTamed(false);
      this.updateAINextTick = true;
   }

   public int getTotalArmorValue() {
      int i = super.getTotalArmorValue() + 2;
      if (i > 20) {
         i = 20;
      }

      return i;
   }

   public boolean attackEntityFrom(DamageSource damSource, float par2) {
      if (this.isEntityInvulnerable(damSource)) {
         return false;
      } else {
         Entity entity = damSource.getTrueSource();
         if (entity instanceof EntityPlayer) {
            List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(32.0, 16.0, 32.0));

             for (Object o : list) {
                 Entity entity1 = (Entity) o;
                 if (entity1 instanceof EntityPech) {
                     EntityPech entitypech = (EntityPech) entity1;
                     entitypech.becomeAngryAt(entity);
                 }
             }

            this.becomeAngryAt(entity);
         }

         return super.attackEntityFrom(damSource, par2);
      }
   }

   public void onUpdate() {
      if (this.mumble > 0.0F) {
         this.mumble *= 0.75F;
      }

      if (this.getAnger() > 0) {
         this.setAnger(this.getAnger() - 1);
      }

      if (this.getAnger() > 0 && this.getAttackTarget() == null) {
         EntityPlayer nearestPlayer = this.world.getClosestPlayerToEntity(this, 32.0);
         if (nearestPlayer != null) {
            this.setAttackTarget(nearestPlayer);
            if (this.chargecount > 0) {
               --this.chargecount;
            }

            if (this.chargecount == 0) {
               this.chargecount = 100;
               this.playTCSound("thaumcraft:pech_charge", this.getSoundVolume(), this.getSoundPitch());
            }

            this.world.setEntityState(this, (byte)17);
         }
      }

      if (this.world.isRemote && this.rand.nextInt(15) == 0 && this.getAnger() > 0) {
         double d0 = this.rand.nextGaussian() * 0.02;
         double d1 = this.rand.nextGaussian() * 0.02;
         double d2 = this.rand.nextGaussian() * 0.02;
         this.world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)0.5F + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
      }

      if (this.world.isRemote && this.rand.nextInt(25) == 0 && this.isTamed()) {
         double d0 = this.rand.nextGaussian() * 0.02;
         double d1 = this.rand.nextGaussian() * 0.02;
         double d2 = this.rand.nextGaussian() * 0.02;
         this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)0.5F + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
      }

      super.onUpdate();
   }

   public void updateAITasks() {
      if (this.updateAINextTick) {
         this.updateAINextTick = false;
         this.setCombatTask();
      }

      super.updateAITasks();
      if (this.ticksExisted % 40 == 0) {
         this.heal(1.0F);
      }

   }

   public boolean canPickup(ItemStack entityItem) {
      if (entityItem == null) {
         return false;
      } else if (!this.isTamed() && valuedItems.containsKey(Item.getIdFromItem(entityItem.getItem()))) {
         return true;
      } else {
         for(int a = 0; a < this.loot.length; ++a) {
            if (this.loot[a] != null && this.loot[a].getCount() <= 0) {
               this.loot[a] = null;
            }

            if (this.loot[a] == null) {
               return true;
            }

            if (InventoryUtils.areItemStacksEqualStrict(entityItem, this.loot[a]) && entityItem.getCount() + this.loot[a].getCount() <= this.loot[a].getMaxStackSize()) {
               return true;
            }
         }

         return false;
      }
   }

   public ItemStack pickupItem(ItemStack entityItem) {
      if (entityItem == null) {
         return entityItem;
      } else if (!this.isTamed() && this.isValued(entityItem)) {
         if (this.rand.nextInt(10) < this.getValue(entityItem)) {
            this.setTamed(true);
            this.updateAINextTick = true;
            this.world.setEntityState(this, (byte)18);
         }

         entityItem.shrink(1);
         return entityItem.getCount() <= 0 ? null : entityItem;
      } else {
         for(int a = 0; a < this.loot.length; ++a) {
            if (this.loot[a] != null && this.loot[a].getCount() <= 0) {
               this.loot[a] = null;
            }

            if (entityItem != null && entityItem.getCount() > 0 && this.loot[a] != null && this.loot[a].getCount() < this.loot[a].getMaxStackSize() && InventoryUtils.areItemStacksEqualStrict(entityItem, this.loot[a])) {
               if (entityItem.getCount() + this.loot[a].getCount() <= this.loot[a].getMaxStackSize()) {
                  this.loot[a].grow(entityItem.getCount());
                  return null;
               }

               int sz = Math.min(entityItem.getCount(), this.loot[a].getMaxStackSize() - this.loot[a].getCount());
               this.loot[a].grow(sz);
               entityItem.shrink(sz);
            }

            if (entityItem != null && entityItem.getCount() <= 0) {
               entityItem = null;
            }
         }

         for(int a = 0; a < this.loot.length; ++a) {
            if (this.loot[a] != null && this.loot[a].getCount() <= 0) {
               this.loot[a] = null;
            }

            if (entityItem != null && entityItem.getCount() > 0 && this.loot[a] == null) {
               this.loot[a] = entityItem.copy();
               return null;
            }
         }

         if (entityItem != null && entityItem.getCount() <= 0) {
            entityItem = null;
         }

         return entityItem;
      }
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (!player.isSneaking() && (player.getHeldItemMainhand().isEmpty() || !(player.getHeldItemMainhand().getItem() instanceof ItemNameTag))) {
         if (!this.world.isRemote && this.isTamed()) {
            player.openGui(Thaumcraft.instance, 1, this.world, this.getEntityId(), 0, 0);
            return true;
         } else {
            return super.processInteract(player, hand);
         }
      } else {
         return false;
      }
   }

   public boolean isValued(ItemStack item) {
      if (item == null) {
         return false;
      } else {
         boolean value = valuedItems.containsKey(Item.getIdFromItem(item.getItem()));
         if (!value) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
            al = ThaumcraftCraftingManager.getBonusTags(item, al);
            if (al.getAmount(Aspect.GREED) > 0) {
               value = true;
            }
         }

         return value;
      }
   }

   public int getValue(ItemStack item) {
      if (item == null) {
         return 0;
      } else {
         int value = valuedItems.containsKey(Item.getIdFromItem(item.getItem())) ? (Integer)valuedItems.get(Item.getIdFromItem(item.getItem())) : 0;
         if (value == 0) {
            AspectList al = ThaumcraftCraftingManager.getObjectTags(item);
            al = ThaumcraftCraftingManager.getBonusTags(item, al);
            value = Math.min(32, al.getAmount(Aspect.GREED));
         }

         return value;
      }
   }

   static {
      valuedItems.put(Item.getIdFromItem(ConfigItems.itemManaBean), 1);
      valuedItems.put(Item.getIdFromItem(Items.GOLD_INGOT), 2);
      valuedItems.put(Item.getIdFromItem(Items.GOLDEN_APPLE), 2);
      valuedItems.put(Item.getIdFromItem(Items.ENDER_PEARL), 3);
      valuedItems.put(Item.getIdFromItem(Items.DIAMOND), 4);
      valuedItems.put(Item.getIdFromItem(Items.EMERALD), 5);
      ArrayList<List> forInv = new ArrayList<>();
      forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemManaBean)));
      forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 16)));
      forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 31)));
      forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 21)));
      if (Config.foundCopperIngot) {
         forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 17)));
      }

      if (Config.foundTinIngot) {
         forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 18)));
      }

      if (Config.foundSilverIngot) {
         forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 19)));
      }

      if (Config.foundLeadIngot) {
         forInv.add(Arrays.asList(1, new ItemStack(ConfigItems.itemNugget, 1, 20)));
      }

      forInv.add(Arrays.asList(2, new ItemStack(Items.BLAZE_ROD)));
      forInv.add(Arrays.asList(2, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 0)));
      forInv.add(Arrays.asList(2, new ItemStack(Items.POTIONITEM, 1, 8201)));
      forInv.add(Arrays.asList(2, new ItemStack(Items.POTIONITEM, 1, 8194)));
      forInv.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
      forInv.add(Arrays.asList(3, new ItemStack(ConfigItems.itemResource, 1, 9)));
      forInv.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE, 1, 0)));
      forInv.add(Arrays.asList(3, new ItemStack(Items.POTIONITEM, 1, 8265)));
      forInv.add(Arrays.asList(3, new ItemStack(Items.POTIONITEM, 1, 8262)));
      forInv.add(Arrays.asList(5, new ItemStack(Items.GOLDEN_APPLE, 1, 1)));
      forInv.add(Arrays.asList(4, new ItemStack(ConfigItems.itemPickThaumium)));
      forInv.add(Arrays.asList(5, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 1)));
      forInv.add(Arrays.asList(5, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 1)));
      tradeInventory.put(0, forInv);
      ArrayList<List> forMag = new ArrayList<>();
      forMag.add(Arrays.asList(1, new ItemStack(ConfigItems.itemManaBean)));

      for(int a = 0; a < 6; ++a) {
         forMag.add(Arrays.asList(1, new ItemStack(ConfigItems.itemShard, 1, a)));
      }

      forMag.add(Arrays.asList(1, new ItemStack(ConfigItems.itemResource, 1, 9)));
      forMag.add(Arrays.asList(2, new ItemStack(ConfigItems.itemResource, 1, 9)));
      forMag.add(Arrays.asList(2, new ItemStack(Items.POTIONITEM, 1, 8193)));
      forMag.add(Arrays.asList(2, new ItemStack(Items.POTIONITEM, 1, 8261)));
      forMag.add(Arrays.asList(3, net.minecraft.item.ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Config.enchHaste, 1))));
      forMag.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE, 1, 0)));
      forMag.add(Arrays.asList(3, new ItemStack(Items.POTIONITEM, 1, 8225)));
      forMag.add(Arrays.asList(3, new ItemStack(Items.POTIONITEM, 1, 8229)));

      for(int a = 0; a < 7; ++a) {
         forMag.add(Arrays.asList(4, new ItemStack(ConfigBlocks.blockCrystal, 1, a)));
      }

      forMag.add(Arrays.asList(5, new ItemStack(Items.GOLDEN_APPLE, 1, 1)));
      forMag.add(Arrays.asList(5, net.minecraft.item.ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Config.enchRepair, 1))));
      forMag.add(Arrays.asList(5, new ItemStack(ConfigItems.itemFocusPouch)));
      forMag.add(Arrays.asList(5, new ItemStack(ConfigItems.itemFocusPech)));
      forMag.add(Arrays.asList(5, new ItemStack(ConfigItems.itemAmuletVis, 1, 0)));
      tradeInventory.put(1, forMag);
      ArrayList<List> forArc = new ArrayList<>();
      forArc.add(Arrays.asList(1, new ItemStack(ConfigItems.itemManaBean)));

      for(int a = 0; a < 15; ++a) {
         forArc.add(Arrays.asList(1, new ItemStack(ConfigBlocks.blockCandle, 1, a)));
      }

      forArc.add(Arrays.asList(2, new ItemStack(Items.GHAST_TEAR)));
      forArc.add(Arrays.asList(2, new ItemStack(Items.POTIONITEM, 1, 8194)));
      forArc.add(Arrays.asList(2, new ItemStack(Items.POTIONITEM, 1, 8201)));
      forArc.add(Arrays.asList(2, net.minecraft.item.ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantments.POWER, 1))));
      forArc.add(Arrays.asList(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
      forArc.add(Arrays.asList(3, new ItemStack(ConfigItems.itemResource, 1, 9)));
      forArc.add(Arrays.asList(3, new ItemStack(Items.POTIONITEM, 1, 8270)));
      forArc.add(Arrays.asList(3, new ItemStack(Items.POTIONITEM, 1, 8225)));
      forArc.add(Arrays.asList(3, new ItemStack(Items.GOLDEN_APPLE, 1, 0)));
      forArc.add(Arrays.asList(5, new ItemStack(Items.GOLDEN_APPLE, 1, 1)));
      forArc.add(Arrays.asList(4, new ItemStack(ConfigItems.itemBootsThaumium)));
      forArc.add(Arrays.asList(5, new ItemStack(ConfigItems.itemRingRunic, 1, 0)));
      forArc.add(Arrays.asList(5, net.minecraft.item.ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantments.FLAME, 1))));
      forArc.add(Arrays.asList(5, net.minecraft.item.ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Enchantments.INFINITY, 1))));
      tradeInventory.put(2, forArc);
   }
}
