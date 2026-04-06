package thaumcraft.common.entities.golems;

import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityGolemBobber extends Entity implements IEntityAdditionalSpawnData {
   private int xTile = -1;
   private int yTile = -1;
   private int zTile = -1;
   private int inTile = 0;
   private int inData = 0;
   private boolean inGround = false;
   private boolean inBlock = false;
   public EntityGolemBase fisher = null;
   private int ticksCatchable;
   private int ticksCaughtDelay;
   private float fishApproachAngle;

   public void writeSpawnData(ByteBuf data) {
      data.writeDouble(this.motionX);
      data.writeDouble(this.motionY);
      data.writeDouble(this.motionZ);
      data.writeInt(this.fisher != null ? this.fisher.getEntityId() : -1);
   }

   public void readSpawnData(ByteBuf data) {
      this.motionX = data.readDouble();
      this.motionY = data.readDouble();
      this.motionZ = data.readDouble();
      int fid = data.readInt();
      if (fid >= 0) {
         this.fisher = (EntityGolemBase)this.world.getEntityByID(fid);
      }
   }

   public EntityGolemBobber(World par1World) {
      super(par1World);
      this.setSize(0.25F, 0.25F);
      this.ignoreFrustumCheck = true;
      this.motionX = 0.0F;
      this.motionY = 0.0F;
      this.motionZ = 0.0F;
   }

   public EntityGolemBobber(World par1World, EntityGolemBase par2EntityLiving, int x, int y, int z) {
      super(par1World);
      this.setSize(0.25F, 0.25F);
      this.fisher = par2EntityLiving;
      this.ignoreFrustumCheck = true;
      double d1 = (double)x + (double)0.5F - this.fisher.posX;
      double d3 = (double)(y + 1) - this.fisher.posY;
      double d5 = (double)z + (double)0.5F - this.fisher.posZ;
      double d7 = MathHelper.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
      double d9 = 0.1;
      this.motionX = d1 * d9;
      this.motionY = d3 * d9 + (double)MathHelper.sqrt(d7) * 0.08;
      this.motionZ = d5 * d9;
      this.setPosition(this.fisher.posX, this.fisher.posY, this.fisher.posZ);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double par1) {
      double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * (double)4.0F;
      d1 *= 64.0F;
      return par1 < d1 * d1;
   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote) {
         if (this.fisher == null || !this.fisher.isEntityAlive()) {
            this.setDead();
            return;
         }

         if (this.rand.nextFloat() < 0.02F && this.world instanceof WorldServer) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.WATER_SPLASH,
               this.posX + (double)this.rand.nextFloat() - (double)this.rand.nextFloat(),
               this.posY + (double)this.rand.nextFloat(),
               this.posZ + (double)this.rand.nextFloat() - (double)this.rand.nextFloat(),
               2 + this.rand.nextInt(2), 0.1, 0.0, 0.1, 0.0);
         }
      }

      if (this.ticksExisted++ > 4000) {
         this.setDead();
      } else {
         if (this.inBlock) {
            this.inBlock = false;
            this.motionX *= this.rand.nextFloat() * 0.2F;
            this.motionY *= this.rand.nextFloat() * 0.2F;
            this.motionZ *= this.rand.nextFloat() * 0.2F;
         }

         Vec3d vec31 = new Vec3d(this.posX, this.posY, this.posZ);
         Vec3d vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec31, vec3);
         vec31 = new Vec3d(this.posX, this.posY, this.posZ);
         vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         if (movingobjectposition != null) {
            vec3 = new Vec3d(movingobjectposition.hitVec.x, movingobjectposition.hitVec.y, movingobjectposition.hitVec.z);
            if (movingobjectposition.entityHit == null) {
               this.inBlock = true;
               BlockPos hitPos = movingobjectposition.getBlockPos();
               if (this.world.getBlockState(hitPos).getBlock().getMaterial(this.world.getBlockState(hitPos)) != Material.WATER) {
                  this.setDead();
               }
            }
         }

         if (!this.inBlock) {
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            float f5 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * (double)180.0F / Math.PI);

            for (this.rotationPitch = (float)(Math.atan2(this.motionY, f5) * (double)180.0F / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            }
            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
               this.prevRotationPitch += 360.0F;
            }
            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
               this.prevRotationYaw -= 360.0F;
            }
            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
               this.prevRotationYaw += 360.0F;
            }
            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            float f6 = 0.92F;
            if (this.onGround || this.collidedHorizontally) {
               f6 = 0.5F;
            }

            byte b0 = 5;
            double d10 = 0.0F;
            AxisAlignedBB bb = this.getEntityBoundingBox();
            for (int j = 0; j < b0; ++j) {
               double d3 = bb.minY + (bb.maxY - bb.minY) * (double)(j) / (double)b0 - (double)0.125F + (double)0.125F;
               double d4 = bb.minY + (bb.maxY - bb.minY) * (double)(j + 1) / (double)b0 - (double)0.125F + (double)0.125F;
               AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(bb.minX, d3, bb.minZ, bb.maxX, d4, bb.maxZ);
               if (this.world.isMaterialInBB(axisalignedbb1, Material.WATER)) {
                  d10 += (double)1.0F / (double)b0;
               }
            }

            if (!this.world.isRemote && d10 > (double)0.0F) {
               WorldServer worldserver = (WorldServer)this.world;
               int k = 1;
               if (this.rand.nextFloat() < 0.25F && this.world.canSeeSky(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY) + 1, MathHelper.floor(this.posZ)))) {
                  k = 2;
               }

               if (this.rand.nextFloat() < 0.5F && !this.world.canBlockSeeSky(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY) + 1, MathHelper.floor(this.posZ)))) {
                  --k;
               }

               if (this.ticksCatchable > 0) {
                  --this.ticksCatchable;
                  if (this.ticksCatchable <= 0) {
                     this.ticksCaughtDelay = 0;
                  }
               } else if (this.ticksCaughtDelay > 0) {
                  this.ticksCaughtDelay -= k;
                  float f1 = 0.15F;
                  if (this.ticksCaughtDelay < 20) {
                     f1 = (float)((double)f1 + (double)(20 - this.ticksCaughtDelay) * 0.05);
                  } else if (this.ticksCaughtDelay < 40) {
                     f1 = (float)((double)f1 + (double)(40 - this.ticksCaughtDelay) * 0.02);
                  } else if (this.ticksCaughtDelay < 60) {
                     f1 = (float)((double)f1 + (double)(60 - this.ticksCaughtDelay) * 0.01);
                  }

                  if (this.rand.nextFloat() < f1) {
                     float f7 = (this.rand.nextFloat() * 360.0F) * ((float)Math.PI / 180F);
                     float f2 = 25.0F + this.rand.nextFloat() * 35.0F;
                     double d11 = this.posX + (double)(MathHelper.sin(f7) * f2 * 0.1F);
                     double d5 = (float)MathHelper.floor(bb.minY) + 1.0F;
                     double d6 = this.posZ + (double)(MathHelper.cos(f7) * f2 * 0.1F);
                     worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, d11, d5, d6, 2 + this.rand.nextInt(2), 0.1, 0.0, 0.1, 0.0);
                  }

                  if (this.ticksCaughtDelay <= 0) {
                     this.fishApproachAngle = (this.rand.nextFloat() * 360.0F);
                  }
               }

               if (this.ticksCatchable > 0) {
                  this.motionY -= (double)(this.rand.nextFloat() * this.rand.nextFloat() * this.rand.nextFloat()) * 0.2;
               }
            }

            double d2 = d10 * (double)2.0F - (double)1.0F;
            this.motionY += (double)0.04F * d2;
            if (d10 > (double)0.0F) {
               f6 = (float)((double)f6 * 0.9);
               this.motionY *= 0.8;
            }

            this.motionX *= f6;
            this.motionY *= f6;
            this.motionZ *= f6;
            this.setPosition(this.posX, this.posY, this.posZ);
         }
      }
   }

   protected void entityInit() {
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
   }
}
