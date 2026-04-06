package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

public class EntityTaintSporeSwarmer extends EntityTaintSpore {
   int spawnCounter = 500;

   public EntityTaintSporeSwarmer(World par1World) {
      super(par1World);
      this.setSporeSize(10);
   }

   public void setSporeSize(int par1) {
      super.setSporeSize(par1);
      this.setSize(1.0F, 1.0F);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.experienceValue = par1;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(75.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
   }

   public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
      if (this.world.isRemote) {
         this.sploosh(10);
      }

      return super.attackEntityFrom(par1DamageSource, par2);
   }

   protected void sporeOnUpdate() {
      this.pushOutOfBlocks(this.posX, this.posY, this.posZ);
      if (this.spawnCounter > 0) {
         --this.spawnCounter;
      }

      if (this.spawnCounter <= 0 && this.world.getClosestPlayerToEntity(this, 16.0) != null) {
         this.spawnCounter = 500;
         this.swarmBurst(1);
      }

      if (this.world.isRemote) {
         for(int a = 0; a < this.swarm.size(); ++a) {
            if (this.swarm.get(a) == null || ((Entity)this.swarm.get(a)).isDead) {
               this.swarm.remove(a);
               break;
            }
         }

         if (this.swarm.size() < (500 - this.spawnCounter) / 25) {
            this.swarm.add(Thaumcraft.proxy.swarmParticleFX(this.world, this, 0.1F, 10.0F, 0.0F));
         }
      }

      if (this.deathTime == 1) {
         this.swarmBurst(1);
      }

   }

   public void onCollideWithPlayer(EntityPlayer par1EntityPlayer) {
   }

   protected void swarmBurst(int amt) {
      if (!this.world.isRemote) {
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:gore")); if (_snd != null) this.world.playSound(null, this.posX, this.posY, this.posZ, _snd, SoundCategory.HOSTILE, 1.0F, 0.9F + this.world.rand.nextFloat() * 0.1F); }

         for(int a = 0; a < amt; ++a) {
            EntityTaintSwarm swarm = new EntityTaintSwarm(this.world);
            swarm.setLocationAndAngles(this.posX, this.posY + (double)0.5F, this.posZ, this.world.rand.nextFloat() * 360.0F, 0.0F);
            this.world.spawnEntity(swarm);
         }

         this.world.setEntityState(this, (byte)6);
      }

   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte par1) {
      if (par1 == 6) {
         this.spawnCounter = 500;
         this.sploosh(25);

          for (Object o : this.swarm) {
              ((Entity) o).setDead();
          }

         this.swarm.clear();
      } else {
         super.handleStatusUpdate(par1);
      }

   }

   public int getTalkInterval() {
       return super.getTalkInterval();
   }

   @Override protected net.minecraft.util.SoundEvent getAmbientSound() { return net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:roots")); }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float par1) {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posZ);
      if (this.world.isBlockLoaded(new BlockPos(i, 0, j))) {
         double d0 = (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) * 0.66;
         int k = MathHelper.floor(this.posY + d0);
         return this.world.getCombinedLight(new BlockPos(i, k, j), 0);
      } else {
         return 0;
      }
   }

   public float getBrightness(float par1) {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posZ);
      if (this.world.isBlockLoaded(new BlockPos(i, 0, j))) {
         double d0 = (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) * 0.66;
         int k = MathHelper.floor(this.posY + d0);
         return this.world.getLightBrightness(new BlockPos(i, k, j));
      } else {
         return 0.0F;
      }
   }

   protected Item getDropItem() {
       return super.getDropItem();
   }

   protected void dropFewItems(boolean flag, int i) {
      for(int a = 0; a <= 1; ++a) {
         if (this.world.rand.nextBoolean()) {
            this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 11), this.height / 2.0F);
         } else {
            this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 12), this.height / 2.0F);
         }
      }

   }
}
