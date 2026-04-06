package thaumcraft.common.entities.monster;

import java.util.List;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.api.damagesource.DamageSourceThaumcraft;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class EntityTaintacle extends EntityMob implements ITaintedMob {
   public float flailIntensity = 1.0F;
   public int attackTime = 0;

   public EntityTaintacle(World par1World) {
      super(par1World);
      this.setSize(0.66F, 3.0F);
      this.experienceValue = 10;
   }

   public boolean getCanSpawnHere() {
      int var1 = MathHelper.floor(this.getEntityBoundingBox().minY);
      int var2 = MathHelper.floor(this.posX);
      int var3 = MathHelper.floor(this.posZ);
      byte var5 = 7;
      List ents = this.world.getEntitiesWithinAABB(EntityTaintacle.class, new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).expand(24.0F, 8.0F, 24.0F));
      net.minecraft.block.state.IBlockState bs1 = this.world.getBlockState(new BlockPos(var2, var1, var3));
      int meta1 = bs1.getBlock().getMetaFromState(bs1);
      net.minecraft.block.state.IBlockState bs2 = this.world.getBlockState(new BlockPos(var2, var1, var3));
      int meta2 = bs2.getBlock().getMetaFromState(bs2);
      boolean onTaint = (bs1.getBlock() == ConfigBlocks.blockTaintFibres && meta1 == 0 || bs2.getBlock() == ConfigBlocks.blockTaint && meta2 == 1) && net.minecraft.world.biome.Biome.getIdForBiome(this.world.getBiome(new BlockPos(var2, 0, var3))) == Config.biomeTaintID;
      return ents.isEmpty() && onTaint && super.getCanSpawnHere();
   }

   public float getShadowSize() {
      return 0.25F;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0F);
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean canBePushed() {
      return true;
   }

   protected Entity findPlayerToAttack() {
      Entity entity = null;
      List ents = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).expand(this.height * 6.0F, this.height * 3.0F, this.height * 6.0F));
      if (!ents.isEmpty()) {
         double distance = Double.MAX_VALUE;

         for (Object ent : ents) {
            if (ent != null) {
               EntityLivingBase el = (EntityLivingBase)ent;
               double d = el.getDistanceSq(this);
               if (!(el instanceof ITaintedMob) && d < distance) {
                  distance = d;
                  entity = el;
               }
            }
         }
      }

      return entity;
   }

   public void move(net.minecraft.entity.MoverType type, double par1, double par3, double par5) {
      par1 = 0.0;
      par5 = 0.0;
      if (par3 > 0.0) {
         par3 = 0.0;
      }

      super.move(type, par1, par3, par5);
   }

   public void onUpdate() {
      if (this.getAttackTarget() != null) {
         this.faceEntity(this.getAttackTarget(), 5.0F);
      }

      super.onUpdate();
      if (!this.world.isRemote && this.ticksExisted % 20 == 0 && net.minecraft.world.biome.Biome.getIdForBiome(this.world.getBiome(new BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ)))) != Config.biomeTaintID) {
         this.damageEntity(DamageSource.STARVE, 1.0F);
      }

      if (this.world.isRemote) {
         Entity target = this.getAttackTarget();
         if ((float)this.ticksExisted > this.height * 10.0F && (this.hurtTime > 0 || this.attackTime > 0 || target != null && target.getDistance(this) < this.height)) {
            if (this.flailIntensity < 3.0F) {
               this.flailIntensity += 0.2F;
            }
         } else if (this.flailIntensity > 1.0F) {
            this.flailIntensity -= 0.2F;
         }

         if ((float)this.ticksExisted < this.height * 10.0F && this.onGround) {
            Thaumcraft.proxy.tentacleAriseFX(this);
         }
      }

      if (this.getAttackTarget() == null) {
         Entity found = this.findPlayerToAttack();
         if (found instanceof EntityLivingBase) {
            this.setAttackTarget((EntityLivingBase) found);
         }
      } else if (this.getAttackTarget().isEntityAlive() && this.getAgitationState()) {
         float f1 = this.getAttackTarget().getDistance(this);
         if (!this.world.isRemote && this.canEntityBeSeen(this.getAttackTarget())) {
            this.attackEntity(this.getAttackTarget(), f1);
         }
      } else {
         this.setAttackTarget(null);
      }

   }

   protected void attackEntity(Entity entity, float par2) {
      if (this.attackTime <= 0) {
         if (par2 <= this.height && entity.getEntityBoundingBox().maxY > this.getEntityBoundingBox().minY && entity.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
            this.attackTime = 20;
            this.attackEntityAsMob(entity);
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:tentacle")); if (_snd != null) this.playSound(_snd, this.getSoundVolume(), this.getSoundPitch()); }
         } else if (par2 > this.height && entity.onGround && !(this instanceof EntityTaintacleSmall)) {
            this.spawnTentacles(entity);
         }
      }

   }

   public boolean attackEntityAsMob(Entity par1Entity) {
      float i = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
      if (this.isPotionActive(MobEffects.STRENGTH)) {
         i += (float)(3 << this.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier());
      }

      if (this.isPotionActive(MobEffects.WEAKNESS)) {
         i -= (float)(2 << this.getActivePotionEffect(MobEffects.WEAKNESS).getAmplifier());
      }

      int j = 0;
      if (par1Entity instanceof EntityLivingBase) {
         i += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)par1Entity).getCreatureAttribute());
         j += EnchantmentHelper.getKnockbackModifier(this);
      }

      boolean flag = par1Entity.attackEntityFrom(DamageSourceThaumcraft.causeTentacleDamage(this), i);
      if (flag) {
         if (j > 0) {
            par1Entity.addVelocity(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)j * 0.5F, 0.1, MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)j * 0.5F);
            this.motionX *= 0.6;
            this.motionZ *= 0.6;
         }

         int k = EnchantmentHelper.getFireAspectModifier(this);
         if (k > 0) {
            par1Entity.setFire(k * 4);
         }

         if (par1Entity instanceof EntityLivingBase) {
            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)par1Entity, this);
         }

         EnchantmentHelper.applyArthropodEnchantments(this, par1Entity);
      }

      return flag;
   }

   protected void spawnTentacles(Entity entity) {
      int i = MathHelper.floor(entity.posX);
      int j = MathHelper.floor(entity.getEntityBoundingBox().minY);
      int k = MathHelper.floor(entity.posZ);
      int biomeId = net.minecraft.world.biome.Biome.getIdForBiome(this.world.getBiome(new BlockPos(i, 0, k)));
      if (biomeId == Config.biomeEldritchID || biomeId == Config.biomeTaintID && (this.world.getBlockState(new BlockPos(i, j, k)).getMaterial() == Config.taintMaterial || this.world.getBlockState(new BlockPos(i, j - 1, k)).getMaterial() == Config.taintMaterial)) {
         this.attackTime = 40 + this.world.rand.nextInt(20);
         EntityTaintacleSmall taintlet = new EntityTaintacleSmall(this.world);
         taintlet.setLocationAndAngles(entity.posX + (double)this.world.rand.nextFloat() - (double)this.world.rand.nextFloat(), entity.posY, entity.posZ + (double)this.world.rand.nextFloat() - (double)this.world.rand.nextFloat(), 0.0F, 0.0F);
         this.world.spawnEntity(taintlet);
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:tentacle")); if (_snd != null) this.playSound(_snd, this.getSoundVolume(), this.getSoundPitch()); }
         if (biomeId == Config.biomeEldritchID && this.world.isAirBlock(new BlockPos(i, j, k)) && BlockUtils.isAdjacentToSolidBlock(this.world, i, j, k)) {
            Utils.setBiomeAt(this.world, i, k, ThaumcraftWorldGenerator.biomeTaint);
            this.world.setBlockState(new BlockPos(i, j, k), ConfigBlocks.blockTaintFibres.getStateFromMeta(this.world.rand.nextInt(4) == 0 ? 1 : 0), 3);
         }
      }

   }

   public boolean attackEntityFrom(DamageSource ds, float par2) {
      if (!(this instanceof EntityTaintacleSmall) && ds.getTrueSource() != null && this.getDistance(ds.getTrueSource()) > 16.0F && !this.world.isRemote) {
         this.spawnTentacles(ds.getTrueSource());
      }

      return super.attackEntityFrom(ds, par2);
   }

   public boolean getAgitationState() {
      return this.getAttackTarget() != null && this.getAttackTarget().getDistanceSq(this) < (double)(this.height * 7.0F * this.height * 7.0F);
   }

   public void faceEntity(Entity par1Entity, float par2) {
      double d0 = par1Entity.posX - this.posX;
      double d1 = par1Entity.posZ - this.posZ;
      float f2 = (float)(Math.atan2(d1, d0) * (double)180.0F / Math.PI) - 90.0F;
      this.rotationYaw = this.updateRotation(this.rotationYaw, f2, par2);
   }

   protected float updateRotation(float par1, float par2, float par3) {
      float f3 = MathHelper.wrapDegrees(par2 - par1);
      if (f3 > par3) {
         f3 = par3;
      }

      if (f3 < -par3) {
         f3 = -par3;
      }

      return par1 + f3;
   }

   public int getTalkInterval() {
      return 200;
   }

   @Override protected net.minecraft.util.SoundEvent getAmbientSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:roots")); }
   @Override protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:tentacle")); }
   @Override protected net.minecraft.util.SoundEvent getDeathSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:tentacle")); }

   protected float getSoundPitch() {
      return 1.3F - this.height / 10.0F;
   }

   protected float getSoundVolume() {
      return this.height / 8.0F;
   }

   protected void dropFewItems(boolean flag, int i) {
      if (this.world.rand.nextBoolean()) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 11), this.height / 2.0F);
      } else {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 12), this.height / 2.0F);
      }

      super.dropFewItems(flag, i);
   }
}
