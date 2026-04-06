package thaumcraft.common.entities.monster.boss;

import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.monster.EntityTaintacle;
import thaumcraft.common.lib.utils.EntityUtils;

public class EntityTaintacleGiant extends EntityTaintacle implements ITaintedMob {
   private static final DataParameter<Integer> ANGER = EntityDataManager.createKey(EntityTaintacleGiant.class, DataSerializers.VARINT);

   public EntityTaintacleGiant(World par1World) {
      super(par1World);
      this.setSize(1.1F, 6.0F);
      this.experienceValue = 20;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(125.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0F);
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data) {
      EntityUtils.makeChampion(this, true);
      return super.onInitialSpawn(difficulty, data);
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.getAnger() > 0) {
         this.setAnger(this.getAnger() - 1);
      }

      if (this.world.isRemote && this.rand.nextInt(15) == 0 && this.getAnger() > 0) {
         double d0 = this.rand.nextGaussian() * 0.02;
         double d1 = this.rand.nextGaussian() * 0.02;
         double d2 = this.rand.nextGaussian() * 0.02;
         this.world.spawnParticle(EnumParticleTypes.VILLAGER_ANGRY, this.posX + (double)(this.rand.nextFloat() * this.width) - (double)this.width / (double)2.0F, this.getEntityBoundingBox().minY + (double)this.height + (double)this.rand.nextFloat() * (double)0.5F, this.posZ + (double)(this.rand.nextFloat() * this.width) - (double)this.width / (double)2.0F, d0, d1, d2);
      }

      if (!this.world.isRemote && this.ticksExisted % 30 == 0) {
         this.heal(1.0F);
      }

   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(ANGER, 0);
   }

   public int getAnger() {
      return this.dataManager.get(ANGER);
   }

   public void setAnger(int par1) {
      this.dataManager.set(ANGER, par1);
   }

   public boolean getCanSpawnHere() {
      return false;
   }

   protected void dropFewItems(boolean flag, int i) {
      ArrayList<Entity> ents = EntityUtils.getEntitiesInRange(this.world, this.posX, this.posY, this.posZ, this, EntityTaintacleGiant.class, 48.0F);
      if (ents == null || ents.isEmpty()) {
         EntityUtils.entityDropSpecialItem(this, new ItemStack(ConfigItems.itemEldritchObject, 1, 3), this.height / 2.0F);
      }

   }

   protected boolean canDespawn() {
      return false;
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   protected int decreaseAirSupply(int air) {
      return air;
   }

   public boolean attackEntityFrom(DamageSource source, float damage) {
      if (!this.world.isRemote && damage > 35.0F) {
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

      return super.attackEntityFrom(source, damage);
   }
}
