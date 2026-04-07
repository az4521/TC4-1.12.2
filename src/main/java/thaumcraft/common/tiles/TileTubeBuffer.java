package thaumcraft.common.tiles;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.wands.IWandable;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.codechicken.lib.vec.Cuboid6;
import net.minecraft.util.math.BlockPos;

public class TileTubeBuffer extends TileThaumcraft implements IAspectContainer, IEssentiaTransport, IWandable {
   public AspectList aspects = new AspectList();
   public final int MAXAMOUNT = 8;
   public boolean[] openSides = new boolean[]{true, true, true, true, true, true};
   public byte[] chokedSides = new byte[]{0, 0, 0, 0, 0, 0};
   int count = 0;
   int bellows = -1;

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.aspects.readFromNBT(nbttagcompound);
      byte[] sides = nbttagcompound.getByteArray("open");
      if (sides != null && sides.length == 6) {
         for(int a = 0; a < 6; ++a) {
            this.openSides[a] = sides[a] == 1;
         }
      }

      this.chokedSides = nbttagcompound.getByteArray("choke");
      if (this.chokedSides == null || this.chokedSides.length < 6) {
         this.chokedSides = new byte[]{0, 0, 0, 0, 0, 0};
      }

   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      this.aspects.writeToNBT(nbttagcompound);
      byte[] sides = new byte[6];

      for(int a = 0; a < 6; ++a) {
         sides[a] = (byte)(this.openSides[a] ? 1 : 0);
      }

      nbttagcompound.setByteArray("open", sides);
      nbttagcompound.setByteArray("choke", this.chokedSides);
   }

   public AspectList getAspects() {
      return this.aspects;
   }

   public void setAspects(AspectList aspects) {
   }

   public int addToContainer(Aspect tt, int am) {
      if (am != 1) {
         return am;
      } else if (this.aspects.visSize() < 8) {
         this.aspects.add(tt, am);
         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         return 0;
      } else {
         return am;
      }
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.aspects.getAmount(tt) >= am) {
         this.aspects.remove(tt, am);
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
      return this.aspects.getAmount(tag) >= amt;
   }

   public boolean doesContainerContain(AspectList ot) {
      return false;
   }

   public int containerContains(Aspect tag) {
      return this.aspects.getAmount(tag);
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }

   public boolean isConnectable(EnumFacing face) {
      return this.openSides[face.ordinal()];
   }

   public boolean canInputFrom(EnumFacing face) {
      return this.openSides[face.ordinal()];
   }

   public boolean canOutputTo(EnumFacing face) {
      return this.openSides[face.ordinal()];
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
      return this.chokedSides[loc.ordinal()] == 2 ? 0 : (this.bellows > 0 && this.chokedSides[loc.ordinal()] != 1 ? this.bellows * 32 : 1);
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return this.aspects.size() > 0 ? this.aspects.getAspects()[this.world.rand.nextInt(this.aspects.getAspects().length)] : null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.aspects.visSize();
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      if (!this.canOutputTo(face)) {
         return 0;
      } else {
         TileEntity te = null;
         IEssentiaTransport ic = null;
         int suction = 0;
         te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), face);
         if (te != null) {
            ic = (IEssentiaTransport)te;
            suction = ic.getSuctionAmount(face.getOpposite());
         }

         for(EnumFacing dir : EnumFacing.values()) {
            if (this.canOutputTo(dir) && dir != face) {
               te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), dir);
               if (te != null) {
                  ic = (IEssentiaTransport)te;
                  int sa = ic.getSuctionAmount(dir.getOpposite());
                  Aspect su = ic.getSuctionType(dir.getOpposite());
                  if ((su == aspect || su == null) && suction < sa && this.getSuctionAmount(dir) < sa) {
                     return 0;
                  }
               }
            }
         }

         if (amount > this.aspects.getAmount(aspect)) {
            amount = this.aspects.getAmount(aspect);
         }

         return this.takeFromContainer(aspect, amount) ? amount : 0;
      }
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
   }

   public void updateEntity() {
      ++this.count;
      if (this.bellows < 0 || this.count % 20 == 0) {
         this.getBellows();
      }

      if (!this.world.isRemote && this.count % 5 == 0) {
         int var10000 = this.aspects.visSize();
         this.getClass();
         if (var10000 < 8) {
            this.fillBuffer();
         }
      }

   }

   void fillBuffer() {
      TileEntity te = null;
      IEssentiaTransport ic = null;

      for(EnumFacing dir : EnumFacing.values()) {
         te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), dir);
         if (te != null) {
            ic = (IEssentiaTransport)te;
            if (ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionAmount(dir.getOpposite()) < this.getSuctionAmount(dir) && this.getSuctionAmount(dir) >= ic.getMinimumSuction()) {
               Aspect ta = ic.getEssentiaType(dir.getOpposite());
               this.addToContainer(ta, ic.takeEssentia(ta, 1, dir.getOpposite()));
               return;
            }
         }
      }

   }

   public void getBellows() {
      this.bellows = TileBellows.getBellows(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), EnumFacing.values());
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      RayTraceResult hit = RayTracer.retraceBlock(world, player, x, y, z);
       if (hit != null) {
           if (hit.subHit >= 0 && hit.subHit < 6) {
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               if (player.isSneaking()) {
                   player.world.playSound(null, new BlockPos(x, y, z), thaumcraft.common.lib.SoundsTC.get("thaumcraft:squeek"), net.minecraft.util.SoundCategory.BLOCKS, 0.6F, 1.1F + world.rand.nextFloat() * 0.2F);
                   if (!this.world.isRemote) {
                       ++this.chokedSides[hit.subHit];
                       if (this.chokedSides[hit.subHit] > 2) {
                           this.chokedSides[hit.subHit] = 0;
                       }

                       this.markDirty();
                       { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
                   }
               } else {
                   player.world.playSound(null, new BlockPos(x, y, z), thaumcraft.common.lib.SoundsTC.get("thaumcraft:tool"), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F);
                   this.openSides[hit.subHit] = !this.openSides[hit.subHit];
                   EnumFacing dir = EnumFacing.byIndex(hit.subHit);
                   TileEntity tile = this.world.getTileEntity(new BlockPos(this.getPos().getX() + dir.getXOffset(), this.getPos().getY() + dir.getYOffset(), this.getPos().getZ() + dir.getZOffset()));
                   if (tile instanceof TileTube) {
                       ((TileTube) tile).openSides[dir.getOpposite().ordinal()] = this.openSides[hit.subHit];
                       { BlockPos _np = this.getPos().offset(dir); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_np); world.notifyBlockUpdate(_np, _bs, _bs, 3); }
                       tile.markDirty();
                   }

                   if (tile instanceof TileTubeBuffer) {
                       ((TileTubeBuffer) tile).openSides[dir.getOpposite().ordinal()] = this.openSides[hit.subHit];
                       { BlockPos _np = this.getPos().offset(dir); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_np); world.notifyBlockUpdate(_np, _bs, _bs, 3); }
                       tile.markDirty();
                   }

                   this.markDirty();
                   { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
               }
           }

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

   public RayTraceResult rayTrace(World world, Vec3d vec3d, Vec3d vec3d1, RayTraceResult fullblock) {
      return fullblock;
   }

   private boolean canConnectSide(int side) {
      EnumFacing dir = EnumFacing.byIndex(side);
      TileEntity tile = this.world.getTileEntity(new BlockPos(this.getPos().getX() + dir.getXOffset(), this.getPos().getY() + dir.getYOffset(), this.getPos().getZ() + dir.getZOffset()));
      return tile instanceof IEssentiaTransport;
   }

   public void addTraceableCuboids(List cuboids) {
      float min = 0.42F;
      float max = 0.58F;
      if (this.canConnectSide(0)) {
         cuboids.add(new IndexedCuboid6(0, new Cuboid6((float)this.getPos().getX() + min, this.getPos().getY(), (float)this.getPos().getZ() + min, (float)this.getPos().getX() + max, (double)this.getPos().getY() + (double)0.5F, (float)this.getPos().getZ() + max)));
      }

      if (this.canConnectSide(1)) {
         cuboids.add(new IndexedCuboid6(1, new Cuboid6((float)this.getPos().getX() + min, (double)this.getPos().getY() + (double)0.5F, (float)this.getPos().getZ() + min, (float)this.getPos().getX() + max, this.getPos().getY() + 1, (float)this.getPos().getZ() + max)));
      }

      if (this.canConnectSide(2)) {
         cuboids.add(new IndexedCuboid6(2, new Cuboid6((float)this.getPos().getX() + min, (float)this.getPos().getY() + min, this.getPos().getZ(), (float)this.getPos().getX() + max, (float)this.getPos().getY() + max, (double)this.getPos().getZ() + (double)0.5F)));
      }

      if (this.canConnectSide(3)) {
         cuboids.add(new IndexedCuboid6(3, new Cuboid6((float)this.getPos().getX() + min, (float)this.getPos().getY() + min, (double)this.getPos().getZ() + (double)0.5F, (float)this.getPos().getX() + max, (float)this.getPos().getY() + max, this.getPos().getZ() + 1)));
      }

      if (this.canConnectSide(4)) {
         cuboids.add(new IndexedCuboid6(4, new Cuboid6(this.getPos().getX(), (float)this.getPos().getY() + min, (float)this.getPos().getZ() + min, (double)this.getPos().getX() + (double)0.5F, (float)this.getPos().getY() + max, (float)this.getPos().getZ() + max)));
      }

      if (this.canConnectSide(5)) {
         cuboids.add(new IndexedCuboid6(5, new Cuboid6((double)this.getPos().getX() + (double)0.5F, (float)this.getPos().getY() + min, (float)this.getPos().getZ() + min, this.getPos().getX() + 1, (float)this.getPos().getY() + max, (float)this.getPos().getZ() + max)));
      }

      cuboids.add(new IndexedCuboid6(6, new Cuboid6((float)this.getPos().getX() + 0.25F, (float)this.getPos().getY() + 0.25F, (float)this.getPos().getZ() + 0.25F, (float)this.getPos().getX() + 0.75F, (float)this.getPos().getY() + 0.75F, (float)this.getPos().getZ() + 0.75F)));
   }
}
