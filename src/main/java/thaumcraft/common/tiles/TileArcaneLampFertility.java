package thaumcraft.common.tiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import net.minecraft.util.math.BlockPos;

public class TileArcaneLampFertility extends TileThaumcraft implements IEssentiaTransport {
   public EnumFacing facing = EnumFacing.byIndex(0);
   public int charges = 0;
   int count = 0;
   int drawDelay = 0;

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      if (this.world != null && this.world.isRemote) {
         this.world.checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
      }

   }

   public void updateEntity() {
      if (!this.world.isRemote) {
         if (this.charges < 4 && this.drawEssentia()) {
            ++this.charges;
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         }

         if (this.charges > 1 && this.count++ % 300 == 0) {
            this.updateAnimals();
         }
      }

   }

   private void updateAnimals() {
      int distance = 7;
      List<EntityAnimal> var5 = this.world.getEntitiesWithinAABB(EntityAnimal.class, new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1).expand(distance, distance, distance));

      for(EntityAnimal var3 : var5) {
         EntityLivingBase var4 = var3;
         if (var3.getGrowingAge() == 0 && !var3.isInLove()) {
            ArrayList<EntityAnimal> sa = new ArrayList<>();

            for(EntityAnimal var7 : var5) {
               if (var7.getClass().equals(var4.getClass())) {
                  sa.add(var7);
               }
            }

            if (sa == null || sa.size() <= 7) {
               Iterator var22 = sa.iterator();
               EntityAnimal partner = null;

               while(var22.hasNext()) {
                  EntityAnimal var33 = (EntityAnimal)var22.next();
                  if (var33.getGrowingAge() == 0 && !var33.isInLove()) {
                     if (partner != null) {
                        this.charges -= 2;
                        var33.setInLove(null);
                        partner.setInLove(null);
                        return;
                     }

                     partner = var33;
                  }
               }
            }
         }
      }

   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = EnumFacing.byIndex(nbttagcompound.getInteger("orientation"));
      this.charges = nbttagcompound.getInteger("charges");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("orientation", this.facing.ordinal());
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

             return ic.getSuctionAmount(this.facing.getOpposite()) < this.getSuctionAmount(this.facing) && ic.takeEssentia(Aspect.LIFE, 1, this.facing.getOpposite()) == 1;
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
      return Aspect.LIFE;
   }

   public int getSuctionAmount(EnumFacing face) {
      return face == this.facing ? 128 - this.charges * 10 : 0;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return 0;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing facing) {
      return 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing facing) {
      return 0;
   }
}
