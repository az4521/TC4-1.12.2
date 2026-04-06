package thaumcraft.common.entities.monster.boss;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.monster.EntityCultist;
import thaumcraft.common.entities.monster.EntityCultistCleric;
import thaumcraft.common.entities.monster.EntityCultistKnight;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockArc;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.world.WorldGenEldritchRing;
import thaumcraft.common.tiles.TileBanner;

public class EntityCultistPortal extends EntityMob  {
   int stage = 0;
   int stagecounter = 200;
   public int pulse = 0;

   public EntityCultistPortal(World par1World) {
      super(par1World);
      this.isImmuneToFire = true;
      this.experienceValue = 30;
      this.setSize(1.5F, 3.0F);
   }

   public int getTotalArmorValue() {
      return 5;
   }

   protected void entityInit() {
      super.entityInit();
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setInteger("stage", this.stage);
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.stage = nbt.getInteger("stage");
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(500.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0F);
      this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
   }

   public float getShadowSize() {
      return 0.0F;
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean canBePushed() {
      return false;
   }

   public void moveEntity(double par1, double par3, double par5) {
   }

   public boolean isInRangeToRenderDist(double par1) {
      return par1 < (double)4096.0F;
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float par1) {
      return 15728880;
   }

   public float getBrightness(float par1) {
      return 1.0F;
   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote) {
         if (this.stagecounter <= 0) {
            if (this.world.getClosestPlayerToEntity(this, 48.0F) != null) {
               this.world.setEntityState(this, (byte)16);
               switch (this.stage) {
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                  case 4:
                     this.stagecounter = 15 + this.rand.nextInt(10 - this.stage) - this.stage;
                     this.spawnMinions();
                     break;
                  case 12:
                     this.stagecounter = 50 + this.getTiming() * 2 + this.rand.nextInt(50);
                     this.spawnBoss();
                  default:
                     int t = this.getTiming();
                     this.stagecounter = t + this.rand.nextInt(5 + t / 3);
                     this.spawnMinions();
                     break;
               }

               ++this.stage;
            } else {
               this.stagecounter = 30 + this.rand.nextInt(30);
            }
         } else {
            --this.stagecounter;
            if (this.stagecounter == 160 && this.stage == 0) {
               this.world.setEntityState(this, (byte)16);

               for(int a = 2; a < 6; ++a) {
                  EnumFacing dir = EnumFacing.byIndex(a);
                  BlockPos bannerPos = new BlockPos((int)this.posX - dir.getXOffset() * 6, (int)this.posY, (int)this.posZ + dir.getZOffset() * 6);
                  this.world.setBlockState(bannerPos, ConfigBlocks.blockWoodenDevice.getStateFromMeta(8), 3);
                  TileEntity te = this.world.getTileEntity(bannerPos);
                  if (te instanceof TileBanner) {
                     ((TileBanner)te).setFacing(WorldGenEldritchRing.bannerFaceFromDirection(a));
                     PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockArc(bannerPos.getX(), bannerPos.getY(), bannerPos.getZ(), this.getEntityId()), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 32.0F));
                  }
               }
            }

            if (this.stagecounter > 20 && this.stagecounter < 150 && this.stage == 0 && this.stagecounter % 13 == 0) {
               int a = (int)this.posX + this.rand.nextInt(5) - this.rand.nextInt(5);
               int b = (int)this.posZ + this.rand.nextInt(5) - this.rand.nextInt(5);
               BlockPos cratePos = new BlockPos(a, (int)this.posY, b);
               if (a != (int)this.posX && b != (int)this.posZ && this.world.isAirBlock(cratePos)) {
                  this.world.setEntityState(this, (byte)16);
                  float rr = this.world.rand.nextFloat();
                  int md = rr < 0.05F ? 2 : (rr < 0.2F ? 1 : 0);
                  this.world.setBlockState(cratePos, ConfigBlocks.blockLootCrate.getStateFromMeta(md), 3);
                  PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockArc(a, (int)this.posY, b, this.getEntityId()), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.posX, this.posY, this.posZ, 32.0F));
               }
            }
         }

         if (this.stage < 12) {
            this.heal(1.0F);
         }
      }

      if (this.pulse > 0) {
         --this.pulse;
      }

   }

   int getTiming() {
      List<Entity> l = EntityUtils.getEntitiesInRange(this.world, this.posX, this.posY, this.posZ, this, EntityCultist.class, 32.0F);
      return l.size() * 20;
   }

   void spawnMinions() {
      EntityCultist cultist = null;
      if ((double)this.rand.nextFloat() > 0.33) {
         cultist = new EntityCultistKnight(this.world);
      } else {
         cultist = new EntityCultistCleric(this.world);
      }

      cultist.setPosition(this.posX + (double)this.rand.nextFloat() - (double)this.rand.nextFloat(), this.posY + (double)0.25F, this.posZ + (double)this.rand.nextFloat() - (double)this.rand.nextFloat());
      cultist.onInitialSpawn(this.world.getDifficultyForLocation(new net.minecraft.util.math.BlockPos(cultist)), null);
      cultist.spawnExplosionParticle();
      cultist.setHomePosAndDistance(new net.minecraft.util.math.BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 32);
      this.world.spawnEntity(cultist);
      SoundEvent snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:wandfail"));
      if (snd != null) cultist.playSound(snd, 1.0F, 1.0F);
      if (this.stage > 12) {
         this.attackEntityFrom(DamageSource.OUT_OF_WORLD, (float)(5 + this.rand.nextInt(5)));
      }

   }

   void spawnBoss() {
      EntityCultistLeader cultist = new EntityCultistLeader(this.world);
      cultist.setPosition(this.posX + (double)this.rand.nextFloat() - (double)this.rand.nextFloat(), this.posY + (double)0.25F, this.posZ + (double)this.rand.nextFloat() - (double)this.rand.nextFloat());
      cultist.onInitialSpawn(this.world.getDifficultyForLocation(new net.minecraft.util.math.BlockPos(cultist)), null);
      cultist.setHomePosAndDistance(new net.minecraft.util.math.BlockPos((int)this.posX, (int)this.posY, (int)this.posZ), 32);
      cultist.spawnExplosionParticle();
      this.world.spawnEntity(cultist);
      SoundEvent snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:wandfail"));
      if (snd != null) cultist.playSound(snd, 1.0F, 1.0F);
   }

   public void onCollideWithPlayer(EntityPlayer p) {
      if (this.getDistanceSq(p) < (double)3.0F && p.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this), 8.0F)) {
         SoundEvent snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:zap"));
         if (snd != null) this.playSound(snd, 1.0F, 1.0F);
      }

   }

   protected float getSoundVolume() {
      return 0.75F;
   }

   public int getTalkInterval() {
      return 540;
   }

   @Override protected net.minecraft.util.SoundEvent getAmbientSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:monolith")); }
   @Override protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); }
   @Override protected net.minecraft.util.SoundEvent getDeathSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:shock")); }

   protected Item getDropItem() {
       return super.getDropItem();
   }

   protected void dropFewItems(boolean flag, int fortune) {
      EntityUtils.entityDropSpecialItem(this, new ItemStack(ConfigItems.itemEldritchObject, 1, 3), this.height / 2.0F);
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte msg) {
      if (msg == 16) {
         this.pulse = 10;
         this.spawnExplosionParticle();
      } else {
         super.handleStatusUpdate(msg);
      }

   }

   public void addPotionEffect(PotionEffect potioneffectIn) {
   }

   protected void fall(float distance) {
   }

   public void onDeath(DamageSource cause) {
      if (!this.world.isRemote) {
         this.world.newExplosion(this, this.posX, this.posY, this.posZ, 2.0F, false, false);
      }

      super.onDeath(cause);
   }
}
