package thaumcraft.common.entities.monster;

import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.api.entities.ITaintedMob;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;

public class EntityTaintSpore extends EntityMob implements ITaintedMob, IEntityAdditionalSpawnData {
   private static final DataParameter<Byte> SPORE_SIZE = EntityDataManager.createKey(EntityTaintSpore.class, DataSerializers.BYTE);

   public ArrayList swarm = new ArrayList<>();
   protected int growth = 0;
   public float displaySize = 0.0F;

   public EntityTaintSpore(World par1World) {
      super(par1World);
      this.setSporeSize(2);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SPORE_SIZE, (byte)1);
   }

   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setInteger("Size", this.getSporeSize() - 1);
   }

   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      this.setSporeSize(par1NBTTagCompound.getInteger("Size") + 1);
   }

   public void setSporeSize(int par1) {
      this.dataManager.set(SPORE_SIZE, (byte)par1);
      float size = Math.max(0.15F * (float)par1, 0.5F);
      this.setSize(size, size);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.experienceValue = par1;
   }

   public int getSporeSize() {
      return this.dataManager.get(SPORE_SIZE);
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0F);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
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

   // Override move to constrain horizontal movement when sitting on taint fibres
   @Override
   public void move(net.minecraft.entity.MoverType type, double dx, double dy, double dz) {
      dx = 0.0;
      dz = 0.0;
      if (dy > 0.0) {
         dy = 0.0;
      }
      int x = MathHelper.floor(this.posX);
      int y = MathHelper.floor(this.getEntityBoundingBox().minY) - 1;
      int z = MathHelper.floor(this.posZ);
      if (this.world.getBlockState(new BlockPos(x, y, z)).getBlock() != ConfigBlocks.blockTaintFibres) {
         super.move(type, dx, dy, dz);
      }
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
      if (!this.world.isRemote && this.ticksExisted % 20 == 0
            && net.minecraft.world.biome.Biome.getIdForBiome(
                  this.world.getBiome(new BlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ))))
               != Config.biomeTaintID) {
         this.attackEntityFrom(DamageSource.STARVE, 1.0F);
      }

      this.sporeOnUpdate();
   }

   protected void sporeOnUpdate() {
      if (this.getSporeSize() < 10 && this.growth++ == 1200) {
         this.setSporeSize(this.getSporeSize() + 1);
         this.growth = 0;
      }

      if (this.world.isRemote) {
         if (this.displaySize < (float)this.getSporeSize()) {
            this.displaySize += 0.02F;
         }

         for (int a = 0; a < this.swarm.size(); ++a) {
            if (this.swarm.get(a) == null || ((Entity)this.swarm.get(a)).isDead) {
               this.swarm.remove(a);
               break;
            }
         }

         if (this.swarm.size() < this.getSporeSize() / 3) {
            this.swarm.add(Thaumcraft.proxy.swarmParticleFX(this.world, this, 0.1F, 10.0F, 0.0F));
         }
      }

      int x = MathHelper.floor(this.posX);
      int y = MathHelper.floor(this.getEntityBoundingBox().minY) - 1;
      int z = MathHelper.floor(this.posZ);
      if (this.world.getBlockState(new BlockPos(x, y, z)).getBlock() == ConfigBlocks.blockTaintFibres) {
         if (this.deathTime > 0) {
            this.spiderBurst();
         }
      } else {
         this.spiderBurst();
      }
   }

   public void onCollideWithPlayer(EntityPlayer par1EntityPlayer) {
      this.spiderBurst();
   }

   protected void spiderBurst() {
      if (!this.world.isRemote) {
         this.world.playSound(null, this.posX, this.posY, this.posZ,
               net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:gore")),
               SoundCategory.HOSTILE, 1.0F, 0.9F + this.world.rand.nextFloat() * 0.1F);
         int q = this.getSporeSize() / 3 + this.world.rand.nextInt(this.getSporeSize() / 2 + 1);

         for (int a = 0; a < q; ++a) {
            EntityTaintSpider spiderling = new EntityTaintSpider(this.world);
            spiderling.setLocationAndAngles(
               this.posX + (double)this.world.rand.nextFloat() - (double)this.world.rand.nextFloat(),
               this.posY + (double)this.world.rand.nextFloat(),
               this.posZ + (double)this.world.rand.nextFloat() - (double)this.world.rand.nextFloat(),
               this.world.rand.nextFloat() * 360.0F, 0.0F);
            this.world.spawnEntity(spiderling);
         }

         int x = MathHelper.floor(this.posX);
         int y = MathHelper.floor(this.getEntityBoundingBox().minY) - 1;
         int z = MathHelper.floor(this.posZ);
         if (this.world.getBlockState(new BlockPos(x, y, z)).getBlock() == ConfigBlocks.blockTaintFibres) {
            // world.setBlockMetadataWithNotify(x, y, z, 3, 3);
         }

         this.setDead();
      } else {
         this.sploosh(50);
      }
   }

   protected void sploosh(int amt) {
      for (int a = 0; a < amt; ++a) {
         Thaumcraft.proxy.splooshFX(this);
      }
   }

   public void writeSpawnData(ByteBuf data) {
      data.writeFloat((float)this.getSporeSize());
   }

   public void readSpawnData(ByteBuf data) {
      try {
         this.displaySize = data.readFloat();
      } catch (Exception ignored) {
      }
   }

   protected float getSoundVolume() {
      return 0.1F;
   }

   public int getTalkInterval() {
      return 200;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return null;
   }

   protected Item getDropItem() {
      return ConfigItems.itemResource;
   }

   protected void dropFewItems(boolean flag, int i) {
      if (this.world.rand.nextBoolean()) {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 11), this.height / 2.0F);
      } else {
         this.entityDropItem(new ItemStack(ConfigItems.itemResource, 1, 12), this.height / 2.0F);
      }
   }
}
