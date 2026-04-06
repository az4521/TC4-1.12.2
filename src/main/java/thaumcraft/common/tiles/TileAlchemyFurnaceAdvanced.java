package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import net.minecraft.util.math.BlockPos;

public class TileAlchemyFurnaceAdvanced extends TileThaumcraft {
   public AspectList aspects = new AspectList();
   public int vis;
   public int maxVis = 500;
   int bellows = -1;
   public int heat = 0;
   public int power1 = 0;
   public int power2 = 0;
   public int maxPower = 500;
   public boolean destroy = false;
   int count = 0;
   int processed = 0;

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ() - 1, this.getPos().getX() + 2, this.getPos().getY() + 2, this.getPos().getZ() + 2);
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.vis = nbttagcompound.getShort("vis");
      this.heat = nbttagcompound.getShort("heat");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setShort("vis", (short)this.vis);
      nbttagcompound.setShort("heat", (short)this.heat);
   }

   public void readFromNBT(NBTTagCompound nbtCompound) {
      super.readFromNBT(nbtCompound);
      this.aspects.readFromNBT(nbtCompound);
      this.power1 = nbtCompound.getShort("power1");
      this.power2 = nbtCompound.getShort("power2");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
      super.writeToNBT(nbtCompound);
      this.aspects.writeToNBT(nbtCompound);
      nbtCompound.setShort("power1", (short)this.power1);
      nbtCompound.setShort("power2", (short)this.power2);
      return nbtCompound;
   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      if (this.world != null) {
         this.world.checkLightFor(EnumSkyBlock.BLOCK, this.getPos());
      }

   }


   public void updateEntity() {
      ++this.count;
      if (!this.world.isRemote) {
         if (this.destroy) {
            for(int a = -1; a <= 1; ++a) {
               for(int b = 0; b <= 1; ++b) {
                  for(int c = -1; c <= 1; ++c) {
                     if ((a != 0 || b != 0 || c != 0)) {
                        BlockPos np = this.getPos().add(a, b, c);
                        net.minecraft.block.state.IBlockState nstate = this.world.getBlockState(np);
                        if (nstate.getBlock() == this.getBlockType()) {
                           this.world.setBlockState(np, nstate.getBlock().getDefaultState(), 3);
                        }
                     }
                  }
               }
            }

            this.world.setBlockState(this.getPos(), this.getBlockType().getDefaultState(), 3);
            return;
         }

         if (this.processed > 0) {
            --this.processed;
         }

         if (this.count % 5 == 0) {
            int pt = this.heat--;
            if (this.heat <= this.maxPower) {
               this.heat += VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.FIRE, 50);
            }

            if (this.power1 <= this.maxPower) {
               this.power1 += VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.ENTROPY, 50);
            }

            if (this.power2 <= this.maxPower) {
               this.power2 += VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.WATER, 50);
            }

            if (pt / 50 != this.heat / 50) {
               { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            }
         }
      }

   }

   public boolean process(ItemStack stack) {
      if (this.processed == 0 && this.canSmelt(stack)) {
         AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
         al = ThaumcraftCraftingManager.getBonusTags(stack, al);
         int aa = al.visSize();
         if (aa * 2 <= this.heat && aa <= this.power1 && aa <= this.power2) {
            this.heat -= aa * 2;
            this.power1 -= aa;
            this.power2 -= aa;
            this.processed = (int)((float)this.processed + 5.0F + Math.max(0.0F, (1.0F - (float)this.heat / (float)this.maxPower) * 100.0F));
            this.aspects.add(al);
            this.vis = this.aspects.visSize();
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean canSmelt(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         AspectList al = ThaumcraftCraftingManager.getObjectTags(stack);
         al = ThaumcraftCraftingManager.getBonusTags(stack, al);
         if (al != null && al.size() != 0) {
            int vs = al.visSize();
            return vs + this.aspects.visSize() <= this.maxVis;
         } else {
            return false;
         }
      }
   }
}
