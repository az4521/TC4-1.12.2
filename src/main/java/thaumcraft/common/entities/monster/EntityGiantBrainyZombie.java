package thaumcraft.common.entities.monster;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigItems;

public class EntityGiantBrainyZombie extends EntityBrainyZombie {
   private static final DataParameter<Float> ANGER = EntityDataManager.createKey(EntityGiantBrainyZombie.class, DataSerializers.FLOAT);

   public EntityGiantBrainyZombie(World world) {
      super(world);
      this.experienceValue = 15;
      this.setSize(this.width * (1.2F + this.getAnger()), this.height * (1.2F + this.getAnger()));
      this.tasks.addTask(2, new EntityAILeapAtTarget(this, 0.4F));
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (this.getAnger() > 1.0F) {
         this.setAnger(this.getAnger() - 0.002F);
         this.setSize(0.6F * (1.2F + this.getAnger()), 1.8F * (1.2F + this.getAnger()));
      }

      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0F + (this.getAnger() - 1.0F) * 5.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(ANGER, 1.0F);
   }

   public float getAnger() {
      return this.dataManager.get(ANGER);
   }

   public void setAnger(float par1) {
      this.dataManager.set(ANGER, par1);
   }

   public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
      this.setAnger(Math.min(2.0F, this.getAnger() + 0.1F));
      return super.attackEntityFrom(par1DamageSource, par2);
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0F);
   }

   protected void dropFewItems(boolean flag, int i) {
      for(int a = 0; a < 6; ++a) {
         if (this.world.rand.nextBoolean()) {
            this.dropItem(Items.ROTTEN_FLESH, 2);
         }
      }

      for(int a = 0; a < 6; ++a) {
         if (this.world.rand.nextBoolean()) {
            this.dropItem(Items.ROTTEN_FLESH, 2);
         }
      }

      if (this.world.rand.nextInt(10) - i <= 4) {
         this.entityDropItem(new ItemStack(ConfigItems.itemZombieBrain), 2.0F);
      }

   }

   protected void dropRareDrop(int par1) {
      switch (this.rand.nextInt(4)) {
         case 0:
            this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 2), 2.0F);
            break;
         case 1:
            this.dropItem(Items.CARROT, 1);
            break;
         case 2:
            this.dropItem(Items.POTATO, 1);
            break;
         case 3:
            this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 6), 2.0F);
      }

   }
}
