package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.lib.utils.CropUtils;
import net.minecraft.util.math.BlockPos;

public class TileArcaneLampGrowth extends TileThaumcraft implements IEssentiaTransport {
   public EnumFacing facing = EnumFacing.byIndex(0);
   private boolean reserve = false;
   public int charges = -1;
   int lx = 0;
   int ly = 0;
   int lz = 0;
   Block lid;
   int lmd;
   ArrayList checklist;
   int drawDelay;

   public TileArcaneLampGrowth() {
      this.lid = Blocks.AIR;
      this.lmd = 0;
      this.checklist = new ArrayList<>();
      this.drawDelay = 0;
   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      if (this.world != null && this.world.isRemote) {
         this.world.checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
      }

   }

   public void updateEntity() {
      if (!this.world.isRemote) {
         if (this.charges <= 0) {
            if (this.reserve) {
               this.charges = 100;
               this.reserve = false;
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            } else if (this.drawEssentia()) {
               this.charges = 100;
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            }
         }

         if (!this.reserve && this.drawEssentia()) {
            this.reserve = true;
         }

         if (this.charges == 0) {
            this.charges = -1;
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         }

         if (this.charges > 0) {
            this.updatePlant();
         }
      }

   }

   boolean isPlant(int x, int y, int z) {
      boolean flag = this.world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof IGrowable;
      Material mat = this.world.getBlockState(new BlockPos(x, y, z)).getMaterial();
      return (flag || mat == Material.CACTUS || mat == Material.PLANTS) && mat != Material.GRASS;
   }

   private void updatePlant() {
      if (this.lid != this.world.getBlockState(new BlockPos(this.lx, this.ly, this.lz)).getBlock() || this.lmd != this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.lx, this.ly, this.lz)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.lx, this.ly, this.lz)))) {
         EntityPlayer p = this.world.getClosestPlayer(this.lx, this.ly, this.lz, 32.0F, false);
         if (p != null) {
            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(this.lx, this.ly, this.lz, 4259648), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.lx, this.ly, this.lz, 32.0F));
         }

         this.lid = this.world.getBlockState(new BlockPos(this.lx, this.ly, this.lz)).getBlock();
         this.lmd = this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.lx, this.ly, this.lz)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.lx, this.ly, this.lz)));
      }

      int distance = 6;
      if (this.checklist.isEmpty()) {
         for(int a = -distance; a <= distance; ++a) {
            for(int b = -distance; b <= distance; ++b) {
               this.checklist.add(new BlockCoordinates(this.getPos().getX() + a, this.getPos().getY() + distance, this.getPos().getZ() + b));
            }
         }

         Collections.shuffle(this.checklist, this.world.rand);
      }

      int x = ((BlockCoordinates)this.checklist.get(0)).x;
      int y = ((BlockCoordinates)this.checklist.get(0)).y;
      int z = ((BlockCoordinates)this.checklist.get(0)).z;
      this.checklist.remove(0);

      while(y >= this.getPos().getY() - distance) {
         if (!this.world.isAirBlock(new BlockPos(x, y, z)) && this.isPlant(x, y, z) && this.getDistanceSq((double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F) < (double)(distance * distance) && !CropUtils.isGrownCrop(this.world, x, y, z) && CropUtils.doesLampGrow(this.world, x, y, z)) {
            --this.charges;
            this.lx = x;
            this.ly = y;
            this.lz = z;
            this.lid = this.world.getBlockState(new BlockPos(x, y, z)).getBlock();
            this.lmd = this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
            this.world.scheduleUpdate(new BlockPos(x, y, z), this.world.getBlockState(new BlockPos(x, y, z)).getBlock(), 1);
            return;
         }

         --y;
      }

   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = EnumFacing.byIndex(nbttagcompound.getInteger("orientation"));
      this.reserve = nbttagcompound.getBoolean("reserve");
      this.charges = nbttagcompound.getInteger("charges");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("orientation", this.facing.ordinal());
      nbttagcompound.setBoolean("reserve", this.reserve);
      nbttagcompound.setInteger("charges", this.charges);
   }

   boolean drawEssentia() {
      if (++this.drawDelay % 5 != 0) {
         return false;
      } else {
         TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.facing);
         if (te != null) {
            IEssentiaTransport ic = (IEssentiaTransport)te;
            if (!ic.canOutputTo(this.facing.getOpposite())) {
               return false;
            }

             return ic.getSuctionAmount(this.facing.getOpposite()) < this.getSuctionAmount(this.facing) && ic.takeEssentia(Aspect.PLANT, 1, this.facing.getOpposite()) == 1;
         }

         return false;
      }
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

   public Aspect getSuctionType(EnumFacing face) {
      return Aspect.PLANT;
   }

   public int getSuctionAmount(EnumFacing face) {
      return face != this.facing || this.reserve && this.charges > 0 ? 0 : 128;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return 0;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing loc) {
      return 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing loc) {
      return 0;
   }
}
