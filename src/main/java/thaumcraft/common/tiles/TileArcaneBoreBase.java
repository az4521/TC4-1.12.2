package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.wands.IWandable;

public class TileArcaneBoreBase extends TileThaumcraft implements IWandable, IEssentiaTransport {
   public EnumFacing orientation = EnumFacing.byIndex(2);

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.orientation = EnumFacing.byIndex(nbttagcompound.getInteger("orientation"));
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("orientation", this.orientation.ordinal());
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      this.orientation = EnumFacing.byIndex(side);
      { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:tool")); if (_snd != null) player.world.playSound(null, new BlockPos(x, y, z), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.3F, 1.9F + player.world.rand.nextFloat() * 0.2F); }
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

   boolean drawEssentia() {
      for (EnumFacing facing : EnumFacing.values()) {
         TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), facing);
         if (te != null) {
            IEssentiaTransport ic = (IEssentiaTransport)te;
            if (!ic.canOutputTo(facing.getOpposite())) {
               return false;
            }

            if (ic.getSuctionAmount(facing.getOpposite()) < this.getSuctionAmount(facing) && ic.takeEssentia(Aspect.ENTROPY, 1, facing.getOpposite()) == 1) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean isConnectable(EnumFacing face) {
      return true;
   }

   public boolean canInputFrom(EnumFacing face) {
      return true;
   }

   public boolean canOutputTo(EnumFacing face) {
      return false;
   }

   public void setSuction(Aspect aspect, int amount) {
   }

   public Aspect getSuctionType(EnumFacing face) {
      return Aspect.ENTROPY;
   }

   public int getSuctionAmount(EnumFacing face) {
      return face != this.orientation ? 128 : 0;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      return 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      return 0;
   }

   public Aspect getEssentiaType(EnumFacing face) {
      return null;
   }

   public int getEssentiaAmount(EnumFacing face) {
      return 0;
   }

   public int getMinimumSuction() {
      return 0;
   }

   public boolean renderExtendedTube() {
      return true;
   }
}
