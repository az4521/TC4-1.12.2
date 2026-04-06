package thaumcraft.common.entities;

import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockTaint;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class EntityFallingTaint extends Entity implements IEntityAdditionalSpawnData {
   public Block block;
   public int metadata;
   public int oldX;
   public int oldY;
   public int oldZ;
   public int fallTime = 0;
   private int fallHurtMax = 40;
   private float fallHurtAmount = 2.0F;

   public EntityFallingTaint(World par1World) {
      super(par1World);
   }

   public EntityFallingTaint(World par1World, double par2, double par4, double par6, Block par8, int par9, int ox, int oy, int oz) {
      super(par1World);
      this.block = par8;
      this.metadata = par9;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.98F);
      this.setPosition(par2, par4, par6);
      this.motionX = 0.0F;
      this.motionY = 0.0F;
      this.motionZ = 0.0F;
      this.prevPosX = par2;
      this.prevPosY = par4;
      this.prevPosZ = par6;
      this.oldX = ox;
      this.oldY = oy;
      this.oldZ = oz;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void entityInit() {
   }

   public void writeSpawnData(ByteBuf data) {
      data.writeInt(Block.getIdFromBlock(this.block));
      data.writeByte(this.metadata);
   }

   public void readSpawnData(ByteBuf data) {
      try {
         this.block = Block.getBlockById(data.readInt());
         this.metadata = data.readByte();
      } catch (Exception ignored) {
      }

   }

   public boolean canBeCollidedWith() {
      return !this.isDead;
   }

   public void onUpdate() {
      if (this.block != null && this.block != Blocks.AIR) {
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         ++this.fallTime;
         this.motionY -= 0.04F;
         this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         this.motionX *= 0.98F;
         this.motionY *= 0.98F;
         this.motionZ *= 0.98F;
         if (!this.world.isRemote) {
            int i = MathHelper.floor(this.posX);
            int j = MathHelper.floor(this.posY);
            int k = MathHelper.floor(this.posZ);
            if (this.fallTime == 1) {
               if (this.world.getBlockState(new BlockPos(this.oldX, this.oldY, this.oldZ)).getBlock() != this.block) {
                  this.setDead();
                  return;
               }

               this.world.setBlockToAir(new BlockPos(this.oldX, this.oldY, this.oldZ));
            }

            if (!this.onGround && this.world.getBlockState(new net.minecraft.util.math.BlockPos(i, j - 1, k)).getBlock() != ConfigBlocks.blockFluxGoo) {
               if (this.fallTime > 100 && !this.world.isRemote && (j < 1 || j > 256) || this.fallTime > 600) {
                  this.setDead();
               }
            } else {
               this.motionX *= 0.7F;
               this.motionZ *= 0.7F;
               this.motionY *= -0.5F;
               if (this.world.getBlockState(new BlockPos(i, j, k)).getBlock() != Blocks.PISTON && this.world.getBlockState(new BlockPos(i, j, k)).getBlock() != Blocks.PISTON_EXTENSION && this.world.getBlockState(new BlockPos(i, j, k)).getBlock() != Blocks.PISTON_HEAD) {
                  // { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:gore")); if (_snd != null) this.world.playSound(null, this.posX, this.posY, this.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F); };
                  this.setDead();
                  if (this.canPlace(i, j, k) && !BlockTaint.canFallBelow(this.world, i, j - 1, k) && this.world.setBlockState(new net.minecraft.util.math.BlockPos(i, j, k), this.block.getStateFromMeta(this.metadata), 3) && this.block instanceof BlockTaint) {
                     ((BlockTaint)this.block).onFinishFalling(this.world, i, j, k, this.metadata);
                  }
               }
            }
         } else if (this.onGround || this.fallTime == 1) {
            for(int j = 0; j < 10; ++j) {
               Thaumcraft.proxy.taintLandFX(this);
            }
         }
      } else {
         this.setDead();
      }

   }

   private boolean canPlace(int i, int j, int k) {
      net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(i, j, k);
      net.minecraft.block.state.IBlockState state = this.world.getBlockState(pos);
      return state.getBlock() == ConfigBlocks.blockTaintFibres || state.getBlock() == ConfigBlocks.blockFluxGoo || state.getMaterial().isReplaceable();
   }

   protected void fall(float par1) {
   }

   protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      par1NBTTagCompound.setInteger("TileID", Block.getIdFromBlock(this.block));
      par1NBTTagCompound.setByte("Data", (byte)this.metadata);
      par1NBTTagCompound.setByte("Time", (byte)this.fallTime);
      par1NBTTagCompound.setFloat("FallHurtAmount", this.fallHurtAmount);
      par1NBTTagCompound.setInteger("FallHurtMax", this.fallHurtMax);
      par1NBTTagCompound.setInteger("OldX", this.oldX);
      par1NBTTagCompound.setInteger("OldY", this.oldY);
      par1NBTTagCompound.setInteger("OldZ", this.oldZ);
   }

   protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      if (par1NBTTagCompound.hasKey("TileID")) {
         this.block = Block.getBlockById(par1NBTTagCompound.getInteger("TileID"));
      }

      this.metadata = par1NBTTagCompound.getByte("Data") & 255;
      this.fallTime = par1NBTTagCompound.getByte("Time") & 255;
      this.oldX = par1NBTTagCompound.getInteger("OldX");
      this.oldY = par1NBTTagCompound.getInteger("OldY");
      this.oldZ = par1NBTTagCompound.getInteger("OldZ");
      if (par1NBTTagCompound.hasKey("HurtEntities")) {
         this.fallHurtAmount = par1NBTTagCompound.getFloat("FallHurtAmount");
         this.fallHurtMax = par1NBTTagCompound.getInteger("FallHurtMax");
      }

      if (this.block == null) {
         this.block = Blocks.SAND;
      }

   }

   public void addEntityCrashInfo(CrashReportCategory par1CrashReportCategory) {
      super.addEntityCrashInfo(par1CrashReportCategory);
      par1CrashReportCategory.addCrashSection("Immitating block ID", Block.getIdFromBlock(this.block));
      par1CrashReportCategory.addCrashSection("Immitating block data", this.metadata);
   }

   @SideOnly(Side.CLIENT)
   public float getShadowSize() {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   public World getWorld() {
      return this.world;
   }

   @SideOnly(Side.CLIENT)
   public boolean canRenderOnFire() {
      return false;
   }
}
