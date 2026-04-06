package thaumcraft.common.entities.golems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.events.EventHandlerEntity;
import thaumcraft.common.lib.utils.InventoryUtils;

public class EntityTravelingTrunk extends EntityLiving implements IEntityOwnable {

   // DataManager parameters (matching original DW IDs by assignment order):
   // id 15 -> DW_UPGRADE (byte)
   // id 16 -> DW_STAY (byte)
   // id 17 -> DW_OWNER (String)
   // id 18 -> DW_ANGER (Integer)
   // id 19 -> DW_OPEN (byte)
   // id 20 -> DW_ROWS (Integer via VARINT)
   private static final DataParameter<Byte>    DW_UPGRADE = EntityDataManager.createKey(EntityTravelingTrunk.class, DataSerializers.BYTE);
   private static final DataParameter<Byte>    DW_STAY    = EntityDataManager.createKey(EntityTravelingTrunk.class, DataSerializers.BYTE);
   private static final DataParameter<String>  DW_OWNER   = EntityDataManager.createKey(EntityTravelingTrunk.class, DataSerializers.STRING);
   private static final DataParameter<Integer> DW_ANGER   = EntityDataManager.createKey(EntityTravelingTrunk.class, DataSerializers.VARINT);
   private static final DataParameter<Byte>    DW_OPEN    = EntityDataManager.createKey(EntityTravelingTrunk.class, DataSerializers.BYTE);
   private static final DataParameter<Integer> DW_ROWS    = EntityDataManager.createKey(EntityTravelingTrunk.class, DataSerializers.VARINT);

   public int slotCount = 27;
   public InventoryTrunk inventory;
   public float lidrot;
   public float field_768_a;
   public float field_767_b;
   private int jumpDelay;
   private int eatDelay;
   private int attackTime;
   private float trunkMoveForward;
   private float trunkMoveStrafing;
   private boolean trunkJump;

   public EntityTravelingTrunk(World world) {
      super(world);
      this.inventory = new InventoryTrunk(this, this.slotCount);
      this.eatDelay = 0;
      this.jumpDelay = 0;
      this.noClip = false;
      this.jumpDelay = this.rand.nextInt(20) + 10;
      this.isImmuneToFire = true;
      this.lidrot = 0.0F;
      this.enablePersistence();
      this.setSize(0.8F, 0.8F);
      // Register AI task so movement runs inside updateEntityActionState()
      // (moveForward/moveStrafing get reset before travel(), so we must set them via AI tasks)
      this.tasks.addTask(1, new net.minecraft.entity.ai.EntityAIBase() {
         public boolean shouldExecute() { return true; }
         public boolean shouldContinueExecuting() { return true; }
         public void updateTask() { updateTrunkAI(); }
      });
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(75.0F);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0F);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(DW_UPGRADE, (byte) 0);
      this.dataManager.register(DW_STAY,    (byte) 0);
      this.dataManager.register(DW_OWNER,   "");
      this.dataManager.register(DW_ANGER,   0);
      this.dataManager.register(DW_OPEN,    (byte) 0);
      this.dataManager.register(DW_ROWS,    0);
   }

   public boolean attackEntityFrom(DamageSource ds, float par2) {
      if (ds == DamageSource.CACTUS) {
         return false;
      } else {
         return this.getUpgrade() != 3 && super.attackEntityFrom(ds, par2);
      }
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setBoolean("Stay", this.getStay());
      nbttagcompound.setByte("upgrade", (byte)this.getUpgrade());
      String ownerName = this.getOwnerName();
      nbttagcompound.setString("Owner", ownerName == null ? "" : ownerName);
      if (this.ownerUUID != null) {
         nbttagcompound.setString("OwnerUUID", this.ownerUUID.toString());
      }
      nbttagcompound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.setStay(nbttagcompound.getBoolean("Stay"));
      this.setUpgrade(nbttagcompound.getByte("upgrade"));
      String s = nbttagcompound.getString("Owner");
      if (!s.isEmpty()) {
         this.setOwner(s);
      }
      if (nbttagcompound.hasKey("OwnerUUID")) {
         try { this.ownerUUID = java.util.UUID.fromString(nbttagcompound.getString("OwnerUUID")); }
         catch (IllegalArgumentException ignored) {}
      }

      NBTTagList nbttaglist = nbttagcompound.getTagList("Inventory", 10);
      this.inventory.readFromNBT(nbttaglist);
      this.setInvSize();
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (this.getUpgrade() == 5) {
         this.pullItems();
      }

   }

   public void onUpdate() {
      super.onUpdate();
      if (this.inWater) {
         this.motionY += 0.033F;
      }

      if (this.world.isRemote) {
         if (!this.onGround && this.motionY < (double)0.0F && !this.inWater) {
            this.lidrot += 0.015F;
         }

         if ((this.onGround || this.inWater) && !this.isOpen()) {
            this.lidrot -= 0.1F;
            if (this.lidrot < 0.0F) {
               this.lidrot = 0.0F;
            }
         }

         if (this.isOpen()) {
            this.lidrot += 0.035F;
         }

         if (this.lidrot > (this.isOpen() ? 0.5F : 0.2F)) {
            this.lidrot = this.isOpen() ? 0.5F : 0.2F;
         }
      } else if (this.getHealth() < this.getMaxHealth() && (this.getUpgrade() == 3 || this.ticksExisted % 50 == 0)) {
         this.heal(1.0F);
      }

   }

   public void updateTrunkAI() {
      if (this.getAnger() > 0) {
         this.setAnger(this.getAnger() - 1);
      }

      if (this.eatDelay > 0) {
         --this.eatDelay;
      }

      this.fallDistance = 0.0F;
      if (this.getOwner() != null) {
         if (!this.world.isRemote) {
            ArrayList<WeakReference<Entity>> ll = EventHandlerEntity.linkedEntities.get(this.getOwner().getName());
            if (ll == null) {
               ll = new ArrayList<>();
            }

            boolean add = true;

            for (WeakReference trunk : ll) {
               if (trunk.get() != null && ((Entity)trunk.get()).getEntityId() == this.getEntityId()) {
                  add = false;
                  break;
               }
            }

            if (add) {
               ll.add(new WeakReference(this));
               EventHandlerEntity.linkedEntities.put(this.getOwner().getName(), ll);
            }
         }

         if (!this.getStay() && this.getOwner() != null && this.getDistance(this.getOwner()) > 20.0F) {
            int i = MathHelper.floor(this.getOwner().posX) - 2;
            int j = MathHelper.floor(this.getOwner().posZ) - 2;
            int k = MathHelper.floor(this.getOwner().getEntityBoundingBox().minY);

            for (int l = 0; l <= 4; ++l) {
               for (int i1 = 0; i1 <= 4; ++i1) {
                  if ((l < 1 || i1 < 1 || l > 3 || i1 > 3)
                        && (this.world.getBlockState(new BlockPos(i + l, k - 1, j + i1)).isNormalCube()
                              || this.world.getBlockState(new BlockPos(i + l, k - 1, j + i1)).getBlock().getDefaultState().getMaterial() == Material.WATER)
                        && !this.world.getBlockState(new BlockPos(i + l, k, j + i1)).isNormalCube()
                        && !this.world.getBlockState(new BlockPos(i + l, k + 1, j + i1)).isNormalCube()) {
                     {
                        SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:entity.endermen.teleport"));
                        if (_snd != null) this.world.playSound(null, new BlockPos(i + l, k, j + i1), _snd, SoundCategory.NEUTRAL, 0.5F, 1.0F);
                     }
                     this.setLocationAndAngles((float)(i + l) + 0.5F, k, (float)(j + i1) + 0.5F, this.rotationYaw, this.rotationPitch);
                     this.setAttackTarget(null);
                     return;
                  }
               }
            }
         }

         if (this.getAttackTarget() != null && this.getAttackTarget().isDead) {
            this.setAttackTarget(null);
            this.setAnger(5);
         }

         if (!this.getStay() && this.getUpgrade() == 2 && this.getAnger() == 0 && this.getAttackTarget() == null
               && this.getOwner() != null
               && this.getOwner() instanceof EntityLiving
               && ((EntityLiving) this.getOwner()).getAttackTarget() != null
               && !((EntityLiving) this.getOwner()).getAttackTarget().isDead
               && ((EntityLiving) this.getOwner()).getAttackTarget() instanceof EntityLivingBase
               && this.canEntityBeSeen(((EntityLiving) this.getOwner()).getAttackTarget())) {
            this.setAnger(600);
            this.setAttackTarget(((EntityLiving) this.getOwner()).getAttackTarget());
         }

         boolean move = false;
         if (this.getAnger() > 0 && this.getAttackTarget() != null && !this.getAttackTarget().isDead
               && this.getAttackTarget() != this.getOwner()) {
            this.faceEntity(this.getAttackTarget(), 10.0F, 20.0F);
            move = true;
            if (this.attackTime <= 0
                  && (double)this.getDistance(this.getAttackTarget()) < (double)1.5F
                  && this.getAttackTarget().getEntityBoundingBox().maxY > this.getEntityBoundingBox().minY
                  && this.getAttackTarget().getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
               this.attackTime = 10 + this.world.rand.nextInt(5);
               this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), 4.0F);
               this.world.setEntityState(this, (byte)17);
               {
                  SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:entity.blaze.hurt"));
                  if (_snd != null) this.playSound(_snd, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
               }
            }
         }

         if (this.getOwner() != null && this.getDistance(this.getOwner()) > 5.0F && this.getAnger() == 0 && !this.getStay()) {
            this.faceEntity(this.getOwner(), 10.0F, 20.0F);
            move = true;
         }

         if ((this.onGround || this.inWater) && this.jumpDelay-- <= 0 && move) {
            boolean fast = this.getUpgrade() == 0;
            this.jumpDelay = this.rand.nextInt(10) + 5;
            this.jumpDelay /= 3;
            this.field_768_a = 1.0F;
            // Direct motion like slimes — bypasses MoveHelper/JumpHelper
            this.motionY = 0.42;
            float speed = fast ? 0.4F : 0.3F;
            if (this.inWater) speed *= 0.75F;
            float yaw = this.rotationYaw * 0.017453292F;
            this.motionX += -Math.sin(yaw) * speed;
            this.motionZ += Math.cos(yaw) * speed;
            this.isAirBorne = true;
            {
               SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:block.chest.close"));
               if (_snd != null) this.playSound(_snd, 0.1F, this.world.rand.nextFloat() * 0.1F + 0.9F);
            }
         }
      }

   }

   protected boolean canDespawn() {
      return false;
   }

   public void onCollideWithPlayer(EntityPlayer entityplayer) {
   }

   protected SoundEvent getHurtSound(DamageSource ds) {
      return SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:block.wood.hit"));
   }

   protected SoundEvent getDeathSound() {
      return SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:entity.item_frame.break"));
   }

   protected Item getDropItem() {
       return super.getDropItem();
   }

   protected float getSoundVolume() {
      return 0.5F;
   }

   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData par1EntityLivingData) {
      this.setInvSize();
      return super.onInitialSpawn(difficulty, par1EntityLivingData);
   }

   public void setInvSize() {
      this.setRows(this.getUpgrade() == 1 ? 4 : 3);
      this.slotCount = this.getRows() * 9;
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (player.isSneaking()) {
         return false;
      } else {
         ItemStack itemstack = player.inventory.getCurrentItem();
         if (!itemstack.isEmpty() && itemstack.getItem() == ConfigItems.itemGolemBell) {
            return this.getUpgrade() == 3 && !this.getOwnerName().equals(player.getName());
         } else if (this.getUpgrade() == -1 && !itemstack.isEmpty() && itemstack.getItem() == ConfigItems.itemGolemUpgrade) {
            this.setUpgrade(itemstack.getItemDamage());
            this.setInvSize();
            itemstack.shrink(1);
            if (itemstack.isEmpty()) {
               player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            }

            {
               SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:upgrade"));
               if (_snd != null) this.playSound(_snd, 0.5F, 1.0F);
            }
            player.swingArm(EnumHand.MAIN_HAND);
            return true;
         } else if (!itemstack.isEmpty() && itemstack.getItem() instanceof ItemFood && this.getHealth() < this.getMaxHealth()) {
            ItemFood itemfood = (ItemFood)itemstack.getItem();
            itemstack.shrink(1);
            this.heal((float)itemfood.getHealAmount(itemstack));
            if (this.getHealth() == this.getMaxHealth()) {
               {
                  SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:entity.player.burp"));
                  if (_snd != null) this.playSound(_snd, 0.5F, this.world.rand.nextFloat() * 0.5F + 0.5F);
               }
            } else {
               {
                  SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:entity.generic.eat"));
                  if (_snd != null) this.playSound(_snd, 0.5F, this.world.rand.nextFloat() * 0.5F + 0.5F);
               }
            }

            this.world.setEntityState(this, (byte)18);
            this.lidrot = 0.15F;
            if (itemstack.isEmpty()) {
               player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            }

            return true;
         } else if (!this.world.isRemote) {
            if (this.getUpgrade() == 3 && !this.getOwnerName().equals(player.getName())) {
               return true;
            } else {
               player.openGui(Thaumcraft.instance, 2, this.world, this.getEntityId(), 0, 0);
               return false;
            }
         } else {
            return true;
         }
      }
   }

   void showHeartsOrSmokeFX(boolean flag) {
      EnumParticleTypes particleType = flag ? EnumParticleTypes.HEART : EnumParticleTypes.EXPLOSION_NORMAL;
      int amount = flag ? 1 : 7;

      for (int i = 0; i < amount; ++i) {
         double d = this.rand.nextGaussian() * 0.02;
         double d1 = this.rand.nextGaussian() * 0.02;
         double d2 = this.rand.nextGaussian() * 0.02;
         this.world.spawnParticle(particleType,
               this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width,
               this.posY + (double)0.5F + (double)(this.rand.nextFloat() * this.height),
               this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width,
               d, d1, d2);
      }

   }

   private void pullItems() {
      if (!this.isDead && !(this.getHealth() <= 0.0F)) {
         List list = null;
         if (!this.world.isRemote) {
            list = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
                  this.posX - (double)0.5F, this.posY - (double)0.5F, this.posZ - (double)0.5F,
                  this.posX + (double)0.5F, this.posY + (double)0.5F, this.posZ + (double)0.5F));

            for (Object o : list) {
               Entity entity = (Entity) o;
               if (entity instanceof EntityItem) {
                  ItemStack stack = ((EntityItem) entity).getItem().copy();
                  ItemStack outstack = InventoryUtils.placeItemStackIntoInventory(stack, this.inventory, 0, true);
                  if (outstack == null || outstack.getCount() != stack.getCount()) {
                     {
                        SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:entity.generic.eat"));
                        if (_snd != null) this.playSound(_snd, 0.5F, this.world.rand.nextFloat() * 0.5F + 0.5F);
                     }
                     this.world.setEntityState(this, (byte) 17);
                     if (outstack != null && outstack.getCount() >= 0) {
                        ((EntityItem) entity).setItem(outstack);
                     } else {
                        entity.setDead();
                     }
                  }
               }
            }
         }

         list = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
               this.posX - (double)3.0F, this.posY - (double)3.0F, this.posZ - (double)3.0F,
               this.posX + (double)3.0F, this.posY + (double)3.0F, this.posZ + (double)3.0F));

         for (Object o : list) {
            Entity entity = (Entity) o;
            if (entity instanceof EntityItem) {
               double d6 = entity.posX - this.posX;
               double d8 = entity.posY - this.posY + (double)(this.height * 0.8F);
               double d10 = entity.posZ - this.posZ;
               double d11 = MathHelper.sqrt(d6 * d6 + d8 * d8 + d10 * d10);
               d6 /= d11;
               d8 /= d11;
               d10 /= d11;
               double d13 = 0.075;
               entity.motionX -= d6 * d13;
               entity.motionY -= d8 * d13;
               entity.motionZ -= d10 * d13;
            }
         }

      }
   }

   public void onDeath(DamageSource par1DamageSource) {
      if (!this.world.isRemote) {
         this.inventory.dropAllItems();
      }

      super.onDeath(par1DamageSource);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   // --- DataManager getters/setters ---

   public int getUpgrade() {
      return this.dataManager.get(DW_UPGRADE);
   }

   public void setUpgrade(int upgrade) {
      this.dataManager.set(DW_UPGRADE, (byte) upgrade);
   }

   public int getRows() {
      return this.dataManager.get(DW_ROWS);
   }

   public void setRows(int rows) {
      this.dataManager.set(DW_ROWS, rows);
   }

   public int getAnger() {
      return this.dataManager.get(DW_ANGER);
   }

   public void setAnger(int anger) {
      this.dataManager.set(DW_ANGER, anger);
   }

   public boolean isOpen() {
      return this.dataManager.get(DW_OPEN) == 1;
   }

   public void setOpen(boolean par1) {
      this.dataManager.set(DW_OPEN, (byte)(par1 ? 1 : 0));
   }

   public boolean getStay() {
      return this.dataManager.get(DW_STAY) == 1;
   }

   public void setStay(boolean par1) {
      this.dataManager.set(DW_STAY, (byte)(par1 ? 1 : 0));
   }

   // --- IEntityOwnable implementation ---

   private java.util.UUID ownerUUID;

   public String getOwnerName() {
      return this.dataManager.get(DW_OWNER);
   }

   public void setOwner(String s) {
      this.dataManager.set(DW_OWNER, s);
   }

   public void setOwnerUUID(java.util.UUID uuid) {
      this.ownerUUID = uuid;
   }

   @Override
   public java.util.UUID getOwnerId() {
      return this.ownerUUID;
   }

   @Override
   public EntityLivingBase getOwner() {
      if (this.ownerUUID == null) return null;
      if (this.world instanceof WorldServer) {
         Entity e = ((WorldServer)this.world).getEntityFromUuid(this.ownerUUID);
         return e instanceof EntityLivingBase ? (EntityLivingBase) e : null;
      }
      // Client side fallback
      for (EntityPlayer p : this.world.playerEntities) {
         if (p.getUniqueID().equals(this.ownerUUID)) return p;
      }
      return null;
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte par1) {
      if (par1 == 17) {
         this.lidrot = 0.15F;
      } else if (par1 == 18) {
         this.lidrot = 0.15F;
         this.showHeartsOrSmokeFX(true);
      } else {
         super.handleStatusUpdate(par1);
      }

   }

   public Entity changeDimension(int par1) {
      if (this.getStay() || this.isDead || this.dimension == par1) {
         return this;
      }
      // If the owner is in the target dimension, teleport near them after the base transfer.
      // super.changeDimension handles NBT copy (copyDataFromOld is private to Entity).
      try {
         MinecraftServer minecraftserver = this.world.getMinecraftServer();
         WorldServer worldserver1 = minecraftserver != null ? minecraftserver.getWorld(par1) : null;
         Entity target = worldserver1 != null ? worldserver1.getPlayerEntityByName(this.getOwnerName()) : null;
         Entity result = super.changeDimension(par1);
         if (result != null && target != null) {
            result.setLocationAndAngles(target.posX, target.posY + 0.25, target.posZ, result.rotationYaw, result.rotationPitch);
         }
         return result;
      } catch (Exception e) {
         Thaumcraft.log.error("Error while teleporting traveling trunk to dimension {}", par1);
         e.printStackTrace();
         return this;
      }
   }
}
