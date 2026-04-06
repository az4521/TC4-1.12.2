package thaumcraft.common.tiles;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileJarBrain extends TileJar {
   public float field_40063_b;
   public float field_40061_d;
   public float field_40059_f;
   public float field_40066_q;
   public float rota;
   public float rotb;
   public int xp = 0;
   public int xpMax = 2000;
   public int eatDelay = 0;
   long lastsigh = System.currentTimeMillis() + 1500L;

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.xp = nbttagcompound.getInteger("XP");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("XP", this.xp);
   }

   public void updateEntity() {
            Entity entity = null;
      if (this.xp > this.xpMax) {
         this.xp = this.xpMax;
      }

      if (this.xp < this.xpMax) {
         entity = this.getClosestXPOrb();
         if (entity != null && this.eatDelay == 0) {
            double var3 = ((double)this.getPos().getX() + (double)0.5F - entity.posX) / (double)7.0F;
            double var5 = ((double)this.getPos().getY() + (double)0.5F - entity.posY) / (double)7.0F;
            double var7 = ((double)this.getPos().getZ() + (double)0.5F - entity.posZ) / (double)7.0F;
            double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
            double var11 = (double)1.0F - var9;
            if (var11 > (double)0.0F) {
               var11 *= var11;
               entity.motionX += var3 / var9 * var11 * 0.15;
               entity.motionY += var5 / var9 * var11 * 0.33;
               entity.motionZ += var7 / var9 * var11 * 0.15;
            }
         }
      }

      if (this.world.isRemote) {
         this.rotb = this.rota;
         if (entity == null) {
            entity = this.world.getClosestPlayer((float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.5F, 6.0F, false);
            if (entity != null && this.lastsigh < System.currentTimeMillis()) {
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:brain")); if (_snd != null) this.world.playSound(null, this.getPos(), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.15F, 0.8F + this.world.rand.nextFloat() * 0.4F); }
               this.lastsigh = System.currentTimeMillis() + 5000L + (long)this.world.rand.nextInt(25000);
            }
         }

         if (entity != null) {
            double d = entity.posX - (double)((float)this.getPos().getX() + 0.5F);
            double d1 = entity.posZ - (double)((float)this.getPos().getZ() + 0.5F);
            this.field_40066_q = (float)Math.atan2(d1, d);
            this.field_40059_f += 0.1F;
            if (this.field_40059_f < 0.5F || rand.nextInt(40) == 0) {
               float f3 = this.field_40061_d;

               do {
                  this.field_40061_d += (float)(rand.nextInt(4) - rand.nextInt(4));
               } while(f3 == this.field_40061_d);
            }
         } else {
            this.field_40066_q += 0.01F;
         }

         while(this.rota >= 3.141593F) {
            this.rota -= 6.283185F;
         }

         while(this.rota < -3.141593F) {
            this.rota += 6.283185F;
         }

         while(this.field_40066_q >= 3.141593F) {
            this.field_40066_q -= 6.283185F;
         }

         while(this.field_40066_q < -3.141593F) {
            this.field_40066_q += 6.283185F;
         }

         float f;
         for(f = this.field_40066_q - this.rota; f >= 3.141593F; f -= 6.283185F) {
         }

         while(f < -3.141593F) {
            f += 6.283185F;
         }

         this.rota += f * 0.04F;
      }

      if (this.eatDelay > 0) {
         --this.eatDelay;
      } else if (this.xp < this.xpMax) {
         List ents = this.world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB((double)this.getPos().getX() - 0.1, (double)this.getPos().getY() - 0.1, (double)this.getPos().getZ() - 0.1, (double)this.getPos().getX() + 1.1, (double)this.getPos().getY() + 1.1, (double)this.getPos().getZ() + 1.1));
         if (!ents.isEmpty()) {
            for(Object ent : ents) {
               EntityXPOrb eo = (EntityXPOrb)ent;
               this.xp += eo.getXpValue();
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("entity.generic.eat")); if (_snd != null) this.world.playSound(null, eo.getPosition(), _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.1F, (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F + 1.0F); }
               eo.setDead();
            }

            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
         }
      }

   }

   public Entity getClosestXPOrb() {
      double cdist = Double.MAX_VALUE;
      Entity orb = null;
      List ents = this.world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(6.0F, 6.0F, 6.0F));
      if (!ents.isEmpty()) {
         for(Object ent : ents) {
            EntityXPOrb eo = (EntityXPOrb)ent;
            double d = this.getDistanceTo(eo.posX, eo.posY, eo.posZ);
            if (d < cdist) {
               orb = eo;
               cdist = d;
            }
         }
      }

      return orb;
   }

   public double getDistanceTo(double par1, double par3, double par5) {
      double var7 = (double)this.getPos().getX() + (double)0.5F - par1;
      double var9 = (double)this.getPos().getY() + (double)0.5F - par3;
      double var11 = (double)this.getPos().getZ() + (double)0.5F - par5;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }
}
