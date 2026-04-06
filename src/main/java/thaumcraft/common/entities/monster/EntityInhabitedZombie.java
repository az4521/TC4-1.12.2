package thaumcraft.common.entities.monster;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigItems;

public class EntityInhabitedZombie extends EntityZombie {
   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0F);
   }

   public EntityInhabitedZombie(World world) {
      super(world);
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityCultist.class, true));
   }

   public void onKillEntity(EntityLivingBase par1EntityLivingBase) {
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData entityData) {
      float diff = this.world.getDifficulty() == EnumDifficulty.HARD ? 0.9F : 0.6F;
      this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ConfigItems.itemHelmetCultistPlate));
      if (this.rand.nextFloat() <= diff) {
         this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ConfigItems.itemChestCultistPlate));
      }

      if (this.rand.nextFloat() <= diff) {
         this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ConfigItems.itemLegsCultistPlate));
      }

      return entityData;
   }

   protected Item getDropItem() {
      return Item.getItemById(0);
   }

   protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
   }

   protected void onDeathUpdate() {
      if (!this.world.isRemote) {
         EntityEldritchCrab crab = new EntityEldritchCrab(this.world);
         crab.setPositionAndRotation(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, this.rotationYaw, this.rotationPitch);
         crab.setHelm(true);
         this.world.spawnEntity(crab);
         if ((this.recentlyHit > 0 || this.isPlayer()) && this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot")) {
            int i = this.getExperiencePoints(this.attackingPlayer);

            while(i > 0) {
               int j = EntityXPOrb.getXPSplit(i);
               i -= j;
               this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
            }
         }
      }

      for(int i = 0; i < 20; ++i) {
         double d2 = this.rand.nextGaussian() * 0.02;
         double d0 = this.rand.nextGaussian() * 0.02;
         double d1 = this.rand.nextGaussian() * 0.02;
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d2, d0, d1);
      }

      this.setDead();
   }

   public void onDeath(DamageSource cause) {
   }

   @Override protected net.minecraft.util.SoundEvent getAmbientSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:crabtalk")); }

   public boolean getCanSpawnHere() {
      List ents = this.world.getEntitiesWithinAABB(EntityInhabitedZombie.class, new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX + (double)1.0F, this.posY + (double)1.0F, this.posZ + (double)1.0F).expand(32.0F, 16.0F, 32.0F));
      return ents.isEmpty() && super.getCanSpawnHere();
   }
}
