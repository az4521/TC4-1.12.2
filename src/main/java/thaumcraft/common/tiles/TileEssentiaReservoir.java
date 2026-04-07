package thaumcraft.common.tiles;

import java.awt.Color;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.wands.IWandable;
import net.minecraft.util.math.BlockPos;

public class TileEssentiaReservoir extends TileThaumcraft implements IAspectSource, IWandable, IEssentiaTransport {
   public AspectList essentia = new AspectList();
   public int maxAmount = 256;
   public EnumFacing facing;
   int count;
   float tr;
   float tri;
   float tg;
   float tgi;
   float tb;
   float tbi;
   public float cr;
   public float cg;
   public float cb;
   public Aspect displayAspect;

   public TileEssentiaReservoir() {
      this.facing = EnumFacing.DOWN;
      this.count = 0;
      this.tr = 1.0F;
      this.tri = 0.0F;
      this.tg = 1.0F;
      this.tgi = 0.0F;
      this.tb = 1.0F;
      this.tbi = 0.0F;
      this.cr = 1.0F;
      this.cg = 1.0F;
      this.cb = 1.0F;
      this.displayAspect = null;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.essentia.readFromNBT(nbttagcompound);
      if (this.essentia.visSize() > this.maxAmount) {
         this.essentia = new AspectList();
      }

      this.facing = EnumFacing.byIndex(nbttagcompound.getByte("face"));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      this.essentia.writeToNBT(nbttagcompound);
      nbttagcompound.setByte("face", (byte)this.facing.ordinal());
   }

   public AspectList getAspects() {
      return this.essentia;
   }

   public void setAspects(AspectList aspects) {
      this.essentia = aspects.copy();
   }

   public int addToContainer(Aspect tt, int am) {
       if (am != 0) {
           int space = this.maxAmount - this.essentia.visSize();
           if (space >= am) {
               this.essentia.add(tt, am);
               am = 0;
           } else {
               this.essentia.add(tt, space);
               am -= space;
           }

           if (space > 0) {
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
               this.markDirty();
           }

       }
       return am;
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.essentia.getAmount(tt) >= am) {
         this.essentia.remove(tt, am);
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tag, int amt) {
      return this.essentia.getAmount(tag) >= amt;
   }

   public boolean doesContainerContain(AspectList ot) {
      for(Aspect tt : ot.getAspects()) {
         if (this.essentia.getAmount(tt) < ot.getAmount(tt)) {
            return false;
         }
      }

      return true;
   }

   public int containerContains(Aspect tag) {
      return this.essentia.getAmount(tag);
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }

   public boolean isConnectable(EnumFacing face) {
      return face == this.facing;
   }

   public boolean canInputFrom(EnumFacing face) {
      return face == this.facing;
   }

   public boolean canOutputTo(EnumFacing face) {
      return face == this.facing;
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public int getMinimumSuction() {
      return 24;
   }

   public Aspect getSuctionType(EnumFacing loc) {
      return null;
   }

   public int getSuctionAmount(EnumFacing loc) {
      return this.essentia.visSize() < this.maxAmount ? 24 : 0;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return this.essentia.visSize() > 0 && loc == null ? this.essentia.getAspects()[0] : null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.essentia.visSize();
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
   }

   public void updateEntity() {
            ++this.count;
      if (!this.world.isRemote && this.count % 5 == 0 && this.essentia.visSize() < this.maxAmount) {
         this.fillReservoir();
      }

      if (this.world.isRemote) {
         int vs = this.essentia.visSize();
         if (vs > 0) {
            if (this.world.rand.nextInt(500 - vs) == 0) {
               this.world.playSound(null, this.getPos(), thaumcraft.common.lib.SoundsTC.get("thaumcraft:creak"), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.4F + this.world.rand.nextFloat() * 0.2F);
            }

            if (this.count % 20 == 0 && this.essentia.size() > 0) {
               this.displayAspect = this.essentia.getAspects()[this.count / 20 % this.essentia.size()];
               Color c = new Color(this.displayAspect.getColor());
               this.tr = (float)c.getRed() / 255.0F;
               this.tg = (float)c.getGreen() / 255.0F;
               this.tb = (float)c.getBlue() / 255.0F;
               this.tri = (this.cr - this.tr) / 20.0F;
               this.tgi = (this.cg - this.tg) / 20.0F;
               this.tbi = (this.cb - this.tb) / 20.0F;
            }

            if (this.displayAspect == null) {
               this.tr = this.tg = this.tb = 1.0F;
               this.tri = this.tgi = this.tbi = 0.0F;
            } else {
               this.cr -= this.tri;
               this.cg -= this.tgi;
               this.cb -= this.tbi;
            }
         }
      }

   }

   void fillReservoir() {
      TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.facing);
      if (te != null) {
         IEssentiaTransport ic = (IEssentiaTransport)te;
         if (!ic.canOutputTo(this.facing.getOpposite())) {
            return;
         }

         Aspect ta = null;
         if (ic.getEssentiaAmount(this.facing.getOpposite()) > 0 && ic.getSuctionAmount(this.facing.getOpposite()) < this.getSuctionAmount(this.facing) && this.getSuctionAmount(this.facing) >= ic.getMinimumSuction()) {
            ta = ic.getEssentiaType(this.facing.getOpposite());
         }

         if (ta != null && ic.getSuctionAmount(this.facing.getOpposite()) < this.getSuctionAmount(this.facing)) {
            this.addToContainer(ta, ic.takeEssentia(ta, 1, this.facing.getOpposite()));
         }
      }

   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      if (player.isSneaking()) {
         this.facing = EnumFacing.byIndex(side);
      } else {
         this.facing = EnumFacing.byIndex(side).getOpposite();
      }

      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
      this.markDirty();
      return 0;
   }

   public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
      return null;
   }

   public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
   }

   public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
   }
}
