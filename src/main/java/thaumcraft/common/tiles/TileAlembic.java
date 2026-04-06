package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class TileAlembic extends TileThaumcraft implements IAspectContainer, IWandable, IEssentiaTransport {
   public Aspect aspect;
   public Aspect aspectFilter = null;
   public int amount = 0;
   public int maxAmount = 32;
   public int facing = 2;
   public boolean aboveAlembic = false;
   public boolean aboveFurnace = false;
   EnumFacing fd = null;

   public AspectList getAspects() {
      return this.aspect != null ? (new AspectList()).add(this.aspect, this.amount) : new AspectList();
   }

   public void setAspects(AspectList aspects) {
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ() - 1, this.getPos().getX() + 2, this.getPos().getY() + 1, this.getPos().getZ() + 2);
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = nbttagcompound.getByte("facing");
      this.aspectFilter = Aspect.getAspect(nbttagcompound.getString("AspectFilter"));
      String tag = nbttagcompound.getString("aspect");
      if (tag != null) {
         this.aspect = Aspect.getAspect(tag);
      }

      this.amount = nbttagcompound.getShort("amount");
      this.fd = EnumFacing.byIndex(this.facing);
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      if (this.aspect != null) {
         nbttagcompound.setString("aspect", this.aspect.getTag());
      }

      if (this.aspectFilter != null) {
         nbttagcompound.setString("AspectFilter", this.aspectFilter.getTag());
      }

      nbttagcompound.setShort("amount", (short)this.amount);
      nbttagcompound.setByte("facing", (byte)this.facing);
   }

   public boolean canUpdate() {
      return false;
   }

   public int addToContainer(Aspect tt, int am) {
      if (this.amount < this.maxAmount && tt == this.aspect || this.amount == 0) {
         this.aspect = tt;
         int added = Math.min(am, this.maxAmount - this.amount);
         this.amount += added;
         am -= added;
      }

      this.markDirty();
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      return am;
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.amount == 0 || this.aspect == null) {
         this.aspect = null;
         this.amount = 0;
      }

      if (this.aspect != null && this.amount >= am && tt == this.aspect) {
         this.amount -= am;
         if (this.amount <= 0) {
            this.aspect = null;
            this.amount = 0;
         }

         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         return true;
      } else {
         return false;
      }
   }

   public boolean doesContainerContain(AspectList ot) {
      return this.amount > 0 && this.aspect != null && ot.getAmount(this.aspect) > 0;
   }

   public boolean doesContainerContainAmount(Aspect tt, int am) {
      return this.amount >= am && tt == this.aspect;
   }

   public int containerContains(Aspect tt) {
      return tt == this.aspect ? this.amount : 0;
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public void getAppearance() {
      this.aboveAlembic = false;
      net.minecraft.util.math.BlockPos bBelow = this.getPos().down();
      net.minecraft.block.state.IBlockState sBelow = this.world.getBlockState(bBelow);
      this.aboveFurnace = sBelow.getBlock() == ConfigBlocks.blockStoneDevice && sBelow.getBlock().getMetaFromState(sBelow) == 0;

      if (sBelow.getBlock() == ConfigBlocks.blockMetalDevice && sBelow.getBlock().getMetaFromState(sBelow) == this.getBlockMetadata()) {
         this.aboveAlembic = true;
      }

   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      this.getAppearance();
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
       if (side > 1) {
           this.facing = side;
           this.fd = EnumFacing.byIndex(this.facing);
           { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
           player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
           this.markDirty();
       }
       return 0;
   }

   public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
      return null;
   }

   public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
   }

   public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
   }

   public boolean isConnectable(EnumFacing face) {
      return face != EnumFacing.byIndex(this.facing) && face != EnumFacing.DOWN;
   }

   public boolean canInputFrom(EnumFacing face) {
      return false;
   }

   public boolean canOutputTo(EnumFacing face) {
      return face != EnumFacing.byIndex(this.facing) && face != EnumFacing.DOWN;
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   public Aspect getSuctionType(EnumFacing loc) {
      return null;
   }

   public int getSuctionAmount(EnumFacing loc) {
      return 0;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return this.aspect;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.amount;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing loc) {
      return 0;
   }

   public int getMinimumSuction() {
      return 0;
   }

   public boolean renderExtendedTube() {
      return true;
   }
}
