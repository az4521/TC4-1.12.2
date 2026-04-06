package thaumcraft.common.entities.monster;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.ai.combat.AIAttackOnCollide;
import thaumcraft.common.entities.ai.misc.AIConvertGrass;

public class EntityTaintSheep extends EntityMob implements IShearable, ITaintedMob {
   private static final DataParameter<Byte> SHEEP_FLAGS = EntityDataManager.createKey(EntityTaintSheep.class, DataSerializers.BYTE);
   private int sheepTimer;
   private AIConvertGrass field_48137_c = new AIConvertGrass(this);

   public EntityTaintSheep(World par1World) {
      super(par1World);
      this.setSize(0.9F, 1.3F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, this.field_48137_c);
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityPlayer.class, 1.0F, false));
      this.tasks.addTask(3, new AIAttackOnCollide(this, EntityVillager.class, 1.0F, true));
      this.tasks.addTask(6, new EntityAIWander(this, 1.0F));
      this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(0, new EntityAIHurtByTarget(this, false));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, false));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SHEEP_FLAGS, (byte)0);
   }

   protected boolean isAIEnabled() {
      return true;
   }

   protected boolean canDespawn() {
      return false;
   }

   public int getTotalArmorValue() {
      return 1;
   }

   protected void updateAITasks() {
      this.sheepTimer = this.field_48137_c.func_48396_h();
      super.updateAITasks();
   }

   public void onLivingUpdate() {
      if (this.world.isRemote) {
         this.sheepTimer = Math.max(0, this.sheepTimer - 1);
      }

      super.onLivingUpdate();
      if (this.world.isRemote && this.ticksExisted < 5) {
         for(int a = 0; a < Thaumcraft.proxy.particleCount(10); ++a) {
            Thaumcraft.proxy.splooshFX(this);
         }
      }

   }

   protected Item getDropItem() {
      return ConfigItems.itemResource;
   }

   protected void dropFewItems(boolean flag, int i) {
      if (this.world.rand.nextInt(3) == 0) {
         if (this.world.rand.nextBoolean()) {
            this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 11), this.height / 2.0F);
         } else {
            this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 12), this.height / 2.0F);
         }
      }

   }

   public void handleStatusUpdate(byte par1) {
      if (par1 == 10) {
         this.sheepTimer = 40;
      } else {
         super.handleStatusUpdate(par1);
      }

   }

   public float func_44003_c(float par1) {
      return this.sheepTimer <= 0 ? 0.0F : (this.sheepTimer >= 4 && this.sheepTimer <= 36 ? 1.0F : (this.sheepTimer < 4 ? ((float)this.sheepTimer - par1) / 4.0F : -((float)(this.sheepTimer - 40) - par1) / 4.0F));
   }

   public float func_44002_d(float par1) {
      if (this.sheepTimer > 4 && this.sheepTimer <= 36) {
         float var2 = ((float)(this.sheepTimer - 4) - par1) / 32.0F;
         return ((float)Math.PI / 5F) + 0.2199115F * MathHelper.sin(var2 * 28.7F);
      } else {
         return this.sheepTimer > 0 ? ((float)Math.PI / 5F) : this.rotationPitch / (180F / (float)Math.PI);
      }
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setBoolean("Sheared", this.getSheared());
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.setSheared(par1NBTTagCompound.getBoolean("Sheared"));
   }

   @Override protected net.minecraft.util.SoundEvent getAmbientSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.sheep.ambient")); }
   @Override protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.sheep.hurt")); }
   @Override protected net.minecraft.util.SoundEvent getDeathSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.sheep.death")); }
   @Override protected void playStepSound(BlockPos pos, Block blockIn) { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:entity.sheep.step")); if (_snd != null) this.playSound(_snd, 0.15F, 1.0F); }

   protected float getSoundPitch() {
      return 0.7F;
   }

   public boolean getSheared() {
      return (this.dataManager.get(SHEEP_FLAGS) & 16) != 0;
   }

   public void setSheared(boolean par1) {
      byte var2 = this.dataManager.get(SHEEP_FLAGS);
      if (par1) {
         this.dataManager.set(SHEEP_FLAGS, (byte)(var2 | 16));
      } else {
         this.dataManager.set(SHEEP_FLAGS, (byte)(var2 & -17));
      }

   }

   public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
      return !this.getSheared();
   }

   @Override
   public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
      List<ItemStack> ret = new ArrayList<>();
      this.setSheared(true);
      int i = 1 + this.rand.nextInt(3);

      for(int j = 0; j < i; ++j) {
         ret.add(new ItemStack(Blocks.WOOL, 1, 10));
      }

      return ret;
   }
}
