package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.EntityUtils;

public class EntityTaintSwarm extends EntityMob implements ITaintedMob {
   private static final DataParameter<Byte> SWARM_FLAGS = EntityDataManager.createKey(EntityTaintSwarm.class, DataSerializers.BYTE);
   private BlockPos currentFlightTarget;
   public int damBonus = 0;
   public ArrayList swarm = new ArrayList<>();
   private int attackTime = 0;

   public EntityTaintSwarm(World par1World) {
      super(par1World);
      this.setSize(2.0F, 2.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SWARM_FLAGS, (byte) 0);
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float par1) {
      return 15728880;
   }

   protected boolean canDespawn() {
       return super.canDespawn();
   }

   public float getBrightness(float par1) {
      return 1.0F;
   }

   protected float getSoundVolume() {
      return 0.1F;
   }

   protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source) {
      return SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:swarmattack"));
   }

   protected net.minecraft.util.SoundEvent getDeathSound() {
      return SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:swarmattack"));
   }

   public boolean canBePushed() {
      return false;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2 + this.damBonus);
   }

   public boolean getIsSummoned() {
      return (this.dataManager.get(SWARM_FLAGS) & 2) != 0;
   }

   public void setIsSummoned(boolean par1) {
      byte var2 = this.dataManager.get(SWARM_FLAGS);
      if (par1) {
         this.dataManager.set(SWARM_FLAGS, (byte)(var2 | 2));
      } else {
         this.dataManager.set(SWARM_FLAGS, (byte)(var2 & -4));
      }

   }


   public void onLivingUpdate() {
      super.onLivingUpdate();
   }

   public void onUpdate() {
      super.onUpdate();
      this.motionY *= 0.6F;
      if (this.world.isRemote) {
         for(int a = 0; a < this.swarm.size(); ++a) {
            if (this.swarm.get(a) == null || ((Entity)this.swarm.get(a)).isDead) {
               this.swarm.remove(a);
               break;
            }
         }

         if (this.swarm.size() < Math.max(Thaumcraft.proxy.particleCount(25), 10)) {
            this.swarm.add(Thaumcraft.proxy.swarmParticleFX(this.world, this, 0.22F, 15.0F, 0.08F));
         }
      }

   }

   protected void updateAITasks() {
      super.updateAITasks();
      if (this.getAttackTarget() == null) {
         if (this.getIsSummoned()) {
            this.attackEntityFrom(DamageSource.GENERIC, 5.0F);
         }

         if (this.currentFlightTarget != null && (!this.world.isAirBlock(new BlockPos(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) || this.currentFlightTarget.getY() < 1 || this.currentFlightTarget.getY() > this.world.getHeight(this.currentFlightTarget.getX(), this.currentFlightTarget.getZ()) + 8 || net.minecraft.world.biome.Biome.getIdForBiome(this.world.getBiome(new BlockPos(this.currentFlightTarget.getX(), 0, this.currentFlightTarget.getZ()))) != Config.biomeTaintID)) {
            this.currentFlightTarget = null;
         }

         if (this.currentFlightTarget == null || this.rand.nextInt(30) == 0 || this.currentFlightTarget.distanceSq((double)this.posX, (double)this.posY, (double)this.posZ) < 4.0F) {
            this.currentFlightTarget = new BlockPos((int)this.posX + this.rand.nextInt(7) - this.rand.nextInt(7), (int)this.posY + this.rand.nextInt(6) - 2, (int)this.posZ + this.rand.nextInt(7) - this.rand.nextInt(7));
         }

         double var1 = (double)this.currentFlightTarget.getX() + (double)0.5F - this.posX;
         double var3 = (double)this.currentFlightTarget.getY() + 0.1 - this.posY;
         double var5 = (double)this.currentFlightTarget.getZ() + (double)0.5F - this.posZ;
         this.motionX += (Math.signum(var1) * (double)0.5F - this.motionX) * 0.015000000014901161;
         this.motionY += (Math.signum(var3) * (double)0.7F - this.motionY) * (double)0.1F;
         this.motionZ += (Math.signum(var5) * (double)0.5F - this.motionZ) * 0.015000000014901161;
         float var7 = (float)(Math.atan2(this.motionZ, this.motionX) * (double)180.0F / Math.PI) - 90.0F;
         float var8 = MathHelper.wrapDegrees(var7 - this.rotationYaw);
         this.moveForward = 0.1F;
         this.rotationYaw += var8;
      } else {
         Entity target = this.getAttackTarget();
         if (target != null) {
            double var1 = target.posX - this.posX;
            double var3 = target.posY + (double)target.getEyeHeight() - this.posY;
            double var5 = target.posZ - this.posZ;
            this.motionX += (Math.signum(var1) * (double)0.5F - this.motionX) * 0.025000000149011613;
            this.motionY += (Math.signum(var3) * (double)0.7F - this.motionY) * (double)0.1F;
            this.motionZ += (Math.signum(var5) * (double)0.5F - this.motionZ) * 0.02500000001490116;
            float var7 = (float)(Math.atan2(this.motionZ, this.motionX) * (double)180.0F / Math.PI) - 90.0F;
            float var8 = MathHelper.wrapDegrees(var7 - this.rotationYaw);
            this.moveForward = 0.1F;
            this.rotationYaw += var8;
         }
      }

      if (this.getAttackTarget() instanceof EntityPlayer && ((EntityPlayer)this.getAttackTarget()).capabilities.disableDamage) {
         this.setAttackTarget(null);
      }

   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void fall(float par1) {
   }

   protected void updateFallState(double par1, boolean par3) {
   }

   public boolean doesEntityNotTriggerPressurePlate() {
      return true;
   }

   public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
      return !this.isEntityInvulnerable(par1DamageSource) && super.attackEntityFrom(par1DamageSource, par2);
   }

   protected void attackEntity(Entity par1Entity, float par2) {
      if (this.attackTime <= 0 && par2 < 3.0F && par1Entity.getEntityBoundingBox().maxY > this.getEntityBoundingBox().minY && par1Entity.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
         if (this.getIsSummoned()) {
            EntityUtils.setRecentlyHit((EntityLivingBase)par1Entity, 100);
         }

         this.attackTime = 10 + this.rand.nextInt(5);
         double mx = par1Entity.motionX;
         double my = par1Entity.motionY;
         double mz = par1Entity.motionZ;
         if (this.attackEntityAsMob(par1Entity) && !this.world.isRemote && par1Entity instanceof EntityLivingBase) {
            ((EntityLivingBase)par1Entity).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 0));
         }

         par1Entity.isAirBorne = false;
         par1Entity.motionX = mx;
         par1Entity.motionY = my;
         par1Entity.motionZ = mz;
         { SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:swarmattack")); if (_snd != null) this.world.playSound(null, this.posX, this.posY, this.posZ, _snd, SoundCategory.HOSTILE, 0.3F, 0.9F + this.world.rand.nextFloat() * 0.2F); }
      }

   }

   protected Entity findPlayerToAttack() {
      double var1 = 12.0F;
      return this.getIsSummoned() ? null : this.world.getClosestPlayerToEntity(this, var1);
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.dataManager.set(SWARM_FLAGS, par1NBTTagCompound.getByte("Flags"));
      this.damBonus = par1NBTTagCompound.getByte("damBonus");
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setByte("Flags", this.dataManager.get(SWARM_FLAGS));
      par1NBTTagCompound.setByte("damBonus", (byte)this.damBonus);
   }

   public boolean getCanSpawnHere() {
      int var1 = MathHelper.floor(this.getEntityBoundingBox().minY);
      int var2 = MathHelper.floor(this.posX);
      int var3 = MathHelper.floor(this.posZ);
      int var4 = this.world.getLightFor(EnumSkyBlock.BLOCK, new BlockPos(var2, var1, var3));
      byte var5 = 7;
      return var4 <= this.rand.nextInt(var5) && super.getCanSpawnHere();
   }

   protected boolean isValidLightLevel() {
      return true;
   }

   protected Item getDropItem() {
      return ConfigItems.itemResource;
   }

   protected void dropFewItems(boolean flag, int i) {
      if (this.world.rand.nextBoolean()) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 11), this.height / 2.0F);
      }

   }
}
