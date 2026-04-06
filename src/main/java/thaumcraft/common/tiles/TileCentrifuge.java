package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import net.minecraft.util.math.BlockPos;

public class TileCentrifuge extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {
   public Aspect aspectOut = null;
   public Aspect aspectIn = null;
   public EnumFacing facing;
   int count;
   int process;
   float rotationSpeed;
   public float rotation;

   public TileCentrifuge() {
      this.facing = EnumFacing.NORTH;
      this.count = 0;
      this.process = 0;
      this.rotationSpeed = 0.0F;
      this.rotation = 0.0F;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.aspectIn = Aspect.getAspect(nbttagcompound.getString("aspectIn"));
      this.aspectOut = Aspect.getAspect(nbttagcompound.getString("aspectOut"));
      this.facing = EnumFacing.byIndex(nbttagcompound.getInteger("facing"));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      if (this.aspectIn != null) {
         nbttagcompound.setString("aspectIn", this.aspectIn.getTag());
      }

      if (this.aspectOut != null) {
         nbttagcompound.setString("aspectOut", this.aspectOut.getTag());
      }

      nbttagcompound.setInteger("facing", this.facing.ordinal());
   }

   public AspectList getAspects() {
      AspectList al = new AspectList();
      if (this.aspectOut != null) {
         al.add(this.aspectOut, 1);
      }

      return al;
   }

   public int addToContainer(Aspect tt, int am) {
      if (am > 0 && this.aspectOut == null) {
         this.aspectOut = tt;
         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         --am;
      }

      return am;
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.aspectOut != null && tt == this.aspectOut) {
         this.aspectOut = null;
         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         return true;
      } else {
         return false;
      }
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tag, int amt) {
      return amt == 1 && tag == this.aspectOut;
   }

   public boolean doesContainerContain(AspectList ot) {
      for(Aspect tt : ot.getAspects()) {
         if (tt == this.aspectOut) {
            return true;
         }
      }

      return false;
   }

   public int containerContains(Aspect tag) {
      return tag == this.aspectOut ? 1 : 0;
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }

   public boolean isConnectable(EnumFacing face) {
      return face == EnumFacing.UP || face == EnumFacing.DOWN;
   }

   public boolean canInputFrom(EnumFacing face) {
      return face == EnumFacing.DOWN;
   }

   public boolean canOutputTo(EnumFacing face) {
      return face == EnumFacing.UP;
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public int getMinimumSuction() {
      return 0;
   }

   public Aspect getSuctionType(EnumFacing face) {
      return null;
   }

   public int getSuctionAmount(EnumFacing face) {
      return face == EnumFacing.DOWN ? (this.gettingPower() ? 0 : (this.aspectIn == null ? 128 : 64)) : 0;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return this.aspectOut;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.aspectOut != null ? 1 : 0;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      if (this.aspectIn == null && !aspect.isPrimal()) {
         this.aspectIn = aspect;
         this.process = 39;
         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         return 1;
      } else {
         return 0;
      }
   }

   public void updateEntity() {
      if (!this.world.isRemote) {
         if (!this.gettingPower()) {
            if (this.aspectOut == null && this.aspectIn == null && ++this.count % 5 == 0) {
               this.drawEssentia();
            }

            if (this.process > 0) {
               --this.process;
            }

            if (this.aspectOut == null && this.aspectIn != null && this.process == 0) {
               this.processEssentia();
            }
         }
      } else {
         if (this.aspectIn != null && !this.gettingPower() && this.rotationSpeed < 20.0F) {
            this.rotationSpeed += 2.0F;
         }

         if ((this.aspectIn == null || this.gettingPower()) && this.rotationSpeed > 0.0F) {
            this.rotationSpeed -= 0.5F;
         }

         int pr = (int)this.rotation;
         this.rotation += this.rotationSpeed;
         if (this.rotation % 180.0F <= 20.0F && pr % 180 >= 160 && this.rotationSpeed > 0.0F) {
            this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "pump")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   void processEssentia() {
      Aspect[] comps = this.aspectIn.getComponents();
      this.aspectOut = comps[this.world.rand.nextInt(2)];
      this.aspectIn = null;
      this.markDirty();
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
   }

   void drawEssentia() {
      TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), EnumFacing.DOWN);
      if (te != null) {
         IEssentiaTransport ic = (IEssentiaTransport)te;
         if (!ic.canOutputTo(EnumFacing.UP)) {
            return;
         }

         Aspect ta = null;
         if (ic.getEssentiaAmount(EnumFacing.UP) > 0 && ic.getSuctionAmount(EnumFacing.UP) < this.getSuctionAmount(EnumFacing.DOWN) && this.getSuctionAmount(EnumFacing.DOWN) >= ic.getMinimumSuction()) {
            ta = ic.getEssentiaType(EnumFacing.UP);
         }

         if (ta != null && !ta.isPrimal() && ic.getSuctionAmount(EnumFacing.UP) < this.getSuctionAmount(EnumFacing.DOWN) && ic.takeEssentia(ta, 1, EnumFacing.UP) == 1) {
            this.aspectIn = ta;
            this.process = 39;
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         }
      }

   }

   public void setAspects(AspectList aspects) {
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY() - 1, this.getPos().getZ() - 1, this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
   }

   public boolean gettingPower() {
      return this.world.isBlockPowered(this.getPos());
   }
}
