package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityMindSpider extends EntitySpider {
   private static final DataParameter<Byte> HARMLESS = EntityDataManager.createKey(EntityMindSpider.class, DataSerializers.BYTE);
   private static final DataParameter<String> VIEWER = EntityDataManager.createKey(EntityMindSpider.class, DataSerializers.STRING);

   private int lifeSpan = Integer.MAX_VALUE;

   public EntityMindSpider(World par1World) {
      super(par1World);
      this.setSize(0.3F, 0.3F);
      this.experienceValue = 1;
   }

   protected int getExperiencePoints(EntityPlayer player) {
      return this.isHarmless() ? 0 : super.getExperiencePoints(player);
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(HARMLESS, (byte) 0);
      this.dataManager.register(VIEWER, "");
   }

   public String getViewer() {
      return this.dataManager.get(VIEWER);
   }

   public void setViewer(String player) {
      this.dataManager.set(VIEWER, player);
   }

   public boolean isHarmless() {
      return this.dataManager.get(HARMLESS) != 0;
   }

   public void setHarmless(boolean h) {
      if (h) {
         this.lifeSpan = 1200;
      }
      this.dataManager.set(HARMLESS, (byte)(h ? 1 : 0));
   }

   protected float getSoundPitch() {
      return 0.7F;
   }

   // findPlayerToAttack removed — EntitySpider uses AI tasks in 1.12.2

   @SideOnly(Side.CLIENT)
   public float spiderScaleAmount() {
      return 0.3F;
   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote && this.ticksExisted > this.lifeSpan) {
         this.setDead();
      }
   }

   public float getShadowSize() {
      return this.isHarmless() ? 0.0F : 0.1F;
   }

   protected Item getDropItem() {
      return Item.getItemById(0);
   }

   protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
   }

   public boolean doesEntityNotTriggerPressurePlate() {
      return true;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   // attackEntity(Entity, float) removed — harmless spiders simply never attack
   // Override attackEntityAsMob instead to suppress damage when harmless
   public boolean attackEntityAsMob(net.minecraft.entity.Entity entityIn) {
      return !this.isHarmless() && super.attackEntityAsMob(entityIn);
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.dataManager.set(HARMLESS, par1NBTTagCompound.getByte("harmless"));
      this.dataManager.set(VIEWER, par1NBTTagCompound.getString("viewer"));
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setByte("harmless", this.dataManager.get(HARMLESS));
      par1NBTTagCompound.setString("viewer", this.dataManager.get(VIEWER));
   }
}
