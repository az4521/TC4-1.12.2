package thaumcraft.common.entities.monster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.monster.boss.EntityCultistLeader;

public class EntityCultist extends EntityMob {
   public EntityCultist(World worldIn) {
      super(worldIn);
      this.setSize(0.6F, 1.8F);
      this.experienceValue = 10;
      ((net.minecraft.pathfinding.PathNavigateGround)this.getNavigator()).setBreakDoors(true);
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0F);
   }

   protected void entityInit() {
      super.entityInit();
   }

   public boolean canPickUpLoot() {
      return false;
   }

   protected boolean isAIEnabled() {
      return true;
   }

   protected Item getDropItem() {
       return super.getDropItem();
   }

   protected void dropFewItems(boolean flag, int i) {
      int r = this.rand.nextInt(10);
      if (r == 0) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 9), 1.5F);
      } else if (r == 1) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 17), 1.5F);
      } else if (r <= 3 + i) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 18), 1.5F);
      }

      super.dropFewItems(flag, i);
   }

   protected void dropRareDrop(int id) {
      this.entityDropItem(new ItemStack(ConfigItems.itemEldritchObject, 1, 1), 1.0F);
   }

   protected void addRandomArmor() {
   }

   protected void enchantEquipment() {
   }

   public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData entityData) {
      this.addRandomArmor();
      this.enchantEquipment();
      return super.onInitialSpawn(difficulty, entityData);
   }

   protected boolean canDespawn() {
       return super.canDespawn();
   }

   public boolean attackEntityAsMob(Entity entityIn) {
      return super.attackEntityAsMob(entityIn);
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      if (nbt.hasKey("HomeD")) {
         this.setHomePosAndDistance(new net.minecraft.util.math.BlockPos(nbt.getInteger("HomeX"), nbt.getInteger("HomeY"), nbt.getInteger("HomeZ")), nbt.getInteger("HomeD"));
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

   public boolean isOnSameTeam(EntityLivingBase el) {
      return el instanceof EntityCultist || el instanceof EntityCultistLeader;
   }

   public boolean canAttackClass(Class clazz) {
      return clazz != EntityCultistCleric.class && clazz != EntityCultistLeader.class && clazz != EntityCultistKnight.class && super.canAttackClass(clazz);
   }
}
