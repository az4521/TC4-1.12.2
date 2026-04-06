package thaumcraft.common.entities.monster;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.combat.AICultistHurtByTarget;

public class EntityCultistKnight extends EntityCultist {
   public EntityCultistKnight(World worldIn) {
      super(worldIn);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityLivingBase.class, 1.0F, false));
      this.tasks.addTask(4, new EntityAIRestrictOpenDoor(this));
      this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
      this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.8));
      this.tasks.addTask(7, new EntityAIWander(this, 0.8));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new AICultistHurtByTarget(this, true));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(36.0F);
   }

   protected void addRandomArmor() {
      this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ConfigItems.itemHelmetCultistPlate));
      this.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(ConfigItems.itemChestCultistPlate));
      this.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(ConfigItems.itemLegsCultistPlate));
      this.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(ConfigItems.itemBootsCultist));
      if (this.rand.nextFloat() < (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.05F : 0.01F)) {
         int i = this.rand.nextInt(5);
         if (i == 0) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ConfigItems.itemSwordVoid));
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(ConfigItems.itemHelmetCultistRobe));
         } else {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ConfigItems.itemSwordThaumium));
            if (this.rand.nextBoolean()) {
               this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
            }
         }
      } else {
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
      }

   }

   protected void enchantEquipment() {
      float f = 1.0F; // getTensionFactorForBlock removed in 1.12.2
      if (this.getHeldItemMainhand() != null && this.rand.nextFloat() < 0.25F * f) {
         EnchantmentHelper.addRandomEnchantment(this.rand, this.getHeldItemMainhand(), (int)(5.0F + f * (float)this.rand.nextInt(18)), false);
      }

   }
}
