package thaumcraft.common.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockAiry;
import thaumcraft.common.config.ConfigBlocks;

public class TileNodeConverter extends TileThaumcraft {
   public int count = -1;
   public int status = 0;

   public void updateEntity() {
      if (this.count == -1) {
         this.checkStatus();
      }

      if (this.status == 1 && !this.world.isRemote && this.count >= 1000) {
         TileEntity tile = this.world.getTileEntity(this.getPos().down());
         if (tile instanceof TileNode) {
            AspectList base = ((TileNode)tile).getAspectsBase();
            NodeType type = ((TileNode)tile).getNodeType();
            NodeModifier mod = ((TileNode)tile).getNodeModifier();
            this.world.setBlockState(this.getPos().down(), ConfigBlocks.blockAiry.getStateFromMeta(5), 3);
            TileEntity tilenew = this.world.getTileEntity(this.getPos().down());
            if (tilenew instanceof TileNodeEnergized) {
               ((TileNodeEnergized)tilenew).setNodeModifier(mod);
               ((TileNodeEnergized)tilenew).setNodeType(type);
               ((TileNodeEnergized)tilenew).setAspects(base.copy());
               ((TileNodeEnergized)tilenew).setupNode();
            }

            this.checkStatus();
            this.world.addBlockEvent(this.getPos(), this.getBlockType(), 10, 10);
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
         }
      }

      if (this.status == 2 && !this.world.isRemote && this.count <= 50) {
         TileEntity tile = this.world.getTileEntity(this.getPos().down());
         if (tile instanceof TileNodeEnergized) {
            AspectList base = ((TileNodeEnergized)tile).getAuraBase();
            NodeType type = ((TileNodeEnergized)tile).getNodeType();
            NodeModifier mod = ((TileNodeEnergized)tile).getNodeModifier();
            this.world.setBlockState(this.getPos().down(), ConfigBlocks.blockAiry.getDefaultState(), 3);
            TileEntity tilenew = this.world.getTileEntity(this.getPos().down());
            if (tilenew instanceof TileNode) {
               ((TileNode)tilenew).setNodeModifier(mod);
               ((TileNode)tilenew).setNodeType(type);
               ((TileNode)tilenew).setAspects(base.copy());

               for(Aspect a : ((TileNode)tilenew).getAspects().getAspects()) {
                  ((TileNode)tilenew).takeFromContainer(a, ((TileNode)tilenew).getAspects().getAmount(a));
               }
            }

            this.world.addBlockEvent(this.getPos(), this.getBlockType(), 10, 10);
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
            this.status = 0;
         }
      }

      if (this.status != 0 && this.world.isBlockPowered(this.getPos())) {
         if (this.count < 1000) {
            ++this.count;
            if (!this.world.isRemote) {
               TileEntity tilenew = this.world.getTileEntity(this.getPos().down());
               if (tilenew instanceof TileNode) {
                  TileNode nd = (TileNode)tilenew;
                  AspectList al = nd.getAspects();
                  if (al.getAspects().length > 0) {
                     nd.takeFromContainer(al.getAspects()[this.world.rand.nextInt(al.getAspects().length)], 1);
                     if (this.count % 5 == 0 || nd.getAspects().visSize() == 0) {
                        { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.getPos().down()); this.world.notifyBlockUpdate(this.getPos().down(), _bs, _bs, 3); }
                     }
                  }
               }
            }

            if (this.count > 50 && this.world.isRemote) {
               if (this.world.rand.nextBoolean()) {
                  Thaumcraft.proxy.nodeBolt(this.world, (float)this.getPos().getX() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() - 0.5F, (float)this.getPos().getZ() + 0.5F);
               }

               if (this.world.rand.nextBoolean() && this.hasStabilizer()) {
                  Thaumcraft.proxy.nodeBolt(this.world, (float)this.getPos().getX() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getY() - 1.5F, (float)this.getPos().getZ() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() - 0.5F, (float)this.getPos().getZ() + 0.5F);
               }
            }
         }
      } else if (this.count > 0) {
         --this.count;
         if (this.count > 50 && this.world.isRemote) {
            if (this.world.rand.nextBoolean()) {
               Thaumcraft.proxy.nodeBolt(this.world, (float)this.getPos().getX() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() - 0.5F, (float)this.getPos().getZ() + 0.5F);
            }

            if (this.world.rand.nextBoolean() && this.hasStabilizer()) {
               Thaumcraft.proxy.nodeBolt(this.world, (float)this.getPos().getX() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getY() - 1.5F, (float)this.getPos().getZ() + 0.25F + this.world.rand.nextFloat() * 0.5F, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() - 0.5F, (float)this.getPos().getZ() + 0.5F);
            }
         }
      }

      if (this.count > 1000) {
         this.count = 1000;
      }

   }

   private boolean hasStabilizer() {
      TileEntity te = this.world.getTileEntity(this.getPos().down(2));
      return !this.world.isBlockPowered(this.getPos().down(2)) && te instanceof TileNodeStabilizer;
   }

   public void checkStatus() {
      if (this.count == -1) {
         this.count = 0;
      }

      BlockPos below = this.getPos().down();
      net.minecraft.block.state.IBlockState belowState = this.world.getBlockState(below);
      int belowMeta = belowState.getBlock().getMetaFromState(belowState);

      if (this.status != 2 || this.count <= 50 || this.hasStabilizer() && belowState.getBlock() == ConfigBlocks.blockAiry && belowMeta == 5) {
         if (this.world.isBlockPowered(this.getPos()) && belowState.getBlock() == ConfigBlocks.blockAiry && belowMeta == 0 && this.hasStabilizer()) {
            this.status = 1;
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         } else if (belowState.getBlock() == ConfigBlocks.blockAiry && belowMeta == 5) {
            this.status = 2;
            this.count = 1000;
            this.markDirty();
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         } else {
            this.status = 0;
         }
      } else {
         BlockAiry.explodify(this.getWorld(), this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ());
         this.status = 0;
         this.count = 50;
         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      }

   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      super.readCustomNBT(nbttagcompound);
      this.status = nbttagcompound.getInteger("status");
      this.count = nbttagcompound.getInteger("count");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      super.writeCustomNBT(nbttagcompound);
      nbttagcompound.setInteger("status", this.status);
      nbttagcompound.setInteger("count", this.count);
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 10 && j == 10) {
         if (this.world.isRemote) {
            Thaumcraft.proxy.burst(this.world, (double)this.getPos().getX() + 0.5D, (double)this.getPos().getY() - 0.5D, (double)this.getPos().getZ() + 0.5D, 1.0F);
            this.world.playSound(null, this.getPos(), new SoundEvent(new ResourceLocation("thaumcraft", "craftfail")), SoundCategory.MASTER, 0.5F, 1.0F);
         }

         return true;
      } else {
         return super.receiveClientEvent(i, j);
      }
   }
}
