package thaumcraft.common.entities.monster.boss;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.BossInfo;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.EntityUtils;

public class EntityThaumcraftBoss extends EntityMob {
   private static final DataParameter<Integer> ANGER = EntityDataManager.createKey(EntityThaumcraftBoss.class, DataSerializers.VARINT);
   HashMap<Integer,Integer> aggro = new HashMap<>();
   int spawnTimer = 0;
   protected final BossInfoServer bossInfo = new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.NOTCHED_6);

   public EntityThaumcraftBoss(World world) {
      super(world);
      this.experienceValue = 50;
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      if (nbt.hasKey("HomeD")) {
         this.setHomePosAndDistance(new BlockPos(nbt.getInteger("HomeX"), nbt.getInteger("HomeY"), nbt.getInteger("HomeZ")), nbt.getInteger("HomeD"));
      }

   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      if (this.getHomePosition() != null && this.getMaximumHomeDistance() > 0.0F) {
         nbt.setInteger("HomeD", (int)this.getMaximumHomeDistance());
         nbt.setInteger("HomeX", this.getHomePosition().getX());
         nbt.setInteger("HomeY", this.getHomePosition().getY());
         nbt.setInteger("HomeZ", this.getHomePosition().getZ());
      }

   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.95);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(ANGER, 0);
   }

   protected void updateAITasks() {
      if (this.getSpawnTimer() == 0) {
         super.updateAITasks();
      }

      if (this.getAttackTarget() != null && this.getAttackTarget().isDead) {
         this.setAttackTarget(null);
      }

   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
      this.setHomePosAndDistance(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 24);
      return super.onInitialSpawn(difficulty, data);
   }

   public int getAnger() {
      return this.dataManager.get(ANGER);
   }

   public void setAnger(int par1) {
      this.dataManager.set(ANGER, par1);
   }

   public int getSpawnTimer() {
      return this.spawnTimer;
   }

   @Override
   public void addTrackingPlayer(EntityPlayerMP player) {
      super.addTrackingPlayer(player);
      this.bossInfo.addPlayer(player);
   }

   @Override
   public void removeTrackingPlayer(EntityPlayerMP player) {
      super.removeTrackingPlayer(player);
      this.bossInfo.removePlayer(player);
   }

   public void onUpdate() {
      super.onUpdate();
      this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
      if (this.getSpawnTimer() > 0) {
         --this.spawnTimer;
      }

      if (this.getAnger() > 0) {
         this.setAnger(this.getAnger() - 1);
      }

      if (this.world.isRemote && this.rand.nextInt(15) == 0 && this.getAnger() > 0) {
         double d0 = this.rand.nextGaussian() * 0.02;
         double d1 = this.rand.nextGaussian() * 0.02;
         double d2 = this.rand.nextGaussian() * 0.02;
         this.world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX + (double)(this.rand.nextFloat() * this.width) - (double)this.width / (double)2.0F, this.getEntityBoundingBox().minY + (double)this.height + (double)this.rand.nextFloat() * (double)0.5F, this.posZ + (double)(this.rand.nextFloat() * this.width) - (double)this.width / (double)2.0F, d0, d1, d2);
      }

      if (!this.world.isRemote) {
         if (this.ticksExisted % 30 == 0) {
            this.heal(1.0F);
         }

         if (this.getAttackTarget() != null && this.ticksExisted % 20 == 0) {
            ArrayList<Integer> dl = new ArrayList<>();
            int players = 0;
            int hei = this.getAttackTarget().getEntityId();
            int ad = this.aggro.getOrDefault(hei, 0);
            int ld = ad;
            Entity newTarget = null;

            for(Integer ei : this.aggro.keySet()) {
               int ca = this.aggro.get(ei);
               if (ca > ad + 25 && (double)ca > (double)ad * 1.1 && ca > ld) {
                  newTarget = this.world.getEntityByID(hei);
                  if (newTarget != null && !newTarget.isDead && !(this.getDistanceSq(newTarget) > (double)16384.0F)) {
                     hei = ei;
                     ld = ei;
                     if (newTarget instanceof EntityPlayer) {
                        ++players;
                     }
                  } else {
                     dl.add(ei);
                  }
               }
            }

            for(Integer ei : dl) {
               this.aggro.remove(ei);
            }

            if (newTarget != null && hei != this.getAttackTarget().getEntityId()) {
               this.setAttackTarget((EntityLivingBase)newTarget);
            }

            float om = this.getMaxHealth();
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            IAttributeInstance iattributeinstance2 = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);

            for(int a = 0; a < 5; ++a) {
               iattributeinstance2.removeModifier(EntityUtils.DMGBUFF[a]);
               iattributeinstance.removeModifier(EntityUtils.HPBUFF[a]);
            }

            for(int a = 0; a < Math.min(5, players - 1); ++a) {
               iattributeinstance.applyModifier(EntityUtils.HPBUFF[a]);
               iattributeinstance2.applyModifier(EntityUtils.DMGBUFF[a]);
            }

            double mm = this.getMaxHealth() / om;
            this.setHealth((float)((double)this.getHealth() * mm));
         }
      }

   }

   public boolean isEntityInvulnerable(DamageSource source) {
      return super.isEntityInvulnerable(source) || this.getSpawnTimer() > 0;
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public boolean canBePushed() {
      return super.canBePushed() && this.getSpawnTimer() <= 0;
   }

   protected int decreaseAirSupply(int air) {
      return air;
   }

   public void setInWeb() {
   }

   public boolean canPickUpLoot() {
      return false;
   }

   protected boolean isAIEnabled() {
      return true;
   }

   protected void addRandomArmor() {
   }

   protected void enchantEquipment() {
   }

   protected boolean canDespawn() {
      return false;
   }

   public boolean isOnSameTeam(EntityLivingBase el) {
      return el instanceof IEldritchMob;
   }

   protected void dropFewItems(boolean flag, int fortune) {
      EntityUtils.entityDropSpecialItem(this, new ItemStack(ConfigItems.itemEldritchObject, 1, 3), this.height / 2.0F);
      this.entityDropItem(new ItemStack(ConfigItems.itemLootbag, 1, 2), 1.5F);
   }

   public boolean attackEntityFrom(DamageSource source, float damage) {
      if (!this.world.isRemote) {
         if (source.getTrueSource() != null && source.getTrueSource() instanceof EntityLivingBase) {
            int target = source.getTrueSource().getEntityId();
            int ad = (int)damage;
            if (this.aggro.containsKey(target)) {
               ad += this.aggro.get(target);
            }

            this.aggro.put(target, ad);
         }

         if (damage > 35.0F) {
            if (this.getAnger() == 0) {
               try {
                  this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, (int)(damage / 15.0F)));
                  this.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 200, (int)(damage / 40.0F)));
                  this.addPotionEffect(new PotionEffect(MobEffects.SPEED, 200, (int)(damage / 40.0F)));
                  this.setAnger(200);
               } catch (Exception ignored) {
               }

               if (source.getTrueSource() != null && source.getTrueSource() instanceof EntityPlayer) {
                  ((EntityPlayer)source.getTrueSource()).sendMessage(new TextComponentString(this.getName() + " " + I18n.translateToLocal("tc.boss.enrage")));
               }
            }

            damage = 35.0F;
         }
      }

      return super.attackEntityFrom(source, damage);
   }

   public void generateName() {
   }
}
