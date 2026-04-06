package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemCrystalEssence;
import thaumcraft.common.lib.utils.InventoryUtils;
import net.minecraft.util.math.BlockPos;

public class TileEssentiaCrystalizer extends TileThaumcraft implements IAspectContainer, IEssentiaTransport {
   public Aspect aspect = null;
   public EnumFacing facing;
   int count;
   int progress;
   final int progMax;
   public float spin;
   public float spinInc;
   float tr;
   float tg;
   float tb;
   public float cr;
   public float cg;
   public float cb;
   int venting;

   public TileEssentiaCrystalizer() {
      this.facing = EnumFacing.DOWN;
      this.count = 0;
      this.progress = 0;
      this.progMax = 200;
      this.spin = 0.0F;
      this.spinInc = 0.0F;
      this.tr = 1.0F;
      this.tg = 1.0F;
      this.tb = 1.0F;
      this.cr = 1.0F;
      this.cg = 1.0F;
      this.cb = 1.0F;
      this.venting = 0;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.aspect = Aspect.getAspect(nbttagcompound.getString("Aspect"));
      this.facing = EnumFacing.byIndex(nbttagcompound.getByte("face"));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      if (this.aspect != null) {
         nbttagcompound.setString("Aspect", this.aspect.getTag());
      }

      nbttagcompound.setByte("face", (byte)this.facing.ordinal());
   }

   public AspectList getAspects() {
      AspectList al = new AspectList();
      if (this.aspect != null) {
         al.add(this.aspect, 1);
      }

      return al;
   }

   public void setAspects(AspectList aspects) {
   }

   public int addToContainer(Aspect tt, int am) {
       if (am != 0) {
           if (this.aspect == null) {
               --am;
               this.aspect = tt;
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
               this.markDirty();
           }

       }
       return am;
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.aspect != null && am == 1) {
         this.aspect = null;
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
      return amt == 1 && this.aspect != null && tag == this.aspect;
   }

   public boolean doesContainerContain(AspectList ot) {
      for(Aspect tt : ot.getAspects()) {
         if (this.aspect == null || this.aspect != tt || ot.getAmount(tt) != 1) {
            return false;
         }
      }

      return true;
   }

   public int containerContains(Aspect tag) {
      return this.aspect != null && tag == this.aspect ? 1 : 0;
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
      return false;
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public int getMinimumSuction() {
      return 0;
   }

   public Aspect getSuctionType(EnumFacing loc) {
      return null;
   }

   public int getSuctionAmount(EnumFacing loc) {
      return this.gettingPower() ? 0 : (loc == this.facing && this.aspect == null ? 128 : 64);
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return this.aspect;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.aspect == null ? 0 : 1;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      return 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
   }

   public void updateEntity() {
            if (!this.world.isRemote) {
         if (++this.count % 5 == 0 && !this.gettingPower()) {
            if (this.aspect == null) {
               this.fillReservoir();
               this.progress = 0;
            } else {
               this.progress += 1 + VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.EARTH, Math.min(20, Math.max(1, (200 - this.progress) / 2))) * 2;
            }
         }

         if (this.aspect != null && this.progress >= 200) {
            this.eject();
            this.aspect = null;
            this.progress = 0;
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
         }
      } else {
         if (this.aspect == null) {
            this.tr = this.tg = this.tb = 1.0F;
         } else {
            Color c = new Color(this.aspect.getColor());
            this.tr = (float)c.getRed() / 220.0F;
            this.tg = (float)c.getGreen() / 220.0F;
            this.tb = (float)c.getBlue() / 220.0F;
         }

         if (this.cr < this.tr) {
            this.cr += 0.05F;
         }

         if (this.cr > this.tr) {
            this.cr -= 0.05F;
         }

         if (this.cg < this.tg) {
            this.cg += 0.05F;
         }

         if (this.cg > this.tg) {
            this.cg -= 0.05F;
         }

         if (this.cb < this.tb) {
            this.cb += 0.05F;
         }

         if (this.cb > this.tb) {
            this.cb -= 0.05F;
         }

         this.spin += this.spinInc;
         if (this.spin > 360.0F) {
            this.spin -= 360.0F;
         }

         if (this.aspect != null && this.spinInc < 20.0F && !this.gettingPower()) {
            this.spinInc += 0.1F;
            if (this.spinInc > 20.0F) {
               this.spinInc = 20.0F;
            }
         } else if ((this.aspect == null || this.gettingPower()) && this.spinInc > 0.0F) {
            this.spinInc -= 0.2F;
            if (this.spinInc < 0.0F) {
               this.spinInc = 0.0F;
            }
         }

         if (this.venting > 0) {
            --this.venting;
            float fx = 0.1F - this.world.rand.nextFloat() * 0.2F;
            float fz = 0.1F - this.world.rand.nextFloat() * 0.2F;
            float fy = 0.1F - this.world.rand.nextFloat() * 0.2F;
            float fx2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
            float fz2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
            float fy2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
            int color = 16777215;
            Thaumcraft.proxy.drawVentParticles(this.world, (float)this.getPos().getX() + 0.5F + fx + (float)this.facing.getOpposite().getXOffset() / 2.1F, (float)this.getPos().getY() + 0.5F + fy + (float)this.facing.getOpposite().getYOffset() / 2.1F, (float)this.getPos().getZ() + 0.5F + fz + (float)this.facing.getOpposite().getZOffset() / 2.1F, (float)this.facing.getOpposite().getXOffset() / 4.0F + fx2, (float)this.facing.getOpposite().getYOffset() / 4.0F + fy2, (float)this.facing.getOpposite().getZOffset() / 4.0F + fz2, color);
         }
      }

   }

   public boolean receiveClientEvent(int i, int j) {
      if (i >= 0) {
         if (this.world.isRemote) {
            this.venting = 7;
         }

         return true;
      } else {
         return super.receiveClientEvent(i, j);
      }
   }

   public void eject() {
      ItemStack stack = new ItemStack(ConfigItems.itemCrystalEssence, 1, 0);
      ((ItemCrystalEssence)stack.getItem()).setAspects(stack, (new AspectList()).add(this.aspect, 1));
      TileEntity inventory = this.world.getTileEntity(new BlockPos(this.getPos().getX() + this.facing.getOpposite().getXOffset(), this.getPos().getY() + this.facing.getOpposite().getYOffset(), this.getPos().getZ() + this.facing.getOpposite().getZOffset()));
      if (inventory instanceof IInventory) {
         stack = InventoryUtils.placeItemStackIntoInventory(stack, (IInventory)inventory, this.facing.ordinal(), true);
      }

      if (stack != null) {
         this.spawnItem(stack);
      }

      this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("random.fizz")), net.minecraft.util.SoundCategory.BLOCKS, 0.25F, 2.6F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.8F);
   }

   public boolean spawnItem(ItemStack stack) {
      EntityItem ie2 = new EntityItem(this.world, (double)this.getPos().getX() + (double)0.5F + (double)this.facing.getOpposite().getXOffset() * 0.65, (double)this.getPos().getY() + (double)0.5F + (double)this.facing.getOpposite().getYOffset() * 0.65, (double)this.getPos().getZ() + (double)0.5F + (double)this.facing.getOpposite().getZOffset() * 0.65, stack);
      ie2.motionX = (float)this.facing.getOpposite().getXOffset() * 0.04F;
      ie2.motionY = (float)this.facing.getOpposite().getYOffset() * 0.04F;
      ie2.motionZ = (float)this.facing.getOpposite().getZOffset() * 0.04F;
      this.world.addBlockEvent(this.getPos(), this.getBlockType(), 0, 0);
      return this.world.spawnEntity(ie2);
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

   public boolean gettingPower() {
      return this.world.isBlockPowered(this.getPos());
   }
}
