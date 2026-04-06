package thaumcraft.common.tiles;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.wands.IWandable;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.codechicken.lib.vec.Cuboid6;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class TileTube extends TileThaumcraft implements IEssentiaTransport, IWandable {
   public EnumFacing facing;
   public boolean[] openSides;
   Aspect essentiaType;
   int essentiaAmount;
   Aspect suctionType;
   int suction;
   int venting;
   int count;
   static final int freq = 5;
   int ventColor;

   public TileTube() {
      this.facing = EnumFacing.NORTH;
      this.openSides = new boolean[]{true, true, true, true, true, true};
      this.essentiaType = null;
      this.essentiaAmount = 0;
      this.suctionType = null;
      this.suction = 0;
      this.venting = 0;
      this.count = 0;
      this.ventColor = 0;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.essentiaType = Aspect.getAspect(nbttagcompound.getString("type"));
      this.essentiaAmount = nbttagcompound.getInteger("amount");
      this.facing = EnumFacing.byIndex(nbttagcompound.getInteger("side"));
      byte[] sides = nbttagcompound.getByteArray("open");
      if (sides != null && sides.length == 6) {
         for(int a = 0; a < 6; ++a) {
            this.openSides[a] = sides[a] == 1;
         }
      }

   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      if (this.essentiaType != null) {
         nbttagcompound.setString("type", this.essentiaType.getTag());
      }

      nbttagcompound.setInteger("amount", this.essentiaAmount);
      byte[] sides = new byte[6];

      for(int a = 0; a < 6; ++a) {
         sides[a] = (byte)(this.openSides[a] ? 1 : 0);
      }

      nbttagcompound.setInteger("side", this.facing.ordinal());
      nbttagcompound.setByteArray("open", sides);
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.suctionType = Aspect.getAspect(nbttagcompound.getString("stype"));
      this.suction = nbttagcompound.getInteger("samount");
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      if (this.suctionType != null) {
         nbttagcompound.setString("stype", this.suctionType.getTag());
      }

      nbttagcompound.setInteger("samount", this.suction);
      return nbttagcompound;
   }

   public void updateEntity() {
      if (this.venting > 0) {
         --this.venting;
      }

      if (this.count == 0) {
         this.count = this.world.rand.nextInt(10);
      }

      if (!this.world.isRemote) {
         if (this.venting <= 0) {
            if (++this.count % 2 == 0) {
               this.calculateSuction(null, false, false);
               this.checkVenting();
               if (this.essentiaType != null && this.essentiaAmount == 0) {
                  this.essentiaType = null;
               }
            }

            if (this.count % 5 == 0 && this.suction > 0) {
               this.equalizeWithNeighbours(false);
            }
         }
      } else if (this.venting > 0) {
         Random r = new Random(this.hashCode() * 4L);
         float rp = r.nextFloat() * 360.0F;
         float ry = r.nextFloat() * 360.0F;
         double fx = -MathHelper.sin(ry / 180.0F * (float)Math.PI) * MathHelper.cos(rp / 180.0F * (float)Math.PI);
         double fz = MathHelper.cos(ry / 180.0F * (float)Math.PI) * MathHelper.cos(rp / 180.0F * (float)Math.PI);
         double fy = -MathHelper.sin(rp / 180.0F * (float)Math.PI);
         Thaumcraft.proxy.drawVentParticles(this.world, (double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F, fx / (double)5.0F, fy / (double)5.0F, fx / (double)5.0F, this.ventColor);
      }

   }

   void calculateSuction(Aspect filter, boolean restrict, boolean directional) {
      this.suction = 0;
      this.suctionType = null;
      EnumFacing loc = null;

      for(int dir = 0; dir < 6; ++dir) {
         try {
            loc = EnumFacing.byIndex(dir);
            if ((!directional || this.facing == loc.getOpposite()) && this.isConnectable(loc)) {
               TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), loc);
               if (te != null) {
                  IEssentiaTransport ic = (IEssentiaTransport)te;
                  if ((filter == null || ic.getSuctionType(loc.getOpposite()) == null || ic.getSuctionType(loc.getOpposite()) == filter) && (filter != null || this.getEssentiaAmount(loc) <= 0 || ic.getSuctionType(loc.getOpposite()) == null || this.getEssentiaType(loc) == ic.getSuctionType(loc.getOpposite())) && (filter == null || this.getEssentiaAmount(loc) <= 0 || this.getEssentiaType(loc) == null || ic.getSuctionType(loc.getOpposite()) == null || this.getEssentiaType(loc) == ic.getSuctionType(loc.getOpposite()))) {
                     int suck = ic.getSuctionAmount(loc.getOpposite());
                     if (suck > 0 && suck > this.suction + 1) {
                        Aspect st = ic.getSuctionType(loc.getOpposite());
                        if (st == null) {
                           st = filter;
                        }

                        this.setSuction(st, restrict ? suck / 2 : suck - 1);
                     }
                  }
               }
            }
         } catch (Exception ignored) {
         }
      }

   }

   void checkVenting() {
      EnumFacing loc = null;

      for(int dir = 0; dir < 6; ++dir) {
         try {
            loc = EnumFacing.byIndex(dir);
            if (this.isConnectable(loc)) {
               TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), loc);
               if (te != null) {
                  IEssentiaTransport ic = (IEssentiaTransport)te;
                  int suck = ic.getSuctionAmount(loc.getOpposite());
                  if (this.suction > 0 && (suck == this.suction || suck == this.suction - 1) && this.suctionType != ic.getSuctionType(loc.getOpposite())) {
                     int c = -1;
                     if (this.suctionType != null) {
                        c = Config.aspectOrder.indexOf(this.suctionType);
                     }

                     this.world.addBlockEvent(this.getPos(), ConfigBlocks.blockTube, 1, c);
                     this.venting = 40;
                  }
               }
            }
         } catch (Exception ignored) {
         }
      }

   }

   void equalizeWithNeighbours(boolean directional) {
      EnumFacing loc = null;
      if (this.essentiaAmount <= 0) {
         for(int dir = 0; dir < 6; ++dir) {
            try {
               loc = EnumFacing.byIndex(dir);
               if ((!directional || this.facing != loc.getOpposite()) && this.isConnectable(loc)) {
                  TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), loc);
                  if (te != null) {
                     IEssentiaTransport ic = (IEssentiaTransport)te;
                     if (ic.canOutputTo(loc.getOpposite()) && (this.getSuctionType(null) == null || this.getSuctionType(null) == ic.getEssentiaType(loc.getOpposite()) || ic.getEssentiaType(loc.getOpposite()) == null) && this.getSuctionAmount(null) > ic.getSuctionAmount(loc.getOpposite()) && this.getSuctionAmount(null) >= ic.getMinimumSuction()) {
                        Aspect a = this.getSuctionType(null);
                        if (a == null) {
                           a = ic.getEssentiaType(loc.getOpposite());
                           if (a == null) {
                              a = ic.getEssentiaType(null);
                           }
                        }

                        int am = this.addEssentia(a, ic.takeEssentia(a, 1, loc.getOpposite()), loc);
                        if (am > 0) {
                           if (this.world.rand.nextInt(100) == 0) {
                              this.world.addBlockEvent(this.getPos(), ConfigBlocks.blockTube, 0, 0);
                           }

                           return;
                        }
                     }
                  }
               }
            } catch (Exception ignored) {
            }
         }

      }
   }

   public boolean isConnectable(EnumFacing face) {
      return face != null && this.openSides[face.ordinal()];
   }

   public boolean canInputFrom(EnumFacing face) {
      return face != null && this.openSides[face.ordinal()];
   }

   public boolean canOutputTo(EnumFacing face) {
      return face != null && this.openSides[face.ordinal()];
   }

   public void setSuction(Aspect aspect, int amount) {
      this.suctionType = aspect;
      this.suction = amount;
   }

   public Aspect getSuctionType(EnumFacing loc) {
      return this.suctionType;
   }

   public int getSuctionAmount(EnumFacing loc) {
      return this.suction;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return this.essentiaType;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return this.essentiaAmount;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      if (this.canOutputTo(face) && this.essentiaType == aspect && this.essentiaAmount > 0 && amount > 0) {
         --this.essentiaAmount;
         if (this.essentiaAmount <= 0) {
            this.essentiaType = null;
         }

         this.markDirty();
         return 1;
      } else {
         return 0;
      }
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      if (this.canInputFrom(face) && this.essentiaAmount == 0 && amount > 0) {
         this.essentiaType = aspect;
         ++this.essentiaAmount;
         this.markDirty();
         return 1;
      } else {
         return 0;
      }
   }

   public int getMinimumSuction() {
      return 0;
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 0) {
         if (this.world.isRemote) {
            this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "creak")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.3F + this.world.rand.nextFloat() * 0.2F);
         }

         return true;
      } else if (i != 1) {
         return super.receiveClientEvent(i, j);
      } else {
         if (this.world.isRemote) {
            if (this.venting <= 0) {
               this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "block.fire.extinguish")), net.minecraft.util.SoundCategory.BLOCKS, 0.1F, 1.0F + this.world.rand.nextFloat() * 0.1F);
            }

            this.venting = 50;
            if (j != -1 && j < Config.aspectOrder.size()) {
               this.ventColor = ((Aspect)Config.aspectOrder.get(j)).getColor();
            } else {
               this.ventColor = 11184810;
            }
         }

         return true;
      }
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      RayTraceResult hit = RayTracer.retraceBlock(world, player, x, y, z);
       if (hit != null) {
           if (hit.subHit >= 0 && hit.subHit < 6) {
               player.world.playSound(null, new BlockPos(x, y, z), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "tool")), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F);
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               this.markDirty();
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
               this.openSides[hit.subHit] = !this.openSides[hit.subHit];
               EnumFacing dir = EnumFacing.byIndex(hit.subHit);
               TileEntity tile = this.world.getTileEntity(new BlockPos(this.getPos().getX() + dir.getXOffset(), this.getPos().getY() + dir.getYOffset(), this.getPos().getZ() + dir.getZOffset()));
               if (tile instanceof TileTube) {
                   ((TileTube) tile).openSides[dir.getOpposite().ordinal()] = this.openSides[hit.subHit];
                   { BlockPos _np = this.getPos().offset(dir); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_np); world.notifyBlockUpdate(_np, _bs, _bs, 3); }
                   tile.markDirty();
               }
           }

           if (hit.subHit == 6) {
               player.world.playSound(null, new BlockPos(x, y, z), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "tool")), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F);
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               int a = this.facing.ordinal();
               this.markDirty();

               while (true) {
                   ++a;
                   if (a >= 20) {
                       break;
                   }

                   if (this.canConnectSide(EnumFacing.byIndex(a % 6).getOpposite().ordinal()) && this.isConnectable(EnumFacing.byIndex(a % 6).getOpposite())) {
                       a %= 6;
                       this.facing = EnumFacing.byIndex(a);
                       { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
                       break;
                   }
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

   protected boolean canConnectSide(int side) {
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

      cuboids.add(new IndexedCuboid6(6, new Cuboid6((double)this.getPos().getX() + (double)0.34375F, (double)this.getPos().getY() + (double)0.34375F, (double)this.getPos().getZ() + (double)0.34375F, (double)this.getPos().getX() + (double)0.65625F, (double)this.getPos().getY() + (double)0.65625F, (double)this.getPos().getZ() + (double)0.65625F)));
   }

   @Override
   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
   }
}
